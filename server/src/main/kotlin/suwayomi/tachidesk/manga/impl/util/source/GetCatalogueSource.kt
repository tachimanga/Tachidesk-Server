package suwayomi.tachidesk.manga.impl.util.source

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory
import eu.kanade.tachiyomi.source.SourceMeta
import eu.kanade.tachiyomi.source.local.LocalSource
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.online.HttpSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mu.KotlinLogging
import okhttp3.Authenticator
import okhttp3.Headers
import okhttp3.OkHttpClient
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import suwayomi.tachidesk.manga.impl.util.PackageTools.loadExtensionSources
import suwayomi.tachidesk.manga.model.table.ExtensionTable
import suwayomi.tachidesk.manga.model.table.SourceTable
import suwayomi.tachidesk.server.ApplicationDirs
import uy.kohesive.injekt.injectLazy
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

object GetCatalogueSource {
    private val logger = KotlinLogging.logger {}
    private val sourceCache = ConcurrentHashMap<Long, CatalogueSource>()
    private val metaCache = ConcurrentHashMap<Long, SourceMeta>()
    private val clientToSourceMap = ConcurrentHashMap<OkHttpClient, Long>()
    private val sourceRandomUaMap = ConcurrentHashMap<Long, Boolean>()
    private val sourceLatestUserAgentMap = ConcurrentHashMap<Long, String>()
    private val applicationDirs by DI.global.instance<ApplicationDirs>()
    private val network: NetworkHelper by injectLazy()
    private val WHITE_LIST = listOf(
        "RandomUserAgentInterceptor",
        "UserAgentInterceptor",
        "HttpLoggingInterceptor",
        "CloudflareInterceptor",
        "RateLimitInterceptor",
        "SpecificHostRateLimitInterceptor",
        "EnableNativeNetInterceptor",
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun getCatalogueSource(sourceId: Long): CatalogueSource? {
        val cachedResult: CatalogueSource? = sourceCache[sourceId]
        if (cachedResult != null) {
            return cachedResult
        }

        val sourceRecord = transaction {
            SourceTable.select { SourceTable.id eq sourceId }.firstOrNull()
        } ?: return null

        val extensionId = sourceRecord[SourceTable.extension]
        val extensionRecord = transaction {
            ExtensionTable.select { ExtensionTable.id eq extensionId }.first()
        }

        val apkName = extensionRecord[ExtensionTable.apkName]
        val className = extensionRecord[ExtensionTable.classFQName]
        val jarName = apkName.substringBefore(".apk") + ".jar"
        val jarPath = "${applicationDirs.extensionsRoot}/$jarName"

        when (val instance = loadExtensionSources(jarPath, className)) {
            is Source -> listOf(instance)
            is SourceFactory -> instance.createSources()
            else -> throw Exception("Unknown source class type! ${instance.javaClass}")
        }.forEach {
            if (it is HttpSource) {
                scope.launch {
                    clientToSourceMap[it.client] = it.id
                }
            }
            sourceCache[it.id] = it as HttpSource
        }
        return sourceCache[sourceId]!!
    }

    fun getCatalogueSourceOrNull(sourceId: Long): CatalogueSource? {
        return runCatching { getCatalogueSource(sourceId) }.getOrNull()
    }

    fun getCatalogueSourceOrStub(sourceId: Long): CatalogueSource {
        return getCatalogueSourceOrNull(sourceId) ?: StubSource(sourceId)
    }

    fun getCatalogueSourceMeta(source: CatalogueSource): SourceMeta {
        val cachedMeta = metaCache[source.id]
        if (cachedMeta != null) {
            return cachedMeta
        }

        val meta = SourceMeta()
        metaCache[source.id] = meta

        try {
            val s = System.currentTimeMillis()
            getCatalogueSourceMeta0(source, meta)
            println("Profiler: getCatalogueSourceMeta0 cost:" + (System.currentTimeMillis() - s) + "ms")
        } catch (e: Exception) {
            logger.error { "getCatalogueSourceMeta0 error $e" }
        }

        return meta
    }

    fun getCatalogueSourceMeta0(source: CatalogueSource, meta: SourceMeta) {
        val httpSource = source as? HttpSource ?: return

        val client = httpSource.client
        // val sClient = client == network.client || client == network.cloudflareClient
        // if (sClient) {
        if (false) {
            meta.simpleClient = true
            println(
                "SourceMeta:" + source.name +
                    ", simpleClient:" + meta.simpleClient,
            )
        } else {
            val sCookie = client.cookieJar == network.cookieJar
            val sRedirects = client.followRedirects
            val sAuth = client.authenticator == Authenticator.NONE
            var sInterceptors = true
            for (interceptor in client.interceptors) {
                if (!WHITE_LIST.contains(interceptor.javaClass.simpleName)) {
                    sInterceptors = false
                    break
                }
            }
            if (client.networkInterceptors.isNotEmpty()) {
                sInterceptors = false
            }
            if (sCookie && sRedirects && sInterceptors && sAuth) {
                meta.simpleClient = true
            }
            println(
                "SourceMeta:" + source.name +
                    ", sCookie:" + sCookie +
                    ", sRedirects:" + sRedirects +
                    ", sInterceptors:" + sInterceptors +
                    ", sAuth:" + sAuth,
            )
        }

        if (!meta.simpleClient) {
            return
        }

        // fun imageRequest
        try {
            val method = source.javaClass.getDeclaredMethod("imageRequest", Page::class.java)
        } catch (ignored: NoSuchMethodException) {
            meta.simpleRequest = true
        }
        println(
            "SourceMeta:" + source.name +
                ", simpleRequest:" + meta.simpleRequest,
        )

        if (!meta.simpleRequest) {
            return
        }

        // headers
        val headers = source.headers
        val map = HashMap<String, String>()
        map["User-Agent"] = HttpSource.DEFAULT_USER_AGENT
        for (i in 0 until headers.size) {
            map[headers.name(i)] = headers.value(i)
        }
        if ((!isDesktopUa(headers) && getSourceRandomUa(source.id)) || isAndroidMobileUa(headers)) {
            map["User-Agent"] = HttpSource.DEFAULT_USER_AGENT
        }
        meta.headers = map

        println(
            "SourceMeta:" + source.name +
                ", headers:" + meta.headers,
        )
    }

    fun registerCatalogueSource(sourcePair: Pair<Long, CatalogueSource>) {
        sourceCache += sourcePair
    }

    fun unregisterCatalogueSource(sourceId: Long) {
        sourceCache.remove(sourceId)
    }

    fun unregisterCatalogueSourceExt(sourceId: Long) {
        sourceRandomUaMap.remove(sourceId)
        metaCache.remove(sourceId)
    }

    fun unregisterAllCatalogueSource() {
        sourceCache.clear()
        LocalSource.registerFast()
        metaCache.clear()
        clientToSourceMap.clear()
        sourceRandomUaMap.clear()
    }

    fun getSourceRandomUaByClient(client: OkHttpClient?, headers: Headers?): Boolean {
        if (client == null) {
            return false
        }
        if (isDesktopUa(headers)) {
            return false
        }
        val sourceId = clientToSourceMap[client] ?: return false
        return getSourceRandomUa(sourceId)
    }

    fun getForceUaByClient(client: OkHttpClient?): String? {
        if (client == null) {
            return null
        }
//        println("[UA] $clientToSourceMap")
        val sourceId = clientToSourceMap[client] ?: return null
        return SourceConfig.getForceUaBySourceId(sourceId)
    }

    fun setSourceRandomUaByClient(client: OkHttpClient?, randomUa: Boolean) {
        if (client == null) {
            return
        }
        if (client == network.client || client == network.cloudflareClient) {
            logger.info { "not custom client, skip" }
            return
        }
        val sourceId = clientToSourceMap[client] ?: return
        setSourceRandomUa(sourceId, randomUa)
    }

    private fun getSourceRandomUa(sourceId: Long): Boolean {
        if (SourceConfig.isForceSourceUa(sourceId)) {
            return false
        }
        var randomUa = sourceRandomUaMap[sourceId]
        if (randomUa == null) {
            val source = transaction {
                SourceTable.select { SourceTable.id eq sourceId }.firstOrNull()
            }
            randomUa = source?.get(SourceTable.randomUa) == true
            sourceRandomUaMap[sourceId] = randomUa
        }
        return randomUa
    }

    private fun setSourceRandomUa(sourceId: Long, randomUa: Boolean) {
        if (getSourceRandomUa(sourceId) == randomUa) {
            return
        }
        metaCache.remove(sourceId)
        transaction {
            SourceTable.update({ SourceTable.id eq sourceId }) {
                it[SourceTable.randomUa] = randomUa
            }
        }
        sourceRandomUaMap[sourceId] = randomUa
    }

    fun recordLatestUserAgent(client: OkHttpClient?, userAgent: String?) {
        val sourceId = clientToSourceMap[client] ?: return
        if (userAgent == null) {
            return
        }
        sourceLatestUserAgentMap[sourceId] = userAgent
    }

    fun queryLatestUserAgent(sourceId: Long): String? {
        return sourceLatestUserAgentMap[sourceId]
    }

    fun isAndroidMobileUa(headers: Headers?): Boolean {
        val userAgent = headers?.get("User-Agent")
        if (userAgent?.startsWith("Mozilla/5.0 (Linux; Android") == true || userAgent?.startsWith("Mozilla/5.0 (Android") == true) {
            logger.info { "isAndroidMobileUa true, ua:$userAgent" }
            return true
        }
        return false
    }

    private fun isDesktopUa(headers: Headers?): Boolean {
        return headers?.get("User-Agent") == "Mozilla/5.0 (Windows NT 6.3; WOW64)"
    }
}
