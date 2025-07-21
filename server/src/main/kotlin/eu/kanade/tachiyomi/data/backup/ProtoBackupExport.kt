package eu.kanade.tachiyomi.data.backup

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.data.backup.models.*
import kotlinx.serialization.protobuf.ProtoBuf
import mu.KotlinLogging
import okio.buffer
import okio.gzip
import okio.sink
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.manga.impl.Category
import suwayomi.tachidesk.manga.model.dataclass.CategoryDataClass
import suwayomi.tachidesk.manga.model.table.*
import suwayomi.tachidesk.server.ApplicationDirs
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ProtoBackupExport {
    val parser = ProtoBuf
    private val logger = KotlinLogging.logger {}
    private val applicationDirs by DI.global.instance<ApplicationDirs>()

    fun createBackup(flags: BackupFlags): String {
        logger.info { "[BACKUP]createBackup..." }

        val t0 = System.currentTimeMillis()
        val mangas = transaction { MangaTable.select { MangaTable.inLibrary eq true }.toList() }
        logger.info { "[BACKUP]mangas size:${mangas.size}, cost:${ System.currentTimeMillis() - t0}ms" }

        val dbCategories = if (flags.includeCategories) {
            Category.getCategoryList().filter { it.id != Category.DEFAULT_CATEGORY_ID }
        } else {
            emptyList()
        }
        val categoryMap = dbCategories.associate { it.id to it.order.toLong() }

        val t1 = System.currentTimeMillis()
        val backup = Backup(
            backupMangas(mangas, flags, categoryMap),
            backupCategories(dbCategories),
            backupSources(mangas),
        )
        logger.info { "[BACKUP]backup done, cost:${ System.currentTimeMillis() - t1}ms" }

        val t2 = System.currentTimeMillis()
        val byteArray = parser.encodeToByteArray(Backup.serializer(), backup)
        logger.info { "[BACKUP]encode done, cost:${ System.currentTimeMillis() - t2}ms" }

        val t3 = System.currentTimeMillis()
        val path = writeToFile(byteArray)
        logger.info { "[BACKUP]write to file done, cost:${ System.currentTimeMillis() - t3}ms, path:$path" }
        return path
    }

    private fun writeToFile(byteArray: ByteArray): String {
        val date = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.ENGLISH).format(Date())
        val path = "${applicationDirs.tempProtoBackups}/tachimanga_$date.tachibk"
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
        file.outputStream()
            .sink().gzip().buffer().use {
                it.write(byteArray)
            }
        return path
    }

    private fun backupMangas(mangas: List<ResultRow>, flags: BackupFlags, categoryMap: Map<Int, Long>): List<BackupManga> {
        val chunkedLists = mangas.chunked(100)
        return chunkedLists.flatMap { list ->
            val mangaIds = list
                .map { it[MangaTable.id].value }
                .toList()

            val mangaChaptersMap = if (flags.includeChapters) batchBackupMangaChapters(mangaIds) else null
            val mangaCategoryMap = if (flags.includeCategories) batchBackupMangaCategory(mangaIds, categoryMap) else null
            val mangaTrackingMap = if (flags.includeTracking) batchBackupMangaTracking(mangaIds) else null
            val mangaHistoryMap = if (flags.includeHistory) batchBackupMangaHistory(mangaIds) else null

            list.map { backupManga(it, flags, mangaChaptersMap, mangaCategoryMap, mangaTrackingMap, mangaHistoryMap) }
        }
    }

    private fun backupManga(
        mangaRow: ResultRow,
        flags: BackupFlags,
        mangaChaptersMap: Map<Int, List<BackupChapter>>?,
        mangaCategoryMap: Map<Int, List<Long>>?,
        mangaTrackingMap: Map<Int, List<BackupTracking>>?,
        mangaHistoryMap: Map<Int, List<BackupHistory>>?,
    ): BackupManga {
        val backupManga = MangaTable.toBackupManga(mangaRow)
        val mangaId = mangaRow[MangaTable.id].value

        if (flags.includeChapters) {
            backupManga.chapters = mangaChaptersMap?.get(mangaId) ?: emptyList()
        }

        if (flags.includeCategories) {
            backupManga.categories = mangaCategoryMap?.get(mangaId) ?: emptyList()
        }

        if (flags.includeTracking) {
            backupManga.tracking = mangaTrackingMap?.get(mangaId) ?: emptyList()
        }

        if (flags.includeHistory) {
            backupManga.history = mangaHistoryMap?.get(mangaId) ?: emptyList()
        }
        return backupManga
    }

    private fun batchBackupMangaChapters(mangaIds: List<Int>): Map<Int, List<BackupChapter>> {
        val chapters = transaction {
            ChapterTable.slice(
                ChapterTable.manga,
                ChapterTable.url,
                ChapterTable.name,
                ChapterTable.scanlator,
                ChapterTable.isRead,
                ChapterTable.isBookmarked,
                ChapterTable.lastPageRead,
                ChapterTable.fetchedAt,
                ChapterTable.date_upload,
                ChapterTable.chapter_number,
                ChapterTable.sourceOrder,
            ).select {
                (ChapterTable.manga inList mangaIds) and
                    ((ChapterTable.isRead eq true) or (ChapterTable.isBookmarked eq true) or (ChapterTable.lastPageRead greater 0) or (ChapterTable.lastReadAt greater 0))
            }.toList()
        }

        val chapterCount = ChapterTable.id.count().alias("chapter_count")
        val chapterCountMap = transaction {
            ChapterTable.slice(ChapterTable.manga, chapterCount)
                .select { ChapterTable.manga inList mangaIds }
                .groupBy(ChapterTable.manga)
                .associate {
                    it[ChapterTable.manga].value to it[chapterCount].toInt()
                }
        }
        return chapters.groupBy { it[ChapterTable.manga].value }
            .mapValues { entry -> entry.value.map { ChapterTable.toBackupChapter(it, chapterCountMap[entry.key] ?: 0) } }
    }

    private fun batchBackupMangaCategory(mangaIds: List<Int>, categoryMap: Map<Int, Long>): Map<Int, List<Long>> {
        val relations = transaction {
            CategoryMangaTable.select { CategoryMangaTable.manga inList mangaIds }.toList()
        }
        return relations.groupBy { it[CategoryMangaTable.manga].value }
            .mapValues { entry ->
                entry.value.mapNotNull { categoryMap[it[CategoryMangaTable.category].value] }.toList()
            }
    }

    private fun batchBackupMangaTracking(mangaIds: List<Int>): Map<Int, List<BackupTracking>> {
        val trackingList = transaction {
            TrackRecordTable.select { TrackRecordTable.mangaId inList mangaIds }
                .toList()
        }
        return trackingList.groupBy { it[TrackRecordTable.mangaId] }
            .mapValues { entry -> entry.value.map { TrackRecordTable.toBackupTracking(it) } }
    }

    private fun batchBackupMangaHistory(mangaIds: List<Int>): Map<Int, List<BackupHistory>> {
        val historyList = transaction {
            HistoryTable
                .slice(HistoryTable.mangaId, HistoryTable.lastChapterId, HistoryTable.lastReadAt)
                .select { (HistoryTable.mangaId inList mangaIds) and (HistoryTable.isDelete eq false) }
                .toList()
        }

        val chapterIds = historyList.map { it[HistoryTable.lastChapterId] }.toList()
        val chapterUrlMap = transaction {
            ChapterTable.slice(ChapterTable.id, ChapterTable.url)
                .select { ChapterTable.id inList chapterIds }
                .associate {
                    it[ChapterTable.id].value to it[ChapterTable.url]
                }
        }

        return historyList.groupBy { it[HistoryTable.mangaId] }
            .mapValues { entry ->
                entry.value.mapNotNull { HistoryTable.toBackupHistory(it, chapterUrlMap) }
            }
    }

    private fun backupCategories(dbCategories: List<CategoryDataClass>): List<BackupCategory> {
        return dbCategories.map {
            BackupCategory(
                it.name,
                it.order.toLong(),
                0,
            )
        }
    }

    private fun backupSources(mangas: List<ResultRow>): List<BackupSource> {
        val sourceIds = mangas
            .map { it[MangaTable.sourceReference] }
            .distinct()
            .toList()
        return transaction {
            SourceTable.slice(SourceTable.id, SourceTable.name)
                .select { (SourceTable.id inList sourceIds) }
                .map { BackupSource(it[SourceTable.name], it[SourceTable.id].value) }
        }
    }
}
