package suwayomi.tachidesk.manga.impl.extension

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.source.local.LocalSource
import mu.KotlinLogging
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.tachiyomi.Profiler
import suwayomi.tachidesk.manga.impl.extension.Extension.getExtensionIconUrl
import suwayomi.tachidesk.manga.impl.extension.github.ExtensionGithubApi
import suwayomi.tachidesk.manga.impl.extension.github.OnlineExtension
import suwayomi.tachidesk.manga.model.dataclass.ExtensionDataClass
import suwayomi.tachidesk.manga.model.table.ExtensionTable
import suwayomi.tachidesk.manga.model.table.RepoTable
import suwayomi.tachidesk.manga.model.table.toDataClass
import suwayomi.tachidesk.server.database.MyBatchInsertStatement
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

object ExtensionsList {
    private val logger = KotlinLogging.logger {}

    private var lastUpdateCheck: Long = 0
    val updateMap = ConcurrentHashMap<Int, OnlineExtension>()
    var cachedOnlineExtensionList: List<OnlineExtension> = listOf()

    suspend fun getExtensionList(): List<ExtensionDataClass> {
        // update if 60 seconds has passed or requested offline and database is empty
        if (lastUpdateCheck + 60.seconds.inWholeMilliseconds < System.currentTimeMillis()) {
            logger.debug("Getting extensions list from the internet")
            lastUpdateCheck = System.currentTimeMillis()

            fetchAndUpdateAllExtensions()
        } else {
            logger.debug("used cached extension list")
        }

        return extensionTableAsDataClass()
    }

    fun resetLastUpdateCheck() {
        lastUpdateCheck = 0
    }

