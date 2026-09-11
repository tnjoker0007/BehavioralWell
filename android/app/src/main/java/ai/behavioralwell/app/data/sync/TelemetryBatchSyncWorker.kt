package ai.behavioralwell.app.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ai.behavioralwell.app.core.database.AppDatabase
import ai.behavioralwell.app.core.network.RetrofitClient
import ai.behavioralwell.app.data.models.BatchTelemetryInput
import ai.behavioralwell.app.data.models.TelemetryInput

class TelemetryBatchSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("BehavioralWellTelemetry", "TelemetryBatchSyncWorker started")
        val database = AppDatabase.getInstance(applicationContext)
        val telemetryDao = database.telemetryDao()
        val pendingRecords = telemetryDao.getPendingTelemetry()
        val pendingCount = pendingRecords.size
        Log.d("BehavioralWellTelemetry", "Room pending count: $pendingCount")

        if (pendingRecords.isEmpty()) {
            return Result.success()
        }

        val telemetryInputs = pendingRecords.map { entity ->
            TelemetryInput(
                idempotencyKey = entity.idempotencyKey,
                timestamp = entity.timestamp,
                typingSpeed = entity.typingSpeed,
                keyPressDuration = entity.keyPressDuration,
                pauseDuration = entity.pauseDuration,
                correctionRate = entity.correctionRate,
                screenTime = entity.screenTime,
                unlockCount = entity.unlockCount,
                nightUsage = entity.nightUsage,
                appSwitchFrequency = entity.appSwitchFrequency,
                movementIntensity = entity.movementIntensity,
                accelerationVariance = entity.accelerationVariance,
                stationaryDuration = entity.stationaryDuration,
                taskAccuracy = entity.taskAccuracy,
                taskCompletionTime = entity.taskCompletionTime,
                taskErrorRate = entity.taskErrorRate,
                speedVariance = entity.speedVariance,
                routeVariability = entity.routeVariability
            )
        }

        return try {
            Log.d("BehavioralWellTelemetry", "Upload attempted for batch of ${telemetryInputs.size} records")
            val response = RetrofitClient.apiService.sendBatchTelemetry(
                BatchTelemetryInput(batch = telemetryInputs)
            )
            val httpStatus = response.code()
            Log.d("BehavioralWellTelemetry", "HTTP status: $httpStatus, isSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val uploadedKeys = pendingRecords.map { it.idempotencyKey }
                telemetryDao.markAsUploaded(uploadedKeys)
                val remainingCount = telemetryDao.getPendingTelemetryCount()
                Log.d("BehavioralWellTelemetry", "Uploaded count: ${uploadedKeys.size}, remaining count: $remainingCount")
                Result.success()
            } else {
                Log.d("BehavioralWellTelemetry", "Upload failed with HTTP status $httpStatus, retaining records")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("BehavioralWellTelemetry", "Upload exception: ${e.localizedMessage}", e)
            Result.retry()
        }
    }
}
