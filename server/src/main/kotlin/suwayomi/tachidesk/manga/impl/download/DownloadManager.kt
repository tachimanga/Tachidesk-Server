package suwayomi.tachidesk.manga.impl.download

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import io.javalin.plugin.json.JsonMapper
import io.javalin.websocket.WsContext
import io.javalin.websocket.WsMessageContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.sample
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import org.tachiyomi.NativeChannel
import suwayomi.tachidesk.manga.impl.download.model.DownloadChapter
import suwayomi.tachidesk.manga.impl.download.model.DownloadState
import suwayomi.tachidesk.manga.impl.download.model.DownloadState.Downloading
import suwayomi.tachidesk.manga.impl.download.model.DownloadState.Finished
import suwayomi.tachidesk.manga.impl.download.model.DownloadStatus
import suwayomi.tachidesk.manga.model.dataclass.ChapterDataClass
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.manga.model.dataclass.task.BgContinuedDownloadResultDataClass
import suwayomi.tachidesk.manga.model.table.ChapterTable
import suwayomi.tachidesk.manga.model.table.MangaTable
import suwayomi.tachidesk.manga.model.table.toDataClass
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

private const val MAX_SOURCES_IN_PARALLEL = 4
private const val MAX_TASK_IN_PARALLEL = 5

object DownloadManager {
    @OptIn(DelicateCoroutinesApi::class)
    private val backgroundDispatcher = newFixedThreadPoolContext(MAX_TASK_IN_PARALLEL, "Downloader")
    private val downloadScope = CoroutineScope(SupervisorJob() + backgroundDispatcher)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val clients = ConcurrentHashMap<String, WsContext>()
    private val downloadQueue = CopyOnWriteArrayList<DownloadChapter>()
    private val downloaders = ConcurrentHashMap<Long, Downloader>()
    private val jsonMapper by DI.global.instance<JsonMapper>()
    private var finishCount: Int = 0

    private var taskInParallel = 1

    private var runningFlag = false
    private var pauseFlag = false

    fun addClient(ctx: WsContext) {
        logger.info { "DownloadManager onConnect ${ctx.sessionId}" }
        clients[ctx.sessionId] = ctx
    }

    fun removeClient(ctx: WsContext) {
        logger.info { "DownloadManager onClose ${ctx.sessionId}" }
        clients.remove(ctx.sessionId)
    }

    fun notifyClient(ctx: WsContext) {
        logger.info { "DownloadManager notifyClient " }
        ctx.send(
            getStatus(),
        )
    }

    fun handleRequest(ctx: WsMessageContext) {
        logger.info { "DownloadManager onMessage ${ctx.message()} from ${ctx.sessionId}" }
        when (ctx.message()) {
            "STATUS" -> notifyClient(ctx)
            else -> ctx.send(
                """
                        |Invalid command.
                        |Supported commands are:
                        |    - STATUS
                        |       sends the current download status
                        |
                """.trimMargin(),
            )
        }
    }

