package suwayomi.tachidesk.manga.model.dataclass

/*
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.serialization.Serializable

@Serializable
data class TrackSearchDataClass(
    val id: Long?,
    val mangaId: Long,
    val syncId: Int,
    val mediaId: Long,
    val libraryId: Long?,
    val title: String,
    val lastChapterRead: Float,
    val totalChapters: Int,
    val score: Float,
    val status: Int,
    val startedReadingDate: Long,
    val finishedReadingDate: Long,
    val trackingUrl: String,
    val coverUrl: String,
    val summary: String,
    val publishingStatus: String,
    val publishingType: String,
    val startDate: String,
)