    private fun migrateExistRepoUrl(baseUrl: String) {
        logger.info("migrateExistRepoUrl $baseUrl")
        if (baseUrl.isEmpty()) {
            return
        }
        try {
            val httpUrl = baseUrl.toHttpUrl()
            val name = httpUrl.pathSegments.firstOrNull() ?: httpUrl.host
            transaction {
                val count = RepoTable.selectAll().count()
                logger.info("exist repo count:$count")
                if (count == 0L) {
                    val ts = System.currentTimeMillis() / 1000
                    val repoId = RepoTable.insertAndGetId {
                        it[RepoTable.name] = name
                        it[RepoTable.metaUrl] = "${baseUrl}index.min.json"
                        it[RepoTable.baseUrl] = baseUrl
                        it[RepoTable.createAt] = ts
                        it[RepoTable.updateAt] = ts
                    }
                    ExtensionTable.update({ ExtensionTable.repoId eq 0 }) {
                        it[ExtensionTable.repoId] = repoId.value
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("migrateExistRepoUrl error", e)
        }
    }

    suspend fun fetchAndUpdateAllExtensions(): List<OnlineExtension> {
        val repoList = transaction {
            RepoTable.select { RepoTable.deleted eq false }.map {
                RepoTable.toDataClass(it)
            }
        }
        if (repoList.isEmpty()) {
            return arrayListOf()
        }
        val extensions =
            repoList.map { repo ->
                kotlin.runCatching {
                    logger.info("fetching ${repo.name}")
                    val list = ExtensionGithubApi.findExtensions(repo)
                    updateExtensionDatabase(list, repo.id)
                    list
                }.onFailure {
                    logger.warn(it) {
                        "Failed to fetch extensions for repo: $repo"
                    }
                }
            }
        if (extensions.all { it.isFailure }) {
            logger.error("all fail")
            extensions[0].getOrThrow()
        }

        val list = extensions.mapNotNull { it.getOrNull() }.flatten()
        cachedOnlineExtensionList = list
        return list
    }

    suspend fun getExtensionListForImport(defaultRepoUrl: String): Pair<List<ExtensionDataClass>, List<OnlineExtension>> {
        logger.debug("Getting extensions list from the internet")
        migrateExistRepoUrl(defaultRepoUrl)
        val foundExtensions = fetchAndUpdateAllExtensions()
        return Pair(extensionTableAsDataClass(), foundExtensions)
    }

    private fun extensionTableAsDataClass() = transaction {
        val repoMap = transaction {
            RepoTable.selectAll().map {
                RepoTable.toDataClass(it)
            }
        }.associateBy { it.id }
        ExtensionTable.selectAll().filter { it[ExtensionTable.name] != LocalSource.EXTENSION_NAME }
            .map {
                val repo = repoMap[it[ExtensionTable.repoId]]
                ExtensionDataClass(
                    it[ExtensionTable.apkName],
                    getExtensionIconUrl(it[ExtensionTable.apkName], it[ExtensionTable.iconUrl]),
                    it[ExtensionTable.name],
                    it[ExtensionTable.pkgName],
                    it[ExtensionTable.pkgFactory],
                    it[ExtensionTable.versionName],
                    it[ExtensionTable.versionCode],
                    it[ExtensionTable.lang],
                    it[ExtensionTable.isNsfw],
                    it[ExtensionTable.isInstalled],
                    it[ExtensionTable.hasUpdate],
                    it[ExtensionTable.hasReadme],
                    it[ExtensionTable.hasChangelog],
                    it[ExtensionTable.isObsolete],
                    it[ExtensionTable.id].value,
                    repo?.id ?: 0,
                    repo?.name ?: "",
                )
            }
    }

    private fun updateExtensionDatabase(foundExtensions: List<OnlineExtension>, repoId: Int) {
        transaction {
            val dbExtensionMap = ExtensionTable.select(ExtensionTable.repoId eq repoId)
                .associateBy { it[ExtensionTable.pkgName] }

            val (insertList, updateList) = foundExtensions
                .partition { dbExtensionMap[it.pkgName] == null }
            Profiler.split("ExtensionTable selectAll")
            updateList.forEach { foundExtension ->
                val extensionRecord = dbExtensionMap[foundExtension.pkgName]
                if (extensionRecord != null) {
                    if (extensionRecord[ExtensionTable.isInstalled]) {
                        var updateFlag = false
                        var obsoleteFlag = false
                        when {
                            foundExtension.versionCode > extensionRecord[ExtensionTable.versionCode] -> {
                                // there is an update
                                updateFlag = true
                                updateMap.putIfAbsent(extensionRecord[ExtensionTable.id].value, foundExtension)
                            }
                            foundExtension.versionCode < extensionRecord[ExtensionTable.versionCode] -> {
                                // somehow the user installed an invalid version
                                obsoleteFlag = true
                            }
                        }
                        val same = extensionRecord[ExtensionTable.hasReadme] == foundExtension.hasReadme &&
                            extensionRecord[ExtensionTable.hasChangelog] == foundExtension.hasChangelog &&
                            extensionRecord[ExtensionTable.iconUrl] == foundExtension.iconUrl &&
                            extensionRecord[ExtensionTable.hasUpdate] == updateFlag &&
                            extensionRecord[ExtensionTable.isObsolete] == obsoleteFlag
                        if (!same) {
                            ExtensionTable.update({ ExtensionTable.id eq extensionRecord[ExtensionTable.id] }) {
                                it[hasReadme] = foundExtension.hasReadme
                                it[hasChangelog] = foundExtension.hasChangelog
                                it[iconUrl] = foundExtension.iconUrl
                                it[hasUpdate] = updateFlag
                                it[isObsolete] = obsoleteFlag
                            }
                        }
                    } else {
                        val same = extensionRecord[ExtensionTable.name] == foundExtension.name &&
                            extensionRecord[ExtensionTable.versionName] == foundExtension.versionName &&
                            extensionRecord[ExtensionTable.versionCode] == foundExtension.versionCode &&
                            extensionRecord[ExtensionTable.lang] == foundExtension.lang &&
                            extensionRecord[ExtensionTable.isNsfw] == foundExtension.isNsfw &&
                            extensionRecord[ExtensionTable.apkName] == foundExtension.apkName &&
                            extensionRecord[ExtensionTable.hasReadme] == foundExtension.hasReadme &&
                            extensionRecord[ExtensionTable.hasChangelog] == foundExtension.hasChangelog &&
                            extensionRecord[ExtensionTable.iconUrl] == foundExtension.iconUrl
                        // println("Profiler: same " + same)
                        if (!same) {
                            // extension is not installed, so we can overwrite the data without a care
                            ExtensionTable.update({ ExtensionTable.id eq extensionRecord[ExtensionTable.id] }) {
                                it[name] = foundExtension.name
                                it[versionName] = foundExtension.versionName
                                it[versionCode] = foundExtension.versionCode
                                it[lang] = foundExtension.lang
                                it[isNsfw] = foundExtension.isNsfw
                                it[apkName] = foundExtension.apkName
                                it[hasReadme] = foundExtension.hasReadme
                                it[hasChangelog] = foundExtension.hasChangelog
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
                    my[ExtensionTable.hasReadme] = foundExtension.hasReadme
                    my[ExtensionTable.hasChangelog] = foundExtension.hasChangelog
                    my[ExtensionTable.iconUrl] = foundExtension.iconUrl
                    my[ExtensionTable.repoId] = repoId
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
                        if (!extensionRecord[ExtensionTable.isObsolete]) {
                            ExtensionTable.update({ ExtensionTable.id eq extensionRecord[ExtensionTable.id] }) {
                                it[isObsolete] = true
                            }
                        }
                    } else {
                        // is not installed, so we can remove the record without a care
                        ExtensionTable.deleteWhere { ExtensionTable.id eq extensionRecord[ExtensionTable.id] }
                    }
                }
            }
            Profiler.split("ExtensionTable clear")
        }
    }
}