    private val notifyFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        scope.launch {
            notifyFlow.sample(1.seconds).collect {
                try {
                    sendStatusToAllClients()
                } catch (e: Exception) {
                    logger.error(e) { "sendStatusToAllClients error" }
                }
            }
        }
    }

    private fun sendStatusToAllClients() {
        val status = getStatus()
        // logger.info { "DownloadManager sendStatusToAllClients, json:${jsonMapper.toJsonString(status)}" }
        clients.forEach {
            it.value.send(status)
        }
        notifyNativeIfNeeded(status)
    }

    private fun notifyNativeIfNeeded(status: DownloadStatus) {
        val prev = runningFlag
        runningFlag = downloadQueue.any { i -> i.state == DownloadState.Downloading }
        logger.info { "notifyNativeIfNeeded prev=$prev curr=$runningFlag" }
        if (!prev && runningFlag) {
            try {
                NativeChannel.call("DOWNLOAD:START", "")
            } catch (e: Exception) {
                logger.error(e) { "notifyNative error" }
            }
        }
    }

    private fun notifyAllClients(immediate: Boolean = false) {
        if (immediate) {
            sendStatusToAllClients()
        } else {
            scope.launch {
                notifyFlow.emit(Unit)
            }
        }
    }

    private fun getStatus(): DownloadStatus {
        return DownloadStatus(
            if (downloadQueue.none { it.state == Downloading }) {
                "Stopped"
            } else {
                "Started"
            },
            downloadQueue.toList(),
            finishCount,
        )
    }

    private val downloaderWatch = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    init {
        scope.launch {
            downloaderWatch.sample(1.seconds).collect {
                try {
                    addDownloaderIfNeeded()
                } catch (e: Exception) {
                    logger.error(e) { "addDownloaderIfNeeded error" }
                }
            }
        }
    }

    private fun addDownloaderIfNeeded() {
        val runningDownloaders = downloaders.values.filter { it.isActive }
        logger.info { "Running: ${runningDownloaders.size}" }
        if (runningDownloaders.size < MAX_SOURCES_IN_PARALLEL) {
            downloadQueue.asSequence()
                .filter { it.state == DownloadState.Queued || (it.state == DownloadState.Error && it.tries < 3) }
                .map { it.manga.sourceId.toLong() }
                .distinct()
                .minus(
                    runningDownloaders.map { it.sourceId }.toSet(),
                )
                .take(MAX_SOURCES_IN_PARALLEL - runningDownloaders.size)
                .map { getDownloader(it) }
                .forEach {
                    it.start(taskInParallel)
                }
            notifyAllClients()
        }
    }

    private fun refreshDownloaders() {
        start()
    }

    fun updateTaskInParallel(limit: Int) {
        logger.info { "prev:$taskInParallel curr:$limit" }
        if (limit <= 0 || limit > MAX_TASK_IN_PARALLEL) {
            logger.info { "invalid limit, skip" }
            return
        }
        if (taskInParallel == limit) {
            logger.info { "same limit, skip" }
            return
        }
        taskInParallel = limit
        val runningDownloaders = downloaders.values.filter { it.isActive }
        runningDownloaders.forEach {
            it.start(taskInParallel)
        }
    }

    private fun getDownloader(sourceId: Long) = downloaders.getOrPut(sourceId) {
        Downloader(
            scope = downloadScope,
            sourceId = sourceId,
            downloadQueue = downloadQueue,
            notifier = ::notifyAllClients,
            onComplete = ::refreshDownloaders,
            onDownloadFinish = ::onDownloadFinish,
        )
    }

    private fun onDownloadFinish() {
        finishCount++
    }

    fun enqueueWithChapterIndex(mangaId: Int, chapterIndex: Int) {
        val chapter = transaction {
            ChapterTable
                .slice(ChapterTable.id)
                .select { ChapterTable.manga.eq(mangaId) and ChapterTable.sourceOrder.eq(chapterIndex) }
                .first()
        }
        enqueue(EnqueueInput(chapterIds = listOf(chapter[ChapterTable.id].value)))
    }

    @Serializable
    // Input might have additional formats in the future, such as "All for mangaID" or "Unread for categoryID"
    // Having this input format is just future-proofing
    data class EnqueueInput(
        val chapterIds: List<Int>?,
    )

    fun enqueue(input: EnqueueInput) {
        if (input.chapterIds.isNullOrEmpty()) return

        val chapterMap = transaction {
            ChapterTable.select { ChapterTable.id inList input.chapterIds }
                .associateBy { it[ChapterTable.id].value }
        }
        val chapters = input.chapterIds.mapNotNull { chapterMap[it] }

        val mangaIds = chapters.map { it[ChapterTable.manga].value }.distinct()
        val mangas = transaction {
            MangaTable
                .select { (MangaTable.id inList mangaIds) }
                .map { MangaTable.toDataClass(it) }
                .associateBy { it.id }
        }

        val inputPairs =
            chapters.map {
                Pair(
                    // this should be safe because mangas is created above from chapters
                    mangas[it[ChapterTable.manga].value]!!,
                    ChapterTable.toDataClass(it),
                )
            }

        addMultipleToQueue(inputPairs)
    }

    fun unqueue(input: EnqueueInput) {
        if (input.chapterIds.isNullOrEmpty()) return

        downloadQueue.removeIf { it.chapter.id in input.chapterIds }

        notifyAllClients()
    }

    /**
     * Tries to add multiple inputs to queue
     * If any of inputs was actually added to queue, starts the queue
     */
    private fun addMultipleToQueue(inputs: List<Pair<MangaDataClass, ChapterDataClass>>) {
        val addedChapters = inputs.mapNotNull { addToQueue(it.first, it.second) }
        if (addedChapters.isNotEmpty()) {
            start()
            notifyAllClients(true)
        }
    }

    /**
     * Tries to add chapter to queue.
     * If chapter is added, returns the created DownloadChapter, otherwise returns null
     */
    private fun addToQueue(manga: MangaDataClass, chapter: ChapterDataClass): DownloadChapter? {
        if (downloadQueue.none { it.mangaId == manga.id && it.chapterIndex == chapter.index }) {
            val downloadChapter = DownloadChapter(
                chapter.index,
                manga.id,
                chapter,
                manga,
            )
            downloadQueue.add(downloadChapter)
            logger.debug { "Added chapter ${chapter.id} to download queue (${manga.title} | ${chapter.name})" }
            return downloadChapter
        }
        logger.debug { "Chapter ${chapter.id} already present in queue (${manga.title} | ${chapter.name})" }
        return null
    }

    fun unqueue(chapterIndex: Int, mangaId: Int) {
        downloadQueue.removeIf { it.mangaId == mangaId && it.chapterIndex == chapterIndex }
        notifyAllClients()
    }

    fun reorder(chapterIndex: Int, mangaId: Int, to: Int) {
        require(to >= 0) { "'to' must be over or equal to 0" }
        val download = downloadQueue.find { it.mangaId == mangaId && it.chapterIndex == chapterIndex }
            ?: return
        downloadQueue -= download
        downloadQueue.add(to, download)
    }

    fun start() {
        logger.info { "Running:start" }
        scope.launch {
            downloaderWatch.emit(Unit)
        }
        pauseFlag = false
    }

    suspend fun stop() {
        coroutineScope {
            downloaders.map { (_, downloader) ->
                async {
                    downloader.stop()
                }
            }.awaitAll()
        }
        pauseFlag = true
        notifyAllClients()
    }

    suspend fun clear() {
        stop()
        downloadQueue.clear()
        notifyAllClients()
    }

    fun getQueueStatus(): Pair<Int, Int> {
        return Pair(
            downloadQueue.count { i -> i.state == Finished || i.state == DownloadState.Error } + finishCount,
            downloadQueue.size + finishCount,
        )
    }

    fun getBgContinuedResult(): BgContinuedDownloadResultDataClass {
        val totalCount = downloadQueue.size + finishCount
        val finishCount = downloadQueue.count { i -> i.state == Finished || i.state == DownloadState.Error } + finishCount
        val failedCount = downloadQueue.count { i -> i.state == DownloadState.Error }
        val running = !pauseFlag && downloadQueue.any { i -> i.state == DownloadState.Downloading || i.state == DownloadState.Queued }
        return BgContinuedDownloadResultDataClass(
            running = running,
            totalCount = totalCount,
            finishCount = finishCount,
            failedCount = failedCount,
        )
    }
}

enum class DownloaderState(val state: Int) {
    Stopped(0),
    Running(1),
    Paused(2),
}
