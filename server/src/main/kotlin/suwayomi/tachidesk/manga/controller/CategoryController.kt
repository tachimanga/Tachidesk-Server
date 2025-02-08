package suwayomi.tachidesk.manga.controller

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import io.javalin.http.HttpCode
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import org.tachiyomi.Profiler
import suwayomi.tachidesk.manga.impl.Category
import suwayomi.tachidesk.manga.impl.CategoryManga
import suwayomi.tachidesk.manga.model.dataclass.CategoryDataClass
import suwayomi.tachidesk.manga.model.dataclass.MangaDataClass
import suwayomi.tachidesk.server.util.formParam
import suwayomi.tachidesk.server.util.handler
import suwayomi.tachidesk.server.util.pathParam
import suwayomi.tachidesk.server.util.withOperation

object CategoryController {
    private val json by DI.global.instance<Json>()
    private val logger = KotlinLogging.logger {}

    /** category list */
    val categoryList = handler(
        documentWith = {
            withOperation {
                summary("Category list")
                description("get a list of categories")
            }
        },
        behaviorOf = { ctx ->
            ctx.json(Category.getCategoryList())
        },
        withResults = {
            json<Array<CategoryDataClass>>(HttpCode.OK)
        },
    )

    /** category create */
    val categoryCreate = handler(
        formParam<String>("name"),
        documentWith = {
            withOperation {
                summary("Category create")
                description("Create a category")
            }
        },
        behaviorOf = { ctx, name ->
            Category.createCategory(name)
            ctx.status(200)
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    /** category modification */
    val categoryModify = handler(
        pathParam<Int>("categoryId"),
        formParam<String?>("name"),
        formParam<Boolean?>("default"),
        documentWith = {
            withOperation {
                summary("Category modify")
                description("Modify a category")
            }
        },
        behaviorOf = { ctx, categoryId, name, isDefault ->
            Category.updateCategory(categoryId, name, isDefault)
            ctx.status(200)
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    /** category delete */
    val categoryDelete = handler(
        pathParam<Int>("categoryId"),
        documentWith = {
            withOperation {
                summary("Category delete")
                description("Delete a category")
            }
        },
        behaviorOf = { ctx, categoryId ->
            Category.removeCategory(categoryId)
            ctx.status(200)
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    /** returns the manga list associated with a category */
    val categoryMangas = handler(
        pathParam<Int>("categoryId"),
        documentWith = {
            withOperation {
                summary("Category manga")
                description("Returns the manga list associated with a category")
            }
        },
        behaviorOf = { ctx, categoryId ->
            Profiler.start()
            ctx.json(CategoryManga.getCategoryMangaListV2(categoryId))
            Profiler.all()
        },
        withResults = {
            json<Array<MangaDataClass>>(HttpCode.OK)
        },
    )

    /** category re-ordering */
    val categoryReorder = handler(
        formParam<Int>("from"),
        formParam<Int>("to"),
        documentWith = {
            withOperation {
                summary("Category re-ordering")
                description("Re-order a category")
            }
        },
        behaviorOf = { ctx, from, to ->
            Category.reorderCategory(from, to)
            ctx.status(200)
        },
        withResults = {
            httpCode(HttpCode.OK)
        },
    )

    /** used to modify a category's meta parameters */
    val meta = handler(
        pathParam<Int>("categoryId"),
        documentWith = {
        },
        behaviorOf = { ctx, categoryId ->
            val input = json.decodeFromString<CategoryMetaUpdate>(ctx.body())
            logger.info { "update category meta: id=$categoryId input:$input" }
            Category.modifyMeta(categoryId, input.key, input.value)
            ctx.status(200)
        },
        withResults = {
            httpCode(HttpCode.OK)
            httpCode(HttpCode.NOT_FOUND)
        },
    )

    @Serializable
    data class CategoryMetaUpdate(
        val key: String,
        val value: String? = null,
    )
}
