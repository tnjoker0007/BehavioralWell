package ai.behavioralwell.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "telemetry_queue")
data class TelemetryEntity(
    @PrimaryKey val idempotencyKey: String = "and_evt_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}",
    val timestamp: String,
    val typingSpeed: Float? = null,
    val keyPressDuration: Float? = null,
    val pauseDuration: Float? = null,
    val correctionRate: Float? = null,
    val screenTime: Float? = null,
    val unlockCount: Int? = null,
    val nightUsage: Float? = null,
    val appSwitchFrequency: Float? = null,
    val movementIntensity: Float? = null,
    val accelerationVariance: Float? = null,
    val stationaryDuration: Float? = null,
    val taskAccuracy: Float? = null,
    val taskCompletionTime: Float? = null,
    val taskErrorRate: Float? = null,
    val speedVariance: Float? = null,
    val routeVariability: Float? = null,
    val isUploaded: Boolean = false
)
