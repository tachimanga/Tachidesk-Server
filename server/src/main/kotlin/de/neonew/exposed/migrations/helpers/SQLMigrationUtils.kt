package de.neonew.exposed.migrations.helpers

/*
 * Copyright (C) 2025 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

object SQLMigrationUtils {
    fun isIgnorableException(e: Exception): Boolean {
        // org.jetbrains.exposed.exceptions.ExposedSQLException: org.sqlite.SQLiteException: [SQLITE_ERROR] SQL error or missing database (table Setting already exists)
        // org.jetbrains.exposed.exceptions.ExposedSQLException: org.sqlite.SQLiteException: [SQLITE_ERROR] SQL error or missing database (index Chapter_idx_manga already exists)
        // org.jetbrains.exposed.exceptions.ExposedSQLException: org.sqlite.SQLiteException: [SQLITE_ERROR] SQL error or missing database (duplicate column name: original_chapter_id)
        return e.message?.contains(" already exists)") == true ||
            e.message?.contains("(duplicate column name: ") == true
    }
}
