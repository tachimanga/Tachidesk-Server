package eu.kanade.tachiyomi.data.backup

/*
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

enum class ImportState {
    INIT,
    RUNNING,
    SUCCESS,
    FAIL,
}
data class ImportStatus(
    val state: String,
    val message: String,
    // extension language codes
    val codes: List<String> = emptyList(),
)
