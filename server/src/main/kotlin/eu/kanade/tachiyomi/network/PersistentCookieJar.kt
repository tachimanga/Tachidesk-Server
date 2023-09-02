package eu.kanade.tachiyomi.network

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

// from TachiWeb-Server
class PersistentCookieJar(context: Context) : CookieJar {

    val store = PersistentCookieStore(context)

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val s = System.currentTimeMillis()
        store.addAll(url, cookies)
        val cost = System.currentTimeMillis() - s
        if (cost > 5) {
            println("addAll $url-$cookies")
            println("Profiler: saveFromResponse cost:" + cost + "ms")
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val s = System.currentTimeMillis()
        val cookies = store.get(url)
        val cost = System.currentTimeMillis() - s
        if (cost > 5) {
            println("loadForRequest $url, cookies: $cookies")
            println("Profiler: loadForRequest cost:" + (System.currentTimeMillis() - s) + "ms")
        }
        return cookies
    }
}
