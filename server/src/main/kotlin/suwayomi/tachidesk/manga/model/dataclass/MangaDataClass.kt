package suwayomi.tachidesk.manga.model.dataclass

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.source.model.UpdateStrategy
import suwayomi.tachidesk.manga.model.table.MangaStatus
import java.time.Instant

data class MangaDataClass(
    val id: Int,
    val sourceId: String,

    val url: String,
    val title: String,
    val thumbnailUrl: String? = null,
    val thumbnailUrlLastFetched: Long = 0,
    val thumbnailImg: ImgDataClass? = null,

    val initialized: Boolean = false,

    val artist: String? = null,
    val author: String? = null,
    val description: String? = null,
    val genre: List<String> = emptyList(),
    val status: String = MangaStatus.UNKNOWN.name,
    val inLibrary: Boolean = false,
    val inLibraryAt: Long = 0,
    var source: SourceDataClass? = null,

    /** meta data for clients */
    var meta: Map<String, String> = emptyMap(),

    val realUrl: String? = null,
    var lastFetchedAt: Long? = 0,
    var chaptersLastFetchedAt: Long? = 0,

    var updateStrategy: UpdateStrategy = UpdateStrategy.ALWAYS_UPDATE,

    val freshData: Boolean = false,
    var unreadCount: Long? = null,
    var unreadCountRaw: Long? = null,
    var downloadCount: Long? = null,
    var chapterCount: Long? = null,
    var lastReadAt: Long? = null,
    var lastChapterRead: ChapterDataClass? = null,
    var lastChapterReadName: String? = null,
    // MAX(ChapterTable.fetchedAt)
    var latestChapterFetchAt: Long? = null,
    var latestChapterUploadAt: Long? = null,
    var readDuration: Int? = null,

    val trackers: List<MangaTrackerDataClass>? = null,

    val age: Long? = if (lastFetchedAt == null) 0 else Instant.now().epochSecond.minus(lastFetchedAt),
    val chaptersAge: Long? = if (chaptersLastFetchedAt == null) null else Instant.now().epochSecond.minus(chaptersLastFetchedAt),

    val existChapterSyncData: Boolean = false,

    var latestChapter: ChapterDataClass? = null,
)

data class PagedMangaListDataClass(
    val mangaList: List<MangaDataClass>,
    val hasNextPage: Boolean,
)

data class MangaBatchQueryDataClass(
    val mangaList: List<MangaDataClass>,
)

internal fun String?.toGenreList() = this?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()
