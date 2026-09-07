package ai.behavioralwell.app.data.collectors

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import ai.behavioralwell.app.core.sensors.SensorCollector
import ai.behavioralwell.app.core.sensors.SensorType
import ai.behavioralwell.app.data.models.TelemetryInput

class DeviceStateCollector(
    private val context: Context,
    var consentGranted: Boolean = true
) : SensorCollector {

    override val sensorType: SensorType = SensorType.DEVICE_STATE

    override fun isHardwareAvailable(): Boolean = true
    override fun hasPermission(): Boolean = true
    override fun isConsentGranted(): Boolean = consentGranted

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override suspend fun collectTelemetry(): TelemetryInput? {
        if (!isConsentGranted()) return null
        return TelemetryInput()
    }
}
