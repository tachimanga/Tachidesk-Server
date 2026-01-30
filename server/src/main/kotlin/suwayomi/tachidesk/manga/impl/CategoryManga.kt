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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.case
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import org.tachiyomi.Profiler
import suwayomi.tachidesk.cloud.impl.Sync
import suwayomi.tachidesk.manga.impl.Category.DEFAULT_CATEGORY_ID
import suwayomi.tachidesk.manga.impl.util.lang.isEmpty
import suwayomi.tachidesk.manga.model.dataclass.CategoryDataClass
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.manga.model.table.*
import java.time.Instant

object CategoryManga {
    private val logger = KotlinLogging.logger {}

    fun addMangaToCategory(mangaId: Int, categoryId: Int) {
        fun notAlreadyInCategory() = CategoryMangaTable.select { (CategoryMangaTable.category eq categoryId) and (CategoryMangaTable.manga eq mangaId) }.isEmpty()

        transaction {
            val now = System.currentTimeMillis()
            if (notAlreadyInCategory()) {
                CategoryMangaTable.insert {
                    it[CategoryMangaTable.category] = categoryId
                    it[CategoryMangaTable.manga] = mangaId
                }
            }
            val mangaEntry = transaction { MangaTable.select { MangaTable.id eq mangaId }.first() }
            MangaTable.update({ MangaTable.id eq mangaId }) {
                it[MangaTable.defaultCategory] = false
                if (!mangaEntry[MangaTable.inLibrary]) {
                    it[MangaTable.inLibrary] = true
                    it[MangaTable.inLibraryAt] = Instant.now().epochSecond
                }
                it[MangaTable.updateAt] = now
                it[MangaTable.dirty] = true
            }
        }
        Sync.setNeedsSync()
    }

    fun removeMangaFromCategory(mangaId: Int, categoryId: Int) {
        val now = System.currentTimeMillis()
        transaction {
            CategoryMangaTable.deleteWhere { (CategoryMangaTable.category eq categoryId) and (CategoryMangaTable.manga eq mangaId) }
            MangaTable.update({ MangaTable.id eq mangaId }) {
                it[MangaTable.defaultCategory] = CategoryMangaTable.select { CategoryMangaTable.manga eq mangaId }.count() == 0L
                it[MangaTable.updateAt] = now
                it[MangaTable.dirty] = true
            }
        }
        Sync.setNeedsSync()
    }

    fun updateCategory(mangaId: Int, categoryIdList: List<Int>) {
        val now = System.currentTimeMillis()
        transaction {
            CategoryMangaTable.deleteWhere { (CategoryMangaTable.manga eq mangaId) }
            categoryIdList.forEach { categoryId ->
                CategoryMangaTable.insert {
                    it[CategoryMangaTable.category] = categoryId
                    it[CategoryMangaTable.manga] = mangaId
                }
            }
            val mangaEntry = transaction { MangaTable.select { MangaTable.id eq mangaId }.first() }
            MangaTable.update({ MangaTable.id eq mangaId }) {
                it[MangaTable.defaultCategory] = categoryIdList.isEmpty()
                if (!mangaEntry[MangaTable.inLibrary]) {
                    it[MangaTable.inLibrary] = true
                    it[MangaTable.inLibraryAt] = Instant.now().epochSecond
                }
                it[MangaTable.updateAt] = now
                it[MangaTable.dirty] = true
            }
        }
        Sync.setNeedsSync()
    }

    fun batchUpdateCategory(pairs: List<Pair<Int, List<Int>>>) {
        if (pairs.isEmpty()) {
            return
        }
        val mangaIds = pairs.map { it.first }.toList()
        val dbRelations = transaction {
            CategoryMangaTable.select { CategoryMangaTable.manga inList mangaIds }
                .toList()
        }
        val dbRelationMap = dbRelations.groupBy { it[CategoryMangaTable.manga].value }
            .mapValues { entry -> entry.value.map { it[CategoryMangaTable.category].value } }
        val now = System.currentTimeMillis()
        val seconds = Instant.now().epochSecond
        for (pair in pairs) {
            val mangaId = pair.first
            val targetCategoryIds = pair.second
            transaction {
                val dirty = updateMangaCategories(mangaId, dbRelationMap.getOrDefault(mangaId, emptyList()), targetCategoryIds)
                if (dirty) {
                    // update inLibraryAt
                    MangaTable.update({ (MangaTable.id eq mangaId) and (MangaTable.inLibrary eq false) }) {
                        it[MangaTable.inLibrary] = true
                        it[MangaTable.inLibraryAt] = seconds
                    }
                    // mark dirty
                    MangaTable.update({ MangaTable.id eq mangaId }) {
                        it[MangaTable.defaultCategory] = targetCategoryIds.isEmpty()
                        it[MangaTable.updateAt] = now
                        it[MangaTable.dirty] = true
                    }
                }
            }
        }
        Sync.setNeedsSync()
    }

