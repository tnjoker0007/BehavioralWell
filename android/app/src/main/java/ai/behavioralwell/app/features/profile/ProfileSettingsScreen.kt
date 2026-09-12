package ai.behavioralwell.app.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.behavioralwell.app.core.config.ApiConfig
import ai.behavioralwell.app.core.design.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToDiagnostics: (() -> Unit)? = null
) {
    var customUrl by remember { mutableStateOf(ApiConfig.baseUrl) }
    var showSavedToast by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Server Connection Card
            BehavioralWellGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = PrimaryCyan)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Backend Server Connection",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customUrl,
                        onValueChange = { customUrl = it },
                        label = { Text("FastAPI Base URL", color = TextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryCyan,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkBg.copy(alpha = 0.5f),
                            unfocusedContainerColor = DarkBg.copy(alpha = 0.3f)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            ApiConfig.resetToDefault()
                            customUrl = ApiConfig.baseUrl
                        }) {
                            Text("Reset Default", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                ApiConfig.baseUrl = customUrl
                                showSavedToast = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save URL", color = DarkBg, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (showSavedToast) {
                        Text(
                            text = "Base URL updated successfully!",
                            color = Stage0Color,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Diagnostics Button Card
            if (onNavigateToDiagnostics != null) {
                BehavioralWellGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sensors, contentDescription = null, tint = AccentPurple)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Sensor Status & DEV Stream",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Inspect telemetry collectors & stream status",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                )
                            }
                        }
                        TextButton(onClick = onNavigateToDiagnostics) {
                            Text("Open", color = PrimaryCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Stage4Color),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = TextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out of Account", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

