package ai.behavioralwell.app.features.sensors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.behavioralwell.app.BuildConfig
import ai.behavioralwell.app.core.config.ApiConfig
import ai.behavioralwell.app.core.design.*
import ai.behavioralwell.app.core.permissions.SensorPermissionManager
import ai.behavioralwell.app.core.sensors.SensorStatus
import ai.behavioralwell.app.core.sensors.SensorStatusState
import ai.behavioralwell.app.core.sensors.SensorType
import ai.behavioralwell.app.data.collectors.*
import ai.behavioralwell.app.dev.LiveTelemetryDebugPublisher
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorStatusDiagnosticsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val permissionManager = remember { SensorPermissionManager(context) }

    val collectors = remember {
        listOf(
            CollectorRegistry.getMotionCollector(context),
            CollectorRegistry.getActivityCollector(context),
            CollectorRegistry.getUsageCollector(context),
            ScreenCollector(context),
            CollectorRegistry.getKeyboardCollector(context),
            LocationCollector(context),
            CollectorRegistry.getMobilityCollector(context),
            BatteryCollector(context),
            DeviceStateCollector(context)
        )
    }

    val sensorStatuses = remember {
        collectors.map { it.getStatus() }
    }

    var isLiveStreaming by remember { mutableStateOf(LiveTelemetryDebugPublisher.isCurrentlyStreaming()) }
    var liveSnapshot by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
    var lastUpdateMs by remember { mutableLongStateOf(0L) }

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        while (true) {
            val snapshot = CollectorRegistry.collectDerivedSnapshot(context)
            if (snapshot.isNotEmpty()) {
                liveSnapshot = snapshot
                lastUpdateMs = System.currentTimeMillis()
            }
            delay(1000L)
        }
    }

    val elapsedSec = if (lastUpdateMs > 0) (System.currentTimeMillis() - lastUpdateMs) / 1000 else 999
    val (statusText, statusColor) = when {
        !isLiveStreaming -> Pair("DISCONNECTED", Stage2Color)
        elapsedSec <= 3 -> Pair("LIVE", Stage0Color)
        else -> Pair("STALE", Stage3Color)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sensor Diagnostics", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        containerColor = DarkBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (BuildConfig.DEBUG) {
                item {
                    DevLiveTelemetryControlCard(
                        isStreaming = isLiveStreaming,
                        targetUrl = "${ApiConfig.baseUrl}dev/telemetry",
                        onToggleStreaming = { start ->
                            if (start) {
                                LiveTelemetryDebugPublisher.startStreaming(context, scope)
                            } else {
                                LiveTelemetryDebugPublisher.stopStreaming()
                            }
                            isLiveStreaming = LiveTelemetryDebugPublisher.isCurrentlyStreaming()
                        }
                    )
                }

                item {
                    SectionHeader(
                        title = "Real-Time Telemetry Metrics",
                        subtitle = if (lastUpdateMs > 0) "Last update: ${timeFormat.format(Date(lastUpdateMs))}" else "Waiting for initial snapshot..."
                    )
                }

                item {
                    LiveMetricsCard(
                        statusText = statusText,
                        statusColor = statusColor,
                        snapshot = liveSnapshot
                    )
                }
            }

            item {
                SectionHeader(title = "Hardware & System Collectors")
            }

            items(sensorStatuses) { status ->
                SensorStatusCard(
                    status = status,
                    onRequestPermission = {
                        if (status.type == SensorType.APP_USAGE) {
                            permissionManager.openUsageAccessSettings()
                        } else {
                            permissionManager.openApplicationDetailsSettings()
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun LiveMetricsCard(
    statusText: String,
    statusColor: androidx.compose.ui.graphics.Color,
    snapshot: Map<String, Any>
) {
    BehavioralWellGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PrimaryCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "REAL DEVICE DATA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PrimaryCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Motion Section
            val motion = snapshot["motion"] as? Map<*, *>
            TelemetrySectionTitle("🏃 MOTION SENSORS")
            TelemetryValueRow("Movement Intensity", motion?.get("movementIntensity")?.let { String.format("%.4f", it) } ?: "Waiting for data")
            TelemetryValueRow("Acceleration Variance", motion?.get("accelerationVariance")?.let { String.format("%.6f", it) } ?: "Waiting for data")
            TelemetryValueRow("Stationary Duration", motion?.get("stationaryDuration")?.let { String.format("%.2f mins", it) } ?: "Waiting for data")

            Spacer(modifier = Modifier.height(12.dp))

            // Usage Section
            val usage = snapshot["usage"] as? Map<*, *>
            TelemetrySectionTitle("📱 APP USAGE")
            TelemetryValueRow("Screen Time", usage?.get("screenTime")?.let { String.format("%.2f hrs", it) } ?: "N/A")
            TelemetryValueRow("Unlock Count", usage?.get("unlockCount")?.toString() ?: "N/A")
            TelemetryValueRow("Night Usage", usage?.get("nightUsage")?.let { String.format("%.2f hrs", it) } ?: "N/A")
            TelemetryValueRow("App Switch Frequency", usage?.get("appSwitchFrequency")?.let { String.format("%.1f /hr", it) } ?: "N/A")

            Spacer(modifier = Modifier.height(12.dp))

            // Keyboard Section
            val keyboard = snapshot["keyboard"] as? Map<*, *>
            TelemetrySectionTitle("⌨️ KEYBOARD METADATA")
            TelemetryValueRow("Typing Speed", keyboard?.get("typingSpeed")?.let { String.format("%.1f WPM", it) } ?: "Waiting for data")
            TelemetryValueRow("Key Press Duration", keyboard?.get("keyPressDuration")?.let { String.format("%.0f ms", it) } ?: "Waiting for data")
            TelemetryValueRow("Pause Duration", keyboard?.get("pauseDuration")?.let { String.format("%.2f s", it) } ?: "Waiting for data")
            TelemetryValueRow("Correction Rate", keyboard?.get("correctionRate")?.let { String.format("%.1f %%", (it as Number).toFloat() * 100) } ?: "Waiting for data")
        }
    }
}

@Composable
fun TelemetrySectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            color = PrimaryCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        ),
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun TelemetryValueRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (value.contains("Waiting") || value == "N/A") TextMuted else TextPrimary,
                fontWeight = if (value.contains("Waiting") || value == "N/A") FontWeight.Normal else FontWeight.Bold
            )
        )
    }
}

