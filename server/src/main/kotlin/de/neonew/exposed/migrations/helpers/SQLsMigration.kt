package de.neonew.exposed.migrations.helpers

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import de.neonew.exposed.migrations.Migration
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.vendors.currentDialect

abstract class SQLsMigration : Migration() {
    abstract val sqls: List<String>
    private val logger = KotlinLogging.logger {}
    override fun run() {
        val t = System.currentTimeMillis()
        with(TransactionManager.current()) {
            for (statement in sqls) {
                exec(statement)
            }
            commit()
            currentDialect.resetCaches()
        }
        logger.info { "SQLsMigration ${this::class.simpleName} cost:${System.currentTimeMillis() - t}ms" }
    }
}
