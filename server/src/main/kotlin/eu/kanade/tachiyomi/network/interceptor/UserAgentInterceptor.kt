package eu.kanade.tachiyomi.network.interceptor

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
        println("Profiler: forceRandomUa:$randomUa, forceUa:$forceUa")
        return if (randomUa || originalRequest.header("User-Agent").isNullOrEmpty() || forceUa?.isNotEmpty() == true || androidMobileUa) {
            val newRequest = originalRequest
                .newBuilder()
                .removeHeader("User-Agent")
                .addHeader("User-Agent", forceUa ?: HttpSource.DEFAULT_USER_AGENT)
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}
