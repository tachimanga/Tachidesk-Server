package suwayomi.tachidesk.manga.model.dataclass

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

data class RepoDataClass(
    var id: Int,
    val type: Int,
    val name: String,
    val metaUrl: String,
    val baseUrl: String,
)
