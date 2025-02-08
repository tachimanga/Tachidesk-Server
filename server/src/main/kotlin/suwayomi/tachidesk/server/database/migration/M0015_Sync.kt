package suwayomi.tachidesk.server.database.migration

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import de.neonew.exposed.migrations.helpers.SQLsMigration

@Suppress("ClassName", "unused")
class M0015_Sync : SQLsMigration() {
    override val sqls = listOf(
        // Category
        """ALTER TABLE Category ADD COLUMN "uuid" VARCHAR(128) NULL""",
        """ALTER TABLE Category ADD COLUMN "create_at" BIGINT NOT NULL DEFAULT 0;""",
        """ALTER TABLE Category ADD COLUMN "update_at" BIGINT NOT NULL DEFAULT 0;""",
        """ALTER TABLE Category ADD COLUMN "is_delete" BOOLEAN NOT NULL DEFAULT 0;""",
        """ALTER TABLE Category ADD COLUMN "dirty" BOOLEAN NOT NULL DEFAULT 0;""",
        """ALTER TABLE Category ADD COLUMN "commit_id" BIGINT NOT NULL DEFAULT 0;""",
        """UPDATE Category SET "uuid" = "name";""",
        """CREATE INDEX Category_idx_dirty ON Category (id, dirty);""",
        // Chapter
        """ALTER TABLE Chapter ADD COLUMN "create_at" BIGINT NOT NULL DEFAULT 0;""",
        """ALTER TABLE Chapter ADD COLUMN "update_at" BIGINT NOT NULL DEFAULT 0;""",
        """ALTER TABLE Chapter ADD COLUMN "dirty" BOOLEAN NOT NULL DEFAULT 0;""",
        """ALTER TABLE Chapter ADD COLUMN "commit_id" BIGINT NOT NULL DEFAULT 0;""",
        """CREATE INDEX Chapter_idx_dirty ON Chapter (id, dirty);""",
        // Extension
        """ALTER TABLE Extension ADD COLUMN "create_at" BIGINT NOT NULL DEFAULT 0;""",
        """ALTER TABLE Extension ADD COLUMN "update_at" BIGINT NOT NULL DEFAULT 0;""",
        """ALTER TABLE Extension ADD COLUMN "dirty" BOOLEAN NOT NULL DEFAULT 0;""",
        """ALTER TABLE Extension ADD COLUMN "commit_id" BIGINT NOT NULL DEFAULT 0;""",
        """CREATE INDEX Extension_idx_dirty ON Extension (id, dirty);""",
        // History
        """ALTER TABLE History ADD COLUMN "dirty" BOOLEAN NOT NULL DEFAULT 0;""",
        """ALTER TABLE History ADD COLUMN "commit_id" BIGINT NOT NULL DEFAULT 0;""",
        """CREATE INDEX History_idx_dirty ON History (id, dirty);""",
        // Manga
        """ALTER TABLE Manga ADD COLUMN "create_at" BIGINT NOT NULL DEFAULT 0;""",
        """ALTER TABLE Manga ADD COLUMN "update_at" BIGINT NOT NULL DEFAULT 0;""",
        """ALTER TABLE Manga ADD COLUMN "dirty" BOOLEAN NOT NULL DEFAULT 0;""",
        """ALTER TABLE Manga ADD COLUMN "commit_id" BIGINT NOT NULL DEFAULT 0;""",
        """CREATE INDEX Manga_idx_dirty ON Manga (id, dirty);""",
        // Repo
        """ALTER TABLE Repo ADD COLUMN "dirty" BOOLEAN NOT NULL DEFAULT 0;""",
        """ALTER TABLE Repo ADD COLUMN "commit_id" BIGINT NOT NULL DEFAULT 0;""",
        """CREATE INDEX Repo_idx_dirty ON Repo (id, dirty);""",
        // TrackRecord
        """ALTER TABLE TrackRecord ADD COLUMN "create_at" BIGINT NOT NULL DEFAULT 0;""",
        """ALTER TABLE TrackRecord ADD COLUMN "update_at" BIGINT NOT NULL DEFAULT 0;""",
        """ALTER TABLE TrackRecord ADD COLUMN "is_delete" BOOLEAN NOT NULL DEFAULT 0;""",
        """ALTER TABLE TrackRecord ADD COLUMN "dirty" BOOLEAN NOT NULL DEFAULT 0;""",
        """ALTER TABLE TrackRecord ADD COLUMN "commit_id" BIGINT NOT NULL DEFAULT 0;""",
        """CREATE INDEX TrackRecord_idx_dirty ON TrackRecord (id, dirty, is_delete);""",
        // SyncState
        """
        CREATE TABLE SyncState(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            create_at BIGINT DEFAULT 0 NOT NULL,
            update_at BIGINT DEFAULT 0 NOT NULL,
            is_delete BOOLEAN DEFAULT 0 NOT NULL,
            email VARCHAR(256) NOT NULL, 
            enable BOOLEAN DEFAULT 0 NOT NULL,
            max_commit_id BIGINT DEFAULT 0 NOT NULL,
            last_sync_time BIGINT DEFAULT 0 NOT NULL,
            interval INTEGER NULL,
            UNIQUE (email) ON CONFLICT REPLACE
        )
        """.trimIndent(),
        // SyncCommit
        """
        CREATE TABLE SyncCommit(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            server_id BIGINT DEFAULT 0 NOT NULL,
            create_at BIGINT DEFAULT 0 NOT NULL,
            data_type VARCHAR(16) NOT NULL, 
            data_id BIGINT DEFAULT 0 NOT NULL,
            content TEXT NOT NULL
        )
        """.trimIndent(),
        """CREATE INDEX SyncCommit_idx_data_type ON SyncCommit (data_type);""",
        // ChapterSync
        """
        CREATE TABLE ChapterSync(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            url	VARCHAR(2048) NOT NULL,
            manga_id	INT NOT NULL,
            read	BOOLEAN NOT NULL DEFAULT 0,
            bookmark	BOOLEAN NOT NULL DEFAULT 0,
            last_page_read	INT NOT NULL DEFAULT 0,
            last_read_at	BIGINT NOT NULL DEFAULT 0,
            create_at	BIGINT NOT NULL DEFAULT 0,
            update_at	BIGINT NOT NULL DEFAULT 0,
            dirty	BOOLEAN NOT NULL DEFAULT 0,
            commit_id	BIGINT NOT NULL DEFAULT 0,
            UNIQUE (manga_id, url) ON CONFLICT REPLACE
        )
        """.trimIndent(),
    )
}
