package ai.behavioralwell.app.data.collectors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import ai.behavioralwell.app.core.sensors.SensorCollector
import ai.behavioralwell.app.core.sensors.SensorType
import ai.behavioralwell.app.data.models.TelemetryInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

private data class MotionSample(
    val timestampMs: Long,
    val magnitude: Float
)

class MotionCollector(
    private val context: Context,
    var consentGranted: Boolean = true
) : SensorCollector, SensorEventListener {

    override val sensorType: SensorType = SensorType.MOTION

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val linearAccel = sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val samples = mutableListOf<MotionSample>()
    private val windowDurationMs = 3000L // 3-second sliding window

    private var gravityEst = SensorManager.GRAVITY_EARTH
    private var lastMovementTimeMs = System.currentTimeMillis()
    private var lastStationaryCheckTimeMs = System.currentTimeMillis()
    private var stationaryMsAccumulated = 0L

    private val backgroundThread = HandlerThread("MotionSensorThread").apply { start() }
    private val backgroundHandler = Handler(backgroundThread.looper)

    init {
        registerListeners()
    }

    private fun registerListeners() {
        linearAccel?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME, backgroundHandler)
        } ?: accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME, backgroundHandler)
        }

        gyroscope?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME, backgroundHandler)
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

    override fun isHardwareAvailable(): Boolean = accelerometer != null || linearAccel != null
    override fun hasPermission(): Boolean = true
    override fun isConsentGranted(): Boolean = consentGranted

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !consentGranted) return

        val now = System.currentTimeMillis()

        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val devMagnitude = sqrt(x.pow(2) + y.pow(2) + z.pow(2))

            addSampleAndPrune(now, devMagnitude)
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val rawMagnitude = sqrt(x.pow(2) + y.pow(2) + z.pow(2))

            // Low-pass filter to subtract device-specific gravity dynamically
            gravityEst = 0.92f * gravityEst + 0.08f * rawMagnitude
            val devMagnitude = abs(rawMagnitude - gravityEst)

            addSampleAndPrune(now, devMagnitude)
        }
    }

    private fun addSampleAndPrune(now: Long, magnitude: Float) {
        synchronized(samples) {
            samples.add(MotionSample(now, magnitude))
            pruneOldSamples(now)
        }

        // Track stationary vs movement state
        val cutoff = now - 1500L
        val recentAvg = synchronized(samples) {
            val recent = samples.filter { it.timestampMs >= cutoff }
            if (recent.isEmpty()) 0.0f else recent.map { it.magnitude }.average().toFloat()
        }

        if (recentAvg > 0.18f) {
            lastMovementTimeMs = now
        } else {
            val delta = now - lastStationaryCheckTimeMs
            if (delta in 1..1000) {
                stationaryMsAccumulated += delta
            }
        }
        lastStationaryCheckTimeMs = now
    }

    private fun pruneOldSamples(now: Long) {
        val cutoff = now - windowDurationMs
        samples.removeAll { it.timestampMs < cutoff }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override suspend fun collectTelemetry(): TelemetryInput? = withContext(Dispatchers.IO) {
        if (!isHardwareAvailable() || !hasPermission() || !isConsentGranted()) return@withContext null

        val (intensity, variance) = computeMetrics()
        val stationaryMins = (stationaryMsAccumulated / (1000.0f * 60.0f))

        TelemetryInput(
            movementIntensity = intensity,
            accelerationVariance = variance,
            stationaryDuration = stationaryMins
        )
    }

    fun currentSnapshot(): Map<String, Any>? {
        if (!isHardwareAvailable() || !hasPermission() || !isConsentGranted()) return null

        val now = System.currentTimeMillis()
        val (intensity, variance) = computeMetrics()

        // Update stationary duration if phone is stationary even between sensor events
        if (now - lastMovementTimeMs > 1500L) {
            val delta = now - lastStationaryCheckTimeMs
            if (delta in 1..2000) {
                stationaryMsAccumulated += delta
            }
        }
        lastStationaryCheckTimeMs = now

        val stationaryMins = (stationaryMsAccumulated / (1000.0f * 60.0f))
        val isStationary = intensity < 0.18f

        Log.d(
            "MotionCollector",
            "[REAL MOTION DEBUG] sensorEventReceived=true timestamp=$now windowSize=${samples.size} movementIntensity=$intensity accelerationVariance=$variance stationary=$isStationary stationaryDuration=$stationaryMins"
        )

        return mapOf(
            "movementIntensity" to intensity,
            "accelerationVariance" to variance,
            "stationaryDuration" to stationaryMins,
            "isStationary" to isStationary
        )
    }

    private fun computeMetrics(): Pair<Float, Float> {
        val now = System.currentTimeMillis()
        synchronized(samples) {
            pruneOldSamples(now)
            if (samples.isEmpty()) return Pair(0.0f, 0.0f)

            val values = samples.map { it.magnitude }
            val mean = values.average().toFloat()
            val varSum = values.fold(0.0f) { acc, valNum -> acc + (valNum - mean).pow(2) }
            val varianceVal = varSum / values.size

            return Pair(mean, varianceVal)
        }
    }
}




