package ai.behavioralwell.app.features.selfcheck

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.behavioralwell.app.core.design.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfCheckInDialog(
    onDismiss: () -> Unit,
    onSubmit: (mood: String, stressLevel: Int, note: String?) -> Unit
) {
    var mood by remember { mutableStateOf("Neutral") }
    var stressLevel by remember { mutableFloatStateOf(3f) }
    var note by remember { mutableStateOf("") }

    val moods = listOf("Great", "Good", "Neutral", "Anxious", "Exhausted")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NeoBg,
        title = {
            Text(
                text = "Self Check-in",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = PrimaryCyan,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "How are you feeling right now?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    moods.forEach { item ->
                        FilterChip(
                            selected = mood == item,
                            onClick = { mood = item },
                            label = { Text(item, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryCyan,
                                selectedLabelColor = NeoBg,
                                containerColor = NeoInsetBg,
                                labelColor = TextSecondaryDark
                            )
                        )
                    }
                }

                Column {
                    Text(
                        text = "Perceived Stress Level: ${stressLevel.toInt()} / 5",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryDark, fontWeight = FontWeight.Bold)
                    )
                    Slider(
                        value = stressLevel,
                        onValueChange = { stressLevel = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryCyan,
                            activeTrackColor = PrimaryCyan,
                            inactiveTrackColor = NeoDarkShadow
                        )
                    )
                }

                NeomorphicTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = "Optional Notes",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TactileNeoButton(
                text = "Submit Check-in",
                onClick = { onSubmit(mood, stressLevel.toInt(), note.ifBlank { null }) },
                modifier = Modifier.width(160.dp),
                cornerRadius = 14.dp
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark, fontWeight = FontWeight.Bold)
            }
        }
    )
}

