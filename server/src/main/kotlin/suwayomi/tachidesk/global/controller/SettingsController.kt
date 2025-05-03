package suwayomi.tachidesk.global.controller

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.network.NetworkHelper
import io.javalin.http.HttpCode
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import org.tachiyomi.NativeChannel
import suwayomi.tachidesk.global.impl.About
import suwayomi.tachidesk.global.impl.AboutDataClass
import suwayomi.tachidesk.manga.impl.Setting
import suwayomi.tachidesk.server.util.handler
import suwayomi.tachidesk.server.util.withOperation
import uy.kohesive.injekt.injectLazy

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
        },
    )

    private val json by DI.global.instance<Json>()
    private val logger = KotlinLogging.logger {}
    val network: NetworkHelper by injectLazy()

    val uploadSettings = handler(
        behaviorOf = { ctx ->
            val input = json.decodeFromString<Setting.SettingData>(ctx.body())
            logger.info { "uploadSettings: $input" }
            Setting.uploadSettings(input)
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    val uploadUserAgent = handler(
        behaviorOf = { ctx ->
            val ua = ctx.header("User-Agent")
            Setting.updateUserAgent(ua)
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    val detectDefaultUserAgent = handler(
        behaviorOf = { ctx ->
            val ua = ctx.header("User-Agent")
            if (ua != null) {
                NativeChannel.call("USERAGENT:DEFAULT", ua)
            }
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
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
        },
    )
}
