package ai.behavioralwell.app.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.behavioralwell.app.core.design.*
import ai.behavioralwell.app.data.models.DashboardResponse
import ai.behavioralwell.app.data.models.ModalityBreakdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToSensors: () -> Unit,
    onNavigateToInterventions: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onOpenSelfCheckIn: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "BehavioralWell",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = PrimaryTealLight,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Multimodal Behavioral Phenotyping",
                            style = MaterialTheme.typography.labelMedium.copy(color = TextSecondaryDark)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSensors) {
                        Icon(Icons.Default.Sensors, contentDescription = "Sensors", tint = PrimaryTealLight)
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondaryDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSlateBackground)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenSelfCheckIn,
                containerColor = PrimaryTeal,
                contentColor = TextPrimaryDark,
                icon = { Icon(Icons.Default.Mood, contentDescription = null) },
                text = { Text("Self Check-in") }
            )
        },
        containerColor = DarkSlateBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is DashboardUiState.Loading -> {
                    CircularProgressIndicator(
                        color = PrimaryTealLight,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is DashboardUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = (uiState as DashboardUiState.Error).message,
                            color = Stage4HighConcern,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadDashboardData() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Text("Retry Connection")
                        }
                    }
                }
                is DashboardUiState.Success -> {
                    val dashboard = (uiState as DashboardUiState.Success).dashboard
                    DashboardContent(
                        dashboard = dashboard,
                        onNavigateToInterventions = onNavigateToInterventions
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    dashboard: DashboardResponse,
    onNavigateToInterventions: () -> Unit
) {
    val risk = dashboard.currentRisk
    val stageColor = when (risk.stage) {
        0 -> Stage0Stable
        1 -> Stage1EarlyDev
        2 -> Stage2PersistentDev
        3 -> Stage3ElevatedRisk
        else -> Stage4HighConcern
    }

    val wellnessScore = (100.0f - risk.riskScore).coerceIn(0.0f, 100.0f).toInt()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // --- Behavioral Wellness Score Card ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BEHAVIORAL WELLNESS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextSecondaryDark,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "$wellnessScore / 100",
                            style = MaterialTheme.typography.displayLarge.copy(
                                color = TextPrimaryDark,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = risk.stageLabel,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = stageColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(stageColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Stage ${risk.stage}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = stageColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // --- Baseline Progress Card ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Personal Baseline Learning",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 16.sp,
                                color = TextPrimaryDark
                            )
                        )
                        Text(
                            text = "${dashboard.baselineProgress.completionPercent.toInt()}%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 16.sp,
                                color = PrimaryTealLight,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { dashboard.baselineProgress.completionPercent / 100.0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = PrimaryTealLight,
                        trackColor = SurfaceSlateLight
                    )

                    Text(
                        text = "${dashboard.baselineProgress.daysCollected} of ${dashboard.baselineProgress.daysRequired} days baseline data recorded",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // --- Modality Contributions ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Modality Contributions",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 16.sp,
                            color = TextPrimaryDark
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    ModalityBar("Keyboard Dynamics", dashboard.currentRisk.modalityScores.keyboard)
                    ModalityBar("Phone Usage", dashboard.currentRisk.modalityScores.usage)
                    ModalityBar("Motion / Accelerometer", dashboard.currentRisk.modalityScores.motion)
                    ModalityBar("Work Behavior", dashboard.currentRisk.modalityScores.work)
                    ModalityBar("Mobility", dashboard.currentRisk.modalityScores.mobility)
                }
            }
        }

        // --- Recommended Interventions Button ---
        item {
            Button(
                onClick = onNavigateToInterventions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceSlateLight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = PrimaryTealLight)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Explore Micro-Interventions (${dashboard.recommendedInterventions.size})",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 16.sp,
                            color = TextPrimaryDark
                        )
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun ModalityBar(label: String, score: Float) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark))
            Text(
                text = "${(score * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryDark, fontWeight = FontWeight.Bold)
            )
        }
        LinearProgressIndicator(
            progress = { score.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = PrimaryTeal,
            trackColor = DarkSlateBackground
        )
    }
}
