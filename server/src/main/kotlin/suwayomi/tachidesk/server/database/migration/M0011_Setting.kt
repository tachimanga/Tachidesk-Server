package suwayomi.tachidesk.server.database.migration

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import de.neonew.exposed.migrations.helpers.SQLMigration

@Suppress("ClassName", "unused")
class M0011_Setting : SQLMigration() {
    override val sql = """
        CREATE TABLE Setting(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            create_at BIGINT DEFAULT 0 NOT NULL,
            update_at BIGINT DEFAULT 0 NOT NULL,
            is_delete BOOLEAN DEFAULT 0 NOT NULL,
            key VARCHAR(256) NOT NULL, 
            value VARCHAR(2048) NOT NULL, 
            UNIQUE (key) ON CONFLICT REPLACE
        )
    """.trimIndent()
}
