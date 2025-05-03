package suwayomi.tachidesk.manga.impl

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.manga.impl.util.lang.isEmpty
import suwayomi.tachidesk.manga.model.table.*

object MangaMeta {
    private val logger = KotlinLogging.logger {}
    private val json by DI.global.instance<Json>()

    fun batchQueryMangaScanlator(mangaIds: List<Int>): List<Pair<Int, MangaScanlatorMeta>> {
        val metaList = transaction {
            MangaMetaTable.slice(MangaMetaTable.value, MangaMetaTable.ref)
                .select { (MangaMetaTable.ref inList mangaIds) and (MangaMetaTable.key eq "flutter_scanlator") }
                .toList()
        }
        if (metaList.isEmpty()) {
            return emptyList()
        }
        val list = metaList.mapNotNull {
            val mangaId = it[MangaMetaTable.ref].value
            val value = it[MangaMetaTable.value]
            val meta = extractScanlatorMeta(value)
            val type = ScanlatorFilterType.valueOf(meta?.type)
            if (meta != null && type == ScanlatorFilterType.Filter && meta.list?.isNotEmpty() == true) {
                mangaId to meta
            } else if (meta != null && type == ScanlatorFilterType.Priority) {
                mangaId to meta
            } else {
                null
            }
        }
        return list
    }

    private fun extractScanlatorMeta(value: String): MangaScanlatorMeta? {
        if (value.isEmpty()) {
            return null
        }
        if (value.startsWith("{")) {
            return try {
                json.decodeFromString<MangaScanlatorMeta>(value)
            } catch (e: Exception) {
                logger.error(e) { "decode MangaScanlatorMeta error, value:$value" }
                null
            }
        }
        return MangaScanlatorMeta(list = listOf(value))
    }

    @Serializable
    data class MangaScanlatorMeta(
        // ScanlatorFilterType
        val type: Int? = null,
        val list: List<String>? = null,
        val priority: List<String>? = null,
    )

    enum class ScanlatorFilterType(val type: Int) {
        Filter(0),
        Priority(1),
        ;

        companion object {
            fun valueOf(type: Int?): ScanlatorFilterType? {
                if (type == null || type == Filter.type) {
                    return Filter
                } else if (type == Priority.type) {
                    return Priority
                }
                return null
            }
        }
    }
}
