package suwayomi.tachidesk.manga.impl

import kotlinx.serialization.Serializable
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.transactions.transaction
import suwayomi.tachidesk.cloud.impl.Sync
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.manga.model.table.*
import kotlin.math.max

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

object History {
    private val logger = KotlinLogging.logger {}

    fun upsertHistory(
        mangaId: Int,
        chapterId: Int,
        readDuration: Int,
        lastReadAt: Long? = null,
        lastChapterName: String? = null,
    ) {
        var markDirty = false
        transaction {
            val history = transaction {
                HistoryTable.select { HistoryTable.mangaId eq mangaId }
                    .firstOrNull()
            }
            val now = System.currentTimeMillis()
            if (history != null) {
                HistoryTable.update({ HistoryTable.id eq history[HistoryTable.id] }) {
                    it[HistoryTable.lastChapterId] = chapterId
                    it[HistoryTable.lastChapterName] = lastChapterName
                    if (lastReadAt != null) {
                        it[HistoryTable.lastReadAt] = max(history[HistoryTable.lastReadAt], lastReadAt)
                    } else {
                        it[HistoryTable.lastReadAt] = now / 1000
                    }
                    if (lastReadAt != null) {
                        it[HistoryTable.readDuration] = max(history[HistoryTable.readDuration], readDuration)
                    } else {
                        it[HistoryTable.readDuration] = HistoryTable.readDuration + readDuration
                    }
                    it[HistoryTable.isDelete] = false
                    if (history[HistoryTable.lastChapterId] != chapterId || history[HistoryTable.isDelete]) {
                        it[HistoryTable.updateAt] = now
                        it[HistoryTable.dirty] = true
                        markDirty = true
                    }
                }
            } else {
                HistoryTable.insert {
                    it[HistoryTable.createAt] = now
                    it[HistoryTable.updateAt] = now
                    it[HistoryTable.mangaId] = mangaId
                    it[HistoryTable.lastChapterId] = chapterId
                    it[HistoryTable.lastChapterName] = lastChapterName
                    if (lastReadAt != null) {
                        it[HistoryTable.lastReadAt] = max(0, lastReadAt)
                    } else {
                        it[HistoryTable.lastReadAt] = now / 1000
                    }
                    it[HistoryTable.readDuration] = readDuration
                    it[HistoryTable.isDelete] = false
                    it[HistoryTable.dirty] = true
                    markDirty = true
                }
            }
        }
        if (markDirty) {
            Sync.setNeedsSync()
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

        val chapterIds = historyList.map { it[HistoryTable.lastChapterId] }
            .filter { it != 0 }
            .toList()
        val chapterList = transaction {
            ChapterTable
                .select { (ChapterTable.id inList chapterIds) }
                .map { ChapterTable.toDataClass(it) }
        }
        val chapterMap = chapterList.associateBy { it.id }

        val list = historyList.mapNotNull {
            val manga = mangaMap[it[HistoryTable.mangaId]]
            val chapter = chapterMap[it[HistoryTable.lastChapterId]]
            if (manga != null) {
                manga.lastReadAt = it[HistoryTable.lastReadAt]
                manga.lastChapterReadName = it[HistoryTable.lastChapterName]
                manga.readDuration = it[HistoryTable.readDuration]
            }
            if (manga != null && chapter != null) {
                manga.lastChapterRead = chapter
            }
            manga
        }
        return list
    }

    fun queryMangaReadDuration(mangaId: Int): Int? {
        return transaction {
            HistoryTable.slice(HistoryTable.readDuration)
                .select { (HistoryTable.mangaId eq mangaId) and (HistoryTable.isDelete eq false) }
                .firstOrNull()
                ?.get(HistoryTable.readDuration)
        }
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
                update[HistoryTable.readDuration] = 0
                update[HistoryTable.dirty] = true
            }
        }
        Sync.setNeedsSync()
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
                update[HistoryTable.readDuration] = 0
                update[HistoryTable.dirty] = true
            }
        }
        Sync.setNeedsSync()
    }

    private fun clearAfter(lastReadAt: Long) {
        val now = System.currentTimeMillis()
        transaction {
            HistoryTable.update({ (HistoryTable.isDelete eq false) and (HistoryTable.lastReadAt greaterEq lastReadAt) }) { update ->
                update[HistoryTable.isDelete] = true
                update[HistoryTable.updateAt] = now
                update[HistoryTable.readDuration] = 0
                update[HistoryTable.dirty] = true
            }
        }
        Sync.setNeedsSync()
    }

    @Serializable
    data class BatchInput(
        val mangaIds: List<Int>? = null,
    )

    @Serializable
    data class ClearInput(
        // seconds
        val lastReadAt: Long? = null,
        val clearAll: Boolean? = null,
    )
}
