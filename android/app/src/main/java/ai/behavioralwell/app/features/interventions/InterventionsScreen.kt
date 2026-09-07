package ai.behavioralwell.app.features.interventions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
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
                title = { Text("Micro-Interventions", color = TextPrimaryDark) },
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
                InterventionCard(
                    title = "Breathing Reset (Box Breathing)",
                    description = "2-minute guided autonomic regulation exercise to restore focus and reduce physiological stress.",
                    duration = "2 min",
                    icon = Icons.Default.SelfImprovement,
                    onStart = onStartBreathing
                )
            }

            item {
                InterventionCard(
                    title = "Reaction Speed Challenge",
                    description = "Quick cognitive reflex assessment measuring response latency and motor consistency.",
                    duration = "1 min",
                    icon = Icons.Default.Speed,
                    onStart = onStartReactionChallenge
                )
            }
        }
    }
}

@Composable
fun InterventionCard(
    title: String,
    description: String,
    duration: String,
    icon: ImageVector,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = PrimaryTealLight)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 16.sp,
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Text(
                    text = duration,
                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondaryDark)
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark),
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )

            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Start Activity")
            }
        }
    }
}
