package suwayomi.tachidesk.manga.impl

import eu.kanade.tachiyomi.network.NetworkHelper
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import okhttp3.Cookie
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
    data class CookieData(
        val cookies: List<CookieInfo>? = null
    )

    @Serializable
    data class CookieInfo(
        val name: String? = null,
        val value: String? = null,
        val expiresAt: Long? = null,
        val domain: String? = null,
        val path: String? = null,
        val secure: Boolean? = null,
        val httpOnly: Boolean? = null
    )

    fun uploadCookies(input: CookieData) {
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
