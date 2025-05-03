package eu.kanade.tachiyomi.network

import android.webkit.CookieManager
import mu.KotlinLogging
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Headers
import okhttp3.HttpUrl
import xyz.nulldev.androidcompat.CommonSwitch

class McCookieJar : CookieJar {
    private val logger = KotlinLogging.logger {}
    private val manager = CookieManager.getInstance()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val enable = CommonSwitch.ENABLE_NATIVE_COOKIE
        logger.info { "[Cookie]saveFromResponse url:$url, cookies:$cookies, enable:$enable" }
        if (!enable) {
            return
        }
        if (cookies.isEmpty()) {
            return
        }
        val urlString = url.toString()
        // cookies.forEach { manager.setCookie(urlString, it.toString()) }
        val cookieStrings = cookies.map { it.toString() }
        manager.setCookie(urlString, cookieStrings.joinToString(", "))
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val enable = CommonSwitch.ENABLE_NATIVE_COOKIE
        logger.info { "[Cookie]loadForRequest... url:$url, enable:$enable" }
        if (!enable) {
            return emptyList()
        }
        val cookies = manager.getCookie(url.toString())
        val list = if (cookies != null && cookies.isNotEmpty()) {
            cookies.split(";").mapNotNull { Cookie.parse(url, it) }
        } else {
            emptyList()
        }
        logger.info { "[Cookie]loadForRequest done url:$url cookies:$list" }
        return list
    }

    fun directSaveFromResponse(url: HttpUrl, headers: Headers) {
        val cookieStrings = headers.values("Set-Cookie")
        // logger.info { "[Cookie]#directSaveFromResponse url:$url, Set-Cookie:$cookieStrings" }
        if (cookieStrings.isNotEmpty()) {
            manager.setCookie(url.toString(), cookieStrings.joinToString(", "))
        }
    }

    fun directLoadForRequest(url: HttpUrl): String {
        // logger.info { "[Cookie]directLoadForRequest... url:$url" }
        val cookies = manager.getCookie(url.toString())
        // logger.info { "[Cookie]#directLoadForRequest done url:$url cookies:$cookies" }
        return cookies ?: ""
    }
}
