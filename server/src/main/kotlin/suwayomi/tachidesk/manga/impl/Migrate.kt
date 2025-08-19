package suwayomi.tachidesk.manga.impl

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.serialization.Serializable
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import suwayomi.tachidesk.manga.impl.download.FolderProvider2
import suwayomi.tachidesk.manga.impl.track.Track
import suwayomi.tachidesk.manga.impl.track.tracker.model.toTrack
import suwayomi.tachidesk.manga.model.dataclass.SourceDataClass
import suwayomi.tachidesk.manga.model.dataclass.migrate.MigrateInfoDataClass
import suwayomi.tachidesk.manga.model.dataclass.migrate.MigrateMangaListDataClass
import suwayomi.tachidesk.manga.model.dataclass.migrate.MigrateSourceDataClass
import suwayomi.tachidesk.manga.model.dataclass.migrate.MigrateSourceListDataClass
import suwayomi.tachidesk.manga.model.table.*
import java.lang.RuntimeException

object Migrate {
    private val logger = KotlinLogging.logger {}

    fun info(): MigrateInfoDataClass {
        val mangaEntry = transaction {
            MangaTable
                .select { MangaTable.inLibrary eq true }
                .limit(1)
                .firstOrNull()
        }
        val existInLibraryManga = mangaEntry != null
        return MigrateInfoDataClass(existInLibraryManga = existInLibraryManga)
    }

    fun sourceList(): MigrateSourceListDataClass {
        val mangaCount = MangaTable.id.count().alias("manga_count")
        val list = transaction {
            MangaTable
                .slice(MangaTable.sourceReference, mangaCount)
                .select { MangaTable.inLibrary eq true }
                .groupBy(MangaTable.sourceReference)
                .map {
                    it[MangaTable.sourceReference] to it[mangaCount]
                }
        }

        val sourceIdList = list.map { it.first }.distinct()
        val sourceDataMap = Source.getFullSourceList(sourceIdList)
            .associateBy { it.id }
        val result = list.mapNotNull {
            val source = sourceDataMap[it.first.toString()] ?: buildObsoleteSource(it.first.toString())
            MigrateSourceDataClass(source = source, count = it.second)
        }
        return MigrateSourceListDataClass(list = result)
    }

    private fun buildObsoleteSource(sourceId: String) = SourceDataClass(
        sourceId,
        sourceId,
        "",
        "",
        null,
        "",
        supportsLatest = false,
        isConfigurable = false,
        isNsfw = false,
        displayName = sourceId,
    )

    fun mangaList(sourceId: String): MigrateMangaListDataClass {
        val mangaList = transaction {
            MangaTable
                .select { (MangaTable.sourceReference eq sourceId.toLong()) and (MangaTable.inLibrary eq true) }
                .map {
                    MangaTable.toDataClass(it)
                }
        }
        return MigrateMangaListDataClass(sourceId = sourceId, list = mangaList)
    }

