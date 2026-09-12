package ai.behavioralwell.app.data.collectors

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
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

    private val defaultLauncherPackage: String? by lazy {
        try {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            context.packageManager.resolveActivity(intent, 0)?.activityInfo?.packageName
        } catch (e: Exception) {
            null
        }
    }

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

        // 1. Precise local midnight bounds
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        var unlockCount = 0
        var appSwitchCount = 0
        var totalForegroundMs = 0L
        var nightForegroundMs = 0L

        val usageEvents = try {
            usageStatsManager.queryEvents(startTime, endTime)
        } catch (e: Exception) {
            null
        }

        if (usageEvents != null) {
            val event = UsageEvents.Event()
            var activeApp: String? = null
            var activeAppStartMs = 0L

            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                val type = event.eventType
                val pkg = event.packageName
                val timestamp = event.timeStamp

                // Unlock count (Event type 17: KEYGUARD_DISMISSED)
                if (type == 17) {
                    unlockCount++
                }

                // Screen turned off (Event type 16: SCREEN_NON_INTERACTIVE)
                if (type == 16) {
                    if (activeApp != null && activeAppStartMs > 0L) {
                        val duration = timestamp - activeAppStartMs
                        if (duration in 100..(1000L * 60 * 60 * 6)) {
                            totalForegroundMs += duration
                            val evCal = Calendar.getInstance().apply { timeInMillis = timestamp }
                            val hr = evCal.get(Calendar.HOUR_OF_DAY)
                            if (hr >= 23 || hr < 6) nightForegroundMs += duration
                        }
                    }
                    activeApp = null
                    activeAppStartMs = 0L
                } else if (type == 1 || type == UsageEvents.Event.ACTIVITY_RESUMED) {
                    if (!isSystemUI(pkg)) {
                        if (activeApp != null && activeAppStartMs > 0L) {
                            val duration = timestamp - activeAppStartMs
                            if (duration in 100..(1000L * 60 * 60 * 6)) {
                                totalForegroundMs += duration
                                val evCal = Calendar.getInstance().apply { timeInMillis = timestamp }
                                val hr = evCal.get(Calendar.HOUR_OF_DAY)
                                if (hr >= 23 || hr < 6) nightForegroundMs += duration
                            }
                            if (activeApp != pkg) {
                                appSwitchCount++
                            }
                        }
                        activeApp = pkg
                        activeAppStartMs = timestamp
                    }
                } else if (type == 2 || type == UsageEvents.Event.ACTIVITY_PAUSED) {
                    if (pkg == activeApp && activeAppStartMs > 0L) {
                        val duration = timestamp - activeAppStartMs
                        if (duration in 100..(1000L * 60 * 60 * 6)) {
                            totalForegroundMs += duration
                            val evCal = Calendar.getInstance().apply { timeInMillis = timestamp }
                            val hr = evCal.get(Calendar.HOUR_OF_DAY)
                            if (hr >= 23 || hr < 6) nightForegroundMs += duration
                        }
                        activeApp = null
                        activeAppStartMs = 0L
                    }
                }
            }

            // Cap active interval to current time if screen currently on
            if (activeApp != null && activeAppStartMs > 0L) {
                val duration = (endTime - activeAppStartMs).coerceAtLeast(0L)
                if (duration in 100..(1000L * 60 * 60 * 6)) {
                    totalForegroundMs += duration
                }
            }
        }

        // Fallback for unlock count if KEYGUARD_DISMISSED is unsupported
        if (unlockCount == 0 && usageEvents != null) {
            val events2 = usageStatsManager.queryEvents(startTime, endTime)
            if (events2 != null) {
                val ev = UsageEvents.Event()
                var wasScreenOff = false
                while (events2.hasNextEvent()) {
                    events2.getNextEvent(ev)
                    if (ev.eventType == 16) {
                        wasScreenOff = true
                    } else if (ev.eventType == 15 && wasScreenOff) {
                        unlockCount++
                        wasScreenOff = false
                    }
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
        val lower = packageName.lowercase()
        return lower == defaultLauncherPackage?.lowercase() ||
                lower.contains("systemui") ||
                lower == "android" ||
                lower.contains("launcher") ||
                lower.contains("miui.home") ||
                lower.contains("miui.touchassistant") ||
                lower.contains("miui.guardprovider") ||
                lower.contains("miui.securitycenter") ||
                lower.contains("sec.android.app.launcher") ||
                lower.contains("nexuslauncher")
    }
}

