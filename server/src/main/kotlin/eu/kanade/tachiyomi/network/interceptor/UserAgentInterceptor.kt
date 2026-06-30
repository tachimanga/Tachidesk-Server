package eu.kanade.tachiyomi.network.interceptor

/*
 * Copyright (C) Contributors to the Suwayomi project
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import eu.kanade.tachiyomi.source.online.HttpSource
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.connection.RealCall
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource

class UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val call = chain.call()
        val client = (call as? RealCall)?.client

        val randomUa = GetCatalogueSource.getSourceRandomUaByClient(client, originalRequest.headers)
        val forceUa = GetCatalogueSource.getForceUaByClient(client)
        val androidMobileUa = GetCatalogueSource.isAndroidMobileUa(originalRequest.headers)
        println("Profiler: forceRandomUa:$randomUa, forceUa:$forceUa a:${originalRequest.header("User-Agent")}, HttpSource:${HttpSource.DEFAULT_USER_AGENT}")
        return if (randomUa || originalRequest.header("User-Agent").isNullOrEmpty() || forceUa?.isNotEmpty() == true || androidMobileUa) {
            val userAgent = forceUa ?: HttpSource.DEFAULT_USER_AGENT
            val newRequest = originalRequest
                .newBuilder()
                .removeHeader("User-Agent")
                .addHeader("User-Agent", userAgent)
                .build()
            GetCatalogueSource.recordLatestUserAgent(client, userAgent)
            chain.proceed(newRequest)
        } else {
            GetCatalogueSource.recordLatestUserAgent(client, originalRequest.header("User-Agent"))
            chain.proceed(originalRequest)
        }
    }
}
