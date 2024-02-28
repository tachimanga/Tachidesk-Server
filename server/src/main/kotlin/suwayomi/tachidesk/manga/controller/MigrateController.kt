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
import suwayomi.tachidesk.manga.impl.Migrate
import suwayomi.tachidesk.server.JavalinSetup.future
import suwayomi.tachidesk.server.util.handler
import suwayomi.tachidesk.server.util.queryParam

object MigrateController {
    private val json by DI.global.instance<Json>()
    private val logger = KotlinLogging.logger {}

    val info = handler(
        behaviorOf = { ctx ->
            ctx.json(Migrate.info())
        },
        withResults = { httpCode(HttpCode.OK) }
    )

    val sourceList = handler(
        behaviorOf = { ctx ->
            ctx.json(Migrate.sourceList())
        },
        withResults = { httpCode(HttpCode.OK) }
    )

    val mangaList = handler(
        queryParam<String>("sourceId"),
        documentWith = {
        },
        behaviorOf = { ctx, sourceId ->
            ctx.json(Migrate.mangaList(sourceId))
        },
        withResults = { httpCode(HttpCode.OK) }
    )

    val migrate = handler(
        behaviorOf = { ctx ->
            val input = json.decodeFromString<Migrate.MigrateRequest>(ctx.body())
            logger.info("migrate: $input")
            ctx.future(future { Migrate.migrate(input) })
        },
        withResults = {
            httpCode(HttpCode.OK)
        }
    )
}
