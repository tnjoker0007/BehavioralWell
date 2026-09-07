package ai.behavioralwell.app.data.collectors

import android.content.Context
import ai.behavioralwell.app.core.permissions.SensorPermissionManager
import ai.behavioralwell.app.core.sensors.SensorCollector
import ai.behavioralwell.app.core.sensors.SensorType
import ai.behavioralwell.app.data.models.TelemetryInput

class LocationCollector(
    private val context: Context,
    var consentGranted: Boolean = true
) : SensorCollector {

    override val sensorType: SensorType = SensorType.LOCATION_MOBILITY

    private val permissionManager = SensorPermissionManager(context)

    override fun isHardwareAvailable(): Boolean = true

    override fun hasPermission(): Boolean {
        return permissionManager.hasLocationPermission()
    }

    override fun isConsentGranted(): Boolean = consentGranted

    override suspend fun collectTelemetry(): TelemetryInput? {
        if (!isHardwareAvailable() || !hasPermission() || !isConsentGranted()) return null

        // Location collector only processes derived mobility features
        return TelemetryInput()
    }
}
