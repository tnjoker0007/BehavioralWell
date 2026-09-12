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

    override fun isHardwareAvailable(): Boolean = true

    override fun hasPermission(): Boolean {
        return permissionManager.hasMotionPermission()
    }

    override fun isConsentGranted(): Boolean = consentGranted

    override suspend fun collectTelemetry(): TelemetryInput? {
        if (!isHardwareAvailable() || !hasPermission() || !isConsentGranted()) return null
        val motionCollector = CollectorRegistry.getMotionCollector(context)
        return motionCollector.collectTelemetry()
    }

    fun currentSnapshot(): Map<String, Any>? {
        if (!isHardwareAvailable() || !hasPermission() || !isConsentGranted()) return null

        val motionCollector = CollectorRegistry.getMotionCollector(context)
        val motionSnapshot = motionCollector.currentSnapshot()
            ?: return mapOf("status" to "WAITING_FOR_MOTION")

        val intensity = (motionSnapshot["movementIntensity"] as? Number)?.toFloat() ?: 0.0f
        val variance = (motionSnapshot["accelerationVariance"] as? Number)?.toFloat() ?: 0.0f

        val activityState = when {
            intensity < 0.25f -> "Stationary"
            intensity < 1.50f -> "Walking"
            else -> "Active Movement"
        }

        return mapOf(
            "status" to "LIVE",
            "movementIntensity" to intensity,
            "accelerationVariance" to variance,
            "activityState" to activityState
        )
    }
}
