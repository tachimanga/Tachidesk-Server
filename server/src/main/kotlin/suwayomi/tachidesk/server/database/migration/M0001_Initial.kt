package suwayomi.tachidesk.server.database.migration

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import de.neonew.exposed.migrations.helpers.AddTableMigration
import org.jetbrains.exposed.sql.Table
import suwayomi.tachidesk.manga.model.table.*

/** initial migration, create all tables */
@Suppress("ClassName", "unused")
class M0001_Initial : AddTableMigration() {

    override val tables: Array<Table>
        get() {
            return arrayOf(
                CategoryMangaTable,
                CategoryMetaTable,
                CategoryTable,
                ChapterMetaTable,
                ChapterTable,
                ExtensionTable,
                MangaMetaTable,
                MangaTable,
                PageTable,
                SourceTable
            )
        }
}
