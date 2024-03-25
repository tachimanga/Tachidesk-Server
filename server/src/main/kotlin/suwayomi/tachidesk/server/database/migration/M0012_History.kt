package suwayomi.tachidesk.server.database.migration

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import de.neonew.exposed.migrations.helpers.SQLMigration

@Suppress("ClassName", "unused")
class M0012_History : SQLMigration() {
    override val sql = """
        CREATE TABLE History(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            create_at BIGINT DEFAULT 0 NOT NULL,
            update_at BIGINT DEFAULT 0 NOT NULL,
            is_delete BOOLEAN DEFAULT 0 NOT NULL,
            manga_id INTEGER NOT NULL,
            last_chapter_id INTEGER NOT NULL,
            last_read_at BIGINT DEFAULT 0 NOT NULL,
            UNIQUE (manga_id) ON CONFLICT REPLACE
        )
    """.trimIndent()
}
