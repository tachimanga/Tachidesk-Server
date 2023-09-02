package suwayomi.tachidesk.manga.model.dataclass

import eu.kanade.tachiyomi.network.NetworkHelper
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import uy.kohesive.injekt.injectLazy

/**
 * @author mc
 */

fun buildImgDataClass(url: String, headers: Map<String, String>?, cacheMap: MutableMap<String, String>? = null): ImgDataClass {
    ImgDataClassHelper.setCookieHeader(url, headers, cacheMap)
    return ImgDataClass(url = url, headers = headers)
}

data class ImgDataClass(
    val url: String,
    val method: String? = "GET",
    val headers: Map<String, String>?
)

object ImgDataClassHelper {
    private val network: NetworkHelper by injectLazy()

    fun setCookieHeader(url: String, headers: Map<String, String>?, cacheMap: MutableMap<String, String>?) {
        if (headers == null || headers !is HashMap) {
            return
        }
        if (url.isEmpty()) {
            return
        }
        val httpUrl = url.toHttpUrlOrNull() ?: return
        val cookies = network.cookieManager.loadForRequest(httpUrl)
        if (cookies.isNotEmpty()) {
            headers["Cookie"] = cookieHeader(cookies)
        }
    }

    private fun cookieHeader(cookies: List<Cookie>): String = buildString {
        cookies.forEachIndexed { index, cookie ->
            if (index > 0) append("; ")
            append(cookie.name).append('=').append(cookie.value)
        }
    }
}
