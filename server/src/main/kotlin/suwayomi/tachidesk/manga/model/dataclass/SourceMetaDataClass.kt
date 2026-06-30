package suwayomi.tachidesk.manga.model.dataclass

/*
 * Copyright (C) 2025 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.serialization.Serializable

@Serializable
data class SourceMetaDataClass(
    val sourceId: Long,
    val key: String,
    val value: String?,
)
