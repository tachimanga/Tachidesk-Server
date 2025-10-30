package suwayomi.tachidesk.manga.model.dataclass.update

import kotlinx.serialization.Serializable

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

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
