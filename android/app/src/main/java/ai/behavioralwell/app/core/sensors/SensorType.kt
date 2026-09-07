package ai.behavioralwell.app.core.sensors

enum class SensorType(
    val displayName: String,
    val description: String,
    val batteryImpact: String
) {
    MOTION("Accelerometer & Gyroscope", "Detects local movement intensity and rotation variance", "Low"),
    ACTIVITY("Activity Recognition", "Classifies physical states (walking, still, vehicle)", "Low"),
    SCREEN_USAGE("Screen & Unlock Usage", "Tracks screen-on sessions and unlock counts", "Minimal"),
    APP_USAGE("Application Categories", "Monitors time spent across app categories", "Minimal"),
    KEYBOARD_DYNAMICS("Keyboard Dynamics", "Extracts typing speed (WPM) and pause variance. Zero text logged.", "Minimal"),
    LOCATION_MOBILITY("Location & Mobility", "Measures movement distance and speed variance locally", "Medium"),
    BATTERY_STATE("Battery & Device Health", "Tracks battery drain rate and charging state", "Negligible"),
    DEVICE_STATE("Device Diagnostics", "Monitors boot events, network state, and idle status", "Negligible")
}

enum class SensorStatusState {
    ACTIVE,
    DISABLED,
    UNAVAILABLE,
    PERMISSION_REQUIRED
}

data class SensorStatus(
    val type: SensorType,
    val state: SensorStatusState,
    val hardwareAvailable: Boolean,
    val permissionGranted: Boolean,
    val userConsentGranted: Boolean,
    val lastReadingTimestamp: Long? = null,
    val qualityScore: Float = 1.0f
)
