package ai.behavioralwell.app.data.collectors

import android.content.Context
import ai.behavioralwell.app.core.sensors.SensorCollector
import ai.behavioralwell.app.core.sensors.SensorType
import ai.behavioralwell.app.data.models.TelemetryInput
import java.util.Calendar

class ScreenCollector(
    private val context: Context,
    var consentGranted: Boolean = true
) : SensorCollector {

    override val sensorType: SensorType = SensorType.SCREEN_USAGE

    override fun isHardwareAvailable(): Boolean = true
    override fun hasPermission(): Boolean = true
    override fun isConsentGranted(): Boolean = consentGranted

    fun getNightUsageEstimate(): Float {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        // If current time is between 11PM (23:00) and 6AM (06:00), estimate active night usage hours
        return if (currentHour >= 23 || currentHour < 6) {
            0.8f
        } else {
            0.2f
        }
    }

    override suspend fun collectTelemetry(): TelemetryInput? {
        if (!isConsentGranted()) return null
        return TelemetryInput(nightUsage = getNightUsageEstimate())
    }
}
