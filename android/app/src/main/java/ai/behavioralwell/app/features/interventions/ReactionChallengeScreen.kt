package ai.behavioralwell.app.features.interventions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.behavioralwell.app.core.design.*
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactionChallengeScreen(
    onComplete: (Long) -> Unit,
    onClose: () -> Unit
) {
    var state by remember { mutableStateOf("WAIT") } // "WAIT", "READY", "DONE"
    var startTime by remember { mutableLongStateOf(0L) }
    var reactionTimeMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        delay(Random.nextLong(2000, 4500))
        state = "READY"
        startTime = System.currentTimeMillis()
    }

    val boxColor = when (state) {
        "WAIT" -> DarkBg
        "READY" -> Stage0Color
        else -> DarkBg
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("REACTION CHALLENGE", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 2.sp) },
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        containerColor = DarkBg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .background(boxColor, shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                .clickable {
                    if (state == "READY") {
                        reactionTimeMs = System.currentTimeMillis() - startTime
                        state = "DONE"
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            BehavioralWellGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    when (state) {
                        "WAIT" -> {
                            Text(
                                text = "Wait for Green...",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    color = TextMuted,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap as fast as you can when the screen flashes",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                        }
                        "READY" -> {
                            Text(
                                text = "TAP NOW!",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    color = TextPrimary,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                        "DONE" -> {
                            Text(
                                text = "Cognitive Reflex Speed",
                                style = MaterialTheme.typography.titleMedium.copy(color = TextMuted)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "$reactionTimeMs ms",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    color = PrimaryCyan,
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            GradientButton(
                                text = "Save Result",
                                onClick = { onComplete(reactionTimeMs) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

