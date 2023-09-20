package eu.kanade.tachiyomi.network.interceptor

import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.connection.RealCall

fun <T : Any> T.getPrivateProperty(variableName: String): Any? {
    return javaClass.getDeclaredField(variableName).let { field ->
        field.isAccessible = true
        return@let field.get(this)
    }
}

class EnableNativeNetInterceptor : Interceptor {

    private val OKHTTP_LIST = listOf(
        "RetryAndFollowUpInterceptor",
        "BridgeInterceptor",
        "CacheInterceptor",
        "ConnectInterceptor",
        "CallServerInterceptor",
        "RateLimitInterceptor",
        "SpecificHostRateLimitInterceptor"
    )
    override fun intercept(chain: Interceptor.Chain): Response {
        val enable = supportNativeNet(chain)
        println("Profiler: native net $enable")
        if (enable) {
            val interceptors = chain.getPrivateProperty("interceptors") as MutableList<Interceptor>
            println(interceptors)
            interceptors.removeIf {
                OKHTTP_LIST.contains(it.javaClass.simpleName)
            }
            interceptors.add(CallNativeNetInterceptor())
            println("Profiler: $interceptors")
        }
        return chain.proceed(chain.request())
    }

    private fun supportNativeNet(chain: Interceptor.Chain): Boolean {
        val originalRequest = chain.request()

        val call = chain.call()
        val client = (call as? RealCall)?.client ?: return false

        if (client.authenticator != Authenticator.NONE) {
            println("authenticator not none")
            return false
        }
        if (client.proxyAuthenticator != Authenticator.NONE) {
            println("proxyAuthenticator not none")
            return false
        }
        if (originalRequest.method != "GET") {
            println("not GET")
            return false
        }
        if (originalRequest.body != null) {
            println("body is not null")
            return false
        }
        return true
    }
}
