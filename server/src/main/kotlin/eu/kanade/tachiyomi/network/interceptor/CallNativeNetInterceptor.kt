package eu.kanade.tachiyomi.network.interceptor

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import io.javalin.plugin.json.JsonMapper
import okhttp3.Headers.Companion.toHeaders
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.http.RealResponseBody
import okio.Buffer
import org.kodein.di.DI
import org.kodein.di.conf.global
import org.kodein.di.instance
import org.tachiyomi.NativeNet
import org.tachiyomi.Profiler
import java.io.IOException

class CallNativeNetInterceptor : Interceptor {
    private val jsonMapper by DI.global.instance<JsonMapper>()

    override fun intercept(chain: Interceptor.Chain): Response {
        return sendNativeRequest(chain.request())
    }

    private fun sendNativeRequest(request: Request): Response {
        val t = System.currentTimeMillis()

        val req = buildRequest(request)
        val resp = NativeNet.call(req.first, req.second, jsonMapper)
        val response = buildResponse(request, resp, t)
        // println("NativeNet: response:$response")

        val nativeCost = response.headers["x-native-cost"]?.toLong()
        val timeMs = System.currentTimeMillis() - t
        val diff = if (nativeCost != null) {
            Profiler.incrNativeNet(request.url.toString(), nativeCost)
            timeMs - nativeCost
        } else { 0 }

        println("Profiler: NativeNet: cost:[$timeMs|$nativeCost|$diff]ms, req:${req.first.url}")
        return response
    }

    private fun buildRequest(request: Request): Pair<NativeNet.Req, Buffer?> {
        val headers = mutableMapOf<String, String>()
        for (i in 0 until request.headers.size) {
            headers[request.headers.name(i)] = request.headers.value(i)
        }

        val body = request.body
        var buffer: Buffer? = null
        if (body != null) {
            val contentType = body.contentType()
            if (contentType != null) {
                headers["Content-Type"] = contentType.toString()
            }
            buffer = Buffer()
            body.writeTo(buffer)
            println("Profiler: NativeNet: body.len ${buffer.size}, content-type:$contentType")
            if (buffer.size == 0L) {
                buffer = null
            }
        }
        val meta = NativeNet.Req(
            request.url.toString(),
            request.method,
            headers
        )
        return Pair(meta, buffer)
    }

    private fun buildResponse(request: Request, resp: NativeNet.Resp, t: Long): Response {
        if (resp.error != null) {
            throw IOException(resp.error)
        }

        val headers = resp.headers ?: mapOf()
        val message = resp.message ?: ""

        val contentType = headers["Content-Type"]
        val builder = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .message(message)
            .code(resp.code)
            .headers(headers.toHeaders())
            .sentRequestAtMillis(t)
            .receivedResponseAtMillis(System.currentTimeMillis())
        if (resp.byteBuffer != null) {
            val buffer = Buffer()
            buffer.write(resp.byteBuffer)
            builder.body(RealResponseBody(contentType, buffer.size, buffer))
        }
        return builder.build()
    }
}
