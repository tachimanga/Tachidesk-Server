package suwayomi.tachidesk.manga.controller

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import io.javalin.http.HttpCode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import org.tachiyomi.Profiler
import suwayomi.tachidesk.manga.impl.*
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.server.util.handler

object HistoryController {
    private val json by DI.global.instance<Json>()
    private val logger = KotlinLogging.logger {}

    val list = handler(
        behaviorOf = { ctx ->
            Profiler.start()
            ctx.json(History.getHistoryMangaListV2())
            Profiler.all()
        },
        withResults = {
            json<Array<MangaDataClass>>(HttpCode.OK)
        },
    )

    val batchDelete = handler(
        behaviorOf = { ctx ->
            val input = json.decodeFromString<History.BatchInput>(ctx.body())
            History.batchDeleteV2(input)
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    val clear = handler(
        behaviorOf = { ctx ->
            val input = json.decodeFromString<History.ClearInput>(ctx.body())
            History.clearHistory(input)
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )
}
