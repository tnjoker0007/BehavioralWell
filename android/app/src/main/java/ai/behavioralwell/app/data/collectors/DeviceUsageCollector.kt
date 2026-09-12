package ai.behavioralwell.app.data.collectors

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import ai.behavioralwell.app.core.permissions.SensorPermissionManager
import ai.behavioralwell.app.core.sensors.SensorCollector
import ai.behavioralwell.app.core.sensors.SensorType
import ai.behavioralwell.app.data.models.TelemetryInput
import java.util.Calendar

class DeviceUsageCollector(
    private val context: Context,
    var consentGranted: Boolean = true
) : SensorCollector {

    override val sensorType: SensorType = SensorType.APP_USAGE

    private val permissionManager = SensorPermissionManager(context)

    override fun isHardwareAvailable(): Boolean = true

    override fun hasPermission(): Boolean {
        return permissionManager.hasUsageAccessPermission()
    }

    override fun isConsentGranted(): Boolean = consentGranted

    override suspend fun collectTelemetry(): TelemetryInput? {
        val snapshot = currentSnapshot() ?: return null
        if (snapshot["status"] != "LIVE") return null

        val screenTime = (snapshot["screenTime"] as? Number)?.toFloat()
        val unlockCount = (snapshot["unlockCount"] as? Number)?.toInt()
        val nightUsage = (snapshot["nightUsage"] as? Number)?.toFloat()
        val appSwitchFrequency = (snapshot["appSwitchFrequency"] as? Number)?.toFloat()

        return TelemetryInput(
            screenTime = screenTime,
            unlockCount = unlockCount,
            nightUsage = nightUsage,
            appSwitchFrequency = appSwitchFrequency
        )
    }

    fun currentSnapshot(): Map<String, Any>? {
        if (!isHardwareAvailable() || !isConsentGranted()) return null

        if (!hasPermission()) {
            return mapOf(
                "status" to "USAGE_ACCESS_REQUIRED",
                "error" to "Usage Access Required"
            )
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return mapOf("status" to "USAGE_SERVICE_UNAVAILABLE")

        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        // Precision UsageEvents processing for exact user screen time
        val usageEvents = try {
            usageStatsManager.queryEvents(startTime, endTime)
        } catch (e: Exception) {
            null
        }

        var totalForegroundMs = 0L
        var nightForegroundMs = 0L
        var unlockCount = 0
        var appSwitchCount = 0

        if (usageEvents != null) {
            val event = UsageEvents.Event()
            var currentPackage: String? = null
            var currentStartTime: Long = 0L

            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)

                // eventType: 1=MOVE_TO_FOREGROUND/ACTIVITY_RESUMED, 2=MOVE_TO_BACKGROUND/ACTIVITY_PAUSED
                // 15=SCREEN_INTERACTIVE, 16=SCREEN_NON_INTERACTIVE, 17=KEYGUARD_DISMISSED
                val type = event.eventType
                if (type == 15 || type == 17) {
                    unlockCount++
                } else if (type == 1 || type == UsageEvents.Event.ACTIVITY_RESUMED) {
                    val pkg = event.packageName
                    if (!isSystemUI(pkg)) {
                        if (currentPackage != null && currentPackage != pkg) {
                            appSwitchCount++
                        }
                        if (currentStartTime > 0L) {
                            val duration = event.timeStamp - currentStartTime
                            if (duration in 1..(1000L * 60 * 60 * 6)) {
                                totalForegroundMs += duration
                                val eventCal = Calendar.getInstance().apply { timeInMillis = event.timeStamp }
                                val hr = eventCal.get(Calendar.HOUR_OF_DAY)
                                if (hr >= 23 || hr < 6) {
                                    nightForegroundMs += duration
                                }
                            }
                        }
                        currentPackage = pkg
                        currentStartTime = event.timeStamp
                    }
                } else if (type == 2 || type == UsageEvents.Event.ACTIVITY_PAUSED) {
                    if (currentStartTime > 0L) {
                        val duration = event.timeStamp - currentStartTime
                        if (duration in 1..(1000L * 60 * 60 * 6)) {
                            totalForegroundMs += duration
                            val eventCal = Calendar.getInstance().apply { timeInMillis = event.timeStamp }
                            val hr = eventCal.get(Calendar.HOUR_OF_DAY)
                            if (hr >= 23 || hr < 6) {
                                nightForegroundMs += duration
                            }
                        }
                    }
                    currentStartTime = 0L
                }
            }

            if (currentStartTime > 0L) {
                val duration = (endTime - currentStartTime).coerceAtLeast(0L)
                if (duration in 1..(1000L * 60 * 60 * 6)) {
                    totalForegroundMs += duration
                }
            }
        }

        // Secondary fallback filtering out launchers and system background UI
        if (totalForegroundMs == 0L) {
            val stats: List<UsageStats> = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            ) ?: emptyList()

            for (usage in stats) {
                val pkg = usage.packageName
                if (usage.totalTimeInForeground > 0 && !isSystemUI(pkg)) {
                    totalForegroundMs += usage.totalTimeInForeground
                    appSwitchCount++
                }
            }
        }

        val screenTimeHours = totalForegroundMs / (1000.0f * 60.0f * 60.0f)
        val nightUsageHours = nightForegroundMs / (1000.0f * 60.0f * 60.0f)
        val switchFrequency = if (screenTimeHours > 0) appSwitchCount / screenTimeHours else 0.0f

        return mapOf(
            "status" to "LIVE",
            "screenTime" to screenTimeHours,
            "unlockCount" to unlockCount,
            "nightUsage" to nightUsageHours,
            "appSwitchFrequency" to switchFrequency
        )
    }

    private fun isSystemUI(packageName: String): Boolean {
        return packageName.contains("systemui") ||
                packageName == "android" ||
                packageName.contains("launcher") ||
                packageName.contains("home") ||
                packageName.contains("system")
    }
}