    private fun updateMangaCategories(
        mangaId: Int,
        dbCategoryIds: List<Int>,
        targetCategoryIds: List<Int>,
    ): Boolean {
        val categoryIdsToDelete = dbCategoryIds.minus(targetCategoryIds.toSet())
        val categoryIdsToAdd = targetCategoryIds.minus(dbCategoryIds.toSet())
        logger.info { "categoryIdsToDelete:$categoryIdsToDelete, categoryIdsToAdd:$categoryIdsToAdd" }
        for (categoryId in categoryIdsToDelete) {
            CategoryMangaTable.deleteWhere { (CategoryMangaTable.category eq categoryId) and (CategoryMangaTable.manga eq mangaId) }
        }
        for (categoryId in categoryIdsToAdd) {
            CategoryMangaTable.insert {
                it[CategoryMangaTable.category] = categoryId
                it[CategoryMangaTable.manga] = mangaId
            }
        }
        return categoryIdsToDelete.isNotEmpty() || categoryIdsToAdd.isNotEmpty()
    }

    fun getCategoryMangaListV2(categoryId: Int): List<MangaDataClass> {
        val mangaList = getMangaListByCategory(categoryId)
        Profiler.split("[Category] get mangaList")

        fillChapterInfo(mangaList)
        Profiler.split("[Category] fillChapterInfo")

        return mangaList
    }

    fun getMangaListByCategory(categoryId: Int): List<MangaDataClass> {
        return if (categoryId == DEFAULT_CATEGORY_ID) {
            transaction {
                MangaTable
                    .select { (MangaTable.inLibrary eq true) and (MangaTable.defaultCategory eq true) }
                    .map { MangaTable.toDataClass(it) }
            }
        } else {
            val mangaIds = transaction {
                CategoryMangaTable
                    .slice(CategoryMangaTable.manga)
                    .select { (CategoryMangaTable.category eq categoryId) }
                    .map { it[CategoryMangaTable.manga].value }
                    .toList()
            }
            transaction {
                MangaTable
                    .select { (MangaTable.id inList mangaIds) and (MangaTable.inLibrary eq true) }
                    .map { MangaTable.toDataClass(it) }
            }
        }
    }

    fun fillChapterInfo(mangaList: List<MangaDataClass>) {
        val mangaIds = mangaList.map { it.id }
        val unreadCountMap = unreadCountMapWithScanlator(mangaIds)

        val unreadCount = Expression.build {
            val caseExpr = case()
                .When(ChapterTable.isRead eq booleanLiteral(false), intLiteral(1))
                .Else(intLiteral(0))
            Sum(caseExpr, IntegerColumnType())
        }
        val downloadedCount = Expression.build {
            val caseExpr = case()
                .When(ChapterTable.isDownloaded eq booleanLiteral(true), intLiteral(1))
                .Else(intLiteral(0))
            Sum(caseExpr, IntegerColumnType())
        }
        val chapterCount = ChapterTable.id.count()
        val lastReadAt = ChapterTable.lastReadAt.max()
        val latestChapterFetchAt = ChapterTable.fetchedAt.max()
        val latestChapterUploadAt = ChapterTable.date_upload.max()

        val mangaMap = mangaList.associateBy { it.id }
        transaction {
            ChapterTable.slice(
                ChapterTable.manga,
                unreadCount,
                downloadedCount,
                chapterCount,
                lastReadAt,
                latestChapterFetchAt,
                latestChapterUploadAt,
            )
                .select { ChapterTable.manga inList mangaIds }
                .groupBy(ChapterTable.manga)
                .forEach {
                    val mangaId = it[ChapterTable.manga].value
                    val dataClass = mangaMap[mangaId]
                    if (dataClass != null) {
                        dataClass.lastReadAt = it[lastReadAt]
                        dataClass.unreadCount = unreadCountMap?.get(mangaId) ?: it[unreadCount]?.toLong()
                        dataClass.downloadCount = it[downloadedCount]?.toLong()
                        dataClass.chapterCount = it[chapterCount]
                        dataClass.latestChapterFetchAt = it[latestChapterFetchAt]
                        dataClass.latestChapterUploadAt = it[latestChapterUploadAt]
                    }
                }
        }
    }

    fun unreadCountMapWithScanlator(mangaIds: List<Int>): Map<Int, Long>? {
        val list = MangaMeta.batchQueryMangaScanlator(mangaIds)
        if (list.isEmpty()) {
            return null
        }
        val map = mutableMapOf<Int, Long>()
        fillUnreadCountMapForScanlatorFilter(list, map)
        fillUnreadCountMapForScanlatorPriority(list, map)
        return map
    }

