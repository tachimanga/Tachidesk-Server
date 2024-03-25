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
import org.jetbrains.exposed.sql.transactions.transaction
import org.tachiyomi.Profiler
import suwayomi.tachidesk.manga.impl.Category.DEFAULT_CATEGORY_ID
import suwayomi.tachidesk.manga.impl.util.lang.isEmpty
import suwayomi.tachidesk.manga.model.dataclass.CategoryDataClass
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.manga.model.table.CategoryMangaTable
import suwayomi.tachidesk.manga.model.table.CategoryTable
import suwayomi.tachidesk.manga.model.table.ChapterTable
import suwayomi.tachidesk.manga.model.table.MangaTable
import suwayomi.tachidesk.manga.model.table.toDataClass
import java.time.Instant

object CategoryManga {
    private val logger = KotlinLogging.logger {}

    fun addMangaToCategory(mangaId: Int, categoryId: Int) {
        fun notAlreadyInCategory() = CategoryMangaTable.select { (CategoryMangaTable.category eq categoryId) and (CategoryMangaTable.manga eq mangaId) }.isEmpty()

        transaction {
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
            }
        }
    }

    fun removeMangaFromCategory(mangaId: Int, categoryId: Int) {
        transaction {
            CategoryMangaTable.deleteWhere { (CategoryMangaTable.category eq categoryId) and (CategoryMangaTable.manga eq mangaId) }
            if (CategoryMangaTable.select { CategoryMangaTable.manga eq mangaId }.count() == 0L) {
                MangaTable.update({ MangaTable.id eq mangaId }) {
                    it[MangaTable.defaultCategory] = true
                }
            }
        }
    }

    fun updateCategory(mangaId: Int, categoryIdList: List<Int>) {
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
            }
        }
    }

    /**
     * list of mangas that belong to a category
     */
    fun getCategoryMangaList(categoryId: Int): List<MangaDataClass> {
        // Select the required columns from the MangaTable and add the aggregate functions to compute unread, download, and chapter counts
        val unreadCount = wrapAsExpression<Int>(
            ChapterTable.slice(ChapterTable.id.count()).select((ChapterTable.isRead eq false) and (ChapterTable.manga eq MangaTable.id))
        )
        val downloadedCount = wrapAsExpression<Int>(
            ChapterTable.slice(ChapterTable.id.count()).select((ChapterTable.isDownloaded eq true) and (ChapterTable.manga eq MangaTable.id))
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
                latestChapterUploadAt
            )
                .select { ChapterTable.manga inList mangaIds }
                .groupBy(ChapterTable.manga)
                .forEach {
                    val dataClass = mangaMap[it[ChapterTable.manga].value]
                    if (dataClass != null) {
                        dataClass.lastReadAt = it[lastReadAt]
                        dataClass.unreadCount = it[unreadCount]?.toLong()
                        dataClass.downloadCount = it[downloadedCount]?.toLong()
                        dataClass.chapterCount = it[chapterCount]
                        dataClass.latestChapterFetchAt = it[latestChapterFetchAt]
                        dataClass.latestChapterUploadAt = it[latestChapterUploadAt]
                    }
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
        val categoryIdList: List<Int>? = null
    )
}
