package ai.behavioralwell.app.data.collectors

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
        if (!isHardwareAvailable() || !hasPermission() || !isConsentGranted()) return null

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null

        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        val stats: List<UsageStats> = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: emptyList()

        var totalTimeForegroundMs = 0L
        var appLaunches = 0

        for (usage in stats) {
            if (usage.totalTimeInForeground > 0) {
                totalTimeForegroundMs += usage.totalTimeInForeground
                appLaunches += 1
            }
        }

        val screenTimeHours = totalTimeForegroundMs / (1000.0f * 60.0f * 60.0f)
        val switchFrequency = if (screenTimeHours > 0) appLaunches / screenTimeHours else 0.0f

        return TelemetryInput(
            screenTime = screenTimeHours,
            unlockCount = appLaunches,
            appSwitchFrequency = switchFrequency
        )
    }
}
