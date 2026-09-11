package ai.behavioralwell.app.core.config

import android.content.Context
import android.content.SharedPreferences
import ai.behavioralwell.app.BuildConfig

object ApiConfig {

    private const val PREFS_NAME = "behavioralwell_config"
    private const val KEY_BASE_URL = "base_url"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var baseUrl: String
        get() {
            var rawUrl = if (::prefs.isInitialized) {
                prefs.getString(KEY_BASE_URL, BuildConfig.DEFAULT_API_BASE_URL) ?: BuildConfig.DEFAULT_API_BASE_URL
            } else {
                BuildConfig.DEFAULT_API_BASE_URL
            }
            if (rawUrl.contains("127.0.0.1") || rawUrl.contains("localhost") || rawUrl.contains("10.0.2.2")) {
                rawUrl = rawUrl.replace("127.0.0.1", "192.168.1.55")
                    .replace("localhost", "192.168.1.55")
                    .replace("10.0.2.2", "192.168.1.55")
            }
            return if (!rawUrl.endsWith("/")) "$rawUrl/" else rawUrl
        }
        set(value) {
            if (::prefs.isInitialized) {
                var formattedUrl = if (!value.endsWith("/")) "$value/" else value
                prefs.edit().putString(KEY_BASE_URL, formattedUrl).apply()
            }
        }



    fun resetToDefault() {
        if (::prefs.isInitialized) {
            prefs.edit().remove(KEY_BASE_URL).apply()
        }
    }
}
