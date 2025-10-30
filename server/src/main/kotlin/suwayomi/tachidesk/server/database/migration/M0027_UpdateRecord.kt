package suwayomi.tachidesk.server.database.migration

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import de.neonew.exposed.migrations.helpers.SQLsMigration

@Suppress("ClassName", "unused")
class M0027_UpdateRecord : SQLsMigration() {
    override val sqls = listOf(
        """
        CREATE TABLE "UpdateRecord" (
            "id" INTEGER PRIMARY KEY AUTOINCREMENT,
            "create_at"	BIGINT NOT NULL DEFAULT 0,
            "update_at"	BIGINT NOT NULL DEFAULT 0,
            "finish_at"	BIGINT NOT NULL DEFAULT 0,
            "type"	    INT NOT NULL DEFAULT 0,
            "status"	INT NOT NULL DEFAULT 0,
            "err_code"	VARCHAR(32),
            "err_msg"	VARCHAR(256),
            "total_count"	INT NOT NULL DEFAULT 0,
            "succ_count"	INT NOT NULL DEFAULT 0,
            "failed_count"	INT NOT NULL DEFAULT 0,
            "skip_count"	INT NOT NULL DEFAULT 0,
            "new_chapter_count"	INT NOT NULL DEFAULT 0
        )
        """.trimIndent(),
    )
}