    private fun fillUnreadCountMapForScanlatorFilter(list: List<Pair<Int, MangaMeta.MangaScanlatorMeta>>, map: MutableMap<Int, Long>) {
        for (pairs in list.chunked(50)) {
            fillUnreadCountMapForScanlatorFilter0(pairs, map)
        }
    }

    private fun fillUnreadCountMapForScanlatorFilter0(list: List<Pair<Int, MangaMeta.MangaScanlatorMeta>>, map: MutableMap<Int, Long>) {
        transaction {
            for (pair in list) {
                val meta = pair.second
                val type = MangaMeta.ScanlatorFilterType.valueOf(meta.type)
                if (type == MangaMeta.ScanlatorFilterType.Filter && meta.list?.isNotEmpty() == true) {
                    val mangaId = pair.first
                    val unreadCount = ChapterTable
                        .select { (ChapterTable.manga eq mangaId) and (ChapterTable.scanlator inList meta.list) and (ChapterTable.isRead eq false) }
                        .count()
                    map[mangaId] = unreadCount
                }
            }
        }
    }

    private fun fillUnreadCountMapForScanlatorPriority(list: List<Pair<Int, MangaMeta.MangaScanlatorMeta>>, map: MutableMap<Int, Long>) {
        val mangaIds = list.filter { it.second.type == MangaMeta.ScanlatorFilterType.Priority.type }
            .map { it.first }
            .toList()
        if (mangaIds.isEmpty()) {
            return
        }

        mangaIds.forEach { mangaId ->
            map[mangaId] = 0
        }

        transaction {
            // SELECT sub.manga, COUNT(sub.manga) FROM
            // (SELECT Chapter.manga, Chapter.chapter_number
            // FROM Chapter
            // WHERE (Chapter.manga IN (4, 5, 1682)) AND (Chapter.chapter_number >= 0.0)
            // GROUP BY Chapter.manga, Chapter.chapter_number
            // HAVING SUM(CASE  WHEN Chapter."read" = 1 THEN 1 ELSE 0 END) = 0
            // ) sub
            // GROUP BY sub.manga
            val caseExpr = case()
                .When(ChapterTable.isRead eq booleanLiteral(true), intLiteral(1))
                .Else(intLiteral(0))
            val subQuery = ChapterTable
                .slice(ChapterTable.manga, ChapterTable.chapter_number)
                .select { (ChapterTable.manga inList mangaIds) and (ChapterTable.chapter_number greaterEq 0f) }
                .groupBy(ChapterTable.manga, ChapterTable.chapter_number)
                .having { Sum(caseExpr, IntegerColumnType()) eq 0 }
                .alias("sub")
            subQuery
                .slice(subQuery[ChapterTable.manga], subQuery[ChapterTable.manga].count())
                .selectAll()
                .groupBy(subQuery[ChapterTable.manga])
                .forEach {
                    val mangaId = it[subQuery[ChapterTable.manga]].value
                    val count = it[subQuery[ChapterTable.manga].count()]
                    map[mangaId] = count
                }

            // SQL: SELECT Chapter.manga, SUM(CASE  WHEN Chapter."read" = 0 THEN 1 ELSE 0 END)
            // FROM Chapter
            // WHERE (Chapter.manga IN (4, 5, 1682)) AND (Chapter.chapter_number < 0.0)
            // GROUP BY Chapter.manga
            val unreadCount = Expression.build {
                val caseExpr2 = case()
                    .When(ChapterTable.isRead eq booleanLiteral(false), intLiteral(1))
                    .Else(intLiteral(0))
                Sum(caseExpr2, IntegerColumnType())
            }
            ChapterTable
                .slice(ChapterTable.manga, unreadCount)
                .select { (ChapterTable.manga inList mangaIds) and (ChapterTable.chapter_number less 0f) }
                .groupBy(ChapterTable.manga)
                .forEach {
                    val mangaId = it[ChapterTable.manga].value
                    val count = it[unreadCount]
                    map[mangaId] = (map[mangaId] ?: 0) + (count ?: 0)
                }
        }
    }

    /**
     * list of categories that a manga belongs to
     */
    fun getMangaCategories(mangaId: Int): List<CategoryDataClass> {
        return transaction {
            val mangaEntry = transaction { MangaTable.select { MangaTable.id eq mangaId }.first() }
            if (mangaEntry[MangaTable.inLibrary]) {
                CategoryMangaTable.innerJoin(CategoryTable).select { CategoryMangaTable.manga eq mangaId }.orderBy(CategoryTable.order to SortOrder.ASC).map {
                    CategoryTable.toDataClass(it)
                }
            } else {
                emptyList()
            }
        }
    }

    @Serializable
    data class MangaCategoryUpdateInput(
        val categoryIdList: List<Int>? = null,
    )
}
