package suwayomi.tachidesk.manga.impl.extension

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import org.tachiyomi.Profiler
import eu.kanade.tachiyomi.source.local.LocalSource
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import suwayomi.tachidesk.manga.impl.extension.Extension.getExtensionIconUrl
import suwayomi.tachidesk.manga.impl.extension.github.ExtensionGithubApi
import suwayomi.tachidesk.manga.impl.extension.github.OnlineExtension
import suwayomi.tachidesk.manga.model.dataclass.ExtensionDataClass
import suwayomi.tachidesk.manga.model.table.ExtensionTable
import suwayomi.tachidesk.server.database.MyBatchInsertStatement
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

object ExtensionsList {
    private val logger = KotlinLogging.logger {}

    var lastUpdateCheck: Long = 0
    var updateMap = ConcurrentHashMap<String, OnlineExtension>()

    suspend fun getExtensionList(repoUrl: String): List<ExtensionDataClass> {
        // update if 60 seconds has passed or requested offline and database is empty
        if (lastUpdateCheck + 60.seconds.inWholeMilliseconds < System.currentTimeMillis()) {
            logger.debug("Getting extensions list from the internet")
            lastUpdateCheck = System.currentTimeMillis()

            val foundExtensions = ExtensionGithubApi.findExtensions(repoUrl)
            updateExtensionDatabase(foundExtensions)

//            for (e in foundExtensions) {
//                try {
//                    Extension.installExtension(e.pkgName)
//                } catch (e: Exception) { e.printStackTrace() }
//            }
        } else {
            logger.debug("used cached extension list")
        }

        return extensionTableAsDataClass()
    }

    fun extensionTableAsDataClass() = transaction {
        ExtensionTable.selectAll().filter { it[ExtensionTable.name] != LocalSource.EXTENSION_NAME }.map {
            ExtensionDataClass(
                it[ExtensionTable.apkName],
                getExtensionIconUrl(it[ExtensionTable.apkName], it[ExtensionTable.iconUrl]),
                it[ExtensionTable.name],
                it[ExtensionTable.pkgName],
                it[ExtensionTable.versionName],
                it[ExtensionTable.versionCode],
                it[ExtensionTable.lang],
                it[ExtensionTable.isNsfw],
                it[ExtensionTable.isInstalled],
                it[ExtensionTable.hasUpdate],
                it[ExtensionTable.isObsolete]
            )
        }
    }

    private fun updateExtensionDatabase(foundExtensions: List<OnlineExtension>) {
        transaction {
            val dbExtensionMap = ExtensionTable.selectAll()
                .associateBy { it[ExtensionTable.pkgName] }

            val (insertList, updateList) = foundExtensions
                .partition { dbExtensionMap[it.pkgName] == null }
            Profiler.split("ExtensionTable selectAll")
            updateList.forEach { foundExtension ->
                // val extensionRecord = ExtensionTable.select { ExtensionTable.pkgName eq foundExtension.pkgName }.firstOrNull()
                val extensionRecord = dbExtensionMap[foundExtension.pkgName]
                if (extensionRecord != null) {
                    if (extensionRecord[ExtensionTable.isInstalled]) {
                        when {
                            foundExtension.versionCode > extensionRecord[ExtensionTable.versionCode] -> {
                                // there is an update
                                ExtensionTable.update({ ExtensionTable.pkgName eq foundExtension.pkgName }) {
                                    it[hasUpdate] = true
                                }
                                updateMap.putIfAbsent(foundExtension.pkgName, foundExtension)
                            }
                            foundExtension.versionCode < extensionRecord[ExtensionTable.versionCode] -> {
                                // somehow the user installed an invalid version
                                ExtensionTable.update({ ExtensionTable.pkgName eq foundExtension.pkgName }) {
                                    it[isObsolete] = true
                                }
                            }
                        }
                    } else {
                        val same = extensionRecord[ExtensionTable.name] == foundExtension.name &&
                            extensionRecord[ExtensionTable.versionName] == foundExtension.versionName &&
                            extensionRecord[ExtensionTable.versionCode] == foundExtension.versionCode &&
                            extensionRecord[ExtensionTable.lang] == foundExtension.lang &&
                            extensionRecord[ExtensionTable.isNsfw] == foundExtension.isNsfw &&
                            extensionRecord[ExtensionTable.apkName] == foundExtension.apkName &&
                            extensionRecord[ExtensionTable.iconUrl] == foundExtension.iconUrl
                        // println("Profiler: same " + same)
                        if (!same) {
                            // extension is not installed, so we can overwrite the data without a care
                            ExtensionTable.update({ ExtensionTable.pkgName eq foundExtension.pkgName }) {
                                it[name] = foundExtension.name
                                it[versionName] = foundExtension.versionName
                                it[versionCode] = foundExtension.versionCode
                                it[lang] = foundExtension.lang
                                it[isNsfw] = foundExtension.isNsfw
                                it[apkName] = foundExtension.apkName
                                it[iconUrl] = foundExtension.iconUrl
                            }
                        }
                    }
                }
            }
            Profiler.split("ExtensionTable upsert")
            if (insertList.isNotEmpty()) {
                val myBatchInsertStatement = MyBatchInsertStatement(ExtensionTable)
                insertList.forEach { foundExtension ->
                    val my = myBatchInsertStatement

                    my.addBatch()

                    my[ExtensionTable.name] = foundExtension.name
                    my[ExtensionTable.pkgName] = foundExtension.pkgName
                    my[ExtensionTable.versionName] = foundExtension.versionName
                    my[ExtensionTable.versionCode] = foundExtension.versionCode
                    my[ExtensionTable.lang] = foundExtension.lang
                    my[ExtensionTable.isNsfw] = foundExtension.isNsfw
                    my[ExtensionTable.apkName] = foundExtension.apkName
                    my[ExtensionTable.iconUrl] = foundExtension.iconUrl
                }

                val sql = myBatchInsertStatement.prepareSQL(this)
                val conn = (TransactionManager.current().connection as JdbcConnectionImpl).connection
                val statement = conn.createStatement()
                // println(sql)
                statement.execute(sql)
            }
            Profiler.split("ExtensionTable insert")

            // deal with obsolete extensions
            val foundExtensionsMap = foundExtensions.associateBy { it.pkgName }
            val toDeleteExtensionList = dbExtensionMap.values
                .filter { it[ExtensionTable.pkgName] != "eu.kanade.tachiyomi.source.local" }
                .filter { foundExtensionsMap[it[ExtensionTable.pkgName]] == null }
                .toList()

            if (toDeleteExtensionList.isNotEmpty()) {
                toDeleteExtensionList.forEach { extensionRecord ->
                    // not in the repo, so these extensions are obsolete
                    if (extensionRecord[ExtensionTable.isInstalled]) {
                        // is installed so we should mark it as obsolete
                        ExtensionTable.update({ ExtensionTable.pkgName eq extensionRecord[ExtensionTable.pkgName] }) {
                            it[isObsolete] = true
                        }
                    } else {
                        // is not installed, so we can remove the record without a care
                        ExtensionTable.deleteWhere { ExtensionTable.pkgName eq extensionRecord[ExtensionTable.pkgName] }
                    }
                }
            }
            Profiler.split("ExtensionTable clear")
        }
    }
}
