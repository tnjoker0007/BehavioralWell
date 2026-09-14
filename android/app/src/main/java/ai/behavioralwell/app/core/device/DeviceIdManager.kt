package ai.behavioralwell.app.core.device

import android.content.Context
import android.os.Build
import ai.behavioralwell.app.BuildConfig
import java.util.UUID

object DeviceIdManager {

    private const val PREFS_NAME = "behavioralwell_device_prefs"
    private const val KEY_DEVICE_ID = "device_id"

    @Volatile
    private var cachedDeviceId: String? = null

    /**
     * Retrieves or generates a persistent UUID for this app installation.
     * Guaranteed to persist across app launches.
     */
    fun getDeviceId(context: Context): String {
        cachedDeviceId?.let { return it }

        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)

        if (deviceId.isNullOrBlank()) {
            val newId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, newId).apply()
            deviceId = newId
        }

        val finalId = deviceId!!
        cachedDeviceId = finalId
        return finalId
    }

    fun getDeviceModel(): String = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

    fun getAndroidVersion(): String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

    fun getAppVersion(): String = BuildConfig.VERSION_NAME

    fun getDeviceDetails(context: Context): Map<String, String> {
        return mapOf(
            "deviceId" to getDeviceId(context),
            "deviceModel" to getDeviceModel(),
            "androidVersion" to getAndroidVersion(),
            "appVersion" to getAppVersion()
        )
    }
}
