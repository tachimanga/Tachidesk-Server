package suwayomi.tachidesk.manga.impl

import eu.kanade.tachiyomi.network.interceptor.EnableNativeNetInterceptor
import eu.kanade.tachiyomi.source.SourceSetting
import eu.kanade.tachiyomi.source.online.HttpSource
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import suwayomi.tachidesk.cloud.impl.Sync
import suwayomi.tachidesk.manga.impl.download.DownloadManager
import suwayomi.tachidesk.manga.impl.update.UpdateManager
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource
import suwayomi.tachidesk.manga.impl.util.source.SourceConfig
import xyz.nulldev.androidcompat.CommonSwitch

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

object Setting {
    private val logger = KotlinLogging.logger {}

    @Serializable
    data class SettingData(
        val cookies: List<CookieInfo>? = null,
        val enableNativeNet: Boolean? = null,
        val enableNativeCookie: Boolean? = null,
        val enableFlutterDirect: Boolean? = null,
        val sourceConfigList: List<SourceConfigInfo>? = null,
        val downloadTaskInParallel: Int? = null,
        val syncListenerInterval: Int? = null,
        // https://api3.tachimanga.app
        val cloudServer: String? = null,
        val appInfo: AppInfoDataClass? = null,
        val locale: String? = null,
        val userAgent: String? = null,
        val selectedCategories: List<String>? = null,
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
            logger.info { "update ENABLE_NATIVE_NET to ${input.enableNativeNet}" }
            EnableNativeNetInterceptor.ENABLE_NATIVE_NET = input.enableNativeNet
        }
        if (input.enableNativeCookie != null) {
            logger.info { "update ENABLE_NATIVE_COOKIE to ${input.enableNativeCookie}" }
            CommonSwitch.ENABLE_NATIVE_COOKIE = input.enableNativeCookie
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
        if (input.userAgent != null) {
            updateUserAgent(input.userAgent)
        }
        if (input.selectedCategories != null) {
            UpdateManager.migrateMigrateSelectedCategoriesIfNeeded(input.selectedCategories)
        }
    }

    fun updateUserAgent(userAgent: String?) {
        logger.info { "[UA]uploadSettings userAgent: $userAgent" }
        if (userAgent?.isNotBlank() == true) {
            val prev = HttpSource.DEFAULT_USER_AGENT
            HttpSource.DEFAULT_USER_AGENT = userAgent
            System.setProperty("http.agent", userAgent)
            if (prev != HttpSource.INIT_USER_AGENT && prev != userAgent) {
                GetCatalogueSource.unregisterAllCatalogueSource()
            }
        }
    }
}
