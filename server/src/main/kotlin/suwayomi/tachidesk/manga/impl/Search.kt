package suwayomi.tachidesk.manga.impl

/*
 * Copyright (C) Contributors to the Suwayomi project
 * Copyright (C) 2025 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import io.javalin.plugin.json.JsonMapper
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.manga.impl.MangaList.processEntries
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource.getCatalogueSourceOrStub
import suwayomi.tachidesk.manga.model.dataclass.CheckBoxFilterDataClass
import suwayomi.tachidesk.manga.model.dataclass.FilterDataClass
import suwayomi.tachidesk.manga.model.dataclass.GroupFilterDataClass
import suwayomi.tachidesk.manga.model.dataclass.HeaderFilterDataClass
import suwayomi.tachidesk.manga.model.dataclass.PagedMangaListDataClass
import suwayomi.tachidesk.manga.model.dataclass.PagedSMangaListDataClass
import suwayomi.tachidesk.manga.model.dataclass.SelectFilterDataClass
import suwayomi.tachidesk.manga.model.dataclass.SeparatorFilterDataClass
import suwayomi.tachidesk.manga.model.dataclass.SortFilterDataClass
import suwayomi.tachidesk.manga.model.dataclass.TextFilterDataClass
import suwayomi.tachidesk.manga.model.dataclass.TriStateFilterDataClass
import suwayomi.tachidesk.manga.model.dataclass.toSMangaDataClass

object Search {
    suspend fun sourceFilter(sourceId: Long, pageNum: Int, filter: FilterData): PagedMangaListDataClass {
        val source = getCatalogueSourceOrStub(sourceId)
        val filterList0 = source.getFilterList()
        val filterList = if (filter.filter != null) updateFilterList(filterList0, filter.filter) else filterList0
        val searchManga = source.getSearchManga(pageNum, filter.searchTerm ?: "", filterList)
        return searchManga.processEntries(sourceId)
    }

    suspend fun simpleSearch(sourceId: Long, pageNum: Int, filter: FilterData): PagedSMangaListDataClass {
        val source = getCatalogueSourceOrStub(sourceId)
        val filterList0 = source.getFilterList()
        val filterList = if (filter.filter != null) updateFilterList(filterList0, filter.filter) else filterList0
        val searchManga = source.getSearchManga(pageNum, filter.searchTerm ?: "", filterList)
        return PagedSMangaListDataClass(
            searchManga.mangas.map { it.toSMangaDataClass(sourceId) },
            searchManga.hasNextPage,
        )
    }

    fun getFilterList(sourceId: Long, reset: Boolean): List<FilterDataClass> {
        val source = getCatalogueSourceOrStub(sourceId)
        return source.getFilterList().list.map { filterOf(it) }
    }

    private fun filterOf(filter: Filter<*>): FilterDataClass =
        when (filter) {
            is Filter.Header -> FilterDataClass(type = "Header", filter = HeaderFilterDataClass(name = filter.name))
            is Filter.Separator -> FilterDataClass(type = "Separator", filter = SeparatorFilterDataClass(name = filter.name))
            is Filter.Select<*> -> FilterDataClass(
                type = "Select",
                filter = SelectFilterDataClass(
                    name = filter.name,
                    state = filter.state,
                    displayValues = filter.displayValues,
                ),
            )
            is Filter.Text -> FilterDataClass(
                type = "Text",
                filter = TextFilterDataClass(name = filter.name, state = filter.state),
            )
            is Filter.CheckBox -> FilterDataClass(
                type = "CheckBox",
                filter = CheckBoxFilterDataClass(name = filter.name, state = filter.state),
            )
            is Filter.TriState -> FilterDataClass(
                type = "TriState",
                filter = TriStateFilterDataClass(name = filter.name, state = filter.state),
            )
            is Filter.Group<*> -> FilterDataClass(
                type = "Group",
                filter = GroupFilterDataClass(
                    name = filter.name,
                    state = filter.state.map { filterOf(it as Filter<*>) },
                ),
            )
            is Filter.Sort -> FilterDataClass(
                type = "Sort",
                filter = SortFilterDataClass(
                    name = filter.name,
                    state = filter.state?.let { SortFilterDataClass.SortSelection(it.index, it.ascending) },
                    values = filter.values.asList(),
                ),
            )
            else -> throw RuntimeException("Unknown filter type")
        }

    private fun updateFilterList(filterList: FilterList, changes: List<FilterChange>): FilterList {
        changes.forEach { change ->
            when (val filter = filterList[change.position]) {
                is Filter.Header -> {
                    // NOOP
                }
                is Filter.Separator -> {
                    // NOOP
                }
                is Filter.Select<*> -> filter.state = change.state.toInt()
                is Filter.Text -> filter.state = change.state
                is Filter.CheckBox -> filter.state = change.state.toBooleanStrict()
                is Filter.TriState -> filter.state = change.state.toInt()
                is Filter.Group<*> -> {
                    val groupChange = jsonMapper.fromJsonString(change.state, FilterChange::class.java)

                    when (val groupFilter = filter.state[groupChange.position]) {
                        is Filter.CheckBox -> groupFilter.state = groupChange.state.toBooleanStrict()
                        is Filter.TriState -> groupFilter.state = groupChange.state.toInt()
                        is Filter.Text -> groupFilter.state = groupChange.state
                        is Filter.Select<*> -> groupFilter.state = groupChange.state.toInt()
                    }
                }
                is Filter.Sort -> {
                    filter.state = jsonMapper.fromJsonString(change.state, Filter.Sort.Selection::class.java)
                }
            }
        }
        return filterList
    }

    private val jsonMapper by DI.global.instance<JsonMapper>()

    @Serializable
    data class FilterChange(
        val position: Int,
        val state: String,
    )

    @Serializable
    data class FilterData(
        val searchTerm: String?,
        val filter: List<FilterChange>?,
    )
}
