package suwayomi.tachidesk.global.controller

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
import suwayomi.tachidesk.global.impl.GlobalMeta
import suwayomi.tachidesk.server.util.handler

object GlobalMetaController {
    private val json by DI.global.instance<Json>()
    private val logger = KotlinLogging.logger {}

    val getMeta = handler(
        behaviorOf = { ctx ->
            val input = json.decodeFromString<GlobalMeta.QueryMetaInput>(ctx.body())
            logger.info { "getMeta: $input" }
            val result = mutableMapOf<String, String?>()
            if (input.key != null) {
                result[input.key] = GlobalMeta.getValue(input.key)
            }
            ctx.json(result)
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    val modifyMeta = handler(
        behaviorOf = { ctx ->
            val input = json.decodeFromString<GlobalMeta.UpdateMetaInput>(ctx.body())
            logger.info { "modifyMeta: $input" }
            if (input.key != null && input.value != null) {
                GlobalMeta.modifyMeta(input.key, input.value)
            }
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )
}
