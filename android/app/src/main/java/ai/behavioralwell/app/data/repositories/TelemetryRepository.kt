package ai.behavioralwell.app.data.repositories

import android.content.Context
import androidx.work.*
import ai.behavioralwell.app.core.database.AppDatabase
import ai.behavioralwell.app.core.network.RetrofitClient
import ai.behavioralwell.app.data.database.TelemetryEntity
import ai.behavioralwell.app.data.models.DashboardResponse
import ai.behavioralwell.app.data.models.RiskAssessmentResponse
import ai.behavioralwell.app.data.models.TelemetryInput
import ai.behavioralwell.app.data.sync.TelemetryBatchSyncWorker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TelemetryRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val telemetryDao = db.telemetryDao()
    private val api = RetrofitClient.apiService

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    suspend fun enqueueTelemetry(input: TelemetryInput) {
        val nowStr = dateFormat.format(Date())
        val entity = TelemetryEntity(
            timestamp = input.timestamp ?: nowStr,
            typingSpeed = input.typingSpeed,
            keyPressDuration = input.keyPressDuration,
            pauseDuration = input.pauseDuration,
            correctionRate = input.correctionRate,
            screenTime = input.screenTime,
            unlockCount = input.unlockCount,
            nightUsage = input.nightUsage,
            appSwitchFrequency = input.appSwitchFrequency,
            movementIntensity = input.movementIntensity,
            accelerationVariance = input.accelerationVariance,
            stationaryDuration = input.stationaryDuration,
            taskAccuracy = input.taskAccuracy,
            taskCompletionTime = input.taskCompletionTime,
            taskErrorRate = input.taskErrorRate,
            speedVariance = input.speedVariance,
            routeVariability = input.routeVariability
        )

        telemetryDao.insertTelemetry(entity)
        scheduleOneTimeSync()
    }

    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<TelemetryBatchSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "telemetry_periodic_batch_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    fun scheduleOneTimeSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<TelemetryBatchSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "telemetry_one_time_sync",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    suspend fun getDashboardData(): NetworkResult<DashboardResponse> {
        return try {
            val response = api.getDashboard()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to load dashboard: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Network error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun getCurrentRisk(): NetworkResult<RiskAssessmentResponse> {
        return try {
            val response = api.getCurrentRisk()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to fetch risk score: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Network error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
