package eu.kanade.tachiyomi.network

/*
 * Copyright (C) Contributors to the Suwayomi project
 * Copyright (C) 2023 Tachimanga
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import kotlinx.serialization.serializer
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import rx.Producer
import rx.Subscription
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resumeWithException

val jsonMime = "application/json; charset=utf-8".toMediaType()

/**
 * extensions-lib 1.4
 */
fun Call.asObservable(): Observable<Response> {
    return Observable.unsafeCreate { subscriber ->
        // Since Call is a one-shot type, clone it for each new subscriber.
        val call = clone()

        // Wrap the call in a helper which handles both unsubscription and backpressure.
        val requestArbiter = object : AtomicBoolean(), Producer, Subscription {
            override fun request(n: Long) {
                if (n == 0L || !compareAndSet(false, true)) return

                try {
                    val response = call.execute()
                    if (!subscriber.isUnsubscribed) {
                        subscriber.onNext(response)
                        subscriber.onCompleted()
                    }
                } catch (error: Exception) {
                    if (!subscriber.isUnsubscribed) {
                        subscriber.onError(error)
                    }
                }
            }

            override fun unsubscribe() {
                call.cancel()
            }

            override fun isUnsubscribed(): Boolean {
                return call.isCanceled()
            }
        }

        subscriber.add(requestArbiter)
        subscriber.setProducer(requestArbiter)
    }
}

/**
 * extensions-lib 1.4
 */
fun Call.asObservableSuccess(): Observable<Response> {
    return asObservable().doOnNext { response ->
        if (!response.isSuccessful) {
            response.close()
            throw HttpException(response.code)
        }
    }
}

/**
 * extensions-lib 1.4
 * Based on https://github.com/gildor/kotlin-coroutines-okhttp
 */
suspend fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->
        val callback =
            object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response) {
                        response.body.close()
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    // Don't bother with resuming the continuation if it is already cancelled.
                    if (continuation.isCancelled) return
                    continuation.resumeWithException(e)
                }
            }

        enqueue(callback)

        continuation.invokeOnCancellation {
            try {
                cancel()
            } catch (ex: Throwable) {
                // Ignore cancel exception
            }
        }
    }
}

/**
 * @since extensions-lib 1.5
 */
suspend fun Call.awaitSuccess(): Response {
    val response = await()
    if (!response.isSuccessful) {
        response.close()
        throw HttpException(response.code)
    }
    return response
}

/**
 * not in extensions-lib
 */
fun OkHttpClient.newCachelessCallWithProgress(request: Request, listener: ProgressListener): Call {
    val progressClient = newBuilder()
        .cache(null)
        .addNetworkInterceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            originalResponse.newBuilder()
                .body(ProgressResponseBody(originalResponse.body, listener))
                .build()
        }
        .build()

    return progressClient.newCall(request)
}

/**
 * not in extensions-lib
 */
context(Json)
inline fun <reified T> Response.parseAs(): T {
    return decodeFromJsonResponse(serializer(), this)
}

context(Json)
@OptIn(ExperimentalSerializationApi::class)
fun <T> decodeFromJsonResponse(
    deserializer: DeserializationStrategy<T>,
    response: Response,
): T {
    return response.body.source().use {
        decodeFromBufferedSource(deserializer, it)
    }
}
