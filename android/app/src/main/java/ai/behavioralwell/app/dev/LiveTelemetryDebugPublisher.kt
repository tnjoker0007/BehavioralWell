package ai.behavioralwell.app.dev

import android.content.Context
import android.util.Log
import ai.behavioralwell.app.BuildConfig
import ai.behavioralwell.app.core.network.RetrofitClient
import ai.behavioralwell.app.data.collectors.CollectorRegistry
import kotlinx.coroutines.*

object LiveTelemetryDebugPublisher {

    private const val TAG = "LiveTelemetryPublisher"
    private var streamingJob: Job? = null
    @Volatile
    private var isStreaming = false

    @Synchronized
    fun startStreaming(context: Context, scope: CoroutineScope) {
        if (!BuildConfig.DEBUG) return

        if (isStreaming || streamingJob?.isActive == true) {
            Log.d(TAG, "Already streaming live telemetry. Ignoring duplicate start request.")
            return
        }

        isStreaming = true

        streamingJob = scope.launch(Dispatchers.IO) {
            Log.d(TAG, "[DEV TELEMETRY DEBUG] Live telemetry publisher loop started (~1Hz)")
            while (isActive && isStreaming) {
                try {
                    val snapshotPayload = CollectorRegistry.collectDerivedSnapshot(context)
                    if (snapshotPayload.isNotEmpty()) {
                        val response = RetrofitClient.apiService.sendDevTelemetry(snapshotPayload)
                        if (response.isSuccessful) {
                            Log.d(TAG, "[DEV TELEMETRY DEBUG] Published derived snapshot: timestamp=${snapshotPayload["timestamp"]}")
                        } else {
                            Log.e(TAG, "[DEV TELEMETRY DEBUG] Server error response code: ${response.code()}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[DEV TELEMETRY DEBUG] Network or publishing error: ${e.message}")
                }
                delay(1000L)
            }
            Log.d(TAG, "[DEV TELEMETRY DEBUG] Live telemetry publisher loop exited.")
        }
    }

    @Synchronized
    fun stopStreaming() {
        if (!BuildConfig.DEBUG) return
        isStreaming = false
        streamingJob?.cancel()
        streamingJob = null
        Log.d(TAG, "[DEV TELEMETRY DEBUG] Live telemetry publisher stopped.")
    }

    fun isCurrentlyStreaming(): Boolean = isStreaming && streamingJob?.isActive == true
}
