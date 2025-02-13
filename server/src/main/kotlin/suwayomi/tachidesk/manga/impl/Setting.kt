package suwayomi.tachidesk.manga.impl

import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.network.interceptor.EnableNativeNetInterceptor
import eu.kanade.tachiyomi.source.SourceSetting
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import okhttp3.Cookie
import suwayomi.tachidesk.cloud.impl.Sync
import suwayomi.tachidesk.manga.impl.download.DownloadManager
import suwayomi.tachidesk.manga.impl.util.source.SourceConfig
import uy.kohesive.injekt.injectLazy

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

object Setting {
    private val logger = KotlinLogging.logger {}
    private val network: NetworkHelper by injectLazy()

    @Serializable
    data class SettingData(
        val cookies: List<CookieInfo>? = null,
        val enableNativeNet: Boolean? = null,
        val enableFlutterDirect: Boolean? = null,
        val sourceConfigList: List<SourceConfigInfo>? = null,
        val downloadTaskInParallel: Int? = null,
        val syncListenerInterval: Int? = null,
        // https://api3.tachimanga.app
        val cloudServer: String? = null,
        val appInfo: AppInfoDataClass? = null,
        val locale: String? = null,
    )

    @Serializable
    data class CookieInfo(
        val name: String? = null,
        val value: String? = null,
        val expiresAt: Long? = null,
        val domain: String? = null,
        val path: String? = null,
        val secure: Boolean? = null,
        val httpOnly: Boolean? = null,
    )

    @Serializable
    data class SourceConfigInfo(
        val sourceId: Long? = null,
        val ua: String? = null,
    )

    @Serializable
    data class AppInfoDataClass(
        val appVersion: String? = null,
        val appBuild: String? = null,
        val bundleId: String? = null,
        val deviceId: String? = null,
        val locale: String? = null,
    )

    fun uploadSettings(input: SettingData) {
        if (input.enableNativeNet != null) {
            println("update ENABLE_NATIVE_NET to " + input.enableNativeNet)
            EnableNativeNetInterceptor.ENABLE_NATIVE_NET = input.enableNativeNet
        }
        if (input.enableFlutterDirect != null) {
            println("update enableFlutterDirect to " + input.enableFlutterDirect)
            SourceSetting.ENABLE_FLUTTER_DIRECT = input.enableFlutterDirect
        }
        if (input.sourceConfigList != null) {
            SourceConfig.updateSourceConfig(input.sourceConfigList)
        }
        if (input.downloadTaskInParallel != null) {
            DownloadManager.updateTaskInParallel(input.downloadTaskInParallel)
        }
        if (input.syncListenerInterval != null) {
            Sync.setListenerInterval(input.syncListenerInterval)
        }
        if (input.cloudServer?.isNotEmpty() == true) {
            Sync.setApiHost(input.cloudServer)
        }
        if (input.appInfo != null) {
            AppInfo.appVersion = input.appInfo.appVersion
            AppInfo.appBuild = input.appInfo.appBuild
            AppInfo.bundleId = input.appInfo.bundleId
            AppInfo.deviceId = input.appInfo.deviceId
        }
        if (input.locale != null) {
            AppInfo.locale = input.locale
        }
    }

    fun uploadCookies(input: SettingData) {
        if (input.cookies.isNullOrEmpty()) {
            return
        }
        val list = input.cookies.map { c ->
            val builder = Cookie.Builder()
            if (c.name != null) {
                builder.name(c.name)
            }
            if (c.value != null) {
                builder.value(c.value)
            }
            if (c.expiresAt != null) {
                builder.expiresAt(c.expiresAt)
            }
            if (c.domain != null) {
                builder.domain(c.domain.removePrefix("."))
            }
            if (c.path != null) {
                builder.path(c.path)
            }
            if (c.secure != null && c.secure) {
                builder.secure()
            }
            if (c.httpOnly != null && c.httpOnly) {
                builder.httpOnly()
            }
            builder.build()
        }
        logger.info { "list $list" }
        network.cookieManager.store.uploadAll(list)
    }

    fun clearCookies() {
        network.cookieManager.store.removeAll()
    }
}
