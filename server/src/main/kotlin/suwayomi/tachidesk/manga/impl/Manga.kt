package suwayomi.tachidesk.manga.impl

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.source.SourceMeta
import eu.kanade.tachiyomi.source.local.LocalSource
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.UpdateStrategy
import eu.kanade.tachiyomi.source.online.HttpSource
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.cloud.impl.Sync
import suwayomi.tachidesk.manga.impl.MangaList.buildThumbnailImg
import suwayomi.tachidesk.manga.impl.MangaList.proxyThumbnailUrl
import suwayomi.tachidesk.manga.impl.Source.getSource
import suwayomi.tachidesk.manga.impl.track.Track
import suwayomi.tachidesk.manga.impl.util.lang.awaitSingle
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource.getCatalogueSourceOrNull
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource.getCatalogueSourceOrStub
import suwayomi.tachidesk.manga.impl.util.source.StubSource
import suwayomi.tachidesk.manga.impl.util.storage.ImageResponse.buildImageResponse
import suwayomi.tachidesk.manga.impl.util.storage.ImageResponse.clearFastCachedImage
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.manga.model.dataclass.toGenreList
import suwayomi.tachidesk.manga.model.table.*
import suwayomi.tachidesk.server.ApplicationDirs
import uy.kohesive.injekt.injectLazy
import java.io.InputStream
import java.time.Instant

object Manga {
    private val logger = KotlinLogging.logger {}

    private fun truncate(text: String?, maxLength: Int): String? {
        return if (text?.length ?: 0 > maxLength) {
            text?.take(maxLength - 3) + "..."
        } else {
            text
        }
    }

    suspend fun getManga(mangaId: Int, onlineFetch: Boolean = false): MangaDataClass {
        var mangaEntry = transaction { MangaTable.select { MangaTable.id eq mangaId }.first() }
        val source = getCatalogueSourceOrNull(mangaEntry[MangaTable.sourceReference])
        val meta = if (source != null) {
            GetCatalogueSource.getCatalogueSourceMeta(source)
        } else {
            null
        }
        val mangaData = if (mangaEntry[MangaTable.initialized] && !onlineFetch) {
            getMangaDataClass(mangaId, mangaEntry, meta)
        } else { // initialize manga
            if (source == null) {
                return getMangaDataClass(mangaId, mangaEntry, meta)
            }
            // Tachiyomi: val networkManga = state.source.getMangaDetails(state.manga.toSManga())
            val sManga = MangaTable.toSManga(mangaEntry)
            val networkManga = source.fetchMangaDetails(sManga).awaitSingle()
            sManga.copyFrom(networkManga)

            val realUrl = runCatching {
                val clone = SManga.create()
                clone.cloneFrom(sManga)
                (source as? HttpSource)?.getMangaUrl(clone)
            }.getOrNull()

            // Tachiyomi: awaitUpdateFromSource
            val remoteTitle = try {
                networkManga.title
            } catch (_: UninitializedPropertyAccessException) {
                ""
            }

            var needClearCoverCache = false

            transaction {
                MangaTable.update({ MangaTable.id eq mangaId }) {
                    // if the manga isn't a favorite, set its title from source and update in db
                    val title = remoteTitle.take(512)
                    if (title.isNotEmpty() && !mangaEntry[MangaTable.inLibrary] && title != mangaEntry[MangaTable.title]) {
                        it[MangaTable.title] = title
                    }

                    it[MangaTable.initialized] = true

                    it[MangaTable.artist] = sManga.artist?.take(512)
                    it[MangaTable.author] = sManga.author?.take(512)
                    it[MangaTable.description] = truncate(sManga.description, 4096)
                    it[MangaTable.genre] = sManga.genre
                    it[MangaTable.status] = sManga.status
                    if (!sManga.thumbnail_url.isNullOrEmpty() && sManga.thumbnail_url != mangaEntry[MangaTable.thumbnail_url]) {
                        it[MangaTable.thumbnail_url] = sManga.thumbnail_url
                        it[MangaTable.thumbnailUrlLastFetched] = Instant.now().epochSecond
                        needClearCoverCache = true
                    }

                    it[MangaTable.realUrl] = realUrl

                    it[MangaTable.lastFetchedAt] = Instant.now().epochSecond

                    it[MangaTable.updateStrategy] = sManga.update_strategy.name
                }
            }

            if (needClearCoverCache) {
                clearMangaCoverCache(mangaId)
            }

            mangaEntry = transaction { MangaTable.select { MangaTable.id eq mangaId }.first() }

            MangaDataClass(
                id = mangaId,
                sourceId = mangaEntry[MangaTable.sourceReference].toString(),

                url = mangaEntry[MangaTable.url],
                title = mangaEntry[MangaTable.title],
                thumbnailUrl = proxyThumbnailUrl(mangaId),
                thumbnailUrlLastFetched = mangaEntry[MangaTable.thumbnailUrlLastFetched],
                thumbnailImg = buildThumbnailImg(mangaEntry[MangaTable.thumbnail_url], meta),

                initialized = true,

                artist = sManga.artist,
                author = sManga.author,
                description = sManga.description,
                genre = sManga.genre.toGenreList(),
                status = MangaStatus.valueOf(sManga.status).name,
                inLibrary = mangaEntry[MangaTable.inLibrary],
                inLibraryAt = mangaEntry[MangaTable.inLibraryAt],
                source = getSource(mangaEntry[MangaTable.sourceReference]),
                meta = getMangaMetaMap(mangaId),
                realUrl = mangaEntry[MangaTable.realUrl],
                lastFetchedAt = mangaEntry[MangaTable.lastFetchedAt],
                chaptersLastFetchedAt = mangaEntry[MangaTable.chaptersLastFetchedAt],
                updateStrategy = UpdateStrategy.valueOf(mangaEntry[MangaTable.updateStrategy]),
                freshData = true,
                trackers = Track.getTrackRecordsByMangaId(mangaId),
            )
        }
        mangaData.readDuration = History.queryMangaReadDuration(mangaId)
        return mangaData
    }

