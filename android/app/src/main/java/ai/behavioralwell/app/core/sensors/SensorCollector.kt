package ai.behavioralwell.app.core.sensors

import ai.behavioralwell.app.data.models.TelemetryInput

interface SensorCollector {
    val sensorType: SensorType

    fun isHardwareAvailable(): Boolean
    fun hasPermission(): Boolean
    fun isConsentGranted(): Boolean

    fun getStatus(): SensorStatus {
        val available = isHardwareAvailable()
        val permission = hasPermission()
        val consent = isConsentGranted()

        val state = when {
            !available -> SensorStatusState.UNAVAILABLE
            !permission -> SensorStatusState.PERMISSION_REQUIRED
            !consent -> SensorStatusState.DISABLED
            else -> SensorStatusState.ACTIVE
        }

        return SensorStatus(
            type = sensorType,
            state = state,
            hardwareAvailable = available,
            permissionGranted = permission,
            userConsentGranted = consent
        )
    }

    suspend fun collectTelemetry(): TelemetryInput?
}
