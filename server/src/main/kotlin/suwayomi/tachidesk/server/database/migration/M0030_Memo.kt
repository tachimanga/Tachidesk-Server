package suwayomi.tachidesk.server.database.migration

/*
 * Copyright (C) 2026 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import de.neonew.exposed.migrations.helpers.SQLsMigration

@Suppress("ClassName", "unused")
class M0030_Memo : SQLsMigration() {
    override val sqls = listOf(
        """ALTER TABLE Manga ADD COLUMN "memo" TEXT NOT NULL DEFAULT '{}';""",
        """ALTER TABLE Chapter ADD COLUMN "memo" TEXT NOT NULL DEFAULT '{}';""",
    )
}
