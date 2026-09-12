package ai.behavioralwell.app.dev

import android.content.Context
import android.util.Log
import ai.behavioralwell.app.BuildConfig
import ai.behavioralwell.app.core.config.ApiConfig
import ai.behavioralwell.app.data.collectors.CollectorRegistry
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object LiveTelemetryDebugPublisher {

    private const val TAG = "LiveTelemetryPublisher"
    private var streamingJob: Job? = null
    @Volatile
    private var isStreaming = false

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val devHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

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
                        val jsonBody = gson.toJson(snapshotPayload)
                        val requestBody = jsonBody.toRequestBody(jsonMediaType)

                        val url = "${ApiConfig.baseUrl}dev/telemetry"
                        val request = Request.Builder()
                            .url(url)
                            .header("Connection", "close")
                            .post(requestBody)
                            .build()

                        devHttpClient.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                Log.d(TAG, "[DEV TELEMETRY DEBUG] Published derived snapshot: timestamp=${snapshotPayload["timestamp"]}")
                            } else {
                                Log.e(TAG, "[DEV TELEMETRY DEBUG] Server error response code: ${response.code}")
                            }
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

