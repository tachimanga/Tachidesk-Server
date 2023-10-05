package suwayomi.tachidesk.manga.impl

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.transactions.transaction
import org.tachiyomi.Profiler
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.manga.model.table.*

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

object History {
    fun getHistoryMangaList(): List<MangaDataClass> {
        val lastChapterList = transaction {
            ChapterTable
                .select { (ChapterTable.lastReadAt greater 0) }
                .orderBy(ChapterTable.lastReadAt to SortOrder.DESC)
                .limit(1000)
                .map { ChapterTable.toDataClass(it) }
        }
        val lastChapterMap = lastChapterList.groupBy { it.mangaId }
            .mapValues { it.value.maxByOrNull { item -> item.lastReadAt } }
        val mangaIds = lastChapterMap.keys.toList()
        Profiler.split("mangaIds done")

        val mangaList = transaction {
            // Fetch data from the MangaTable and join with the CategoryMangaTable, if a category is specified
            MangaTable
                .select { (MangaTable.id inList mangaIds) }
                .map {
                    // Map the data from the result row to the MangaDataClass
                    val dataClass = MangaTable.toDataClass(it)
                    dataClass.lastReadAt = lastChapterMap[dataClass.id]?.lastReadAt
                    dataClass.lastChapterRead = lastChapterMap[dataClass.id]
                    dataClass
                }
        }
        Profiler.split("mangaList done")
        return mangaList.sortedByDescending { it.lastReadAt }
    }

    fun batchDelete(input: BatchInput) {
        if (input.mangaIds.isNullOrEmpty()) {
            return
        }
        transaction {
            ChapterTable.update({ (ChapterTable.manga inList input.mangaIds) }) { update ->
                update[ChapterTable.lastReadAt] = 0
            }
        }
    }

    @Serializable
    data class BatchInput(
        val mangaIds: List<Int>? = null
    )
}
