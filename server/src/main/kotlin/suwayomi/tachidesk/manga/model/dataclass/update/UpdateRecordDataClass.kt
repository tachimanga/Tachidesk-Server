package suwayomi.tachidesk.manga.model.dataclass.update

/*
 * Copyright (C) 2025 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.serialization.Serializable

@Serializable
data class UpdateRecordDataClass(
    val id: Int,
    val createAt: Long,
    val updateAt: Long,
    val finishAt: Long,
    val type: Int,
    val status: Int,
    val errCode: String?,
    val errMsg: String?,
    val totalCount: Int,
    val succCount: Int,
    val failedCount: Int,
    val skipCount: Int,
    val newChapterCount: Int,
)
