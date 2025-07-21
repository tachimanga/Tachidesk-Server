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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.manga.impl.download.FolderProvider2
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.manga.model.table.*
import suwayomi.tachidesk.server.ApplicationDirs
import java.io.File

object Download {
    private val logger = KotlinLogging.logger {}
    private val applicationDirs by DI.global.instance<ApplicationDirs>()

    fun getDownloadedMangaList(): List<MangaDataClass> {
        val mangaList = transaction {
            MangaTable
                .select { (MangaTable.lastDownloadAt greater 0) }
                .orderBy(MangaTable.lastDownloadAt to SortOrder.DESC)
                .limit(300)
                .map {
                    MangaTable.toDataClass(it)
                }
        }
        return mangaList
    }

    fun batchRemoveDownloads(ids: List<Int>?) {
        if (ids.isNullOrEmpty()) {
            return
        }
        val mangaIds = ids.distinct().toList()
        for (mangaId in mangaIds) {
            FolderProvider2(mangaId, 0, 0).deleteAll()
        }
        transaction {
            MangaTable.update({ MangaTable.id inList mangaIds }) {
                it[lastDownloadAt] = 0
            }
            ChapterTable.update({ (ChapterTable.manga inList mangaIds) and (ChapterTable.isDownloaded eq true) }) {
                it[ChapterTable.isDownloaded] = false
            }
        }
    }

    fun batchRemoveLegacyDownloads(input: BatchRemoveLegacyDownloadsInput) {
        if (input.list?.isNotEmpty() == true) {
            for (info in input.list) {
                if (info.source?.isNotEmpty() == true && info.source?.isNotEmpty() == true) {
                    try {
                        val mangaDir = "${applicationDirs.mangaDownloadsRoot}/${info.source}/${info.title}"
                        logger.info { "batchRemoveLegacyDownloads dir=$mangaDir" }
                        File(mangaDir).deleteRecursively()
                    } catch (e: Exception) {
                        logger.error("FolderProvider deleteAll error", e)
                    }
                }
            }
        }
    }

    fun batchQueryDownloadMangaInfo(): DownloadMangaQueryOutput {
        val mangaIds = mutableListOf<String>()

        // downloads2 -> hash -> mangaId -> chapterId
        val rootDir = File(applicationDirs.mangaDownloadsRoot2)
        traverseDirectories(rootDir, mangaIds, 1)

        val ids =
            mangaIds.mapNotNull { it.toIntOrNull() }
                .take(5000)
                .toList()

        return batchQueryDownloadMangaInfoByIds(ids)
    }

    private fun traverseDirectories(dir: File, mangaIds: MutableList<String>, depth: Int) {
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                if (depth == 2) {
                    mangaIds.add(file.name)
                } else {
                    traverseDirectories(file, mangaIds, depth + 1)
                }
            }
        }
    }

    private fun batchQueryDownloadMangaInfoByIds(mangaIds: List<Int>): DownloadMangaQueryOutput {
        if (mangaIds.isEmpty()) {
            logger.info { "input.mangaIds is empty, skip" }
            return DownloadMangaQueryOutput()
        }
        val mangaRows = transaction {
            MangaTable.slice(MangaTable.id, MangaTable.title, MangaTable.sourceReference, MangaTable.inLibrary, MangaTable.lastDownloadAt)
                .select { MangaTable.id inList mangaIds }
                .toList()
        }
        val sourceMap = mutableMapOf<Long, Int>()
        val sourceList = mutableListOf<DownloadSourceDataClass>()
        val mangaList = mangaRows.map {
            val sourceId = it[MangaTable.sourceReference]
            var sourceIdx = sourceMap[sourceId]
            if (sourceIdx == null) {
                val sourceRow = transaction {
                    SourceTable.slice(SourceTable.id, SourceTable.name, SourceTable.lang)
                        .select { SourceTable.id eq sourceId }
                        .firstOrNull()
                }
                if (sourceRow != null) {
                    val sourceData = DownloadSourceDataClass(id = sourceRow[SourceTable.id].value.toString(), name = sourceRow[SourceTable.name], lang = sourceRow[SourceTable.lang])
                    sourceList.add(sourceData)
                    sourceIdx = sourceList.size - 1
                } else {
                    sourceIdx = -1
                }
                sourceMap[sourceId] = sourceIdx
            }
            DownloadMangaDataClass(sourceIdx = sourceIdx, mangaId = it[MangaTable.id].value, title = it[MangaTable.title], inLibrary = it[MangaTable.inLibrary], lastDownloadAt = it[MangaTable.lastDownloadAt])
        }
        return DownloadMangaQueryOutput(list = mangaList, sourceList = sourceList)
    }

    data class DownloadMangaQueryOutput(
        val list: List<DownloadMangaDataClass>? = null,
        val sourceList: List<DownloadSourceDataClass>? = null,
    )

    data class DownloadMangaDataClass(
        val sourceIdx: Int,
        val inLibrary: Boolean,
        val mangaId: Int,
        val title: String,
        val lastDownloadAt: Long,
    )

    data class DownloadSourceDataClass(
        val id: String,
        val name: String,
        val lang: String,
    )

    @Serializable
    data class BatchInput(
        val mangaIds: List<Int>? = null,
    )

    @Serializable
    data class BatchRemoveLegacyDownloadsInput(
        val list: List<LegacyDownloadsInfo>? = null,
    )

    @Serializable
    data class LegacyDownloadsInfo(
        val source: String? = null,
        val title: String? = null,
    )
}
