package suwayomi.tachidesk.manga.impl

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import suwayomi.tachidesk.cloud.impl.Sync
import suwayomi.tachidesk.cloud.util.UUIDUtils
import suwayomi.tachidesk.manga.impl.CategoryManga.removeMangaFromCategory
import suwayomi.tachidesk.manga.impl.util.lang.isNotEmpty
import suwayomi.tachidesk.manga.model.dataclass.CategoryDataClass
import suwayomi.tachidesk.manga.model.table.*

object Category {
    private val logger = KotlinLogging.logger {}

    /**
     * The new category will be placed at the end of the list
     */
    fun createCategory(name: String): Int {
        // creating a category named Default is illegal
        if (name.equals(DEFAULT_CATEGORY_NAME, ignoreCase = true)) return -1
        val now = System.currentTimeMillis()
        val categoryId = transaction {
            val categoryDb = CategoryTable.select { CategoryTable.name eq name }.firstOrNull()
            if (categoryDb == null) {
                val sameUuidCategory = CategoryTable.select { CategoryTable.uuid eq name }.firstOrNull()
                logger.info { "sameUuidCategory = $sameUuidCategory" }
                val uuid = if (sameUuidCategory == null) name else UUIDUtils.generateUUID()
                val newCategoryId = CategoryTable.insertAndGetId {
                    it[CategoryTable.name] = name
                    it[CategoryTable.order] = Int.MAX_VALUE
                    it[CategoryTable.uuid] = uuid
                    it[CategoryTable.isDelete] = false
                    it[CategoryTable.createAt] = now
                    it[CategoryTable.updateAt] = now
                    it[CategoryTable.dirty] = true
                }.value

                normalizeCategories()

                newCategoryId
            } else if (categoryDb[CategoryTable.isDelete]) {
                val id = categoryDb[CategoryTable.id].value
                CategoryTable.update({ CategoryTable.id eq id }) {
                    it[CategoryTable.order] = Int.MAX_VALUE
                    it[CategoryTable.isDelete] = false
                    it[CategoryTable.updateAt] = now
                    it[CategoryTable.dirty] = true
                }

                normalizeCategories()

                id
            } else {
                -1
            }
        }
        if (categoryId > 0) {
            Sync.setNeedsSync()
        }
        return categoryId
    }

    fun updateCategory(categoryId: Int, name: String?, isDefault: Boolean?) {
        val now = System.currentTimeMillis()
        transaction {
            CategoryTable.update({ CategoryTable.id eq categoryId }) {
                if (name != null && !name.equals(DEFAULT_CATEGORY_NAME, ignoreCase = true)) it[CategoryTable.name] = name
                if (isDefault != null) it[CategoryTable.isDefault] = isDefault
                it[CategoryTable.updateAt] = now
                it[CategoryTable.dirty] = true
            }
        }
        Sync.setNeedsSync()
    }

    /**
     * Move the category from order number `from` to `to`
     */
    fun reorderCategory(from: Int, to: Int) {
        val now = System.currentTimeMillis()
        transaction {
            val categories = CategoryTable.select { CategoryTable.isDelete eq false }
                .orderBy(CategoryTable.order to SortOrder.ASC).toMutableList()
            categories.add(to - 1, categories.removeAt(from - 1))
            categories.forEachIndexed { index, category ->
                val order = index + 1
                if (category[CategoryTable.order] != order) {
                    CategoryTable.update({ CategoryTable.id eq category[CategoryTable.id].value }) {
                        it[CategoryTable.order] = order
                        it[CategoryTable.updateAt] = now
                        it[CategoryTable.dirty] = true
                    }
                }
            }
        }
        Sync.setNeedsSync()
    }

    fun removeCategory(categoryId: Int) {
        val now = System.currentTimeMillis()
        transaction {
            CategoryMangaTable.select { CategoryMangaTable.category eq categoryId }
                .forEach {
                    removeMangaFromCategory(it[CategoryMangaTable.manga].value, categoryId)
                }
            CategoryTable.update({ CategoryTable.id eq categoryId }) {
                it[CategoryTable.isDelete] = true
                it[CategoryTable.updateAt] = now
                it[CategoryTable.dirty] = true
            }
            normalizeCategories()
        }
        Sync.setNeedsSync()
    }

    /** make sure category order numbers starts from 1 and is consecutive */
    fun normalizeCategories() {
        val now = System.currentTimeMillis()
        transaction {
            val categories = CategoryTable.select { CategoryTable.isDelete eq false }
                .orderBy(CategoryTable.order to SortOrder.ASC)
            categories.forEachIndexed { index, category ->
                val order = index + 1
                if (category[CategoryTable.order] != order) {
                    CategoryTable.update({ CategoryTable.id eq category[CategoryTable.id].value }) {
                        it[CategoryTable.order] = order
                        it[CategoryTable.updateAt] = now
                        it[CategoryTable.dirty] = true
                    }
                }
            }
        }
    }

