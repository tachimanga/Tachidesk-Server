package suwayomi.tachidesk.manga.controller

import eu.kanade.tachiyomi.source.model.UpdateStrategy
import io.javalin.http.HttpCode
import io.javalin.websocket.WsConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.manga.impl.CategoryManga
import suwayomi.tachidesk.manga.impl.Chapter
import suwayomi.tachidesk.manga.impl.update.IUpdater
import suwayomi.tachidesk.manga.impl.update.UpdateStatus
import suwayomi.tachidesk.manga.impl.update.UpdaterSocket
import suwayomi.tachidesk.manga.model.dataclass.MangaChapterDataClass
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.manga.model.dataclass.PaginatedList
import suwayomi.tachidesk.manga.model.table.MangaTable
import suwayomi.tachidesk.manga.model.table.toDataClass
import suwayomi.tachidesk.server.JavalinSetup.future
import suwayomi.tachidesk.server.util.handler
import suwayomi.tachidesk.server.util.pathParam
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
                }
            )
        },
        withResults = {
            json<PagedMangaChapterListDataClass>(HttpCode.OK)
        }
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
                addCategoriesToUpdateQueue(input.categoryIds, true)
            } else {
                logger.info { "Adding Library to Update Queue" }
                addCategoriesToUpdateQueue(emptyList(), true)
            }
        },
        withResults = {
            httpCode(HttpCode.OK)
        }
    )

    private fun addCategoriesToUpdateQueue(categories: List<Int>, clear: Boolean = false) {
        val updater by DI.global.instance<IUpdater>()
        if (clear) {
            updater.reset()
        }
        updater.updateStatus(true)
        var existManga = false
        if (categories.isEmpty()) {
            val mangaList = transaction {
                MangaTable
                    .select { (MangaTable.inLibrary eq true) }
                    .toList()
            }
            mangaList.map { MangaTable.toDataClass(it) }
                .filter { it.updateStrategy == UpdateStrategy.ALWAYS_UPDATE }
                .forEach { manga ->
                    existManga = true
                    updater.addMangaToQueue(manga)
                }
        } else {
            categories
                .flatMap { CategoryManga.getCategoryMangaListV2(it) }
                .distinctBy { it.id }
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER, MangaDataClass::title))
                .filter { it.updateStrategy == UpdateStrategy.ALWAYS_UPDATE }
                .forEach { manga ->
                    existManga = true
                    updater.addMangaToQueue(manga)
                }
        }
        if (!existManga) {
            updater.updateStatus(null)
        }
    }

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
        }
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
                    updater.reset()
                }
            )
        },
        withResults = {
            httpCode(HttpCode.OK)
        }
    )

    @Serializable
    data class FetchRequest(
        val categoryIds: List<Int>? = null
    )
}
