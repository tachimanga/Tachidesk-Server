package eu.kanade.tachiyomi.network.interceptor

import eu.kanade.tachiyomi.network.NetworkHelper
import mu.KotlinLogging
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.connection.RealCall
import suwayomi.tachidesk.manga.impl.util.source.GetCatalogueSource
import uy.kohesive.injekt.injectLazy
import java.io.IOException

class CloudflareInterceptor : Interceptor {
    private val logger = KotlinLogging.logger {}

    private val network: NetworkHelper by injectLazy()

    override fun intercept(chain: Interceptor.Chain): Response {
        logger.trace { "CloudflareInterceptor is being used." }

        var blocked = false
        try {
            val originalResponse = chain.proceed(chain.request())
            // Check if Cloudflare anti-bot is on
            blocked = (originalResponse.code in ERROR_CODES && originalResponse.header("Server") in SERVER_CHECK)
            if (!blocked) {
                return originalResponse
            }
            throw IOException("Blocked by Cloudflare")
        } catch (e: Exception) {
            // Check for Readcomiconline.kt
            if (e.message?.contains("Solve captcha in WebView") == true) {
                blocked = true
            }
            throw e
        } finally {
            if (blocked) {
                val call = chain.call()
                val client = (call as? RealCall)?.client
                GetCatalogueSource.setSourceRandomUaByClient(client, true)
            }
        }
    }

    companion object {
        private val ERROR_CODES = listOf(403, 503)
        private val SERVER_CHECK = arrayOf("cloudflare-nginx", "cloudflare")
        private val COOKIE_NAMES = listOf("cf_clearance")
    }
}
