package ai.behavioralwell.app.features.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.behavioralwell.app.core.design.*
import ai.behavioralwell.app.data.models.DashboardResponse

@Composable
fun PatternsScreen(
    data: DashboardResponse?,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(
                title = "Behavioral Patterns",
                subtitle = "Deep analytics & personal baseline trends"
            )
        }

        // 1. Baseline Progress Card
        item {
            BehavioralWellGlassCard {
                Text(
                    text = "PERSONAL BASELINE PROGRESS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = PrimaryCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Baseline Status",
                        style = MaterialTheme.typography.titleMedium.copy(color = TextPrimaryDark)
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Stage0Stable.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ESTABLISHED (14 DAYS)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Stage0Stable,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { 1.0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PrimaryCyan,
                    trackColor = Color(0xFF1E293B)
                )

                Text(
                    text = "Your 14-day personal baseline is mature. Deviations are evaluated against your individual routine.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // 2. 7-Day Trend Chart Card
        item {
            BehavioralWellGlassCard {
                Text(
                    text = "7-DAY PATTERN TREND",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = PrimaryCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )

                Text(
                    text = "Weekly Behavioral Stability",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(top = 16.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val points = listOf(15f, 18f, 22f, 35f, 42f, 38f, 28f)
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
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                        Text(day, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark))
                    }
                }
            }
        }

        // 3. Modality Deviation Summary
        item {
            Text(
                text = "Modality Summary",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        val modalityScores = data?.currentRisk?.modalityScores?.let {
            mapOf(
                "Keyboard" to it.keyboard,
                "Usage" to it.usage,
                "Motion" to it.motion,
                "Work" to it.work,
                "Mobility" to it.mobility
            )
        } ?: mapOf(
            "Keyboard" to 12.0f, "Usage" to 18.0f, "Motion" to 8.0f, "Work" to 15.0f, "Mobility" to 5.0f
        )

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                modalityScores.forEach { (mod, score) ->
                    BehavioralWellGlassCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = mod,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Deviation Score: ${score.toInt()}/100",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (score > 25.0f) Stage2Color.copy(alpha = 0.2f) else Stage0Color.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (score > 25.0f) "ELEVATED" else "NORMAL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (score > 25.0f) Stage2Color else Stage0Color,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Explainability Factors ("Why this changed")
        item {
            SectionHeader(
                title = "Why Patterns Shifted",
                subtitle = "Personal baseline Z-score attributions"
            )
        }

        val contributors = data?.currentRisk?.topContributors ?: emptyList()
        if (contributors.isNotEmpty()) {
            items(contributors.size) { idx ->
                val c = contributors[idx]
                BehavioralWellGlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (c.zScore > 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (c.zScore > 0) Stage2Color else Stage0Color,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = c.feature.replace("_", " ").uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PrimaryCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = c.humanExplanation,
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
 else {
            item {
                BehavioralWellGlassCard {
                    Text(
                        text = "Your recent behavior matches your historical baseline. No significant shifts detected.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
                    )
                }
            }
        }
    }
}
