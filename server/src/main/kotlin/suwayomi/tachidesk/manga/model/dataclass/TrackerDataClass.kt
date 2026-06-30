package suwayomi.tachidesk.manga.model.dataclass

/*
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

data class TrackerDataClass(
    val id: Long,
    val name: String,
    val icon: String,
    val isLogin: Boolean,
    val authUrl: String?,
)
