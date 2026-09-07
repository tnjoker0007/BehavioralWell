package ai.behavioralwell.app.features.interventions

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.behavioralwell.app.core.design.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingResetScreen(
    onComplete: () -> Unit,
    onClose: () -> Unit
) {
    var phase by remember { mutableStateOf("Inhale") }
    var secondsLeft by remember { mutableIntStateOf(120) }

    LaunchedEffect(Unit) {
        val phases = listOf("Inhale", "Hold", "Exhale", "Hold")
        var phaseIdx = 0
        while (secondsLeft > 0) {
            phase = phases[phaseIdx % 4]
            delay(4000)
            phaseIdx += 1
            secondsLeft -= 4
        }
        onComplete()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val circleSize by infiniteTransition.animateFloat(
        initialValue = 120f,
        targetValue = 240f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "size"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Breathing Reset", color = TextPrimaryDark) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Box Breathing (4-4-4-4)",
                style = MaterialTheme.typography.titleLarge.copy(color = TextSecondaryDark)
            )

            Box(
                modifier = Modifier
                    .size(circleSize.dp)
                    .clip(CircleShape)
                    .background(PrimaryTeal.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = phase,
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = TextPrimaryDark,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${secondsLeft / 60}:${(secondsLeft % 60).toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = PrimaryTealLight,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("Finish Activity early")
                }
            }
        }
    }
}
