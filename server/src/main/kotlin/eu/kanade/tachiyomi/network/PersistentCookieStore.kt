package eu.kanade.tachiyomi.network

import android.content.Context
import okhttp3.Cookie
import okhttp3.HttpUrl
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

// from TachiWeb-Server
class PersistentCookieStore(context: Context) {

    private val cookieMap = ConcurrentHashMap<String, List<Cookie>>()
    private val prefs = context.getSharedPreferences("cookie_store", Context.MODE_PRIVATE)

    /*
    init {
        for ((key, value) in prefs.all) {
            println("load cookies $key:$value")
            @Suppress("UNCHECKED_CAST")
            val cookies = value as? Set<String>
            if (cookies != null) {
                try {
                    val nonExpiredCookies = cookies.mapNotNull { Cookie.parse(url, it) }
                        .filter { !it.hasExpired() }
                    cookieMap.put(key, nonExpiredCookies)
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }*/
    @Synchronized
    fun addAll(url: HttpUrl, cookies: List<Cookie>) {
        val key = url.toUri().host

        // Append or replace the cookies for this domain.
        val cookiesForDomain = cookieMap[key].orEmpty().toMutableList()
        for (cookie in cookies) {
            // Find a cookie with the same name. Replace it if found, otherwise add a new one.
            val pos = cookiesForDomain.indexOfFirst { it.name == cookie.name }
            if (pos == -1) {
                cookiesForDomain.add(cookie)
            } else {
                cookiesForDomain[pos] = cookie
            }
        }
        cookieMap.put(key, cookiesForDomain)

//        // Get cookies to be stored in disk
//        val newValues = cookiesForDomain.asSequence()
//            .filter { it.persistent && !it.hasExpired() }
//            .map(Cookie::toString)
//            .toSet()
//
//        prefs.edit().putStringSet(key, newValues).apply()
    }

    @Synchronized
    fun uploadAll(cookies: List<Cookie>) {
        val key = "UPLOAD"
        cookieMap.put(key, cookies)

//        // Get cookies to be stored in disk
//        val newValues = cookies
//            .map(Cookie::toString)
//            .toSet()
//        prefs.edit().putStringSet(key, newValues).apply()
    }

    @Synchronized
    fun removeAll() {
        // prefs.edit().clear().apply()
        cookieMap.clear()
    }

    fun remove(uri: URI) {
        // prefs.edit().remove(uri.host).apply()
        cookieMap.remove(uri.host)
    }

    //    fun get(url: HttpUrl) = get(url.toUri().host)
    fun get(url: HttpUrl): List<Cookie> {
        return cookieMap.flatMap { it.value }
            .filter { cookie -> !cookie.hasExpired() && cookie.matches(url) }
    }
//
//    fun get(uri: URI) = get(uri.host)
//
//    private fun get(url/*host*/: String): List<Cookie> {
//        return cookieMap[url].orEmpty().filter { !it.hasExpired() }
//    }

    private fun Cookie.hasExpired() = System.currentTimeMillis() >= expiresAt
}
