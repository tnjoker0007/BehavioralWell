package ai.behavioralwell.app.data.collectors

import android.content.Context
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
     * Zero second SensorManager registration, zero raw sensor array leakage.
     */
    fun collectDerivedSnapshot(context: Context): Map<String, Any> {
        val motion = getMotionCollector(context).currentSnapshot()
        val usage = getUsageCollector(context).currentSnapshot()
        val keyboard = getKeyboardCollector(context).currentSnapshot()
        val mobility = getMobilityCollector(context).currentSnapshot()
        val activity = getActivityCollector(context).currentSnapshot()

        val payload = mutableMapOf<String, Any>()
        payload["timestamp"] = dateFormat.format(Date())

        if (motion != null) payload["motion"] = motion
        if (usage != null) payload["usage"] = usage
        if (keyboard != null) payload["keyboard"] = keyboard
        if (mobility != null) payload["mobility"] = mobility
        if (activity != null) payload["activity"] = activity

        return payload
    }
}
