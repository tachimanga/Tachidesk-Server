package suwayomi.tachidesk.manga.impl

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.getPreferenceKey
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.source.sourceSupportDirect
import io.javalin.plugin.json.JsonMapper
import mu.KotlinLogging
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.manga.impl.extension.Extension.getExtensionIconUrl
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource.getCatalogueSourceOrNull
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource.getCatalogueSourceOrStub
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource.unregisterCatalogueSource
import suwayomi.tachidesk.manga.model.dataclass.SourceDataClass
import suwayomi.tachidesk.manga.model.table.ExtensionTable
import suwayomi.tachidesk.manga.model.table.SourceTable
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import xyz.nulldev.androidcompat.androidimpl.CustomContext

object Source {
    private val logger = KotlinLogging.logger {}

    fun getSourceList(): List<SourceDataClass> {
        val dbExtensionMap = transaction {
            ExtensionTable.selectAll()
                .associateBy { it[ExtensionTable.id] }
        }
        val sourceList = transaction {
            SourceTable.selectAll().toList()
        }
        var limit = 0
        return sourceList.mapNotNull {
            val catalogueSource = getCatalogueSourceOrNull(it[SourceTable.id].value) ?: return@mapNotNull null
            // val sourceExtension = ExtensionTable.select { ExtensionTable.id eq it[SourceTable.extension] }.first()
            val sourceExtension = dbExtensionMap[it[SourceTable.extension]]
            if (it[SourceTable.isDirect] == null && limit++ < 10) {
                it[SourceTable.isDirect] = fillSourceDirectFlag(catalogueSource)
            }
            val direct = it[SourceTable.isDirect] == true
            SourceDataClass(
                it[SourceTable.id].value.toString(),
                it[SourceTable.name],
                it[SourceTable.lang],
                if (sourceExtension != null) getExtensionIconUrl(sourceExtension[ExtensionTable.apkName], sourceExtension[ExtensionTable.iconUrl]) else "",
                if (catalogueSource is HttpSource) catalogueSource.baseUrl else null,
                if (sourceExtension != null) sourceExtension[ExtensionTable.pkgName] else "",
                catalogueSource.supportsLatest,
                catalogueSource is ConfigurableSource,
                it[SourceTable.isNsfw],
                catalogueSource.toString() + if (direct) { " ⚡" } else { "" }
            )
        }
    }

    private fun fillSourceDirectFlag(source: CatalogueSource): Boolean {
        println("fillSourceDirectFlag: id=${source.id}, name=${source.name}")
        val direct = sourceSupportDirect(GetCatalogueSource.getCatalogueSourceMeta(source))
        transaction {
            SourceTable.update({ SourceTable.id eq source.id }) {
                it[SourceTable.isDirect] = direct
            }
        }
        return direct
    }

    fun getSource(sourceId: Long): SourceDataClass? { // all the data extracted fresh form the source instance
        return transaction {
            val source = SourceTable.select { SourceTable.id eq sourceId }.firstOrNull() ?: return@transaction null
            val catalogueSource = getCatalogueSourceOrNull(sourceId) ?: return@transaction null
            val extension = ExtensionTable.select { ExtensionTable.id eq source[SourceTable.extension] }.first()
            val direct = source[SourceTable.isDirect] == true
            SourceDataClass(
                sourceId.toString(),
                source[SourceTable.name],
                source[SourceTable.lang],
                getExtensionIconUrl(extension[ExtensionTable.apkName], extension[ExtensionTable.iconUrl]),
                if (catalogueSource is HttpSource) catalogueSource.baseUrl else null,
                extension[ExtensionTable.pkgName],
                catalogueSource.supportsLatest,
                catalogueSource is ConfigurableSource,
                source[SourceTable.isNsfw],
                catalogueSource.toString() + if (direct) { " ⚡" } else { "" }
            )
        }
    }

    private val context by DI.global.instance<CustomContext>()

    /**
     * (2021-11) Clients should support these types for extensions to work properly
     * - EditTextPreference
     * - SwitchPreferenceCompat
     * - ListPreference
     * - CheckBoxPreference
     * - MultiSelectListPreference
     */
    data class PreferenceObject(
        val type: String,
        val props: Any
    )

    var preferenceScreenMap: MutableMap<Long, PreferenceScreen> = mutableMapOf()

    /**
     *  Gets a source's PreferenceScreen, puts the result into [preferenceScreenMap]
     */
    fun getSourcePreferences(sourceId: Long): List<PreferenceObject> {
        val source = getCatalogueSourceOrStub(sourceId)

        if (source is ConfigurableSource) {
            val sourceShardPreferences =
                Injekt.get<Application>().getSharedPreferences(source.getPreferenceKey(), Context.MODE_PRIVATE)

            val screen = PreferenceScreen(context)
            screen.sharedPreferences = sourceShardPreferences

            source.setupPreferenceScreen(screen)

            preferenceScreenMap[sourceId] = screen

            return screen.preferences.map {
                PreferenceObject(it::class.java.simpleName, it)
            }
        }
        return emptyList()
    }

    data class SourcePreferenceChange(
        val position: Int,
        val value: String
    )

    private val jsonMapper by DI.global.instance<JsonMapper>()

    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    fun setSourcePreference(sourceId: Long, change: SourcePreferenceChange) {
        val screen = preferenceScreenMap[sourceId]!!
        val pref = screen.preferences[change.position]

        println(jsonMapper::class.java.name)
        val newValue = when (pref.defaultValueType) {
            "String" -> change.value
            "Boolean" -> change.value.toBoolean()
            "Set<String>" -> jsonMapper.fromJsonString(change.value, List::class.java as Class<List<String>>).toSet()
            else -> throw RuntimeException("Unsupported type conversion")
        }

        pref.saveNewValue(newValue)
        pref.callChangeListener(newValue)

        // must reload the source because a preference was changed
        unregisterCatalogueSource(sourceId)
    }
}
