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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import suwayomi.tachidesk.manga.model.dataclass.ChapterDataClass
import suwayomi.tachidesk.manga.model.dataclass.MangaBatchQueryDataClass
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.manga.model.dataclass.SourceDataClass
import suwayomi.tachidesk.manga.model.table.*

object MangaQuery {
    private val logger = KotlinLogging.logger {}

    fun batchQuery(input: MangaBatchQueryInput): MangaBatchQueryDataClass {
        if (input.mangaIds.isEmpty()) {
            return MangaBatchQueryDataClass(mangaList = emptyList())
        }

        val list = transaction {
            MangaTable
                .select { (MangaTable.id inList input.mangaIds) }
                .map { MangaTable.toDataClass(it) }
        }

        if (input.queryChapterCount == true) {
            CategoryManga.fillChapterInfo(list)
        }

        if (input.querySource == true) {
            fillSourceInfo(list)
        }

        if (input.queryLatestChapter == true) {
            fillLatestChapter(list)
        }

        return MangaBatchQueryDataClass(list)
    }

    fun fillSourceInfo(mangaList: List<MangaDataClass>) {
        val sourceIds = mangaList.map { it.sourceId }.distinct().map { it.toLong() }.toList()
        if (sourceIds.isEmpty()) {
            return
        }

        val sourceMap = transaction {
            SourceTable
                .slice(SourceTable.id, SourceTable.name, SourceTable.lang)
                .select { (SourceTable.id inList sourceIds) }
                .map {
                    SourceDataClass(
                        id = it[SourceTable.id].value.toString(),
                        name = it[SourceTable.name],
                        lang = it[SourceTable.lang],
                        iconUrl = "",
                        extPkgName = "",
                        supportsLatest = false,
                        isConfigurable = false,
                        isNsfw = false,
                        displayName = "",
                    )
                }.associateBy { it.id }
        }
        mangaList.forEach {
            it.source = sourceMap[it.sourceId]
        }
    }

    private fun fillLatestChapter(mangaList: List<MangaDataClass>) {
        for (list in mangaList.chunked(50)) {
            for (manga in list) {
                manga.latestChapter = transaction {
                    ChapterTable
                        .slice(ChapterTable.id, ChapterTable.name, ChapterTable.chapter_number, ChapterTable.manga)
                        .select { ChapterTable.manga eq manga.id }
                        .orderBy(ChapterTable.sourceOrder to SortOrder.DESC)
                        .limit(1)
                        .map {
                            ChapterDataClass(
                                id = it[ChapterTable.id].value,
                                name = it[ChapterTable.name],
                                mangaId = it[ChapterTable.manga].value,
                                chapterNumber = it[ChapterTable.chapter_number],
                                url = "",
                                scanlator = "",
                                read = false,
                                bookmarked = false,
                                lastPageRead = 0,
                                lastReadAt = 0,
                                index = 0,
                                fetchedAt = 0,
                                downloaded = false,
                                uploadDate = 0,
                            )
                        }
                        .firstOrNull()
                }
            }
        }
    }

    @Serializable
    data class MangaBatchQueryInput(
        val mangaIds: List<Int>,
        val querySource: Boolean? = null,
        val queryChapterCount: Boolean? = null,
        val queryLatestChapter: Boolean? = null,
    )
}
