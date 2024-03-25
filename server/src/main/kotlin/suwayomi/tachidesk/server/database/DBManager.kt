package suwayomi.tachidesk.server.database

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import de.neonew.exposed.migrations.loadMigrationsFrom
import de.neonew.exposed.migrations.runMigrations
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.manga.impl.History
import suwayomi.tachidesk.server.ApplicationDirs
import suwayomi.tachidesk.server.ServerConfig

object DBManager {

    val db by lazy {
        val applicationDirs by DI.global.instance<ApplicationDirs>()
//        Database.connect(
//            "jdbc:h2:${applicationDirs.dataRoot}/database",
//            "org.h2.Driver",
//            databaseConfig = DatabaseConfig {
//                useNestedTransactions = true
//                sqlLogger = StdOutSqlLogger
//            }
//        )
        Database.connect(
            "jdbc:sqlite:${applicationDirs.dataRoot}/sqlite8.db?transaction_mode=IMMEDIATE",
            "org.sqlite.JDBC",
            databaseConfig = DatabaseConfig {
                useNestedTransactions = false
                sqlLogger = StdOutSqlLogger
                // warnLongQueriesDuration = 1 //<logger name="Exposed" level="INFO"/>
            }
        )
    }
}

private val logger = KotlinLogging.logger {}

fun databaseUp(db: Database = DBManager.db) {
    // call db to initialize the lazy object
    logger.info {
        "Using ${db.vendor} database version ${db.version}"
    }

    val migrations = loadMigrationsFrom("suwayomi.tachidesk.server.database.migration", ServerConfig::class.java)
    // val migrations = arrayListOf<Migration>(M0001_Initial())
    runMigrations(migrations)

    // migrate history
    try {
        History.migrateHistoryIfNeeded()
    } catch (e: Throwable) {
        logger.error(e) { "migrateHistoryIfNeeded error" }
    }
}
