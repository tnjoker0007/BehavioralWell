package ai.behavioralwell.app.features.interventions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.behavioralwell.app.core.design.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterventionsScreen(
    onNavigateBack: () -> Unit,
    onStartBreathing: () -> Unit,
    onStartReactionChallenge: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activities", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                Column {
                    Text(
                        text = "Reset your rhythm",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Evidence-based micro-interventions to restore focus and autonomic balance.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }
            }

            item {
                ActivityProductCard(
                    title = "Breathing Reset",
                    subtitle = "Slow down for 2 minutes. Autonomic regulation exercise.",
                    duration = "2 MIN",
                    icon = Icons.Default.SelfImprovement,
                    onStart = onStartBreathing
                )
            }

            item {
                ActivityProductCard(
                    title = "Reaction Challenge",
                    subtitle = "Reset your attention. Quick motor latency test.",
                    duration = "1 MIN",
                    icon = Icons.Default.Speed,
                    onStart = onStartReactionChallenge
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ActivityProductCard(
    title: String,
    subtitle: String,
    duration: String,
    icon: ImageVector,
    onStart: () -> Unit
) {
    BehavioralWellGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStart() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = PrimaryCyan.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = PrimaryCyan)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = GlassBorder,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = duration,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "START ACTIVITY",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = PrimaryCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Start",
                    tint = PrimaryCyan
                )
            }
        }
    }
}

