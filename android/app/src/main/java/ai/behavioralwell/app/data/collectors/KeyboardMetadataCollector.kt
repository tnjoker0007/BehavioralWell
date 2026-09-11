package ai.behavioralwell.app.data.collectors

import android.content.Context
import ai.behavioralwell.app.core.sensors.SensorCollector
import ai.behavioralwell.app.core.sensors.SensorType
import ai.behavioralwell.app.data.models.TelemetryInput

class KeyboardMetadataCollector(
    private val context: Context,
    var consentGranted: Boolean = true
) : SensorCollector {

    override val sensorType: SensorType = SensorType.KEYBOARD_DYNAMICS

    override fun isHardwareAvailable(): Boolean = true
    override fun hasPermission(): Boolean = true
    override fun isConsentGranted(): Boolean = consentGranted

    // Local timing metrics accumulators (Zero text stored)
    private var totalKeyPresses = 0
    private var totalBackspaceEvents = 0
    private var totalDwellTimeMs = 0L
    private var totalPauseTimeMs = 0L

    fun recordKeyPressTiming(dwellTimeMs: Long, interKeyPauseMs: Long, isBackspace: Boolean = false) {
        if (!consentGranted) return

        totalKeyPresses += 1
        if (isBackspace) {
            totalBackspaceEvents += 1
        }
        totalDwellTimeMs += dwellTimeMs
        totalPauseTimeMs += interKeyPauseMs
    }

    override suspend fun collectTelemetry(): TelemetryInput? {
        if (!isConsentGranted()) return null

        if (totalKeyPresses == 0) {
            return null
        }

        val avgDwellMs = totalDwellTimeMs.toFloat() / totalKeyPresses
        val avgPauseSec = (totalPauseTimeMs.toFloat() / totalKeyPresses) / 1000.0f
        val backspaceRatio = totalBackspaceEvents.toFloat() / totalKeyPresses

        // Approximate WPM calculation based on keypress frequency (assuming avg 5 chars per word)
        val estimatedWpm = (totalKeyPresses / 5.0f)

        return TelemetryInput(
            typingSpeed = estimatedWpm,
            keyPressDuration = avgDwellMs,
            pauseDuration = avgPauseSec,
            correctionRate = backspaceRatio
        )
    }
}

