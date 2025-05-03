package eu.kanade.tachiyomi.network.interceptor

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import mu.KotlinLogging
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.connection.RealCall
import xyz.nulldev.androidcompat.CommonSwitch.ENABLE_NATIVE_COOKIE

fun <T : Any> T.getPrivateProperty(variableName: String): Any? {
    return javaClass.getDeclaredField(variableName).let { field ->
        field.isAccessible = true
        return@let field.get(this)
    }
}

class EnableNativeNetInterceptor : Interceptor {
    private val logger = KotlinLogging.logger {}

    private val OKHTTP_LIST = listOf(
        "RetryAndFollowUpInterceptor",
        "BridgeInterceptor",
        "CacheInterceptor",
        "ConnectInterceptor",
        "CallServerInterceptor",
        "RateLimitInterceptor",
        "SpecificHostRateLimitInterceptor",
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val enable = supportNativeNet(chain)
        logger.info { "[NativeNet]enable:$enable (ENABLE_NATIVE_NET:$ENABLE_NATIVE_NET, ENABLE_NATIVE_COOKIE:$ENABLE_NATIVE_COOKIE)" }
        if (enable) {
            val interceptors = chain.getPrivateProperty("interceptors") as MutableList<Interceptor>
            // println("Profiler: interceptors1 $interceptors")
            if (!ENABLE_NATIVE_COOKIE) {
                interceptors.removeIf {
                    OKHTTP_LIST.contains(it.javaClass.simpleName)
                }
                addFollowUpInterceptorIfNeeded(chain, interceptors)
                interceptors.add(CallNativeNetInterceptor())
            } else {
                val index = interceptors.indexOfFirst {
                    it.javaClass.simpleName == "ConnectInterceptor"
                }
                val client = (chain.call() as? RealCall)?.client
                if (client != null && index != -1) {
                    interceptors.add(index, McCookieInterceptor(client.cookieJar))
                    interceptors.add(index, FollowUpInterceptor2(client))
                }
                interceptors.add(CallNativeNetInterceptor())
                interceptors.removeIf {
                    OKHTTP_LIST.contains(it.javaClass.simpleName)
                }
            }
            // println("Profiler: interceptors2 $interceptors")
        }
        return chain.proceed(chain.request())
    }

    private fun addFollowUpInterceptorIfNeeded(chain: Interceptor.Chain, interceptors: MutableList<Interceptor>) {
        val client = (chain.call() as? RealCall)?.client ?: return
        if (client.authenticator != Authenticator.NONE) {
            logger.info { "[NativeNet] add FollowUpInterceptor" }
            interceptors.add(FollowUpInterceptor(client))
        }
    }

    private fun supportNativeNet(chain: Interceptor.Chain): Boolean {
        if (ENABLE_NATIVE_NET != true) {
            return false
        }

        val call = chain.call()
        val client = (call as? RealCall)?.client ?: return false

        if (client.proxyAuthenticator != Authenticator.NONE) {
            logger.info { "[NativeNet]proxyAuthenticator not none" }
            return false
        }
        return true
    }

    companion object {
        var ENABLE_NATIVE_NET: Boolean? = true
    }
}
