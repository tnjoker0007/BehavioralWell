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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.behavioralwell.app.core.design.*
import ai.behavioralwell.app.core.permissions.SensorPermissionManager
import ai.behavioralwell.app.core.sensors.SensorStatus
import ai.behavioralwell.app.core.sensors.SensorStatusState
import ai.behavioralwell.app.core.sensors.SensorType
import ai.behavioralwell.app.data.collectors.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorStatusDiagnosticsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val permissionManager = remember { SensorPermissionManager(context) }

    val collectors = remember {
        listOf(
            MotionCollector(context),
            ActivityCollector(context),
            DeviceUsageCollector(context),
            ScreenCollector(context),
            KeyboardMetadataCollector(context),
            LocationCollector(context),
            MobilityCollector(context),
            BatteryCollector(context),
            DeviceStateCollector(context)
        )
    }

    val sensorStatuses = remember {
        collectors.map { it.getStatus() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sensor Diagnostics", color = TextPrimaryDark) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimaryDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSlateBackground)
            )
        },
        containerColor = DarkSlateBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
        }
    }
}

@Composable
fun SensorStatusCard(
    status: SensorStatus,
    onRequestPermission: () -> Unit
) {
    val stateColor = when (status.state) {
        SensorStatusState.ACTIVE -> Stage0Stable
        SensorStatusState.DISABLED -> Stage2PersistentDev
        SensorStatusState.PERMISSION_REQUIRED -> Stage3ElevatedRisk
        SensorStatusState.UNAVAILABLE -> Stage4HighConcern
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = status.type.displayName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 16.sp,
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = status.type.description,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "Battery Impact: ${status.type.batteryImpact}",
                    style = MaterialTheme.typography.labelMedium.copy(color = PrimaryTealLight),
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
                        Text("Grant Permission", color = PrimaryTealLight, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
