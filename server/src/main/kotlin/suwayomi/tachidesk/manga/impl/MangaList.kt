package suwayomi.tachidesk.manga.impl

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.source.SourceMeta
import eu.kanade.tachiyomi.source.local.LocalSource
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.UpdateStrategy
import eu.kanade.tachiyomi.source.sourceSupportDirect
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.tachiyomi.Profiler
import suwayomi.tachidesk.manga.impl.Manga.batchGetMangaMetaMap
import suwayomi.tachidesk.manga.impl.util.lang.awaitSingle
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource.getCatalogueSourceMeta
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource.getCatalogueSourceOrStub
import suwayomi.tachidesk.manga.model.dataclass.*
import suwayomi.tachidesk.manga.model.dataclass.toGenreList
import suwayomi.tachidesk.manga.model.table.MangaStatus
import suwayomi.tachidesk.manga.model.table.MangaTable
import java.time.Instant

object MangaList {
    fun proxyThumbnailUrl(mangaId: Int): String {
        return "/api/v1/manga/$mangaId/thumbnail"
    }

    fun buildThumbnailImg(thumbnail_url: String?, meta: SourceMeta?): ImgDataClass? {
        if (thumbnail_url == null) {
            return null
        }
        val direct = sourceSupportDirect(meta)
        if (direct) {
            return buildImgDataClass(url = thumbnail_url, headers = meta?.headers)
        }
        return null
    }

    suspend fun getMangaList(sourceId: Long, pageNum: Int = 1, popular: Boolean): PagedMangaListDataClass {
        require(pageNum > 0) {
            "pageNum = $pageNum is not in valid range"
        }
        val source = getCatalogueSourceOrStub(sourceId)
        val mangasPage = if (popular) {
            source.fetchPopularManga(pageNum).awaitSingle()
        } else {
            if (source.supportsLatest) {
                source.fetchLatestUpdates(pageNum).awaitSingle()
            } else {
                throw Exception("Source $source doesn't support latest")
            }
        }
        return mangasPage.processEntries(sourceId)
    }

    fun MangasPage.processEntries(sourceId: Long): PagedMangaListDataClass {
        val mangasPage = this
        if (mangasPage.mangas.isEmpty()) {
            return PagedMangaListDataClass(
                emptyList(),
                mangasPage.hasNextPage
            )
        }
        val source = getCatalogueSourceOrStub(sourceId)
        val meta = getCatalogueSourceMeta(source)
        Profiler.split("before processEntries")
        val dbMangaMap = transaction {
            val urls = mangasPage.mangas.map { it.url }.toList()
            MangaTable.select {
                (MangaTable.url inList urls) and (MangaTable.sourceReference eq sourceId)
            }.associateBy { it[MangaTable.url] }
        }
        val mangaList = transaction {
            return@transaction mangasPage.mangas.map { manga ->
                val mangaEntry = dbMangaMap[manga.url]
                if (mangaEntry == null) { // create manga entry
                    val mangaId = MangaTable.insertAndGetId {
                        it[url] = manga.url
                        it[title] = manga.title.take(512)

                        it[artist] = manga.artist?.take(512)
                        it[author] = manga.author?.take(512)
                        it[description] = manga.description
                        it[genre] = manga.genre
                        it[status] = manga.status
                        it[thumbnail_url] = manga.thumbnail_url
                        it[updateStrategy] = manga.update_strategy.name

                        it[sourceReference] = sourceId

                        // tachiyomi: networkToLocalManga.await(it.toDomainManga(sourceId))
                        it[initialized] = manga.initialized
                    }.value
                    if (sourceId == LocalSource.ID) {
                        setupDemoMangaExt(manga, mangaId)
                    }
                    MangaDataClass(
                        id = mangaId,
                        sourceId = sourceId.toString(),

                        url = manga.url,
                        title = manga.title,
                        thumbnailUrl = proxyThumbnailUrl(mangaId),
                        thumbnailUrlLastFetched = 0,
                        thumbnailImg = buildThumbnailImg(manga.thumbnail_url, meta),

                        initialized = manga.initialized,

                        artist = manga.artist,
                        author = manga.author,
                        description = manga.description,
                        genre = manga.genre.toGenreList(),
                        status = MangaStatus.valueOf(manga.status).name,
                        inLibrary = false, // It's a new manga entry
                        inLibraryAt = 0,
                        // meta = getMangaMetaMap(mangaId),
                        realUrl = null,
                        lastFetchedAt = 0,
                        chaptersLastFetchedAt = 0,
                        updateStrategy = manga.update_strategy,
                        freshData = true
                    )
                } else {
                    val mangaId = mangaEntry[MangaTable.id].value

                    if (manga.thumbnail_url?.isNotBlank() == true &&
                        manga.thumbnail_url != mangaEntry[MangaTable.thumbnail_url]
                    ) {
                        MangaTable.update({ MangaTable.id eq mangaId }) {
                            it[MangaTable.thumbnail_url] = manga.thumbnail_url
                            it[MangaTable.thumbnailUrlLastFetched] = Instant.now().epochSecond
                        }
                    }

                    MangaDataClass(
                        id = mangaId,
                        sourceId = sourceId.toString(),

                        url = manga.url,
                        title = manga.title,
                        thumbnailUrl = proxyThumbnailUrl(mangaId),
                        thumbnailUrlLastFetched = mangaEntry[MangaTable.thumbnailUrlLastFetched],
                        thumbnailImg = buildThumbnailImg(manga.thumbnail_url, meta),

                        initialized = true,

                        artist = mangaEntry[MangaTable.artist],
                        author = mangaEntry[MangaTable.author],
                        description = mangaEntry[MangaTable.description],
                        genre = mangaEntry[MangaTable.genre].toGenreList(),
                        status = MangaStatus.valueOf(mangaEntry[MangaTable.status]).name,
                        inLibrary = mangaEntry[MangaTable.inLibrary],
                        inLibraryAt = mangaEntry[MangaTable.inLibraryAt],
                        // meta = getMangaMetaMap(mangaId),
                        realUrl = mangaEntry[MangaTable.realUrl],
                        lastFetchedAt = mangaEntry[MangaTable.lastFetchedAt],
                        chaptersLastFetchedAt = mangaEntry[MangaTable.chaptersLastFetchedAt],
                        updateStrategy = UpdateStrategy.valueOf(mangaEntry[MangaTable.updateStrategy]),
                        freshData = false
                    )
                }
            }
        }
        Profiler.split("before metaMap")
        val metaMap = batchGetMangaMetaMap(mangaList.map { it.id })
        mangaList.forEach {
            it.meta = metaMap.getOrDefault(it.id, emptyMap())
        }
        Profiler.split("after metaMap")
        return PagedMangaListDataClass(
            mangaList,
            mangasPage.hasNextPage
        )
    }

    private fun setupDemoMangaExt(manga: SManga, mangaId: Int) {
        var mode = "webtoon"
        if (manga.title == "Left to right") {
            mode = "singleHorizontalLTR"
        } else if (manga.title == "Right to left") {
            mode = "singleHorizontalRTL"
        }
        Manga.modifyMangaMeta(mangaId, "flutter_readerMode", mode)
    }
}
