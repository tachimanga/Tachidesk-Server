package suwayomi.tachidesk.manga.impl.extension.github

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.network.await
import eu.kanade.tachiyomi.network.parseAs
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import suwayomi.tachidesk.manga.impl.util.PackageTools.LIB_VERSION_MAX
import suwayomi.tachidesk.manga.impl.util.PackageTools.LIB_VERSION_MIN
import suwayomi.tachidesk.manga.model.dataclass.RepoDataClass
import uy.kohesive.injekt.injectLazy
import java.util.concurrent.ConcurrentHashMap

object ExtensionGithubApi {
    private val requiresFallbackSourceMap = ConcurrentHashMap<Int, Boolean>()
    private val logger = KotlinLogging.logger {}
    private val GITHUB_REGEX = Regex("https://raw\\.githubusercontent\\.com/(.*?)/(.*?)/(.*)")

    @Serializable
    private data class ExtensionJsonObject(
        val name: String,
        val pkg: String,
        val apk: String,
        val lang: String,
        val code: Int,
        val version: String,
        val nsfw: Int,
        val hasReadme: Int = 0,
        val hasChangelog: Int = 0,
        val sources: List<ExtensionSourceJsonObject>?,
    )

    @Serializable
    private data class ExtensionSourceJsonObject(
        val name: String,
        val lang: String,
        val id: Long,
        val baseUrl: String,
    )

    suspend fun findExtensions(repo: RepoDataClass): List<OnlineExtension> {
        val fallback = requiresFallbackSourceMap[repo.id]
        val githubResponse = if (fallback == true) {
            null
        } else {
            try {
                client.newCall(GET(repo.metaUrl)).await()
            } catch (e: Throwable) {
                logger.error(e) { "Failed to get extensions from GitHub, repo:$repo" }
                requiresFallbackSourceMap[repo.id] = true
                null
            }
        }
        val response = githubResponse ?: run {
            client.newCall(GET(toJsDeliverUrl(repo.metaUrl))).await()
        }
        return response
            .parseAs<List<ExtensionJsonObject>>()
            .toExtensions(repo)
    }

    fun getApkUrl(repo: RepoDataClass, apkName: String): String {
        return "${toJsDeliverUrlIfNeeded(repo, repo.baseUrl)}apk/$apkName"
    }

    private val client by lazy {
        val network: NetworkHelper by injectLazy()
        network.client.newBuilder()
            .addNetworkInterceptor { chain ->
                val originalResponse = chain.proceed(chain.request())
                originalResponse.newBuilder()
                    .header("Content-Type", "application/json")
                    .build()
            }
            .build()
    }

    private fun List<ExtensionJsonObject>.toExtensions(repo: RepoDataClass): List<OnlineExtension> {
        val baseUrl = toJsDeliverUrlIfNeeded(repo, repo.baseUrl)
        return this
            .filter {
                val libVersion = it.version.substringBeforeLast('.').toDouble()
                libVersion in LIB_VERSION_MIN..LIB_VERSION_MAX
            }
            .map {
                OnlineExtension(
                    name = it.name.substringAfter("Tachiyomi: "),
                    pkgName = it.pkg,
                    versionName = it.version,
                    versionCode = it.code,
                    lang = it.lang,
                    isNsfw = it.nsfw == 1,
                    hasReadme = it.hasReadme == 1,
                    hasChangelog = it.hasChangelog == 1,
                    sources = it.sources?.toExtensionSources() ?: emptyList(),
                    apkName = it.apk,
                    iconUrl = "${baseUrl}icon/${it.pkg}.png",
                    repoId = repo.id,
                    repoName = repo.name,
                )
            }
    }

    private fun List<ExtensionSourceJsonObject>.toExtensionSources(): List<OnlineExtensionSource> {
        return this.map {
            OnlineExtensionSource(
                name = it.name,
                lang = it.lang,
                id = it.id,
                baseUrl = it.baseUrl,
            )
        }
    }

    private fun toJsDeliverUrlIfNeeded(repo: RepoDataClass, url: String): String {
        val fallback = requiresFallbackSourceMap[repo.id]
        return if (fallback == true) {
            toJsDeliverUrl(url)
        } else {
            url
        }
    }

    private fun toJsDeliverUrl(url: String): String {
        if (GITHUB_REGEX.matches(url)) {
            return GITHUB_REGEX.replace(url, "https://gcore.jsdelivr.net/gh/$1/$2@$3")
        }
        return url
    }
}