    suspend fun migrate(request: MigrateRequest) {
        logger.info { "do migrate $request" }
        val srcMangaId = request.srcMangaId ?: throw RuntimeException("srcMangaId is null")
        val destMangaId = request.destMangaId ?: throw RuntimeException("destMangaId is null")
        if (srcMangaId == destMangaId) {
            logger.info { "same id, skip" }
            return
        }
        // 1. fetch manga
        logger.info { "(1/8)fetch manga..." }
        val destMangaData = Manga.getManga(destMangaId, false)

        // 2. fetch chapters
        logger.info { "(2/8)fetch chapters..." }
        val chapters = Chapter.getChapterList(destMangaId, true)

        // 3. migrateChapter
        logger.info { "(3/8)migrateChapter..." }
        if (request.migrateChapterFlag == true) {
            migrateChapter(srcMangaId, destMangaId)
        }

        // 4. migrateCategory
        logger.info { "(4/8)migrateCategory..." }
        if (request.migrateCategoryFlag == true) {
            migrateCategory(srcMangaId, destMangaId)
        }

        // 5. migrateTracking
        logger.info { "(5/8)migrateTrackFlag..." }
        if (request.migrateTrackFlag == true) {
            migrateTracking(srcMangaId, destMangaId)
        }

        // 6. migrateMeta
        logger.info { "(6/8)migrateMeta..." }
        migrateMeta(srcMangaId, destMangaId)

        logger.info { "(7/8)add to library if needed..." }
        val destManga = transaction { MangaTable.select { MangaTable.id eq destMangaId }.first() }
        if (!destManga[MangaTable.inLibrary]) {
            Library.addMangaToLibrary(destMangaId)
        }

        logger.info { "migrateHistory..." }
        migrateHistory(srcMangaId, destMangaId)

        logger.info { "(8/8)replace if needed..." }
        if (request.replaceFlag == true) {
            val now = System.currentTimeMillis()
            transaction {
                val srcManga = transaction { MangaTable.select { MangaTable.id eq srcMangaId }.first() }
                if (srcManga[MangaTable.inLibrary]) {
                    MangaTable.update({ MangaTable.id eq srcMangaId }) {
                        it[MangaTable.inLibrary] = false
                        it[MangaTable.updateAt] = now
                        it[MangaTable.dirty] = true
                    }
                }
                MangaTable.update({ MangaTable.id eq destMangaId }) {
                    it[MangaTable.inLibraryAt] = srcManga[MangaTable.inLibraryAt]
                    it[MangaTable.chaptersLastFetchedAt] = srcManga[MangaTable.chaptersLastFetchedAt]
                }
            }

            if (request.removeDownloadsIfMigrate == true) {
                FolderProvider2(srcMangaId, 0, 0).deleteAll()
                transaction {
                    ChapterTable.update({ (ChapterTable.manga eq srcMangaId) and (ChapterTable.isDownloaded eq true) }) {
                        it[ChapterTable.isDownloaded] = false
                    }
                }
            }
        }
    }

    private fun migrateChapter(srcMangaId: Int, destMangaId: Int) {
        val srcChapterList = transaction {
            ChapterTable.select { ChapterTable.manga eq srcMangaId }
                .toList()
        }
        if (srcChapterList.isEmpty()) {
            logger.info { "srcChapterList is empty, skip" }
            return
        }
        val destChapterList = transaction {
            ChapterTable.select { ChapterTable.manga eq destMangaId }
                .toList()
        }
        if (destChapterList.isEmpty()) {
            logger.info { "destChapterList is empty, skip" }
            return
        }

        val srcChapterNameMap = srcChapterList
            .associateBy { it[ChapterTable.name] }
        val srcChapterNumberMap = srcChapterList
            .filter { it[ChapterTable.chapter_number] > -1 }
            .associateBy { it[ChapterTable.chapter_number] }

        val maxChapterRead = srcChapterList
            .filter { it[ChapterTable.isRead] && it[ChapterTable.chapter_number] > -1 }
            .maxOfOrNull { it[ChapterTable.chapter_number] }
        logger.info { "maxChapterRead $maxChapterRead" }

        val now = System.currentTimeMillis()
        transaction {
            for (destChapter in destChapterList) {
                val destChapterNumber = destChapter[ChapterTable.chapter_number]
                val destChapterName = destChapter[ChapterTable.name]

                var srcChapter: ResultRow? = null

                if (destChapterNumber > -1 &&
                    srcChapterNumberMap[destChapterNumber] != null
                ) {
                    srcChapter = srcChapterNumberMap[destChapterNumber]
                } else if (srcChapterNameMap[destChapterName] != null) {
                    srcChapter = srcChapterNameMap[destChapterName]
                }
                if (srcChapter == null) {
                    logger.info { "no match chapter for destChapter $destChapterNumber $destChapterName" }
                }
                val isRead = maxChapterRead != null && destChapterNumber <= maxChapterRead

                if (srcChapter != null || isRead) {
                    ChapterTable.update({ ChapterTable.id eq destChapter[ChapterTable.id].value }) {
                        var dirty = false
                        if (srcChapter != null) {
                            it[ChapterTable.isRead] = srcChapter[ChapterTable.isRead]
                            it[ChapterTable.isBookmarked] = srcChapter[ChapterTable.isBookmarked]
                            it[ChapterTable.lastReadAt] = srcChapter[ChapterTable.lastReadAt]
                            if (srcChapter[ChapterTable.isRead] || srcChapter[ChapterTable.isBookmarked] || srcChapter[ChapterTable.lastReadAt] > 0) {
                                dirty = true
                            }
                        }
                        if (isRead) {
                            it[ChapterTable.isRead] = true
                            dirty = true
                        }
                        if (dirty) {
                            it[ChapterTable.updateAt] = now
                            it[ChapterTable.dirty] = true
                        }
                    }
                }
            }
        }
    }

