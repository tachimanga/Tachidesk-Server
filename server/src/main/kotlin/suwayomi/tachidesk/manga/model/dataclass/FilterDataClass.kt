package suwayomi.tachidesk.manga.model.dataclass

/*
 * Copyright (C) 2026 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.serialization.Serializable

@Serializable
data class FilterDataClass(
    val type: String,
    val filter: FilterObject,
)

@Serializable
sealed class FilterObject

@Serializable
data class HeaderFilterDataClass(
    val name: String,
) : FilterObject()

@Serializable
data class SeparatorFilterDataClass(
    val name: String,
) : FilterObject()

@Serializable
data class SelectFilterDataClass(
    val name: String,
    val state: Int,
    val displayValues: List<String>,
) : FilterObject()

@Serializable
data class TextFilterDataClass(
    val name: String,
    val state: String,
) : FilterObject()

@Serializable
data class CheckBoxFilterDataClass(
    val name: String,
    val state: Boolean,
) : FilterObject()

@Serializable
data class TriStateFilterDataClass(
    val name: String,
    val state: Int,
) : FilterObject()

@Serializable
data class SortFilterDataClass(
    val name: String,
    val state: SortSelection?,
    val values: List<String>,
) : FilterObject() {
    @Serializable
    data class SortSelection(
        val index: Int,
        val ascending: Boolean,
    )
}

@Serializable
data class GroupFilterDataClass(
    val name: String,
    val state: List<FilterDataClass>,
) : FilterObject()
