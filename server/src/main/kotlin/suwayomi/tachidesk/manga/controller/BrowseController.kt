package suwayomi.tachidesk.manga.controller

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import io.javalin.http.HttpCode
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.manga.impl.Browse
import suwayomi.tachidesk.server.JavalinSetup.future
import suwayomi.tachidesk.server.util.handler

object BrowseController {
    private val json by DI.global.instance<Json>()
    private val logger = KotlinLogging.logger {}

    val fetchUrl = handler(
        behaviorOf = { ctx ->
            val input = json.decodeFromString<Browse.UrlFetchInput>(ctx.body())
            ctx.future(future { Browse.fetchUrl(input) })
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )
}
