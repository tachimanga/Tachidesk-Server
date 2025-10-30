package suwayomi.tachidesk.manga.impl.update

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import mu.KotlinLogging
import org.jetbrains.exposed.sql.update
import org.tachiyomi.NativeChannel
import org.tachiyomi.Profiler
import suwayomi.tachidesk.manga.impl.Chapter
import suwayomi.tachidesk.manga.impl.Manga
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.manga.model.dataclass.update.BgUpdateResultDataClass
import suwayomi.tachidesk.manga.model.dataclass.update.UpdateMangaChapterDataClass
import java.util.concurrent.ConcurrentHashMap

private const val MAX_UPDATER_IN_PARAllEL = 2

class Updater : IUpdater {
    private val logger = KotlinLogging.logger {}

    @OptIn(DelicateCoroutinesApi::class)
    private val dispatcher = newFixedThreadPoolContext(MAX_UPDATER_IN_PARAllEL, "Updater")
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _status = MutableStateFlow(UpdateStatus())
    override val status = _status.asStateFlow()

    private val tracker = ConcurrentHashMap<Int, UpdateJob>()
    private val updateChannels = ConcurrentHashMap<String, Channel<UpdateJob>>()

    private val semaphore = Semaphore(MAX_UPDATER_IN_PARAllEL)

    private var currentTask: UpdateTask? = null

    private fun getOrCreateUpdateChannelFor(source: String): Channel<UpdateJob> {
        return updateChannels.getOrPut(source) {
            logger.debug { "getOrCreateUpdateChannelFor: created channel for $source - channels: ${updateChannels.size + 1}" }
            createUpdateChannel()
        }
    }

    private fun createUpdateChannel(): Channel<UpdateJob> {
        val channel = Channel<UpdateJob>(Channel.UNLIMITED)
        channel.consumeAsFlow()
            .onEach { job ->
                semaphore.withPermit {
                    process(job)
                    updateStatus(null)
                }
            }
            .catch { logger.error(it) { "Error during updates" } }
            .launchIn(scope)
        return channel
    }

    override fun updateStatus(running: Boolean?) {
        val prev = _status.value.running
        val flag = running
            ?: tracker.any { (_, job) ->
                job.status == JobStatus.PENDING || job.status == JobStatus.RUNNING
            }
        _status.value = UpdateStatus(tracker.values, flag)

        if (prev != flag) {
            notifyNative(prev, flag)

            logger.info { "updateTaskRecord id=${this.currentTask?.recordId} prev=$prev curr=$flag" }
            val status = if (flag) TaskStatus.RUNNING else TaskStatus.SUCC
            UpdateRecord.updateTaskRecord(this.currentTask, status)
        }
    }

    private fun notifyNative(prev: Boolean, running: Boolean) {
        try {
            NativeChannel.call("UPDATE:STATUS", if (running) "START" else "STOP")
        } catch (e: Exception) {
            logger.error(e) { "notifyNative error" }
        }
    }

    private suspend fun process(job: UpdateJob) {
        tracker[job.manga.id] = job.copy(status = JobStatus.RUNNING)
        updateStatus(true)
        tracker[job.manga.id] = try {
            logger.info { "Updating \"${job.manga.title}\" (source: ${job.manga.sourceId})" }
            Profiler.start()
            if (!job.manga.initialized) {
                Manga.getManga(job.manga.id)
            }
            val chapterList = Chapter.getChapterList(job.manga.id, true)
            val newChapterCount = chapterList.count { it.newChapter == true }
            val latestChapterInfo = if (newChapterCount > 0) {
                val chapter = chapterList.lastOrNull { it.newChapter == true }
                LatestChapterInfo(newChapterCount = newChapterCount, latestChapterId = chapter?.id, latestChapterName = chapter?.name)
            } else {
                null
            }
            Profiler.all()
            job.copy(status = JobStatus.COMPLETE, latestChapterInfo = latestChapterInfo)
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            logger.error(e) { "Error while updating ${job.manga.title}" }
            job.copy(status = JobStatus.FAILED, failedInfo = FailedInfo(errorCode = JobErrorCode.UPDATE_FAILED, errorMessage = e.message))
        }
    }

