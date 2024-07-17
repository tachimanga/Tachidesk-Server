package suwayomi.tachidesk.manga.impl

import kotlinx.serialization.Serializable
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.manga.model.table.*

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

object History {
    private val logger = KotlinLogging.logger {}

    fun upsertHistory(mangaId: Int, chapterId: Int) {
        transaction {
            val history = transaction {
                HistoryTable.select { HistoryTable.mangaId eq mangaId }
                    .firstOrNull()
            }
            val now = System.currentTimeMillis()
            if (history != null) {
                HistoryTable.update({ HistoryTable.id eq history[HistoryTable.id] }) {
                    it[HistoryTable.updateAt] = now
                    it[HistoryTable.lastChapterId] = chapterId
                    it[HistoryTable.lastReadAt] = now / 1000
                    it[HistoryTable.isDelete] = false
                }
            } else {
                HistoryTable.insert {
                    it[HistoryTable.createAt] = now
                    it[HistoryTable.updateAt] = now
                    it[HistoryTable.mangaId] = mangaId
                    it[HistoryTable.lastChapterId] = chapterId
                    it[HistoryTable.lastReadAt] = now / 1000
                    it[HistoryTable.isDelete] = false
                }
            }
        }
    }

    fun getHistoryMangaListV2(): List<MangaDataClass> {
        val historyList = transaction {
            HistoryTable.select { HistoryTable.isDelete eq false }
                .orderBy(HistoryTable.lastReadAt to SortOrder.DESC)
                .limit(200)
                .toList()
        }

        val mangaIds = historyList.map { it[HistoryTable.mangaId] }.toList()
        val mangaList = transaction {
            MangaTable
                .select { (MangaTable.id inList mangaIds) }
                .map {
                    MangaTable.toDataClass(it)
                }
        }
        val mangaMap = mangaList.associateBy { it.id }

        val chapterIds = historyList.map { it[HistoryTable.lastChapterId] }.toList()
        val chapterList = transaction {
            ChapterTable
                .select { (ChapterTable.id inList chapterIds) }
                .map { ChapterTable.toDataClass(it) }
        }
        val chapterMap = chapterList.associateBy { it.id }

        val list = historyList.mapNotNull {
            val manga = mangaMap[it[HistoryTable.mangaId]]
            val chapter = chapterMap[it[HistoryTable.lastChapterId]]
            if (manga != null && chapter != null) {
                manga.lastReadAt = it[HistoryTable.lastReadAt]
                manga.lastChapterRead = chapter
            }
            manga
        }
        return list
    }

    fun batchDeleteV2(input: BatchInput) {
        if (input.mangaIds.isNullOrEmpty()) {
            return
        }
        val now = System.currentTimeMillis()
        transaction {
            HistoryTable.update({ (HistoryTable.mangaId inList input.mangaIds) and (HistoryTable.isDelete eq false) }) { update ->
                update[HistoryTable.isDelete] = true
                update[HistoryTable.updateAt] = now
            }
        }
    }

    fun clearHistory(input: ClearInput) {
        if (input.clearAll == true) {
            clearAll()
        } else if (input.lastReadAt != null) {
            clearAfter(input.lastReadAt)
        }
    }

    private fun clearAll() {
        val now = System.currentTimeMillis()
        transaction {
            HistoryTable.update({ (HistoryTable.isDelete eq false) }) { update ->
                update[HistoryTable.isDelete] = true
                update[HistoryTable.updateAt] = now
            }
        }
    }

    private fun clearAfter(lastReadAt: Long) {
        val now = System.currentTimeMillis()
        transaction {
            HistoryTable.update({ (HistoryTable.isDelete eq false) and (HistoryTable.lastReadAt greaterEq lastReadAt) }) { update ->
                update[HistoryTable.isDelete] = true
                update[HistoryTable.updateAt] = now
            }
        }
    }

    @Serializable
    data class BatchInput(
        val mangaIds: List<Int>? = null
    )

    @Serializable
    data class ClearInput(
        // seconds
        val lastReadAt: Long? = null,
        val clearAll: Boolean? = null
    )
}
