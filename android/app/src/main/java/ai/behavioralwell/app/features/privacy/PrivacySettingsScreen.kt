package ai.behavioralwell.app.features.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.behavioralwell.app.core.design.*
import ai.behavioralwell.app.data.repositories.TelemetryRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { TelemetryRepository(context) }
    val scope = rememberCoroutineScope()

    var keyboardConsent by remember { mutableStateOf(true) }
    var usageConsent by remember { mutableStateOf(true) }
    var motionConsent by remember { mutableStateOf(true) }
    var workConsent by remember { mutableStateOf(true) }
    var mobilityConsent by remember { mutableStateOf(true) }

    fun updateConsentState(
        kb: Boolean = keyboardConsent,
        usg: Boolean = usageConsent,
        mot: Boolean = motionConsent,
        wrk: Boolean = workConsent,
        mob: Boolean = mobilityConsent
    ) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.updateConsent(kb, usg, mot, wrk, mob)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Sensor Consent", color = TextPrimaryDark) },
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
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Privacy Boundary Guarantee",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 16.sp,
                                color = PrimaryTealLight,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "BehavioralWell measures changes in derived metadata patterns. We NEVER collect raw typed text, passwords, messages, microphone recordings, photos, or raw GPS track logs.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            item {
                ConsentToggleCard(
                    title = "Keyboard Dynamics Telemetry",
                    description = "Collects typing speed (WPM) and pause variance. Zero typed characters stored.",
                    checked = keyboardConsent,
                    onCheckedChange = {
                        keyboardConsent = it
                        updateConsentState(kb = it)
                    }
                )
            }

            item {
                ConsentToggleCard(
                    title = "App & Screen Usage Telemetry",
                    description = "Collects screen-on hours, unlock count, and app category switching.",
                    checked = usageConsent,
                    onCheckedChange = {
                        usageConsent = it
                        updateConsentState(usg = it)
                    }
                )
            }

            item {
                ConsentToggleCard(
                    title = "Motion & Activity Telemetry",
                    description = "Collects accelerometer movement intensity and rotation variance.",
                    checked = motionConsent,
                    onCheckedChange = {
                        motionConsent = it
                        updateConsentState(mot = it)
                    }
                )
            }

            item {
                ConsentToggleCard(
                    title = "Work & Task Telemetry",
                    description = "Tracks task completion times and error rates from check-ins.",
                    checked = workConsent,
                    onCheckedChange = {
                        workConsent = it
                        updateConsentState(wrk = it)
                    }
                )
            }

            item {
                ConsentToggleCard(
                    title = "Location & Mobility Telemetry",
                    description = "Derives travel distance and speed variance locally without saving raw GPS logs.",
                    checked = mobilityConsent,
                    onCheckedChange = {
                        mobilityConsent = it
                        updateConsentState(mob = it)
                    }
                )
            }
        }
    }
}

@Composable
fun ConsentToggleCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
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
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 15.sp,
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark),
                    modifier = Modifier.padding(top = 2.dp, end = 8.dp)
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextPrimaryDark,
                    checkedTrackColor = PrimaryTeal,
                    uncheckedThumbColor = TextSecondaryDark,
                    uncheckedTrackColor = SurfaceSlateLight
                )
            )
        }
    }
}
