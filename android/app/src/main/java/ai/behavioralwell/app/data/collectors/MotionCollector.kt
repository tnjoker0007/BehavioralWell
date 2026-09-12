package ai.behavioralwell.app.data.collectors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import ai.behavioralwell.app.core.sensors.SensorCollector
import ai.behavioralwell.app.core.sensors.SensorType
import ai.behavioralwell.app.data.models.TelemetryInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.math.sqrt

class MotionCollector(
    private val context: Context,
    var consentGranted: Boolean = true
) : SensorCollector, SensorEventListener {

    override val sensorType: SensorType = SensorType.MOTION

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val accelerationBuffer = mutableListOf<Float>()
    private var stationaryMsAccumulated = 0L
    private var lastSampleTimeMs = 0L

    private val backgroundThread = HandlerThread("MotionSensorThread").apply { start() }
    private val backgroundHandler = Handler(backgroundThread.looper)

    init {
        registerListeners()
    }

    private fun registerListeners() {
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL, backgroundHandler)
        }
        gyroscope?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL, backgroundHandler)
        }
    }

    fun unregisterListeners() {
        sensorManager?.unregisterListener(this)
        try {
            backgroundThread.quitSafely()
        } catch (e: Exception) {
            // Ignored
        }
    }

    override fun isHardwareAvailable(): Boolean = accelerometer != null
    override fun hasPermission(): Boolean = true
    override fun isConsentGranted(): Boolean = consentGranted

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !consentGranted) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val rawMagnitude = sqrt(x.pow(2) + y.pow(2) + z.pow(2))
            val devMagnitude = kotlin.math.abs(rawMagnitude - SensorManager.GRAVITY_EARTH)

            synchronized(accelerationBuffer) {
                if (accelerationBuffer.size >= 100) {
                    accelerationBuffer.removeAt(0)
                }
                accelerationBuffer.add(devMagnitude)
            }

            val now = System.currentTimeMillis()
            if (devMagnitude < 0.25f) {
                if (lastSampleTimeMs > 0) {
                    val delta = now - lastSampleTimeMs
                    if (delta in 1..1000) {
                        stationaryMsAccumulated += delta
                    }
                }
            }
            lastSampleTimeMs = now
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override suspend fun collectTelemetry(): TelemetryInput? = withContext(Dispatchers.IO) {
        if (!isHardwareAvailable() || !hasPermission() || !isConsentGranted()) return@withContext null

        val (intensity, variance) = synchronized(accelerationBuffer) {
            if (accelerationBuffer.isEmpty()) return@synchronized Pair(0.0f, 0.0f)

            val mean = accelerationBuffer.average().toFloat()
            val varSum = accelerationBuffer.fold(0.0f) { acc, valNum -> acc + (valNum - mean).pow(2) }
            val varianceVal = varSum / accelerationBuffer.size

            Pair(mean, varianceVal)
        }

        val stationaryMins = (stationaryMsAccumulated / (1000.0f * 60.0f))

        TelemetryInput(
            movementIntensity = intensity,
            accelerationVariance = variance,
            stationaryDuration = stationaryMins
        )
    }

    fun currentSnapshot(): Map<String, Any>? {
        if (!isHardwareAvailable() || !hasPermission() || !isConsentGranted()) return null

        val (intensity, variance) = synchronized(accelerationBuffer) {
            if (accelerationBuffer.isEmpty()) return@synchronized Pair(0.0f, 0.0f)

            val mean = accelerationBuffer.average().toFloat()
            val varSum = accelerationBuffer.fold(0.0f) { acc, valNum -> acc + (valNum - mean).pow(2) }
            val varianceVal = varSum / accelerationBuffer.size

            Pair(mean, varianceVal)
        }

        val stationaryMins = (stationaryMsAccumulated / (1000.0f * 60.0f))

        return mapOf(
            "movementIntensity" to intensity,
            "accelerationVariance" to variance,
            "stationaryDuration" to stationaryMins
        )
    }
}



