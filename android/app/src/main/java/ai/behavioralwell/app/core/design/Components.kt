package ai.behavioralwell.app.core.design

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BehavioralWellGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderColor: Color = BorderGlass,
    backgroundColor: Color = GlassSurface,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
            .shadow(12.dp, RoundedCornerShape(cornerRadius), ambientColor = Color.Black, spotColor = PrimaryCyanGlow),
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            content = content
        )
    }
}

@Composable
fun RefinedCircularRiskGauge(
    score: Float,
    stage: Int,
    stageLabel: String,
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val stageColor = when (stage) {
        0 -> Stage0Stable
        1 -> Stage1EarlyDev
        2 -> Stage2PersistentDev
        3 -> Stage3ElevatedRisk
        else -> Stage4HighConcern
    }

    val animatedScore by animateFloatAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "riskScoreAnimation"
    )

    val sweepAngle = (animatedScore / 100.0f) * 260.0f

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()

                // Track Ring (Dark Background)
                drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = 140f,
                    sweepAngle = 260f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Active Score Ring
                drawArc(
                    color = stageColor,
                    startAngle = 140f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${animatedScore.toInt()}",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimaryDark
                    )
                )
                
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clip(CircleShape)
                        .background(stageColor.copy(alpha = 0.2f))
                        .border(1.dp, stageColor.copy(alpha = 0.4f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stageLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = stageColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Text(
                    text = "Confidence: ${confidence.toInt()}%",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = PrimaryCyanGlow),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PrimaryCyan, AccentPurple)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (icon != null) {
                        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }
    }
}


@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = PrimaryCyan,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .clickable { onActionClick() }
                    .padding(4.dp)
            )
        }
    }
}

enum class NavigationTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    PATTERNS("Patterns", Icons.Default.Insights),
    ACTIVITIES("Activities", Icons.Default.SelfImprovement),
    PRIVACY("Privacy", Icons.Default.Security),
    PROFILE("Profile", Icons.Default.Person)
}

@Composable
fun BehavioralWellBottomBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderGlass, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        color = GlassSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationTab.values().forEach { tab ->
                val selected = (tab == currentTab)
                val color = if (selected) PrimaryCyan else TextSecondaryDark

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = color,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    height: Dp = 100.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B))
    )
}
