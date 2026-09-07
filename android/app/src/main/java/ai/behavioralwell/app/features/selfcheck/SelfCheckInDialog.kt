package ai.behavioralwell.app.features.selfcheck

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
        containerColor = SurfaceSlate,
        title = {
            Text(
                text = "Self Check-in",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = PrimaryTealLight,
                    fontWeight = FontWeight.Bold
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
                            label = { Text(item, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryTeal,
                                selectedLabelColor = TextPrimaryDark,
                                containerColor = SurfaceSlateLight,
                                labelColor = TextSecondaryDark
                            )
                        )
                    }
                }

                Column {
                    Text(
                        text = "Perceived Stress Level: ${stressLevel.toInt()} / 5",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryDark)
                    )
                    Slider(
                        value = stressLevel,
                        onValueChange = { stressLevel = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryTealLight,
                            activeTrackColor = PrimaryTeal
                        )
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Optional Notes", color = TextSecondaryDark) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryTealLight,
                        unfocusedBorderColor = BorderSlate,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(mood, stressLevel.toInt(), note.ifBlank { null }) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
            ) {
                Text("Submit Check-in")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        }
    )
}
