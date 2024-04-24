package suwayomi.tachidesk.global.controller

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.source.online.HttpSource
import io.javalin.http.HttpCode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.global.impl.About
import suwayomi.tachidesk.global.impl.AboutDataClass
import suwayomi.tachidesk.global.impl.AppUpdate
import suwayomi.tachidesk.global.impl.UpdateDataClass
import suwayomi.tachidesk.manga.impl.Setting
import suwayomi.tachidesk.server.JavalinSetup.future
import suwayomi.tachidesk.server.util.handler
import suwayomi.tachidesk.server.util.withOperation

/** Settings Page/Screen */
object SettingsController {
    /** returns some static info about the current app build */
    val about = handler(
        documentWith = {
            withOperation {
                summary("About Tachidesk")
                description("Returns some static info about the current app build")
            }
        },
        behaviorOf = { ctx ->
            ctx.json(About.getAbout())
        },
        withResults = {
            json<AboutDataClass>(HttpCode.OK)
        }
    )

    /** check for app updates */
    val checkUpdate = handler(
        documentWith = {
            withOperation {
                summary("Tachidesk update check")
                description("Check for app updates")
            }
        },
        behaviorOf = { ctx ->
            ctx.future(
                future { AppUpdate.checkUpdate() }
            )
        },
        withResults = {
            json<Array<UpdateDataClass>>(HttpCode.OK)
        }
    )

    private val json by DI.global.instance<Json>()
    private val logger = KotlinLogging.logger {}

    val uploadSettings = handler(
        behaviorOf = { ctx ->
            val input = json.decodeFromString<Setting.SettingData>(ctx.body())
            logger.info { "uploadSettings: $input" }
            Setting.uploadSettings(input)
            Setting.uploadCookies(input)
        },
        withResults = {
            httpCode(HttpCode.OK)
        }
    )

    val clearCookies = handler(
        behaviorOf = { ctx ->
            Setting.clearCookies()
        },
        withResults = {
            httpCode(HttpCode.OK)
        }
    )

    val uploadUserAgent = handler(
        behaviorOf = { ctx ->
            val ua = ctx.header("User-Agent")
            logger.info { "uploadUserAgent: $ua" }
            if (ua != null) {
                HttpSource.DEFAULT_USER_AGENT = ua
                System.setProperty("http.agent", ua)
            }
        },
        withResults = {
            httpCode(HttpCode.OK)
        }
    )

    val systemInfo = handler(
        behaviorOf = { ctx ->
            val map = HashMap<String, Long>()
//            map.put("processor", Runtime.getRuntime().availableProcessors().toLong())
            map["freeMemory"] = Runtime.getRuntime().freeMemory()
            map["totalMemory"] = Runtime.getRuntime().totalMemory()
            map["maxMemory"] = Runtime.getRuntime().maxMemory()
            ctx.json(map)
        },
        withResults = {
            httpCode(HttpCode.OK)
        }
    )
}
