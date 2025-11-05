package okhttp3.brotli

import okhttp3.Interceptor
import okhttp3.Response

object BrotliInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}