    const val DEFAULT_CATEGORY_ID = 0
    const val DEFAULT_CATEGORY_NAME = "Default"
    private fun addDefaultIfNecessary(categories: List<CategoryDataClass>): List<CategoryDataClass> =
        if (categories.isEmpty() ||
            MangaTable.select { (MangaTable.inLibrary eq true) and (MangaTable.defaultCategory eq true) }.isNotEmpty()
        ) {
            listOf(CategoryDataClass(DEFAULT_CATEGORY_ID, 0, DEFAULT_CATEGORY_NAME, true)) + categories
        } else {
            categories
        }

    fun getCategoryList(): List<CategoryDataClass> {
        val categories = transaction {
            val categories = CategoryTable.select { CategoryTable.isDelete eq false }
                .orderBy(CategoryTable.order to SortOrder.ASC)
                .map {
                    CategoryTable.toDataClass(it)
                }
            addDefaultIfNecessary(categories)
        }

        val categoryIds = categories.map { it.id }
        val metaMap = batchGetCategoryMetaMap(categoryIds)
        categories.forEach {
            val meta = metaMap[it.id]
            if (meta != null) {
                it.meta = meta
            }
        }

        return categories
    }

    fun getCategoryById(categoryId: Int): CategoryDataClass? {
        return transaction {
            CategoryTable.select { CategoryTable.id eq categoryId }.firstOrNull()?.let {
                CategoryTable.toDataClass(it)
            }
        }
    }

    fun getCategoryMetaMap(categoryId: Int): Map<String, String> {
        return transaction {
            CategoryMetaTable.select { CategoryMetaTable.ref eq categoryId }
                .associate { it[CategoryMetaTable.key] to it[CategoryMetaTable.value] }
        }
    }

    fun batchGetCategoryMetaMap(categoryIds: List<Int>): Map<Int, Map<String, String>> {
        val list = transaction {
            CategoryMetaTable.select { CategoryMetaTable.ref inList categoryIds }
                .toList()
        }
        val map = list.groupBy { it[CategoryMetaTable.ref].value }
            .mapValues { kv ->
                kv.value.associate { it[CategoryMetaTable.key] to it[CategoryMetaTable.value] }
            }
        return map
    }

    fun modifyMeta(categoryId: Int, key: String, value: String?) {
        if (value == null) {
            transaction {
                CategoryMetaTable.deleteWhere { (CategoryMetaTable.ref eq categoryId) and (CategoryMetaTable.key eq key) }
            }
            markDirtyAndSetNeedSync(categoryId)
            return
        }
        transaction {
            val meta = transaction {
                CategoryMetaTable.select { (CategoryMetaTable.ref eq categoryId) and (CategoryMetaTable.key eq key) }
            }.firstOrNull()

            if (meta == null) {
                CategoryMetaTable.insert {
                    it[CategoryMetaTable.key] = key
                    it[CategoryMetaTable.value] = value
                    it[CategoryMetaTable.ref] = categoryId
                }
            } else {
                CategoryMetaTable.update({ (CategoryMetaTable.ref eq categoryId) and (CategoryMetaTable.key eq key) }) {
                    it[CategoryMetaTable.value] = value
                }
            }
        }
        markDirtyAndSetNeedSync(categoryId)
    }

    fun modifyMetas(categoryId: Int, metaMap: Map<String, String>) {
        transaction {
            val metaMapDb = CategoryMetaTable.select { CategoryMetaTable.ref eq categoryId }
                .associate { it[CategoryMetaTable.key] to it[CategoryMetaTable.value] }
            metaMap.forEach { (k, v) ->
                val valueDb = metaMapDb[k]
                if (valueDb == null) {
                    CategoryMetaTable.insert {
                        it[CategoryMetaTable.key] = k
                        it[CategoryMetaTable.value] = v
                        it[CategoryMetaTable.ref] = categoryId
                    }
                } else if (valueDb != v) {
                    CategoryMetaTable.update({ (CategoryMetaTable.ref eq categoryId) and (CategoryMetaTable.key eq k) }) {
                        it[CategoryMetaTable.value] = v
                    }
                }
            }
        }
        markDirtyAndSetNeedSync(categoryId)
    }

    private fun markDirtyAndSetNeedSync(categoryId: Int) {
        val now = System.currentTimeMillis()
        transaction {
            CategoryTable.update({ CategoryTable.id eq categoryId }) {
                it[CategoryTable.updateAt] = now
                it[CategoryTable.dirty] = true
            }
        }
        Sync.setNeedsSync()
    }
}