@Composable
fun DevLiveTelemetryControlCard(
    isStreaming: Boolean,
    targetUrl: String,
    onToggleStreaming: (Boolean) -> Unit
) {
    BehavioralWellGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DEV Live Telemetry Monitor",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Streams real-time derived features to live browser monitor (~1Hz). Zero DB writes.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Switch(
                    checked = isStreaming,
                    onCheckedChange = { onToggleStreaming(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TextPrimary,
                        checkedTrackColor = Stage0Color,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = GlassSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Endpoint: $targetUrl",
                    style = MaterialTheme.typography.labelMedium.copy(color = PrimaryCyan, fontSize = 11.sp)
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isStreaming) Stage0Color.copy(alpha = 0.2f) else Stage2Color.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isStreaming) "STREAMING LIVE (~1Hz)" else "STOPPED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isStreaming) Stage0Color else Stage2Color,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SensorStatusCard(
    status: SensorStatus,
    onRequestPermission: () -> Unit
) {
    val stateColor = when (status.state) {
        SensorStatusState.ACTIVE -> Stage0Color
        SensorStatusState.DISABLED -> Stage2Color
        SensorStatusState.PERMISSION_REQUIRED -> Stage3Color
        SensorStatusState.UNAVAILABLE -> Stage4Color
    }

    BehavioralWellGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = status.type.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = status.type.description,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "Battery Impact: ${status.type.batteryImpact}",
                    style = MaterialTheme.typography.labelMedium.copy(color = PrimaryCyan),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(stateColor.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = status.state.name,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = stateColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                if (status.state == SensorStatusState.PERMISSION_REQUIRED) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onRequestPermission) {
                        Text("Grant Permission", color = PrimaryCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


