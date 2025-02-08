package suwayomi.tachidesk.server.database.migration

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import de.neonew.exposed.migrations.helpers.AddTableMigration
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import suwayomi.tachidesk.manga.model.table.RepoTable.default
import suwayomi.tachidesk.manga.model.table.RepoTable.nullable

@Suppress("ClassName", "unused")
class M0010_Repo : AddTableMigration() {

    private class RepoTable : IntIdTable() {
        init {
            integer("type").default(0)
            varchar("name", 1024)
            varchar("meta_url", 2048)
            varchar("base_url", 2048)
            varchar("homepage", 2048).nullable()
            bool("deleted").default(false)
            long("create_at").default(0)
            long("update_at").default(0)
        }
    }

    override val tables: Array<Table>
        get() {
            val repoTable = RepoTable()
            return arrayOf(
                repoTable,
            )
        }
}