    suspend fun getMangaFull(mangaId: Int, onlineFetch: Boolean = false): MangaDataClass {
        val mangaDaaClass = getManga(mangaId, onlineFetch)

        return transaction {
            val unreadCount =
                ChapterTable
                    .select { (ChapterTable.manga eq mangaId) and (ChapterTable.isRead eq false) }
                    .count()

            val downloadCount =
                ChapterTable
                    .select { (ChapterTable.manga eq mangaId) and (ChapterTable.isDownloaded eq true) }
                    .count()

            val chapterCount =
                ChapterTable
                    .select { (ChapterTable.manga eq mangaId) }
                    .count()

            val lastChapterRead =
                ChapterTable
                    .select { (ChapterTable.manga eq mangaId) }
                    .orderBy(ChapterTable.sourceOrder to SortOrder.DESC)
                    .firstOrNull { it[ChapterTable.isRead] }

            mangaDaaClass.unreadCount = unreadCount
            mangaDaaClass.downloadCount = downloadCount
            mangaDaaClass.chapterCount = chapterCount
            mangaDaaClass.lastChapterRead = lastChapterRead?.let { ChapterTable.toDataClass(it) }

            mangaDaaClass
        }
    }

    private fun getMangaDataClass(mangaId: Int, mangaEntry: ResultRow, meta: SourceMeta?) = MangaDataClass(
        id = mangaId,
        sourceId = mangaEntry[MangaTable.sourceReference].toString(),

        url = mangaEntry[MangaTable.url],
        title = mangaEntry[MangaTable.title],
        thumbnailUrl = proxyThumbnailUrl(mangaId),
        thumbnailUrlLastFetched = mangaEntry[MangaTable.thumbnailUrlLastFetched],
        thumbnailImg = buildThumbnailImg(mangaEntry[MangaTable.thumbnail_url], meta),

        initialized = true,

        artist = mangaEntry[MangaTable.artist],
        author = mangaEntry[MangaTable.author],
        description = mangaEntry[MangaTable.description],
        genre = mangaEntry[MangaTable.genre].toGenreList(),
        status = MangaStatus.valueOf(mangaEntry[MangaTable.status]).name,
        inLibrary = mangaEntry[MangaTable.inLibrary],
        inLibraryAt = mangaEntry[MangaTable.inLibraryAt],
        source = getSource(mangaEntry[MangaTable.sourceReference]),
        meta = getMangaMetaMap(mangaId),
        realUrl = mangaEntry[MangaTable.realUrl],
        lastFetchedAt = mangaEntry[MangaTable.lastFetchedAt],
        chaptersLastFetchedAt = mangaEntry[MangaTable.chaptersLastFetchedAt],
        updateStrategy = UpdateStrategy.valueOf(mangaEntry[MangaTable.updateStrategy]),
        freshData = false,
        trackers = Track.getTrackRecordsByMangaId(mangaId),
    )

    fun getMangaMetaMap(mangaId: Int): Map<String, String> {
        return transaction {
            MangaMetaTable.select { MangaMetaTable.ref eq mangaId }
                .associate { it[MangaMetaTable.key] to it[MangaMetaTable.value] }
        }
    }