    private fun migrateCategory(srcMangaId: Int, destMangaId: Int) {
        val srcCategoryIds = transaction {
            CategoryMangaTable
                .slice(CategoryMangaTable.category)
                .select { (CategoryMangaTable.manga eq srcMangaId) }
                .map { it[CategoryMangaTable.category].value }
                .toList()
        }
        logger.info { "srcCategoryIds $srcCategoryIds" }
        if (srcCategoryIds.isEmpty()) {
            logger.info { "srcCategoryIds is empty, skip" }
            return
        }
        CategoryManga.updateCategory(destMangaId, srcCategoryIds)
    }

    private fun migrateTracking(srcMangaId: Int, destMangaId: Int) {
        val srcTrackList = transaction {
            TrackRecordTable.select { (TrackRecordTable.mangaId eq srcMangaId) and (TrackRecordTable.isDelete eq false) }
                .toList()
        }
        // logger.info { "srcTrackList $srcTrackList" }
        if (srcTrackList.isEmpty()) {
            logger.info { "srcTrackList is empty, skip" }
        }
        for (srcTrack in srcTrackList) {
            val track = srcTrack.toTrack()
            track.manga_id = destMangaId.toLong()
            Track.upsertTrackRecord(track)
        }
    }

    private fun migrateMeta(srcMangaId: Int, destMangaId: Int) {
        val metaList = transaction {
            MangaMetaTable.select { MangaMetaTable.ref eq srcMangaId }
                .toList()
        }
        // logger.info { "metaList $metaList" }
        if (metaList.isEmpty()) {
            logger.info { "metaList is empty, skip" }
        }
        for (meta in metaList) {
            if (meta[MangaMetaTable.key] == "flutter_scanlator") {
                continue
            }
            Manga.modifyMangaMeta(destMangaId, meta[MangaMetaTable.key], meta[MangaMetaTable.value])
        }
    }

    private fun migrateHistory(srcMangaId: Int, destMangaId: Int) {
        val srcHistory = transaction {
            HistoryTable
                .slice(
                    HistoryTable.lastChapterId,
                    HistoryTable.lastChapterName,
                    HistoryTable.readDuration,
                    HistoryTable.lastReadAt,
                )
                .select { HistoryTable.mangaId eq srcMangaId }
                .firstOrNull()
        }
        if (srcHistory == null) {
            logger.info { "srcHistory is empty, skip" }
            return
        }

        val lastChapterName = srcHistory[HistoryTable.lastChapterName]
            ?: transaction {
                ChapterTable.slice(ChapterTable.name)
                    .select { ChapterTable.id eq srcHistory[HistoryTable.lastChapterId] }
                    .firstOrNull()
                    ?.get(ChapterTable.name)
            }
        if (lastChapterName == null) {
            logger.info { "lastChapterName is empty, skip" }
            return
        }

        val existDuration = transaction {
            HistoryTable.slice(HistoryTable.readDuration)
                .select { HistoryTable.mangaId eq destMangaId }
                .firstOrNull()
                ?.get(HistoryTable.readDuration)
        }

        History.upsertHistory(
            destMangaId,
            0,
            srcHistory[HistoryTable.readDuration] + (existDuration ?: 0),
            lastReadAt = srcHistory[HistoryTable.lastReadAt],
            lastChapterName = lastChapterName,
        )
        History.batchDeleteV2(History.BatchInput(mangaIds = listOf(srcMangaId)))
    }

    @Serializable
    data class MigrateRequest(
        val srcMangaId: Int? = null,
        val destMangaId: Int? = null,
        val migrateChapterFlag: Boolean? = null,
        val migrateCategoryFlag: Boolean? = null,
        val migrateTrackFlag: Boolean? = null,
        val replaceFlag: Boolean? = null,
        val removeDownloadsIfMigrate: Boolean? = null,
    )
}
