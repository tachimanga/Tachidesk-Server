package eu.kanade.tachiyomi.network

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.content.Context
import eu.kanade.tachiyomi.network.interceptor.CloudflareInterceptor
import eu.kanade.tachiyomi.network.interceptor.EnableNativeNetInterceptor
import eu.kanade.tachiyomi.network.interceptor.UserAgentInterceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@Suppress("UNUSED_PARAMETER")
class NetworkHelper(context: Context) {

    val cookieJar = McCookieJar()

    private val baseClientBuilder: OkHttpClient.Builder
        get() {
            val builder = OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .callTimeout(2, TimeUnit.MINUTES)
                .addInterceptor(UserAgentInterceptor())
                .addInterceptor(CloudflareInterceptor())
                .addInterceptor(EnableNativeNetInterceptor())
//                .sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.getTrustManager()[0] as X509TrustManager)
//                .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                .eventListenerFactory(McLoggingEventListener.Factory())

//            if (serverConfig.debugLogsEnabled) {
//                val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
//                    level = HttpLoggingInterceptor.Level.BASIC
//                }
//                builder.addInterceptor(httpLoggingInterceptor)
//            }
            return builder
        }

    private val commonClient: OkHttpClient = baseClientBuilder.build()

    val client: OkHttpClient = commonClient
    val cloudflareClient: OkHttpClient = commonClient
}
