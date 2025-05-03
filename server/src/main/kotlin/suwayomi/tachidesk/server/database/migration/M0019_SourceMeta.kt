package suwayomi.tachidesk.server.database.migration

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import de.neonew.exposed.migrations.helpers.SQLsMigration

@Suppress("ClassName", "unused")
class M0019_SourceMeta : SQLsMigration() {
    override val sqls = listOf(
        """
        CREATE TABLE "SourceMeta" (
            "id" INTEGER PRIMARY KEY AUTOINCREMENT,
            "create_at"	BIGINT NOT NULL DEFAULT 0,
            "update_at"	BIGINT NOT NULL DEFAULT 0,
            "is_delete"	BOOLEAN NOT NULL DEFAULT 0,
            "dirty"	BOOLEAN NOT NULL DEFAULT 0,
            "commit_id"	BIGINT NOT NULL DEFAULT 0,
            "source_id"	BIGINT NOT NULL,
            "key"	VARCHAR(256) NOT NULL,
            "value"	VARCHAR(4096) NOT NULL,
            UNIQUE (source_id, key) ON CONFLICT REPLACE
        )
        """.trimIndent(),
        """CREATE INDEX SourceMeta_idx_dirty ON SourceMeta (id, dirty);""",
    )
}
