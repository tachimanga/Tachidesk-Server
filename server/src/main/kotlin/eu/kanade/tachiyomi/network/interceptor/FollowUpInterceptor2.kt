package eu.kanade.tachiyomi.network.interceptor

import mu.KotlinLogging
import okhttp3.*
import okhttp3.internal.closeQuietly
import okhttp3.internal.http.*
import okhttp3.internal.stripBody
import java.io.IOException
import java.net.HttpURLConnection.HTTP_MOVED_PERM
import java.net.HttpURLConnection.HTTP_MOVED_TEMP
import java.net.HttpURLConnection.HTTP_MULT_CHOICE
import java.net.HttpURLConnection.HTTP_SEE_OTHER
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.net.ProtocolException

/**
 * This interceptor recovers from failures and follows redirects as necessary. It may throw an
 * [IOException] if the call was canceled.
 */
class FollowUpInterceptor2(private val client: OkHttpClient) : Interceptor {
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
    private fun followUpRequest(
        userResponse: Response,
    ): Request? {
        val responseCode = userResponse.code
        val method = userResponse.request.method
        when (responseCode) {
            HTTP_UNAUTHORIZED -> return client.authenticator.authenticate(null, userResponse)

            HTTP_PERM_REDIRECT, HTTP_TEMP_REDIRECT, HTTP_MULT_CHOICE, HTTP_MOVED_PERM, HTTP_MOVED_TEMP, HTTP_SEE_OTHER -> {
                return buildRedirectRequest(userResponse, method)
            }

            else -> return null
        }
    }

    private fun buildRedirectRequest(
        userResponse: Response,
        method: String,
    ): Request? {
        // Does the client allow redirects?
        if (!client.followRedirects) return null

        val location = userResponse.header("Location") ?: return null
        // Don't follow redirects to unsupported protocols.
        val url = userResponse.request.url.resolve(location) ?: return null

        // If configured, don't follow redirects between SSL and non-SSL.
        val sameScheme = url.scheme == userResponse.request.url.scheme
        if (!sameScheme && !client.followSslRedirects) return null

        // Most redirects don't include a request body.
        val requestBuilder = userResponse.request.newBuilder()
        if (HttpMethod.permitsRequestBody(method)) {
            val responseCode = userResponse.code
            val maintainBody =
                HttpMethod.redirectsWithBody(method) ||
                    responseCode == HTTP_PERM_REDIRECT ||
                    responseCode == HTTP_TEMP_REDIRECT
            if (HttpMethod.redirectsToGet(method) && responseCode != HTTP_PERM_REDIRECT && responseCode != HTTP_TEMP_REDIRECT) {
                requestBuilder.method("GET", null)
            } else {
                val requestBody = if (maintainBody) userResponse.request.body else null
                requestBuilder.method(method, requestBody)
            }
            if (!maintainBody) {
                requestBuilder.removeHeader("Transfer-Encoding")
                requestBuilder.removeHeader("Content-Length")
                requestBuilder.removeHeader("Content-Type")
            }
        }

        // When redirecting across hosts, drop all authentication headers. This
        // is potentially annoying to the application layer since they have no
        // way to retain them.
        if (!userResponse.request.url.canReuseConnectionFor(url)) {
            requestBuilder.removeHeader("Authorization")
        }

        return requestBuilder.url(url).build()
    }

    companion object {
        /**
         * How many redirects and auth challenges should we attempt? Chrome follows 21 redirects; Firefox,
         * curl, and wget follow 20; Safari follows 16; and HTTP/1.0 recommends 5.
         */
        private const val MAX_FOLLOW_UPS = 16
    }
}
fun HttpUrl.canReuseConnectionFor(other: HttpUrl): Boolean =
    host == other.host &&
        port == other.port &&
        scheme == other.scheme
