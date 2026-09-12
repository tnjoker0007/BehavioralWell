package ai.behavioralwell.app.features.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.behavioralwell.app.core.design.*
import ai.behavioralwell.app.data.models.DashboardResponse
import ai.behavioralwell.app.features.privacy.PrivacySettingsScreen
import ai.behavioralwell.app.features.profile.ProfileSettingsScreen
import ai.behavioralwell.app.features.sensors.SensorStatusDiagnosticsScreen
import ai.behavioralwell.app.features.interventions.InterventionsScreen

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
    var selectedTab by remember { mutableStateOf(NavigationTab.HOME) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryCyanGlow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = PrimaryCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "BehavioralWell",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TextPrimaryDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = "Behavioral Risk & Digital Phenotyping",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSensors) {
                        Icon(Icons.Default.Sensors, contentDescription = "Sensors", tint = PrimaryCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        bottomBar = {
            BehavioralWellBottomBar(
                currentTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    if (tab == NavigationTab.ACTIVITIES) {
                        onNavigateToInterventions()
                    } else if (tab == NavigationTab.PROFILE) {
                        onNavigateToProfile()
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == NavigationTab.HOME) {
                ExtendedFloatingActionButton(
                    onClick = onOpenSelfCheckIn,
                    containerColor = PrimaryCyan,
                    contentColor = DarkBg,
                    icon = { Icon(Icons.Default.Mood, contentDescription = null) },
                    text = { Text("Self Check-in", fontWeight = FontWeight.Bold) }
                )
            }
        },
        containerColor = DarkBg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                NavigationTab.HOME -> {
                    when (val state = uiState) {
                        is DashboardUiState.Loading -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                SkeletonLoader(height = 220.dp)
                                SkeletonLoader(height = 140.dp)
                                SkeletonLoader(height = 180.dp)
                            }
                        }
                        is DashboardUiState.Error -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.CloudOff, contentDescription = null, tint = Stage4HighConcern, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = state.message,
                                    color = Stage4HighConcern,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                GradientButton(
                                    text = "Retry Connection",
                                    onClick = { viewModel.loadDashboardData() }
                                )
                            }
                        }
                        is DashboardUiState.Success -> {
                            DashboardHomeContent(
                                dashboard = state.dashboard,
                                onNavigateToInterventions = onNavigateToInterventions
                            )
                        }
                    }
                }
                NavigationTab.PATTERNS -> {
                    val successData = (uiState as? DashboardUiState.Success)?.dashboard
                    PatternsScreen(data = successData)
                }
                NavigationTab.PRIVACY -> {
                    PrivacySettingsScreen(onNavigateBack = { selectedTab = NavigationTab.HOME })
                }
                else -> {
                    // Handled by NavHost
                }
            }
        }
    }
}

@Composable
fun DashboardHomeContent(
    dashboard: DashboardResponse,
    onNavigateToInterventions: () -> Unit
) {
    val risk = dashboard.currentRisk

    val supportiveText = when (risk.stage) {
        0 -> "Your recent behavior is close to your usual personal baseline."
        1 -> "We've noticed a mild shift in your daily routine."
        2 -> "Your recent patterns show a persistent deviation from baseline."
        3 -> "Multimodal indicators show elevated behavioral deviation."
        else -> "Significant behavioral pattern shifts detected across multiple modalities."
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Contextual Greeting
        item {
            Column {
                Text(
                    text = "Good morning",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                )
                Text(
                    text = "Here's how your behavioral patterns look today.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
                )
            }
        }

        // Hero Risk Gauge Card
        item {
            BehavioralWellGlassCard {
                Text(
                    text = "BEHAVIORAL RISK ASSESSMENT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = PrimaryCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                RefinedCircularRiskGauge(
                    score = risk.riskScore,
                    stage = risk.stage,
                    stageLabel = risk.stageLabel.split("—").lastOrNull()?.trim() ?: risk.stageLabel,
                    confidence = risk.confidence
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = supportiveText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimaryDark,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 7-Day Pattern Trend Sparkline Card
        item {
            BehavioralWellGlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "7-DAY PATTERN TREND",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PrimaryCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = "Weekly Pattern Stability",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimaryDark,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Stage0Stable.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = risk.trend.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Stage0Stable,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(top = 12.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val points = listOf(15f, 20f, 18f, 32f, 28f, 22f, risk.riskScore)
                        val maxP = 100f
                        val widthP = size.width
                        val heightP = size.height
                        val stepX = widthP / (points.size - 1)

                        val path = Path()
                        points.forEachIndexed { i, p ->
                            val x = i * stepX
                            val y = heightP - (p / maxP) * heightP
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        drawPath(
                            path = path,
                            color = PrimaryCyan,
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }
                }
            }
        }

        // Modality Contribution Cards
        item {
            SectionHeader(
                title = "What's Changing",
                subtitle = "Multimodal deviation scores"
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModalityTile(
                        title = "Keyboard",
                        score = risk.modalityScores.keyboard,
                        modifier = Modifier.weight(1f)
                    )
                    ModalityTile(
                        title = "Usage",
                        score = risk.modalityScores.usage,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModalityTile(
                        title = "Motion",
                        score = risk.modalityScores.motion,
                        modifier = Modifier.weight(1f)
                    )
                    ModalityTile(
                        title = "Work",
                        score = risk.modalityScores.work,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Suggested Intervention Card
        item {
            BehavioralWellGlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PrimaryCyanGlow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = PrimaryCyan)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Suggested for you",
                                style = MaterialTheme.typography.labelSmall.copy(color = PrimaryCyan, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "5-minute Breathing Reset",
                                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryDark, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    TextButton(onClick = onNavigateToInterventions) {
                        Text("START →", color = PrimaryCyan, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun ModalityTile(
    title: String,
    score: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .border(1.dp, BorderGlass, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = GlassSurface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(color = TextSecondaryDark)
            )
            Text(
                text = "${score.toInt()}%",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = if (score > 25.0) Stage2PersistentDev else TextPrimaryDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
