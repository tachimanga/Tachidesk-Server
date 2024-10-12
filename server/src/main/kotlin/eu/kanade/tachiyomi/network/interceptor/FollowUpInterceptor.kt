package eu.kanade.tachiyomi.network.interceptor

import mu.KotlinLogging
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.closeQuietly
import okhttp3.internal.http.*
import okhttp3.internal.stripBody
import java.io.IOException
import java.net.ProtocolException

/**
 * This interceptor recovers from failures and follows redirects as necessary. It may throw an
 * [IOException] if the call was canceled.
 */
class FollowUpInterceptor(private val client: OkHttpClient) : Interceptor {
    private val logger = KotlinLogging.logger {}

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val call = chain.call()
        var followUpCount = 0
        var priorResponse: Response? = null
        while (true) {
            logger.info { "FollowUpInterceptor request:$request followUpCount:$followUpCount" }

            if (call.isCanceled()) {
                throw IOException("Canceled")
            }

            var response = chain.proceed(request)

            // Clear out downstream interceptor's additional request headers, cookies, etc.
            response = response.newBuilder()
                .request(request)
                .priorResponse(priorResponse?.stripBody())
                .build()

            val followUp = followUpRequest(response) ?: return response

            val followUpBody = followUp.body
            if (followUpBody != null && followUpBody.isOneShot()) {
                return response
            }

            response.body.closeQuietly()

            if (++followUpCount > MAX_FOLLOW_UPS) {
                throw ProtocolException("Too many follow-up requests: $followUpCount")
            }

            request = followUp
            priorResponse = response
        }
    }

    @Throws(IOException::class)
    private fun followUpRequest(userResponse: Response): Request? {
        if (userResponse.code == HTTP_UNAUTHORIZED) {
            return client.authenticator.authenticate(null, userResponse)
        }
        return null
    }

    companion object {
        /**
         * How many redirects and auth challenges should we attempt? Chrome follows 21 redirects; Firefox,
         * curl, and wget follow 20; Safari follows 16; and HTTP/1.0 recommends 5.
         */
        private const val MAX_FOLLOW_UPS = 5
    }
}
