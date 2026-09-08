package ai.behavioralwell.app.data.repositories

import android.content.Context
import androidx.work.*
import ai.behavioralwell.app.core.database.AppDatabase
import ai.behavioralwell.app.core.network.RetrofitClient
import ai.behavioralwell.app.data.database.TelemetryEntity
import ai.behavioralwell.app.data.models.*
import ai.behavioralwell.app.data.sync.TelemetryBatchSyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TelemetryRepository(private val context: Context) {

    private val db by lazy { AppDatabase.getInstance(context) }
    private val telemetryDao by lazy { db.telemetryDao() }
    private val api = RetrofitClient.apiService

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val consentDao by lazy { db.consentDao() }

    val pendingTelemetryCount = telemetryDao.getTotalTelemetryCount()

    suspend fun collectAndEnqueueAll() = withContext(Dispatchers.IO) {
        val usageCollector = ai.behavioralwell.app.data.collectors.DeviceUsageCollector(context)
        val motionCollector = ai.behavioralwell.app.data.collectors.MotionCollector(context)
        val keyboardCollector = ai.behavioralwell.app.data.collectors.KeyboardMetadataCollector(context)
        val mobilityCollector = ai.behavioralwell.app.data.collectors.MobilityCollector(context)
        val activityCollector = ai.behavioralwell.app.data.collectors.ActivityCollector(context)

        val usageInput = usageCollector.collectTelemetry()
        val motionInput = motionCollector.collectTelemetry()
        val keyboardInput = keyboardCollector.collectTelemetry()
        val mobilityInput = mobilityCollector.collectTelemetry()
        val activityInput = activityCollector.collectTelemetry()

        val mergedInput = TelemetryInput(
            screenTime = usageInput?.screenTime,
            unlockCount = usageInput?.unlockCount,
            nightUsage = usageInput?.nightUsage,
            appSwitchFrequency = usageInput?.appSwitchFrequency,
            movementIntensity = motionInput?.movementIntensity ?: activityInput?.movementIntensity,
            accelerationVariance = motionInput?.accelerationVariance,
            stationaryDuration = motionInput?.stationaryDuration ?: activityInput?.stationaryDuration,
            typingSpeed = keyboardInput?.typingSpeed,
            keyPressDuration = keyboardInput?.keyPressDuration,
            pauseDuration = keyboardInput?.pauseDuration,
            correctionRate = keyboardInput?.correctionRate,
            speedVariance = mobilityInput?.speedVariance,
            routeVariability = mobilityInput?.routeVariability
        )

        if (hasTelemetryContent(mergedInput)) {
            enqueueTelemetry(mergedInput)
        }
    }

    private fun hasTelemetryContent(input: TelemetryInput): Boolean {
        return input.screenTime != null || input.unlockCount != null ||
                input.movementIntensity != null || input.accelerationVariance != null ||
                input.typingSpeed != null || input.taskAccuracy != null ||
                input.speedVariance != null
    }

    suspend fun updateConsent(
        keyboard: Boolean,
        usage: Boolean,
        motion: Boolean,
        work: Boolean,
        mobility: Boolean
    ): NetworkResult<ConsentResponse> = withContext(Dispatchers.IO) {
        val consentEntity = ai.behavioralwell.app.data.database.ConsentEntity(
            userId = "current_user",
            keyboardEnabled = keyboard,
            usageEnabled = usage,
            motionEnabled = motion,
            workEnabled = work,
            mobilityEnabled = mobility
        )
        consentDao.saveConsent(consentEntity)

        try {
            val response = api.updateConsent(
                ConsentUpdateRequest(
                    keyboardEnabled = keyboard,
                    usageEnabled = usage,
                    motionEnabled = motion,
                    workEnabled = work,
                    mobilityEnabled = mobility
                )
            )
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Consent update failed: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun enqueueTelemetry(input: TelemetryInput) = withContext(Dispatchers.IO) {
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

    suspend fun getDashboardData(): NetworkResult<DashboardResponse> = withContext(Dispatchers.IO) {
        try {
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

    suspend fun getCurrentRisk(): NetworkResult<RiskAssessmentResponse> = withContext(Dispatchers.IO) {
        try {
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

    suspend fun startIntervention(activityType: String): NetworkResult<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            val response = api.startIntervention(InterventionStartInput(activityType = activityType))
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to start intervention: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Network error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun completeIntervention(sessionId: Int?, feedbackScore: Int?, metrics: Map<String, Any>?): NetworkResult<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            val response = api.completeIntervention(InterventionCompleteInput(sessionId = sessionId, feedbackScore = feedbackScore, resultMetrics = metrics))
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to complete intervention: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Network error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun submitSelfCheck(mood: String, stressLevel: Int, note: String?): NetworkResult<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            val response = api.submitSelfCheck(SelfReportInput(mood = mood, stressLevel = stressLevel, note = note))
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to submit self-check: ${response.code()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Network error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
