package eu.kanade.tachiyomi.network.interceptor

import eu.kanade.tachiyomi.network.McCookieJar
import okhttp3.*
import okhttp3.internal.http.receiveHeaders
import java.io.IOException
import java.util.*

/**
 * Bridges from application code to network code. First it builds a network request from a user
 * request. Then it proceeds to call the network. Finally it builds a user response from the network
 * response.
 */
class McCookieInterceptor(private val cookieJar: CookieJar) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val userRequest = chain.request()
        val requestBuilder = userRequest.newBuilder()

        if (cookieJar is McCookieJar) {
            val cookies = cookieJar.directLoadForRequest(userRequest.url)
            if (cookies.isNotEmpty()) {
                requestBuilder.header("Cookie", cookies)
            }
        } else {
            val cookies = cookieJar.loadForRequest(userRequest.url)
            if (cookies.isNotEmpty()) {
                requestBuilder.header("Cookie", cookieHeader(cookies))
            }
        }

        val networkRequest = requestBuilder.build()
        val networkResponse = chain.proceed(networkRequest)

        if (cookieJar is McCookieJar) {
            cookieJar.directSaveFromResponse(networkRequest.url, networkResponse.headers)
        } else {
            cookieJar.receiveHeaders(networkRequest.url, networkResponse.headers)
        }

        return networkResponse
    }

    /** Returns a 'Cookie' HTTP request header with all cookies, like `a=b; c=d`. */
    private fun cookieHeader(cookies: List<Cookie>): String =
        buildString {
            cookies.forEachIndexed { index, cookie ->
                if (index > 0) append("; ")
                append(cookie.name).append('=').append(cookie.value)
            }
        }
}
