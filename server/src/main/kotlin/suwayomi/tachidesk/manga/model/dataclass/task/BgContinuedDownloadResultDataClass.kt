package suwayomi.tachidesk.manga.model.dataclass.task

import kotlinx.serialization.Serializable

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

@Serializable
data class BgContinuedDownloadResultDataClass(
    val running: Boolean,
    val totalCount: Int,
    // succ + fail
    val finishCount: Int,
    val failedCount: Int,
)
