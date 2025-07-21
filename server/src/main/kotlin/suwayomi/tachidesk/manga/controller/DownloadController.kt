package suwayomi.tachidesk.manga.controller

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import io.javalin.http.HttpCode
import io.javalin.websocket.WsConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.manga.impl.Download
import suwayomi.tachidesk.manga.impl.download.DownloadManager
import suwayomi.tachidesk.manga.impl.download.DownloadManager.EnqueueInput
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.server.JavalinSetup.future
import suwayomi.tachidesk.server.util.handler
import suwayomi.tachidesk.server.util.pathParam
import suwayomi.tachidesk.server.util.withOperation

object DownloadController {
    private val json by DI.global.instance<Json>()
    private val logger = KotlinLogging.logger {}

    /** Download queue stats */
    fun downloadsWS(ws: WsConfig) {
        ws.onConnect { ctx ->
            DownloadManager.addClient(ctx)
            DownloadManager.notifyClient(ctx)
        }
        ws.onMessage { ctx ->
            DownloadManager.handleRequest(ctx)
        }
        ws.onClose { ctx ->
            DownloadManager.removeClient(ctx)
        }
    }

    /** Start the downloader */
    val start = handler(
        documentWith = {
            withOperation {
                summary("Downloader start")
                description("Start the downloader")
            }
        },
        behaviorOf = {
            DownloadManager.start()
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    /** Stop the downloader */
    val stop = handler(
        documentWith = {
            withOperation {
                summary("Downloader stop")
                description("Stop the downloader")
            }
        },
        behaviorOf = { ctx ->
            ctx.future(
                future { DownloadManager.stop() },
            )
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    /** clear download queue */
    val clear = handler(
        documentWith = {
            withOperation {
                summary("Downloader clear")
                description("Clear download queue")
            }
        },
        behaviorOf = { ctx ->
            ctx.future(
                future { DownloadManager.clear() },
            )
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    val updateSetting = handler(
        behaviorOf = { ctx ->
            val input = json.decodeFromString<UpdateSettingInput>(ctx.body())
            logger.info { "updateSetting $input" }
            ctx.future(
                future {
                    if (input.taskInParallel != null) {
                        DownloadManager.updateTaskInParallel(input.taskInParallel)
                    }
                },
            )
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    /** Queue single chapter for download */
    val queueChapter = handler(
        pathParam<Int>("chapterIndex"),
        pathParam<Int>("mangaId"),
        documentWith = {
            withOperation {
                summary("Downloader add single chapter")
                description("Queue single chapter for download")
            }
        },
        behaviorOf = { ctx, chapterIndex, mangaId ->
            ctx.future(
                future {
                    DownloadManager.enqueueWithChapterIndex(mangaId, chapterIndex)
                },
            )
        },
        withResults = {
            httpCode(HttpCode.OK)
            httpCode(HttpCode.NOT_FOUND)
        },
    )

    val queueChapters = handler(
        documentWith = {
            withOperation {
                summary("Downloader add multiple chapters")
                description("Queue multiple chapters for download")
            }
            // body<EnqueueInput>()
        },
        behaviorOf = { ctx ->
            val inputs = json.decodeFromString<EnqueueInput>(ctx.body())
            ctx.future(
                future {
                    DownloadManager.enqueue(inputs)
                },
            )
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    /** delete multiple chapters from download queue */
    val unqueueChapters = handler(
        documentWith = {
            withOperation {
                summary("Downloader remove multiple downloads")
                description("Remove multiple chapters downloads from queue")
            }
            // body<EnqueueInput>()
        },
        behaviorOf = { ctx ->
            val input = json.decodeFromString<EnqueueInput>(ctx.body())
            ctx.future(
                future {
                    DownloadManager.unqueue(input)
                },
            )
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    /** delete chapter from download queue */
    val unqueueChapter = handler(
        pathParam<Int>("chapterIndex"),
        pathParam<Int>("mangaId"),
        documentWith = {
            withOperation {
                summary("Downloader remove chapter")
                description("Delete chapter from download queue")
            }
        },
        behaviorOf = { ctx, chapterIndex, mangaId ->
            DownloadManager.unqueue(chapterIndex, mangaId)

            ctx.status(200)
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    /** clear download queue */
    val reorderChapter = handler(
        pathParam<Int>("chapterIndex"),
        pathParam<Int>("mangaId"),
        pathParam<Int>("to"),
        documentWith = {
            withOperation {
                summary("Downloader reorder chapter")
                description("Reorder chapter in download queue")
            }
        },
        behaviorOf = { _, chapterIndex, mangaId, to ->
            DownloadManager.reorder(chapterIndex, mangaId, to)
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    val getDownloadedMangaList = handler(
        behaviorOf = { ctx ->
            ctx.json(Download.getDownloadedMangaList())
        },
        withResults = {
            json<Array<MangaDataClass>>(HttpCode.OK)
        },
    )

    val deleteDownloadedManga = handler(
        behaviorOf = { ctx ->
            val input = json.decodeFromString<Download.BatchInput>(ctx.body())
            Download.batchRemoveDownloads(input.mangaIds)
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    val batchRemoveLegacyDownloads = handler(
        behaviorOf = { ctx ->
            val input = json.decodeFromString<Download.BatchRemoveLegacyDownloadsInput>(ctx.body())
            Download.batchRemoveLegacyDownloads(input)
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    val batchQueryMangaInfo = handler(
        behaviorOf = { ctx ->
            ctx.future(
                future { Download.batchQueryDownloadMangaInfo() },
            )
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    @Serializable
    data class UpdateSettingInput(
        val taskInParallel: Int? = null,
    )
}
