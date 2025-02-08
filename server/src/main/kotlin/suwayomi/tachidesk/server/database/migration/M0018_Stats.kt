package suwayomi.tachidesk.server.database.migration

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import de.neonew.exposed.migrations.helpers.SQLsMigration

@Suppress("ClassName", "unused")
class M0018_Stats : SQLsMigration() {
    override val sqls = listOf(
        """
        CREATE TABLE "Stats" (
            "id" INTEGER PRIMARY KEY AUTOINCREMENT,
            "create_at"	BIGINT NOT NULL DEFAULT 0,
            "update_at"	BIGINT NOT NULL DEFAULT 0,
            "is_delete"	BOOLEAN NOT NULL DEFAULT 0,
            "dirty"	BOOLEAN NOT NULL DEFAULT 0,
            "commit_id"	BIGINT NOT NULL DEFAULT 0,
            "day"	INTEGER NOT NULL DEFAULT 0,
            "manga_id"	INTEGER NOT NULL,
            "read_duration"	INT NOT NULL DEFAULT 0
        )
        """.trimIndent(),
        """CREATE INDEX Stats_idx_dirty ON Stats (id, dirty);""",
        """CREATE INDEX Stats_idx_manga_id ON Stats (manga_id);""",
        """CREATE INDEX Stats_idx_day ON Stats (day);""",
    )
}
