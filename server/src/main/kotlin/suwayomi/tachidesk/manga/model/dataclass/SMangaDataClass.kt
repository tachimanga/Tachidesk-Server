package suwayomi.tachidesk.manga.model.dataclass

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.UpdateStrategy
import kotlinx.serialization.Serializable

@Serializable
data class SMangaDataClass(
    val sourceId: Long,

    val url: String,

    val title: String,

    val artist: String? = null,

    val author: String? = null,

    val description: String? = null,

    val genre: String? = null,

    val status: Int,

    val thumbnailUrl: String? = null,

    val updateStrategy: String,

    val initialized: Boolean,
)

fun SMangaDataClass.toSManga(): SManga {
    val data = this
    return SManga.create().apply {
        url = data.url
        title = data.title
        artist = data.artist
        author = data.author
        description = data.description
        genre = data.genre
        status = data.status
        thumbnail_url = data.thumbnailUrl
        update_strategy = UpdateStrategy.valueOf(data.updateStrategy)
        initialized = data.initialized
    }
}

fun SManga.toSMangaDataClass(sourceId: Long): SMangaDataClass {
    return SMangaDataClass(
        sourceId = sourceId,
        url = this.url,
        title = this.title,
        artist = this.artist,
        author = this.author,
        description = this.description,
        genre = this.genre,
        status = this.status,
        thumbnailUrl = this.thumbnail_url,
        updateStrategy = this.update_strategy.name,
        initialized = this.initialized,
    )
}

data class PagedSMangaListDataClass(
    val mangaList: List<SMangaDataClass>,
    val hasNextPage: Boolean,
)
