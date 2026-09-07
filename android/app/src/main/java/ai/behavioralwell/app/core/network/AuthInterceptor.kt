package ai.behavioralwell.app.core.network

import ai.behavioralwell.app.core.security.TokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenStorage: TokenStorage) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth header for login/register endpoints
        val urlString = originalRequest.url.toString()
        if (urlString.contains("/auth/login") || urlString.contains("/auth/register")) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking { tokenStorage.getAccessToken() }
        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNull_or_empty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
}
