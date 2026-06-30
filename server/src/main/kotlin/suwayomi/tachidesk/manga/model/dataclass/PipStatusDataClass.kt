package suwayomi.tachidesk.manga.model.dataclass

/*
 * Copyright (C) 2024 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.serialization.Serializable

@Serializable
data class PipStatusDataClass(
    val downloadFinishCount: Int,
    val downloadTotalCount: Int,
    val updateFinishCount: Int,
    val updateTotalCount: Int,
    val working: Boolean,
)
