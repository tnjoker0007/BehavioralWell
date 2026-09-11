package ai.behavioralwell.app.data.collectors

import android.content.Context
import ai.behavioralwell.app.core.sensors.SensorCollector
import ai.behavioralwell.app.core.sensors.SensorType
import ai.behavioralwell.app.data.models.TelemetryInput

class MobilityCollector(
    private val context: Context,
    var consentGranted: Boolean = true
) : SensorCollector {

    override val sensorType: SensorType = SensorType.LOCATION_MOBILITY

    override fun isHardwareAvailable(): Boolean = true
    override fun hasPermission(): Boolean = true
    override fun isConsentGranted(): Boolean = consentGranted

    private var speedVarianceVal: Float? = null
    private var routeVariabilityVal: Float? = null

    fun recordMobility(speedVar: Float, routeVar: Float) {
        if (!consentGranted) return
        speedVarianceVal = speedVar
        routeVariabilityVal = routeVar
    }

    override suspend fun collectTelemetry(): TelemetryInput? {
        if (!isConsentGranted() || speedVarianceVal == null) return null

        return TelemetryInput(
            speedVariance = speedVarianceVal,
            routeVariability = routeVariabilityVal
        )
    }
}

