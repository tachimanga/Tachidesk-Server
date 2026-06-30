package suwayomi.tachidesk.manga.impl.util

/*
 * Copyright (C) 2024 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import org.tachiyomi.NativeChannel

object LogEvent {
    private val json by DI.global.instance<Json>()

    fun log(event: String, params: Map<String, String>? = null) {
        val payload = EventContent(event = event, params = params)
        NativeChannel.call("LOG_EVENT", json.encodeToString(payload))
    }
}

@Serializable
data class EventContent(
    val event: String,
    val params: Map<String, String>?,
)
