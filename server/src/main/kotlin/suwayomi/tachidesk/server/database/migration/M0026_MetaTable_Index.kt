package suwayomi.tachidesk.server.database.migration

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import de.neonew.exposed.migrations.helpers.SQLsMigration

@Suppress("ClassName", "unused")
class M0026_MetaTable_Index : SQLsMigration() {
    override val sqls = listOf(
        """CREATE INDEX CategoryMeta_idx_category_ref ON CategoryMeta(category_ref);""",
        """CREATE INDEX ChapterMeta_idx_chapter_ref ON ChapterMeta(chapter_ref);""",
        """CREATE INDEX MangaMeta_idx_manga_ref ON MangaMeta(manga_ref);""",
    )
}
