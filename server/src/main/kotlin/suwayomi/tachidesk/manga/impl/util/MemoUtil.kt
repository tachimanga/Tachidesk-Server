package suwayomi.tachidesk.manga.impl.util

/*
 * Copyright (C) 2026 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

private val emptyJsonObject = JsonObject(emptyMap())

fun String.parseMemo(): JsonObject {
    if (isEmpty() || this == "{}") return emptyJsonObject
    return runCatching { Json.decodeFromString<JsonObject>(this) }.getOrDefault(emptyJsonObject)
}
