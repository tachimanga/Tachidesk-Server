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
import org.tachiyomi.Profiler
import suwayomi.tachidesk.manga.impl.Chapter
import suwayomi.tachidesk.manga.impl.Manga
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
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
        val flag = running
            ?: tracker.any { (_, job) ->
                job.status == JobStatus.PENDING || job.status == JobStatus.RUNNING
            }
        _status.value = UpdateStatus(tracker.values, flag)
    }

    private suspend fun process(job: UpdateJob): List<UpdateJob> {
        tracker[job.manga.id] = job.copy(status = JobStatus.RUNNING)
        updateStatus(true)
        tracker[job.manga.id] = try {
            logger.info { "Updating \"${job.manga.title}\" (source: ${job.manga.sourceId})" }
            Profiler.start()
            if (!job.manga.initialized) {
                Manga.getManga(job.manga.id)
            }
            Chapter.getChapterList(job.manga.id, true)
            Profiler.all()
            job.copy(status = JobStatus.COMPLETE)
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            logger.error(e) { "Error while updating ${job.manga.title}" }
            job.copy(status = JobStatus.FAILED)
        }
        return tracker.values.toList()
    }

    override fun addMangaToQueue(manga: MangaDataClass) {
        val updateChannel = getOrCreateUpdateChannelFor(manga.sourceId)
        scope.launch {
            updateChannel.send(UpdateJob(manga))
        }
        tracker[manga.id] = UpdateJob(manga)
        updateStatus(true)
    }

    override fun reset() {
        scope.coroutineContext.cancelChildren()
        tracker.clear()
        _status.update { UpdateStatus() }
        updateChannels.forEach { (_, channel) -> channel.cancel() }
        updateChannels.clear()
    }

    override fun getQueueStatus(): Pair<Int, Int> {
        val jobs = tracker.values
        return Pair(
            jobs.count { i -> i.status == JobStatus.COMPLETE || i.status == JobStatus.FAILED },
            jobs.size,
        )
    }
}
