package ai.behavioralwell.app.data.collectors

import android.content.Context
import ai.behavioralwell.app.core.permissions.SensorPermissionManager
import ai.behavioralwell.app.core.sensors.SensorCollector
import ai.behavioralwell.app.core.sensors.SensorType
import ai.behavioralwell.app.data.models.TelemetryInput

class ActivityCollector(
    private val context: Context,
    var consentGranted: Boolean = true
) : SensorCollector {

    override val sensorType: SensorType = SensorType.ACTIVITY

    private val permissionManager = SensorPermissionManager(context)

    override fun isHardwareAvailable(): Boolean {
        return true
    }

    override fun hasPermission(): Boolean {
        return permissionManager.hasMotionPermission()
    }

    override fun isConsentGranted(): Boolean {
        return consentGranted
    }

    override suspend fun collectTelemetry(): TelemetryInput? {
        if (!isHardwareAvailable() || !hasPermission() || !isConsentGranted()) return null

        // Returns baseline motion telemetry state
        return TelemetryInput()
    }
}
