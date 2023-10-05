package eu.kanade.tachiyomi.network.interceptor

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.connection.RealCall

fun <T : Any> T.getPrivateProperty(variableName: String): Any? {
    return javaClass.getDeclaredField(variableName).let { field ->
        field.isAccessible = true
        return@let field.get(this)
    }
}

class EnableNativeNetInterceptor : Interceptor {

    private val OKHTTP_LIST = listOf(
        "RetryAndFollowUpInterceptor",
        "BridgeInterceptor",
        "CacheInterceptor",
        "ConnectInterceptor",
        "CallServerInterceptor",
        "RateLimitInterceptor",
        "SpecificHostRateLimitInterceptor"
    )
    override fun intercept(chain: Interceptor.Chain): Response {
        val enable = supportNativeNet(chain)
        println("Profiler: native net $enable")
        if (enable) {
            val interceptors = chain.getPrivateProperty("interceptors") as MutableList<Interceptor>
            // println("Profiler: interceptors1 $interceptors")
            interceptors.removeIf {
                OKHTTP_LIST.contains(it.javaClass.simpleName)
            }
            interceptors.add(CallNativeNetInterceptor())
            // println("Profiler: interceptors2 $interceptors")
        }
        return chain.proceed(chain.request())
    }

    private fun supportNativeNet(chain: Interceptor.Chain): Boolean {
        println("Profiler: ENABLE_NATIVE_NET $ENABLE_NATIVE_NET")
        if (ENABLE_NATIVE_NET != true) {
            return false
        }

        val originalRequest = chain.request()

        val call = chain.call()
        val client = (call as? RealCall)?.client ?: return false

        if (client.authenticator != Authenticator.NONE) {
            println("authenticator not none")
            return false
        }
        if (client.proxyAuthenticator != Authenticator.NONE) {
            println("proxyAuthenticator not none")
            return false
        }
//        if (originalRequest.method != "GET") {
//            println("not GET")
//            return false
//        }
//        if (originalRequest.body != null) {
//            println("body is not null")
//            return false
//        }
        return true
    }

    companion object {
        var ENABLE_NATIVE_NET: Boolean? = null
    }
}