    fun batchGetMangaMetaMap(mangaIds: List<Int>): Map<Int, Map<String, String>> {
        val list = transaction {
            MangaMetaTable.select { MangaMetaTable.ref inList mangaIds }
                .toList()
        }
        val map = list.groupBy { it[MangaMetaTable.ref].value }
            .mapValues { kv ->
                kv.value.associate { it[MangaMetaTable.key] to it[MangaMetaTable.value] }
            }
        return map
    }

    fun modifyMangaMeta(mangaId: Int, key: String, value: String) {
        transaction {
            val meta =
                MangaMetaTable.select { (MangaMetaTable.ref eq mangaId) and (MangaMetaTable.key eq key) }
                    .firstOrNull()

            if (meta == null) {
                MangaMetaTable.insert {
                    it[MangaMetaTable.key] = key
                    it[MangaMetaTable.value] = value
                    it[MangaMetaTable.ref] = mangaId
                }
            } else {
                MangaMetaTable.update({ (MangaMetaTable.ref eq mangaId) and (MangaMetaTable.key eq key) }) {
                    it[MangaMetaTable.value] = value
                }
            }
        }
        markDirtyAndSetNeedSync(mangaId)
    }

    fun removeMangaMeta(mangaId: Int, key: String) {
        transaction {
            MangaMetaTable.deleteWhere { (MangaMetaTable.ref eq mangaId) and (MangaMetaTable.key eq key) }
        }
        markDirtyAndSetNeedSync(mangaId)
    }

    private fun markDirtyAndSetNeedSync(mangaId: Int) {
        val now = System.currentTimeMillis()
        transaction {
            MangaTable.update({ MangaTable.id eq mangaId }) {
                it[MangaTable.updateAt] = now
                it[MangaTable.dirty] = true
            }
        }
        Sync.setNeedsSync()
    }

    fun markDirtyIfCommitIdZero(mangaId: Int) {
        val now = System.currentTimeMillis()
        transaction {
            MangaTable.update({ (MangaTable.id eq mangaId) and (MangaTable.dirty eq false) and (MangaTable.commitId eq 0) }) {
                it[MangaTable.updateAt] = now
                it[MangaTable.dirty] = true
            }
        }
    }

    fun batchMarkDirtyIfCommitIdZero(mangaIds: List<Int>) {
        if (mangaIds.isEmpty()) {
            return
        }
        val now = System.currentTimeMillis()
        transaction {
            MangaTable.update({ (MangaTable.id inList mangaIds) and (MangaTable.dirty eq false) and (MangaTable.commitId eq 0) }) {
                it[MangaTable.updateAt] = now
                it[MangaTable.dirty] = true
            }
        }
    }

    private val applicationDirs by DI.global.instance<ApplicationDirs>()
    private val network: NetworkHelper by injectLazy()
    suspend fun getMangaThumbnail(mangaId: Int): Pair<InputStream, String> {
        val mangaEntry = transaction { MangaTable.select { MangaTable.id eq mangaId }.first() }
        val sourceId = mangaEntry[MangaTable.sourceReference]

        return when (val source = getCatalogueSourceOrStub(sourceId)) {
            is HttpSource -> buildImageResponse {
                val thumbnailUrl = mangaEntry[MangaTable.thumbnail_url]
                    ?: if (!mangaEntry[MangaTable.initialized]) {
                        // initialize then try again
                        getManga(mangaId)
                        transaction {
                            MangaTable.select { MangaTable.id eq mangaId }.first()
                        }[MangaTable.thumbnail_url]!!
                    } else {
                        // source provides no thumbnail url for this manga
                        throw NullPointerException("No thumbnail found")
                    }
                source.client.newCall(
                    GET(thumbnailUrl, source.headers),
                ).asObservableSuccess().awaitSingle()
            }

            is LocalSource -> {
                LocalSource.getCoverImage(mangaEntry[MangaTable.url], mangaEntry[MangaTable.thumbnail_url])
            }

            is StubSource -> buildImageResponse {
                val thumbnailUrl = mangaEntry[MangaTable.thumbnail_url]
                    ?: throw NullPointerException("No thumbnail found")
                network.client.newCall(
                    GET(thumbnailUrl),
                ).awaitSuccess()
            }

            else -> throw IllegalArgumentException("Unknown source")
        }
    }

    private fun clearMangaCoverCache(mangaId: Int) {
        val saveDir = applicationDirs.coversRoot
        val fileName = mangaId.toString()

        clearFastCachedImage(saveDir, fileName)
    }
}
