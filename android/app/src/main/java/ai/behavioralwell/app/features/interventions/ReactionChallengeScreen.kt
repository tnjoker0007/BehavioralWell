package ai.behavioralwell.app.features.interventions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
        delay(Random.nextLong(2000, 5000))
        state = "READY"
        startTime = System.currentTimeMillis()
    }

    val boxColor = when (state) {
        "WAIT" -> SurfaceSlate
        "READY" -> Stage0Stable
        else -> PrimaryTealContainer
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reaction Challenge", color = TextPrimaryDark) },
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimaryDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSlateBackground)
            )
        },
        containerColor = DarkSlateBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .background(boxColor, shape = RoundedCornerShape(16.dp))
                .clickable {
                    if (state == "READY") {
                        reactionTimeMs = System.currentTimeMillis() - startTime
                        state = "DONE"
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (state) {
                    "WAIT" -> {
                        Text(
                            text = "Wait for Green...",
                            style = MaterialTheme.typography.displayLarge.copy(
                                color = TextPrimaryDark,
                                fontSize = 28.sp
                            )
                        )
                    }
                    "READY" -> {
                        Text(
                            text = "TAP NOW!",
                            style = MaterialTheme.typography.displayLarge.copy(
                                color = DarkSlateBackground,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    "DONE" -> {
                        Text(
                            text = "Reaction Time: ${reactionTimeMs} ms",
                            style = MaterialTheme.typography.displayLarge.copy(
                                color = TextPrimaryDark,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { onComplete(reactionTimeMs) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Text("Save Result")
                        }
                    }
                }
            }
        }
    }
}
