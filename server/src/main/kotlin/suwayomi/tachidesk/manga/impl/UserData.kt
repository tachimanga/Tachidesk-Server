package suwayomi.tachidesk.manga.impl

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import mu.KotlinLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.statements.api.ExposedConnection
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.transactions.transaction
import suwayomi.tachidesk.manga.impl.util.LogEvent
import suwayomi.tachidesk.manga.model.table.PageTable
import suwayomi.tachidesk.server.database.DBManager

object UserData {
    private val logger = KotlinLogging.logger {}

    fun cleanDb() {
        try {
            doCleanPage()
        } catch (e: Throwable) {
            LogEvent.log("DATA:CLEAN:PAGE:FAIL", mapOf("error" to (e.message ?: "")))
            logger.error(e) { "doCleanPage error" }
        }
    }

    fun vacuum() {
        try {
            doVacuum()
        } catch (e: Throwable) {
            LogEvent.log("DATA:CLEAN:VACUUM:FAIL", mapOf("error" to (e.message ?: "")))
            logger.error(e) { "vacuum error" }
        }
    }

    private fun doCleanPage() {
        val t0 = System.currentTimeMillis()
        transaction {
            val lastPage = PageTable.slice(PageTable.id).selectAll().orderBy(PageTable.id, SortOrder.DESC)
                .limit(1, 3000)
                .firstOrNull()
            if (lastPage != null) {
                PageTable.deleteWhere { PageTable.id less lastPage[PageTable.id] }
            }
        }
        logger.info { "doCleanPage cost=${System.currentTimeMillis() - t0}ms" }
    }

    private fun doVacuum() {
        val t0 = System.currentTimeMillis()
        var conn: ExposedConnection<*>? = null
        var stmt: PreparedStatementApi? = null
        try {
            conn = DBManager.db.connector()
            stmt = conn.prepareStatement("VACUUM;", false)
            stmt.executeUpdate()
        } finally {
            stmt?.closeIfPossible()
            conn?.close()
        }
        logger.info { "doVacuum cost=${System.currentTimeMillis() - t0}ms" }
    }
}
