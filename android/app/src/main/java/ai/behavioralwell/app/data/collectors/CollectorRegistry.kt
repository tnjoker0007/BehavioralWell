package ai.behavioralwell.app.data.collectors

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object CollectorRegistry {

    private var motionCollector: MotionCollector? = null
    private var usageCollector: DeviceUsageCollector? = null
    private var keyboardCollector: KeyboardMetadataCollector? = null
    private var mobilityCollector: MobilityCollector? = null
    private var activityCollector: ActivityCollector? = null

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    @Synchronized
    fun getMotionCollector(context: Context): MotionCollector {
        if (motionCollector == null) {
            motionCollector = MotionCollector(context.applicationContext)
        }
        return motionCollector!!
    }

    @Synchronized
    fun getUsageCollector(context: Context): DeviceUsageCollector {
        if (usageCollector == null) {
            usageCollector = DeviceUsageCollector(context.applicationContext)
        }
        return usageCollector!!
    }

    @Synchronized
    fun getKeyboardCollector(context: Context): KeyboardMetadataCollector {
        if (keyboardCollector == null) {
            keyboardCollector = KeyboardMetadataCollector(context.applicationContext)
        }
        return keyboardCollector!!
    }

    @Synchronized
    fun getMobilityCollector(context: Context): MobilityCollector {
        if (mobilityCollector == null) {
            mobilityCollector = MobilityCollector(context.applicationContext)
        }
        return mobilityCollector!!
    }

    @Synchronized
    fun getActivityCollector(context: Context): ActivityCollector {
        if (activityCollector == null) {
            activityCollector = ActivityCollector(context.applicationContext)
        }
        return activityCollector!!
    }

    /**
     * Reads current derived feature snapshots directly from single shared collector instances.
     * Injects canonical snapshotId, timestamp, and REAL_DEVICE provenance metadata.
     */
    fun collectDerivedSnapshot(context: Context): Map<String, Any> {
        val snapshotId = "telemetry-${System.currentTimeMillis()}"
        val timestamp = dateFormat.format(Date())

        val motion = getMotionCollector(context).currentSnapshot()
        val usage = getUsageCollector(context).currentSnapshot()
        val keyboard = getKeyboardCollector(context).currentSnapshot()
        val mobility = getMobilityCollector(context).currentSnapshot()
        val activity = getActivityCollector(context).currentSnapshot()

        val payload = mutableMapOf<String, Any>()
        payload["snapshotId"] = snapshotId
        payload["timestamp"] = timestamp
        payload["source"] = "REAL_DEVICE"
        payload["provenance"] = "REAL_DEVICE"

        if (motion != null) payload["motion"] = motion
        if (usage != null) payload["usage"] = usage
        if (keyboard != null) payload["keyboard"] = keyboard
        if (mobility != null) payload["mobility"] = mobility
        if (activity != null) payload["activity"] = activity

        Log.d(
            "COLLECTOR_SNAPSHOT",
            "[COLLECTOR SNAPSHOT] snapshotId=$snapshotId motion=${motion?.get("isStationary") ?: "N/A"} " +
                    "usage=${usage?.get("status") ?: "UNAVAILABLE"} " +
                    "keyboard=${keyboard?.get("status") ?: "UNAVAILABLE"} " +
                    "activity=${activity?.get("status") ?: "UNAVAILABLE"} " +
                    "mobility=${mobility?.get("status") ?: "UNAVAILABLE"}"
        )

        return payload
    }
}
