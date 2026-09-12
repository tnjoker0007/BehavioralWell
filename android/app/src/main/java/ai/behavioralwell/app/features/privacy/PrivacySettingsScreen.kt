package ai.behavioralwell.app.features.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
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
                title = { Text("Privacy Controls", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
            item {
                BehavioralWellGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Your data. Your control.",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 18.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "BehavioralWell analyzes derived behavioral patterns. Raw typed content, passwords, messages and precise location are not transmitted.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextMuted,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }
            }

            item {
                SectionHeader(title = "Telemetry Modalities")
            }

            item {
                ModalityConsentCard(
                    icon = "⌨",
                    title = "Keyboard Patterns",
                    subtitle = "Typing dynamics only • No message content collected",
                    checked = keyboardConsent,
                    onCheckedChange = {
                        keyboardConsent = it
                        updateConsentState(kb = it)
                    }
                )
            }

            item {
                ModalityConsentCard(
                    icon = "📱",
                    title = "Device Usage",
                    subtitle = "Screen/session metadata • App category switches",
                    checked = usageConsent,
                    onCheckedChange = {
                        usageConsent = it
                        updateConsentState(usg = it)
                    }
                )
            }

            item {
                ModalityConsentCard(
                    icon = "🏃",
                    title = "Motion",
                    subtitle = "Derived movement features • Rotation variance",
                    checked = motionConsent,
                    onCheckedChange = {
                        motionConsent = it
                        updateConsentState(mot = it)
                    }
                )
            }

            item {
                ModalityConsentCard(
                    icon = "💼",
                    title = "Work Patterns",
                    subtitle = "Task performance metadata • Error rates",
                    checked = workConsent,
                    onCheckedChange = {
                        workConsent = it
                        updateConsentState(wrk = it)
                    }
                )
            }

            item {
                ModalityConsentCard(
                    icon = "🚗",
                    title = "Mobility",
                    subtitle = "Derived movement statistics • No raw GPS logs saved",
                    checked = mobilityConsent,
                    onCheckedChange = {
                        mobilityConsent = it
                        updateConsentState(mob = it)
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
fun ModalityConsentCard(
    icon: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    BehavioralWellGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (checked) "ON" else "OFF",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (checked) Stage0Color else TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(top = 2.dp, end = 8.dp)
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextPrimary,
                    checkedTrackColor = PrimaryCyan,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = GlassSurface
                )
            )
        }
    }
}

