package suwayomi.tachidesk.manga.impl.download

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.coroutines.*
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import suwayomi.tachidesk.manga.impl.chapter.getChapterDownloadReady
import suwayomi.tachidesk.manga.impl.download.model.DownloadChapter
import suwayomi.tachidesk.manga.impl.download.model.DownloadState.Downloading
import suwayomi.tachidesk.manga.impl.download.model.DownloadState.Error
import suwayomi.tachidesk.manga.impl.download.model.DownloadState.Finished
import suwayomi.tachidesk.manga.impl.download.model.DownloadState.Queued
import suwayomi.tachidesk.manga.model.table.ChapterTable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.locks.ReentrantLock

private val logger = KotlinLogging.logger {}

class Downloader(
    private val scope: CoroutineScope,
    val sourceId: Long,
    private val downloadQueue: CopyOnWriteArrayList<DownloadChapter>,
    private val notifier: (immediate: Boolean) -> Unit,
    private val onComplete: () -> Unit,
    private val onDownloadFinish: () -> Unit
) {
    private val jobs = mutableListOf<Job>()
    private val lock = ReentrantLock()

    class StopDownloadException : Exception("Cancelled download")
    class PauseDownloadException : Exception("Pause download")

    private suspend fun step(download: DownloadChapter?, immediate: Boolean) {
        // printDownloadAll()
        notifier(immediate)

        if (download?.state == Finished) {
            return
        }

        // if (!job.isActive) throw CancellationException()
        currentCoroutineContext().ensureActive()

        if (download != null) {
            val topDownloads = downloadQueue.filter { it.manga.sourceId.toLong() == sourceId && it.state != Error }.take(activeJobsCount)
            if (!topDownloads.contains(download)) {
                if (download in downloadQueue) {
                    throw PauseDownloadException()
                } else {
                    throw StopDownloadException()
                }
            }
        }
//        logger.info("=====current download======")
//        printDownload(download)
//        logger.info("=====top download======")
//        topDownloads.forEach { printDownload(it) }
    }

    fun printDownloadAll() {
        logger.info("activeJobsCount=$activeJobsCount")
        logger.info("jobs.size=${jobs.size}")
        downloadQueue.filter { it.manga.sourceId.toLong() == sourceId }
            .forEach {
                printDownload(it)
            }
    }

    private fun printDownload(it: DownloadChapter?) {
        if (it == null) {
            logger.info("null")
            return
        }
        logger.info("${it.mangaId}-${it.chapterIndex} ${it.state}")
    }

    val isActive
        get() = activeJobsCount > 0

    private val activeJobsCount
        get() = jobs.count { j -> j.isActive }

    fun start(limit: Int) {
        logger.info("[Downloader] start $limit")
        val left = limit - activeJobsCount
        if (left > 0) {
            for (i in 1..left) {
                val job = scope.launch {
                    run()
                }.also { job ->
                    job.invokeOnCompletion {
                        logger.info("[Downloader] complete")
                        jobs.remove(job)
                        if (it !is CancellationException) {
                            // refreshDownloaders
                            onComplete()
                        }
                    }
                }
                jobs.add(job)
            }
        } else if (left < 0) {
            for (i in 1..-left) {
                val job = jobs.removeLast()
                job.cancel()
            }
        }
        notifier(false)
    }

    suspend fun stop() {
        val list = jobs.toList()
        jobs.clear()
        list.forEach { it.cancel() }
        list.forEach { it.join() }
    }

    private suspend fun run() {
        while (downloadQueue.isNotEmpty() && currentCoroutineContext().isActive) {
            lock.lock()
            val download = try {
                val d = downloadQueue.firstOrNull {
                    it.manga.sourceId.toLong() == sourceId &&
                        (it.state == Queued || (it.state == Error && it.tries < 3)) // 3 re-tries per download
                } ?: break
                d.state = Downloading
                d
            } finally {
                lock.unlock()
            }
            val currentThreadName = Thread.currentThread().name
            logger.info("[Downloader]run thread name: $currentThreadName")
            printDownload(download)

            try {
                step(download, true)

                download.chapter = getChapterDownloadReady(download.chapterIndex, download.mangaId)
                step(download, false)

                FolderProvider2(download.mangaId, download.chapter.id, download.chapter.originalChapterId)
                    .download(download, scope, this::step)

                download.state = Finished
                transaction {
                    ChapterTable.update({ (ChapterTable.id eq download.chapter.id) }) {
                        it[isDownloaded] = true
                    }
                }
                step(download, true)
                delay(300)
                downloadQueue.removeIf { it.mangaId == download.mangaId && it.chapterIndex == download.chapterIndex }
                onDownloadFinish()
                step(null, false)
            } catch (e: CancellationException) {
                logger.debug("[Downloader] Downloader was stopped")
                downloadQueue.filter { it.state == Downloading }.forEach { it.state = Queued }
            } catch (e: PauseDownloadException) {
                logger.info("[Downloader] PauseDownloadException")
                download.state = Queued
            } catch (e: Throwable) {
                logger.info("[Downloader] Downloader faced an exception", e)
                download.tries++
                download.state = Error
                download.error = e.message
            } finally {
                notifier(false)
            }
        }
    }
}
