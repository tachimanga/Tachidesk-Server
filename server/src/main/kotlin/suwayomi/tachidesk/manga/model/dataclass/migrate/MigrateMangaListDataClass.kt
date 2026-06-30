package suwayomi.tachidesk.manga.model.dataclass.migrate

/*
 * Copyright (C) 2024 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass

data class MigrateMangaListDataClass(
    val sourceId: String,
    val list: List<MangaDataClass>,
)
