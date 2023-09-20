package eu.kanade.tachiyomi.network.interceptor

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import org.tachiyomi.NativeNet
import io.javalin.plugin.json.JsonMapper
import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import okhttp3.internal.http.RealResponseBody
import okio.Buffer
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance

class CallNativeNetInterceptor : Interceptor {
    private val jsonMapper by DI.global.instance<JsonMapper>()

    override fun intercept(chain: Interceptor.Chain): Response {
//         return chain.proceed(chain.request())
        return sendNativeRequest(call = chain.call())
    }

    private fun sendNativeRequest(call: Call): Response {
        val t = System.currentTimeMillis()

        val req = buildRequest(call)
        val t1 = System.currentTimeMillis()
        println("NativeNet: buildRequest cost:${t1 - t} ms, req:${req.url}")

        val resp = NativeNet.call(req, jsonMapper)

        val t2 = System.currentTimeMillis()
        println("NativeNet: call cost:${t2 - t1} ms, req:${req.url}")

        val response = buildResponse(call, resp)

        val t3 = System.currentTimeMillis()
        println("NativeNet: buildResponse cost:${t3 - t2} ms, req:${req.url}")

        println("NativeNet: response:$response")
        val timeMs = System.currentTimeMillis() - t
        println("Profiler: NativeNet: sendNativeRequest all cost $timeMs ms, req:${req.url}")

        return response
    }

    private fun buildRequest(call: Call): NativeNet.Req {
        val headers = mutableMapOf<String, String>()
        val request = call.request()
        for (i in 0 until request.headers.size) {
            headers[request.headers.name(i)] = request.headers.value(i)
        }
        return NativeNet.Req(
            request.url.toString(),
            "GET",
            headers
        )
    }

    private fun buildResponse(call: Call, resp: NativeNet.Resp): Response {
        val buffer = Buffer()
        buffer.write(resp.body)

        val contentType = resp.headers["Content-Type"]
        return Response.Builder()
            .request(call.request())
            .protocol(Protocol.HTTP_1_1)
            .message(resp.message)
            .code(resp.code)
            .headers(resp.headers.toHeaders())
            .body(RealResponseBody(contentType, resp.contentLength, buffer))
            .sentRequestAtMillis(System.currentTimeMillis() - 1000) // TODO
            .receivedResponseAtMillis(System.currentTimeMillis())
            .build()
    }
}
