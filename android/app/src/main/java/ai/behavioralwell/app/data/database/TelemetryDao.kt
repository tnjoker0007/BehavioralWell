package ai.behavioralwell.app.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTelemetry(telemetry: TelemetryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(telemetryList: List<TelemetryEntity>)

    @Query("SELECT * FROM telemetry_queue WHERE isUploaded = 0 ORDER BY timestamp ASC LIMIT 50")
    suspend fun getPendingTelemetry(): List<TelemetryEntity>

    @Query("SELECT COUNT(*) FROM telemetry_queue WHERE isUploaded = 0")
    suspend fun getPendingTelemetryCount(): Int

    @Query("UPDATE telemetry_queue SET isUploaded = 1 WHERE idempotencyKey IN (:keys)")
    suspend fun markAsUploaded(keys: List<String>)

    @Query("SELECT COUNT(*) FROM telemetry_queue")
    fun getTotalTelemetryCount(): Flow<Int>

    @Query("DELETE FROM telemetry_queue WHERE isUploaded = 1")
    suspend fun clearUploadedTelemetry()
}
