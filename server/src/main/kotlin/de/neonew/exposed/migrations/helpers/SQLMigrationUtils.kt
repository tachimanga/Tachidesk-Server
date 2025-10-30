package de.neonew.exposed.migrations.helpers

object SQLMigrationUtils {
    fun isIgnorableException(e: Exception): Boolean {
        // org.jetbrains.exposed.exceptions.ExposedSQLException: org.sqlite.SQLiteException: [SQLITE_ERROR] SQL error or missing database (table Setting already exists)
        // org.jetbrains.exposed.exceptions.ExposedSQLException: org.sqlite.SQLiteException: [SQLITE_ERROR] SQL error or missing database (index Chapter_idx_manga already exists)
        // org.jetbrains.exposed.exceptions.ExposedSQLException: org.sqlite.SQLiteException: [SQLITE_ERROR] SQL error or missing database (duplicate column name: original_chapter_id)
        return e.message?.contains(" already exists)") == true ||
            e.message?.contains("(duplicate column name: ") == true
    }
}
