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

    /**
     * list of mangas that belong to a category
     */
    fun getCategoryMangaList(categoryId: Int): List<MangaDataClass> {
        // Select the required columns from the MangaTable and add the aggregate functions to compute unread, download, and chapter counts
        val unreadCount = wrapAsExpression<Int>(
            ChapterTable.slice(ChapterTable.id.count()).select((ChapterTable.isRead eq false) and (ChapterTable.manga eq MangaTable.id)),
        )
        val downloadedCount = wrapAsExpression<Int>(
            ChapterTable.slice(ChapterTable.id.count()).select((ChapterTable.isDownloaded eq true) and (ChapterTable.manga eq MangaTable.id)),
        )

        val chapterCount = ChapterTable.id.count().alias("chapter_count")
        val lastReadAt = ChapterTable.lastReadAt.max().alias("last_read_at")
        val selectedColumns = MangaTable.columns + unreadCount + downloadedCount + chapterCount + lastReadAt

        val transform: (ResultRow) -> MangaDataClass = {
            // Map the data from the result row to the MangaDataClass
            val dataClass = MangaTable.toDataClass(it)
            dataClass.lastReadAt = it[lastReadAt]
            dataClass.unreadCount = it[unreadCount]?.toLong()
            dataClass.downloadCount = it[downloadedCount]?.toLong()
            dataClass.chapterCount = it[chapterCount]
            dataClass
        }

        return transaction {
            // Fetch data from the MangaTable and join with the CategoryMangaTable, if a category is specified
            val query = if (categoryId == DEFAULT_CATEGORY_ID) {
                MangaTable
                    .leftJoin(ChapterTable, { MangaTable.id }, { ChapterTable.manga })
                    .slice(columns = selectedColumns)
                    .select { (MangaTable.inLibrary eq true) and (MangaTable.defaultCategory eq true) }
            } else {
                MangaTable
                    .innerJoin(CategoryMangaTable)
                    .leftJoin(ChapterTable, { MangaTable.id }, { ChapterTable.manga })
                    .slice(columns = selectedColumns)
                    .select { (MangaTable.inLibrary eq true) and (CategoryMangaTable.category eq categoryId) }
            }

            // Join with the ChapterTable to fetch the last read chapter for each manga
            query.groupBy(*MangaTable.columns.toTypedArray()).map(transform)
        }
    }

    fun getCategoryMangaListV2(categoryId: Int): List<MangaDataClass> {
        return if (categoryId == DEFAULT_CATEGORY_ID) {
            val mangaList = transaction {
                MangaTable
                    .select { (MangaTable.inLibrary eq true) and (MangaTable.defaultCategory eq true) }
                    .map { MangaTable.toDataClass(it) }
            }
            Profiler.split("[Category] get mangaList")
            fillChapterInfo(mangaList)
            Profiler.split("[Category] fillChapterInfo")
            mangaList
        } else {
            val mangaIds = transaction {
                CategoryMangaTable
                    .slice(CategoryMangaTable.manga)
                    .select { (CategoryMangaTable.category eq categoryId) }
                    .map { it[CategoryMangaTable.manga].value }
                    .toList()
            }
            Profiler.split("[Category] get mangaIds")
            val mangaList = transaction {
                MangaTable
                    .select { (MangaTable.id inList mangaIds) and (MangaTable.inLibrary eq true) }
                    .map { MangaTable.toDataClass(it) }
            }
            Profiler.split("[Category] get mangaList")
            fillChapterInfo(mangaList)
            Profiler.split("[Category] fillChapterInfo")
            mangaList
        }
    }

    private fun fillChapterInfo(mangaList: List<MangaDataClass>) {
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

    private fun unreadCountMapWithScanlator(mangaIds: List<Int>): Map<Int, Long>? {
        val list = MangaMeta.batchQueryMangaScanlator(mangaIds)
        if (list.isEmpty()) {
            return null
        }
        val map = mutableMapOf<Int, Long>()
        transaction {
            for (pair in list) {
                val mangaId = pair.first
                val unreadCount = ChapterTable
                    .select { (ChapterTable.manga eq mangaId) and (ChapterTable.scanlator inList pair.second) and (ChapterTable.isRead eq false) }
                    .count()
                map[mangaId] = unreadCount
            }
        }
        return map
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
