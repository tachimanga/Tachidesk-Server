package suwayomi.tachidesk.manga.model.dataclass.update

/*
 * Copyright (C) 2025 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.serialization.Serializable

@Serializable
data class BgUpdateResultDataClass(
    val running: Boolean,
    val totalCount: Int,
    val pendingCount: Int,
    val runningCount: Int,
    val finishCount: Int,
    val failedCount: Int,
    val skipCount: Int,
    // only if running = false
    val mangaChapterList: List<UpdateMangaChapterDataClass>? = null,
)

@Serializable
data class UpdateMangaChapterDataClass(
    val mangaId: Int,
    val mangaTitle: String,
    val chapterId: Int,
    val chapterTitle: String,
    val newChapterCount: Int,
)
