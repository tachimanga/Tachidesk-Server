package suwayomi.tachidesk.manga.impl

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.serialization.Serializable
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import suwayomi.tachidesk.manga.impl.extension.ExtensionsList
import suwayomi.tachidesk.manga.impl.extension.github.ExtensionGithubApi
import suwayomi.tachidesk.manga.impl.extension.github.OnlineExtension
import suwayomi.tachidesk.manga.model.dataclass.RepoDataClass
import suwayomi.tachidesk.manga.model.table.*
import java.lang.RuntimeException

object Repo {
    fun repoList(): List<RepoDataClass> {
        return transaction {
            RepoTable.select { RepoTable.deleted eq false }.map {
                RepoTable.toDataClass(it)
            }
        }
    }

    suspend fun checkRepo(repoName: String?, metaUrl: String?): List<OnlineExtension> {
        val repo = buildRepoDataClass(repoName, metaUrl)
        return ExtensionGithubApi.findExtensions(repo)
    }

    fun createRepo(repoName: String?, metaUrl: String?): RepoDataClass {
        val repo = buildRepoDataClass(repoName, metaUrl)
        transaction {
            val dbRepo = RepoTable.select { RepoTable.metaUrl eq repo.metaUrl }.firstOrNull()
            val ts = System.currentTimeMillis() / 1000
            if (dbRepo == null) {
                val repoId = RepoTable.insertAndGetId {
                    it[RepoTable.name] = repo.name
                    it[RepoTable.metaUrl] = repo.metaUrl
                    it[RepoTable.baseUrl] = repo.baseUrl
                    it[RepoTable.createAt] = ts
                    it[RepoTable.updateAt] = ts
                }
                repo.id = repoId.value
            } else {
                repo.id = dbRepo[RepoTable.id].value
                RepoTable.update({ RepoTable.id eq dbRepo[RepoTable.id].value }) {
                    it[RepoTable.name] = repo.name
                    it[RepoTable.baseUrl] = repo.baseUrl
                    it[RepoTable.deleted] = false
                    it[RepoTable.updateAt] = ts
                }
            }
        }
        ExtensionsList.resetLastUpdateCheck()
        return repo
    }

    fun updateRepoByMetaUrl(metaUrl: String?, targetMetaUrl: String?) {
        if (metaUrl == null || targetMetaUrl == null) {
            return
        }
        val update = buildRepoDataClass(null, targetMetaUrl)
        transaction {
            val dbRepo = RepoTable.select { RepoTable.metaUrl eq metaUrl }.firstOrNull()
            val targetDbRepo = RepoTable.select { RepoTable.metaUrl eq targetMetaUrl }.firstOrNull()
            val ts = System.currentTimeMillis() / 1000
            if (dbRepo != null && targetDbRepo == null) {
                RepoTable.update({ RepoTable.id eq dbRepo[RepoTable.id].value }) {
                    it[RepoTable.name] = update.name
                    it[RepoTable.metaUrl] = update.metaUrl
                    it[RepoTable.baseUrl] = update.baseUrl
                    it[RepoTable.updateAt] = ts
                }
            }
        }
        ExtensionsList.resetLastUpdateCheck()
    }

    fun removeRepo(repoId: Int) {
        transaction {
            val ts = System.currentTimeMillis() / 1000
            RepoTable.update({ RepoTable.id eq repoId }) {
                it[RepoTable.deleted] = true
                it[RepoTable.updateAt] = ts
            }
            ExtensionTable.deleteWhere { (ExtensionTable.repoId eq repoId) and (ExtensionTable.isInstalled eq false) }
            ExtensionTable.update({ (ExtensionTable.repoId eq repoId) and (ExtensionTable.isInstalled eq true) }) {
                it[isObsolete] = true
            }
        }
        ExtensionsList.resetLastUpdateCheck()
    }

    private fun buildRepoDataClass(repoName: String?, metaUrl: String?): RepoDataClass {
        if (repoName == null && metaUrl == null) {
            throw RuntimeException("repoName and metaUrl are empty")
        }
        val meta = metaUrl ?: "https://raw.githubusercontent.com/$repoName/repo/index.min.json"
        val httpUrl = meta.toHttpUrl()
        val baseUrl = if (metaUrl != null) {
            val pos = metaUrl.lastIndexOf("/")
            metaUrl.substring(0, pos + 1)
        } else {
            "https://raw.githubusercontent.com/$repoName/repo/"
        }
        val name = httpUrl.pathSegments.firstOrNull() ?: httpUrl.host
        return RepoDataClass(
            -1,
            0,
            name,
            meta,
            baseUrl
        )
    }

    @Serializable
    data class RepoCreate(
        val repoName: String? = null,
        val metaUrl: String? = null
    )

    @Serializable
    data class RepoUpdateByMetaUrl(
        val metaUrl: String? = null,
        val targetMetaUrl: String? = null
    )
}