    override fun addMangaToQueue(manga: MangaDataClass) {
        val updateChannel = getOrCreateUpdateChannelFor(manga.sourceId)
        scope.launch {
            updateChannel.send(UpdateJob(manga))
        }
        tracker[manga.id] = UpdateJob(manga)
        updateStatus(true)
    }

    override fun addMangaListToQueue(mangaList: List<MangaDataClass>) {
        if (mangaList.isEmpty()) {
            return
        }
        for (manga in mangaList) {
            val prevJob = tracker[manga.id]
            if (prevJob?.status == JobStatus.PENDING || prevJob?.status == JobStatus.COMPLETE) {
                logger.info { "[UPDATE] jon exist, skip. mangaId=${manga.id}, job=$prevJob" }
                continue
            }
            val updateChannel = getOrCreateUpdateChannelFor(manga.sourceId)
            scope.launch {
                updateChannel.send(UpdateJob(manga))
            }
            tracker[manga.id] = UpdateJob(manga)
        }
        updateStatus(true)
    }

    override fun addMangaToTracker(job: UpdateJob) {
        tracker[job.manga.id] = job
    }

    override fun reset(task: UpdateTask?) {
        logger.info { "reset task=${task?.type} prev=${this.currentTask?.type}" }
        if (_status.value.running) {
            UpdateRecord.updateTaskRecord(this.currentTask, TaskStatus.FAIL, TaskErrorCode.CANCEL)
        }

        scope.coroutineContext.cancelChildren()
        tracker.clear()
        _status.update { UpdateStatus() }
        updateChannels.forEach { (_, channel) -> channel.cancel() }
        updateChannels.clear()

        this.currentTask = task
        UpdateRecord.createTaskRecord(task)
    }

    override fun getQueueStatus(): Pair<Int, Int> {
        val jobs = tracker.values
        val skipCount = jobs.count { i -> i.status == JobStatus.FAILED && i.failedInfo?.errorCode != JobErrorCode.UPDATE_FAILED }
        return Pair(
            jobs.count { i -> i.status == JobStatus.COMPLETE || i.status == JobStatus.FAILED } - skipCount,
            jobs.size - skipCount,
        )
    }

    override fun fetchUpdateResult(): BgUpdateResultDataClass {
        val jobs = tracker.values.toList()

        val skipCount = jobs.count { i -> i.status == JobStatus.FAILED && i.failedInfo?.errorCode != JobErrorCode.UPDATE_FAILED }
        val pendingCount = jobs.count { i -> i.status == JobStatus.PENDING }
        val runningCount = jobs.count { i -> i.status == JobStatus.RUNNING }
        val failedCount = jobs.count { i -> i.status == JobStatus.FAILED } - skipCount
        val finishCount = jobs.count { i -> i.status == JobStatus.COMPLETE }

        val running = pendingCount + runningCount > 0

        val mangaChapterList = if (!running) {
            jobs.filter { i -> i.status == JobStatus.COMPLETE && i.latestChapterInfo != null }
                .map { i ->
                    UpdateMangaChapterDataClass(
                        mangaId = i.manga.id,
                        mangaTitle = i.manga.title,
                        chapterId = i.latestChapterInfo?.latestChapterId ?: 0,
                        chapterTitle = i.latestChapterInfo?.latestChapterName ?: "",
                        newChapterCount = i.latestChapterInfo?.newChapterCount ?: 0,
                    )
                }
        } else {
            null
        }

        return BgUpdateResultDataClass(
            running = running,
            totalCount = jobs.size,
            pendingCount = pendingCount,
            runningCount = runningCount,
            finishCount = finishCount,
            failedCount = failedCount,
            skipCount = skipCount,
            mangaChapterList = mangaChapterList,
        )
    }

    override fun fetchAllJobs(): List<UpdateJob> {
        return tracker.values.toList()
    }
}
