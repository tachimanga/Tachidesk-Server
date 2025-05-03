package suwayomi.tachidesk.global.impl

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import suwayomi.tachidesk.manga.model.table.SettingTable

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

object GlobalMeta {
    fun modifyMeta(key: String, value: String) {
        val now = System.currentTimeMillis()
        transaction {
            val meta = transaction {
                SettingTable.select { SettingTable.key eq key }
            }.firstOrNull()

            if (meta == null) {
                SettingTable.insert {
                    it[SettingTable.key] = key
                    it[SettingTable.value] = value
                    it[SettingTable.createAt] = now
                    it[SettingTable.updateAt] = now
                }
            } else {
                SettingTable.update({ SettingTable.key eq key }) {
                    it[SettingTable.value] = value
                    it[SettingTable.updateAt] = now
                }
            }
        }
    }

    fun getValue(key: String): String? {
        return transaction {
            SettingTable.slice(SettingTable.value)
                .select { SettingTable.key eq key }
                .map { it[SettingTable.value] }
                .firstOrNull()
        }
    }

    fun getMetaMap(): Map<String, String> {
        return transaction {
            SettingTable.selectAll()
                .associate { it[SettingTable.key] to it[SettingTable.value] }
        }
    }

    @Serializable
    data class QueryMetaInput(
        val key: String? = null,
    )

    @Serializable
    data class UpdateMetaInput(
        val key: String? = null,
        val value: String? = null,
    )
}
