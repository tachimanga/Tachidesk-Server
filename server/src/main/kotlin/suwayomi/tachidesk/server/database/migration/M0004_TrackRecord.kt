package suwayomi.tachidesk.server.database.migration

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import de.neonew.exposed.migrations.helpers.SQLMigration

@Suppress("ClassName", "unused")
class M0004_TrackRecord : SQLMigration() {
    override val sql = """
        CREATE TABLE TrackRecord(
            id INTEGER NOT NULL PRIMARY KEY,
            manga_id INTEGER NOT NULL,
            sync_id INTEGER NOT NULL,
            remote_id INTEGER NOT NULL,
            library_id INTEGER,
            title TEXT NOT NULL,
            last_chapter_read REAL NOT NULL,
            total_chapters INTEGER NOT NULL,
            status INTEGER NOT NULL,
            score REAL NOT NULL,
            remote_url TEXT NOT NULL,
            start_date INTEGER NOT NULL,
            finish_date INTEGER NOT NULL,
            UNIQUE (manga_id, sync_id) ON CONFLICT REPLACE
        )
    """.trimIndent()
}
