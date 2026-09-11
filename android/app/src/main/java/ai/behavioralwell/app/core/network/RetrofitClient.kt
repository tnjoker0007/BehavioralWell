package ai.behavioralwell.app.core.network

import android.util.Log
import ai.behavioralwell.app.BuildConfig
import ai.behavioralwell.app.BehavioralWellApplication
import ai.behavioralwell.app.core.config.ApiConfig
import ai.behavioralwell.app.data.api.BehavioralWellApiService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor { message ->
            if (BuildConfig.DEBUG) {
                // Sanitize sensitive tokens, credentials, and payloads from Logcat
                val sanitized = message
                    .replace(Regex("(?i)authorization:\\s*bearer\\s+[^\\s]+"), "Authorization: Bearer [REDACTED]")
                    .replace(Regex("(?i)\"access_token\"\\s*:\\s*\"[^\"]+\""), "\"access_token\": \"[REDACTED]\"")
                    .replace(Regex("(?i)\"refresh_token\"\\s*:\\s*\"[^\"]+\""), "\"refresh_token\": \"[REDACTED]\"")
                    .replace(Regex("(?i)\"password\"\\s*:\\s*\"[^\"]+\""), "\"password\": \"[REDACTED]\"")
                    .replace(Regex("(?i)\"note\"\\s*:\\s*\"[^\"]+\""), "\"note\": \"[REDACTED]\"")
                Log.d("BehavioralWellApi", sanitized)
            }
        }.apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            redactHeader("Authorization")
            redactHeader("Cookie")
            redactHeader("Set-Cookie")
        }

    }

    val okHttpClient: OkHttpClient by lazy {
        val app = BehavioralWellApplication.instance
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(app.tokenStorage))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        .create()

    val apiService: BehavioralWellApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(BehavioralWellApiService::class.java)
    }
}
