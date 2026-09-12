package ai.behavioralwell.app.data.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import ai.behavioralwell.app.BuildConfig
import ai.behavioralwell.app.data.collectors.CollectorRegistry
import ai.behavioralwell.app.dev.LiveTelemetryDebugPublisher
import kotlinx.coroutines.*

class TelemetryForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false

    companion object {
        private const val TAG = "TelemetryService"
        private const val CHANNEL_ID = "behavioral_well_service_channel"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, TelemetryForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, TelemetryForegroundService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        if (!isRunning) {
            isRunning = true
            startTelemetryCollectionLoop()
        }

        return START_STICKY
    }

    private fun startTelemetryCollectionLoop() {
        serviceScope.launch {
            Log.d(TAG, "Background Telemetry Service Started — Continuous Protection Active")
            
            if (BuildConfig.DEBUG) {
                LiveTelemetryDebugPublisher.startStreaming(applicationContext, serviceScope)
            }

            while (isActive && isRunning) {
                try {
                    val snapshot = CollectorRegistry.collectDerivedSnapshot(applicationContext)
                    Log.d(TAG, "Background snapshot derived: timestamp=${snapshot["timestamp"]}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error in background telemetry loop: ${e.message}")
                }
                delay(3000L)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "BehavioralWell Protection Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Runs background digital phenotyping and behavioral risk monitoring."
                setSound(null, null)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BehavioralWell Active")
            .setContentText("Monitoring daily routines and digital wellness in background.")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceScope.cancel()
        Log.d(TAG, "Background Telemetry Service Destroyed")
    }
}
