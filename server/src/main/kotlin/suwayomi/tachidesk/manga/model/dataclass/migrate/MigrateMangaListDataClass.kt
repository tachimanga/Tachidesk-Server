package suwayomi.tachidesk.manga.model.dataclass.migrate

import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

data class MigrateMangaListDataClass(
    val sourceId: String,
    val list: List<MangaDataClass>,
)
