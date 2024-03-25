package suwayomi.tachidesk.manga.impl

import kotlinx.serialization.Serializable
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.tachiyomi.Profiler
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.manga.model.table.*
import suwayomi.tachidesk.server.database.MyBatchInsertStatement

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

object History {
    private val logger = KotlinLogging.logger {}

    fun migrateHistoryIfNeeded() {
        transaction {
            val flag = SettingTable.select { SettingTable.key eq SettingKey.HistoryMigrate.name }.count()
            logger.info { "migrateHistoryIfNeeded flag=$flag" }
            if (flag > 0) {
                return@transaction
            }
            val list = getHistoryMangaList()
            val now = System.currentTimeMillis()
            if (list.isNotEmpty()) {
                val myBatchInsertStatement = MyBatchInsertStatement(HistoryTable)
                list.forEach { mangaData ->
                    val my = myBatchInsertStatement

                    my.addBatch()

                    my[HistoryTable.createAt] = now
                    my[HistoryTable.updateAt] = now
                    my[HistoryTable.mangaId] = mangaData.id
                    my[HistoryTable.lastChapterId] = mangaData.lastChapterRead?.id ?: 0
                    my[HistoryTable.lastReadAt] = mangaData.lastReadAt ?: 0
                }

                val sql = myBatchInsertStatement.prepareSQL(this)
                val conn = (TransactionManager.current().connection as JdbcConnectionImpl).connection
                val statement = conn.createStatement()
                // println(sql)
                statement.execute(sql)
            }
            SettingTable.insert {
                it[SettingTable.key] = SettingKey.HistoryMigrate.name
                it[SettingTable.value] = "1"
                it[SettingTable.createAt] = now
                it[SettingTable.updateAt] = now
            }
        }
    }

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

    fun clearAll() {
        val now = System.currentTimeMillis()
        transaction {
            HistoryTable.update({ (HistoryTable.isDelete eq false) }) { update ->
                update[HistoryTable.isDelete] = true
                update[HistoryTable.updateAt] = now
            }
        }
    }

    @Serializable
    data class BatchInput(
        val mangaIds: List<Int>? = null
    )
}
