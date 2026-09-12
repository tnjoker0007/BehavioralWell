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
            val rawUrl = if (::prefs.isInitialized) {
                prefs.getString(KEY_BASE_URL, null) ?: BuildConfig.DEFAULT_API_BASE_URL
            } else {
                BuildConfig.DEFAULT_API_BASE_URL
            }

            return if (!rawUrl.endsWith("/")) "$rawUrl/" else rawUrl
        }
        set(value) {
            if (::prefs.isInitialized) {
                val formattedUrl = if (!value.endsWith("/")) "$value/" else value
                prefs.edit().putString(KEY_BASE_URL, formattedUrl).apply()
            }
        }

    fun resetToDefault() {
        if (::prefs.isInitialized) {
            prefs.edit().remove(KEY_BASE_URL).apply()
        }
    }
}
