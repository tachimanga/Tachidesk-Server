package suwayomi.tachidesk.manga.controller

import io.javalin.http.HttpCode
import io.javalin.websocket.WsConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.manga.impl.Chapter
import suwayomi.tachidesk.manga.impl.update.*
import suwayomi.tachidesk.manga.model.dataclass.MangaChapterDataClass
import suwayomi.tachidesk.manga.model.dataclass.PaginatedList
import suwayomi.tachidesk.server.JavalinSetup.future
import suwayomi.tachidesk.server.util.handler
import suwayomi.tachidesk.server.util.pathParam
import suwayomi.tachidesk.server.util.queryParam
import suwayomi.tachidesk.server.util.withOperation

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

object UpdateController {
    private val logger = KotlinLogging.logger { }
    private val json by DI.global.instance<Json>()

    /** get recently updated manga chapters */
    val recentChapters = handler(
        pathParam<Int>("pageNum"),
        documentWith = {
            withOperation {
                summary("Updates fetch")
                description("Get recently updated manga chapters")
            }
        },
        behaviorOf = { ctx, pageNum ->
            ctx.future(
                future {
                    Chapter.getRecentChapters(pageNum)
                },
            )
        },
        withResults = {
            json<PagedMangaChapterListDataClass>(HttpCode.OK)
        },
    )

    /**
     * Class made for handling return type in the documentation for [recentChapters],
     * since OpenApi cannot handle runtime generics.
     */
    private class PagedMangaChapterListDataClass : PaginatedList<MangaChapterDataClass>(emptyList(), false)

    val categoryUpdate2 = handler(
        behaviorOf = { ctx ->
            val input = json.decodeFromString<FetchRequest>(ctx.body())
            logger.info("categoryUpdate input: $input")
            if (input.categoryIds?.isNotEmpty() == true) {
                UpdateManager.updateByCategories(input.categoryIds)
            } else {
                logger.info { "Adding Library to Update Queue" }
                UpdateManager.updateAll()
            }
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    val retryByCodes = handler(
        behaviorOf = { ctx ->
            val input = json.decodeFromString<RetryUpdateRequest>(ctx.body())
            if (input.errorCodes?.isNotEmpty() == true) {
                UpdateManager.retryByCodes(input.errorCodes)
            }
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    val retrySkipped = handler(
        behaviorOf = { ctx ->
            ctx.future(
                future {
                    UpdateManager.retrySkipped()
                },
            )
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    fun categoryUpdateWS(ws: WsConfig) {
        ws.onConnect { ctx ->
            UpdaterSocket.addClient(ctx)
        }
        ws.onMessage { ctx ->
            UpdaterSocket.handleRequest(ctx)
        }
        ws.onClose { ctx ->
            UpdaterSocket.removeClient(ctx)
        }
    }

    val updateSummary = handler(
        documentWith = {
            withOperation {
                summary("Updater summary")
                description("Gets the latest updater summary")
            }
        },
        behaviorOf = { ctx ->
            val updater by DI.global.instance<IUpdater>()
            ctx.json(updater.status.value)
        },
        withResults = {
            json<UpdateStatus>(HttpCode.OK)
        },
    )

    val reset = handler(
        documentWith = {
            withOperation {
                summary("Updater reset")
                description("Stops and resets the Updater")
            }
        },
        behaviorOf = { ctx ->
            val updater by DI.global.instance<IUpdater>()
            logger.info { "Resetting Updater" }
            ctx.future(
                future {
                    updater.reset(null)
                },
            )
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    val queryUpdateRecords = handler(
        queryParam<Int?>("type"),
        documentWith = {
        },
        behaviorOf = { ctx, type ->
            ctx.json(UpdateRecord.queryUpdateRecords(TaskType.valueOf(type)))
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    @Serializable
    data class FetchRequest(
        val categoryIds: List<Int>? = null,
    )

    @Serializable
    data class RetryUpdateRequest(
        val errorCodes: List<JobErrorCode>? = null,
    )
}
