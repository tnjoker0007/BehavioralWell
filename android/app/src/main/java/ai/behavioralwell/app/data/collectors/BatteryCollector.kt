package ai.behavioralwell.app.data.collectors

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import ai.behavioralwell.app.core.sensors.SensorCollector
import ai.behavioralwell.app.core.sensors.SensorType
import ai.behavioralwell.app.data.models.TelemetryInput

class BatteryCollector(
    private val context: Context,
    var consentGranted: Boolean = true
) : SensorCollector {

    override val sensorType: SensorType = SensorType.BATTERY_STATE

    override fun isHardwareAvailable(): Boolean = true
    override fun hasPermission(): Boolean = true
    override fun isConsentGranted(): Boolean = consentGranted

    fun getBatteryLevel(): Float {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            (level / scale.toFloat()) * 100.0f
        } else {
            100.0f
        }
    }

    override suspend fun collectTelemetry(): TelemetryInput? {
        if (!isConsentGranted()) return null
        return TelemetryInput()
    }
}
