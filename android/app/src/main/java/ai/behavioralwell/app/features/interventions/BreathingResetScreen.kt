package ai.behavioralwell.app.features.interventions

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
    var phase by remember { mutableStateOf("INHALE") }
    var secondsLeft by remember { mutableIntStateOf(120) }

    LaunchedEffect(Unit) {
        val phases = listOf("INHALE", "HOLD", "EXHALE", "HOLD")
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
        initialValue = 140f,
        targetValue = 240f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "size"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BREATHING RESET", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 2.sp) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Central Animated Breathing Circle
            Box(
                modifier = Modifier
                    .size(circleSize.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrimaryCyan.copy(alpha = 0.35f),
                                AccentPurple.copy(alpha = 0.15f),
                                DarkBg
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(listOf(PrimaryCyan, AccentPurple)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = phase,
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 3.sp
                        )
                    )
                    Text(
                        text = "Follow the circle",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Bottom Timer & Actions
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "${secondsLeft / 60}:${(secondsLeft % 60).toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = PrimaryCyan,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "Autonomic Reset • Box Breathing 4-4-4-4",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                OutlinedButton(
                    onClick = onComplete,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Complete Activity")
                }
            }
        }
    }
}

