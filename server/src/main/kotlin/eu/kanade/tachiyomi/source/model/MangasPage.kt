package eu.kanade.tachiyomi.source.model

/*
 * Copyright (C) Contributors to the Suwayomi project
 * Copyright (C) 2026 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

// data class MangasPage -> class MangasPage
class MangasPage(val mangas: List<SManga>, val hasNextPage: Boolean) {
    operator fun component1(): List<SManga> = mangas
    operator fun component2(): Boolean = hasNextPage
    fun copy(
        mangas: List<SManga> = this.mangas,
        hasNextPage: Boolean = this.hasNextPage,
    ): MangasPage = MangasPage(
        mangas = mangas,
        hasNextPage = hasNextPage,
    )
}
