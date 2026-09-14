package ai.behavioralwell.app.data.collectors

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.util.Log
import ai.behavioralwell.app.core.permissions.SensorPermissionManager
import ai.behavioralwell.app.core.sensors.SensorCollector
import ai.behavioralwell.app.core.sensors.SensorType
import ai.behavioralwell.app.data.models.TelemetryInput
import java.util.Calendar
import java.util.TimeZone

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

        // 1. Precise local device midnight bounds (Device Local Timezone)
        val calendar = Calendar.getInstance(TimeZone.getDefault()).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        var unlockCount = 0
        var appSwitchCount = 0
        var totalForegroundMs = 0L
        var nightForegroundMs = 0L

        // Keyguard state machine for exact unlock count tracking:
        // Keyguard starts locked. Only genuine keyguard dismissal events increment count once per session.
        var isKeyguardLocked = true

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
                val pkg = event.packageName ?: ""
                val timestamp = event.timeStamp

                // ----------------------------------------------------
                // A. UNLOCK COUNT STATE MACHINE
                // 17 = KEYGUARD_DISMISSED, 18 = KEYGUARD_GOING_AWAY, 23 = USER_PRESENT
                // 14 = KEYGUARD_SHOWN, 15 = SCREEN_INTERACTIVE, 16 = SCREEN_NON_INTERACTIVE
                // ----------------------------------------------------
                if (type in listOf(14, 15, 16, 17, 18, 23)) {
                    Log.d("UsageEventsDebug", "Type=$type, Pkg=$pkg, Time=$timestamp, isLocked=$isKeyguardLocked, unlocks=$unlockCount")
                }

                if (type == 17 || type == 18 || type == 23) {
                    if (isKeyguardLocked) {
                        unlockCount++
                        isKeyguardLocked = false
                        Log.d("UsageEventsDebug", "-> UNLOCK INCREMENTED! New count: $unlockCount")
                    }
                } else if (type == 14 || type == 16) {
                    isKeyguardLocked = true
                }

                // ----------------------------------------------------
                // B. SCREEN TIME & SESSION TRACKING
                // ----------------------------------------------------
                if (type == 16) { // Screen turned off
                    if (activeApp != null && activeAppStartMs > 0L) {
                        val sessionStart = maxOf(activeAppStartMs, startTime)
                        val sessionEnd = minOf(timestamp, endTime)
                        val duration = sessionEnd - sessionStart
                        if (duration in 100..(1000L * 60 * 60 * 12)) {
                            totalForegroundMs += duration
                            val evCal = Calendar.getInstance(TimeZone.getDefault()).apply { timeInMillis = sessionEnd }
                            val hr = evCal.get(Calendar.HOUR_OF_DAY)
                            if (hr >= 23 || hr < 6) nightForegroundMs += duration
                        }
                    }
                    activeApp = null
                    activeAppStartMs = 0L

                } else if (type == 1 || type == UsageEvents.Event.ACTIVITY_RESUMED) {
                    if (activeApp != null && activeAppStartMs > 0L) {
                        val sessionStart = maxOf(activeAppStartMs, startTime)
                        val sessionEnd = minOf(timestamp, endTime)
                        val duration = sessionEnd - sessionStart
                        if (duration in 100..(1000L * 60 * 60 * 12)) {
                            totalForegroundMs += duration
                            val evCal = Calendar.getInstance(TimeZone.getDefault()).apply { timeInMillis = sessionEnd }
                            val hr = evCal.get(Calendar.HOUR_OF_DAY)
                            if (hr >= 23 || hr < 6) nightForegroundMs += duration
                        }
                        if (activeApp != pkg && !isSystemUI(pkg)) {
                            appSwitchCount++
                        }
                    }

                    if (isSystemUI(pkg)) {
                        activeApp = null
                        activeAppStartMs = 0L
                    } else {
                        activeApp = pkg
                        activeAppStartMs = timestamp
                    }

                } else if (type == 2 || type == UsageEvents.Event.ACTIVITY_PAUSED) {
                    if (pkg == activeApp && activeAppStartMs > 0L) {
                        val sessionStart = maxOf(activeAppStartMs, startTime)
                        val sessionEnd = minOf(timestamp, endTime)
                        val duration = sessionEnd - sessionStart
                        if (duration in 100..(1000L * 60 * 60 * 12)) {
                            totalForegroundMs += duration
                            val evCal = Calendar.getInstance(TimeZone.getDefault()).apply { timeInMillis = sessionEnd }
                            val hr = evCal.get(Calendar.HOUR_OF_DAY)
                            if (hr >= 23 || hr < 6) nightForegroundMs += duration
                        }
                        activeApp = null
                        activeAppStartMs = 0L
                    }
                }
            }

            // Account for ongoing active session up to current time
            if (activeApp != null && activeAppStartMs > 0L && !isKeyguardLocked) {
                val sessionStart = maxOf(activeAppStartMs, startTime)
                val sessionEnd = minOf(endTime, System.currentTimeMillis())
                val duration = (sessionEnd - sessionStart).coerceAtLeast(0L)
                if (duration in 100..(1000L * 60 * 60 * 12)) {
                    totalForegroundMs += duration
                }
            }
        }

        // 2. Diagnostic logging for system UsageStats vs event-derived calculation
        try {
            val statsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            if (!statsList.isNullOrEmpty()) {
                var statsSumMs = 0L
                for (stats in statsList) {
                    if (!isSystemUI(stats.packageName) && stats.totalTimeInForeground > 0) {
                        statsSumMs += stats.totalTimeInForeground
                    }
                }
                Log.d("DeviceUsageCollector", "Screen time comparison: Event-derived=${totalForegroundMs}ms, System queryUsageStats=${statsSumMs}ms")
            }
        } catch (e: Exception) {
            Log.w("DeviceUsageCollector", "Failed to query system UsageStats", e)
        }

        // Clip total foreground time to physically possible elapsed time since midnight today
        val maxPossibleTodayMs = (endTime - startTime).coerceAtLeast(0L)
        val boundedForegroundMs = totalForegroundMs.coerceAtMost(maxPossibleTodayMs)

        val screenTimeHours = boundedForegroundMs / (1000.0f * 60.0f * 60.0f)
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
        if (packageName.isBlank()) return true
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
                lower.contains("nexuslauncher") ||
                lower.contains("oplus.launcher") ||
                lower.contains("coloros.launcher")
    }
}


