package ai.behavioralwell.app.data.collectors

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import ai.behavioralwell.app.core.permissions.SensorPermissionManager
import ai.behavioralwell.app.core.sensors.SensorCollector
import ai.behavioralwell.app.core.sensors.SensorType
import ai.behavioralwell.app.data.models.TelemetryInput
import kotlin.math.pow

class MobilityCollector(
    private val context: Context,
    var consentGranted: Boolean = true
) : SensorCollector, LocationListener {

    override val sensorType: SensorType = SensorType.LOCATION_MOBILITY

    private val permissionManager = SensorPermissionManager(context)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val speedHistory = mutableListOf<Float>()
    private val distanceHistory = mutableListOf<Float>()
    private var lastLocation: Location? = null

    init {
        registerLocationListener()
    }

    @SuppressLint("MissingPermission")
    fun registerLocationListener() {
        if (hasPermission()) {
            try {
                if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        3000L,
                        5.0f,
                        this
                    )
                }
                if (locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        5000L,
                        10.0f,
                        this
                    )
                }
            } catch (e: Exception) {
                // Safe fallback if provider fails
            }
        }
    }

    fun unregisterLocationListener() {
        try {
            locationManager?.removeUpdates(this)
        } catch (e: Exception) {
            // Ignored
        }
    }

    override fun isHardwareAvailable(): Boolean = locationManager != null
    override fun hasPermission(): Boolean = permissionManager.hasLocationPermission()
    override fun isConsentGranted(): Boolean = consentGranted

    override fun onLocationChanged(location: Location) {
        if (!consentGranted) return

        val speed = if (location.hasSpeed()) location.speed else 0.0f
        synchronized(speedHistory) {
            if (speedHistory.size >= 50) speedHistory.removeAt(0)
            speedHistory.add(speed)
        }

        lastLocation?.let { last ->
            val dist = last.distanceTo(location)
            synchronized(distanceHistory) {
                if (distanceHistory.size >= 50) distanceHistory.removeAt(0)
                distanceHistory.add(dist)
            }
        }
        lastLocation = location
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) { registerLocationListener() }
    override fun onProviderDisabled(provider: String) {}

    fun recordMobility(speedVar: Float, routeVar: Float) {
        if (!consentGranted) return
        synchronized(speedHistory) {
            speedHistory.add(speedVar)
        }
        synchronized(distanceHistory) {
            distanceHistory.add(routeVar)
        }
    }

    override suspend fun collectTelemetry(): TelemetryInput? {
        if (!isConsentGranted()) return null
        val snapshot = currentSnapshot() ?: return null
        val spd = (snapshot["speedVariance"] as? Number)?.toFloat()
        val rte = (snapshot["routeVariability"] as? Number)?.toFloat()
        return TelemetryInput(
            speedVariance = spd,
            routeVariability = rte
        )
    }

    fun currentSnapshot(): Map<String, Any>? {
        if (!isConsentGranted()) return null

        if (!hasPermission()) {
            return mapOf(
                "status" to "LOCATION_PERMISSION_REQUIRED",
                "error" to "Location Permission Required"
            )
        }

        val (speedVar, routeVar) = synchronized(speedHistory) {
            if (speedHistory.isEmpty()) return@synchronized Pair(null, null)

            val meanSpeed = speedHistory.average().toFloat()
            val speedVarVal = speedHistory.fold(0.0f) { acc, s -> acc + (s - meanSpeed).pow(2) } / speedHistory.size

            val meanDist = if (distanceHistory.isNotEmpty()) distanceHistory.average().toFloat() else 0.0f
            val routeVarVal = if (distanceHistory.isNotEmpty()) {
                distanceHistory.fold(0.0f) { acc, d -> acc + (d - meanDist).pow(2) } / distanceHistory.size
            } else 0.0f

            Pair(speedVarVal, routeVarVal)
        }

        if (speedVar == null) {
            return mapOf("status" to "WAITING_FOR_LOCATION")
        }

        return mapOf(
            "status" to "LIVE",
            "speedVariance" to speedVar,
            "routeVariability" to (routeVar ?: 0.0f)
        )
    }
}
