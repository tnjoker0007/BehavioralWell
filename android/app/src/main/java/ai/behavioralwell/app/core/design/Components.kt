package ai.behavioralwell.app.core.design

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 3D Neomorphism Light & Shadow Constants
val NeoLightHighlight = Color(0xFFFFFFFF)
val NeoDarkShadow = Color(0xFFB8B4D4)       // Rich soft lavender shadow
val NeoInsetBg = Color(0xFFDFDCF0)

/**
 * Custom Modifier for 3D Neomorphic Raised Surface.
 * Draws top-left soft white highlight and bottom-right soft dark shadow.
 */
fun Modifier.neoRaised(
    cornerRadius: Dp = 20.dp,
    shadowOffset: Dp = 6.dp,
    blurRadius: Dp = 10.dp,
    backgroundColor: Color = NeoBg,
    darkShadowColor: Color = NeoDarkShadow,
    lightShadowColor: Color = NeoLightHighlight
): Modifier = this.drawBehind {
    val cornerPx = cornerRadius.toPx()
    val offsetPx = shadowOffset.toPx()
    val blurPx = blurRadius.toPx()

    drawIntoCanvas { canvas ->
        val nativeCanvas = canvas.nativeCanvas

        // 1. Bottom-Right Soft Dark Shadow
        val darkPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = darkShadowColor.toArgb()
            maskFilter = android.graphics.BlurMaskFilter(blurPx, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        nativeCanvas.drawRoundRect(
            offsetPx,
            offsetPx,
            size.width + offsetPx,
            size.height + offsetPx,
            cornerPx,
            cornerPx,
            darkPaint
        )

        // 2. Top-Left Soft Light Highlight
        val lightPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = lightShadowColor.toArgb()
            maskFilter = android.graphics.BlurMaskFilter(blurPx, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        nativeCanvas.drawRoundRect(
            -offsetPx,
            -offsetPx,
            size.width - offsetPx,
            size.height - offsetPx,
            cornerPx,
            cornerPx,
            lightPaint
        )

        // 3. Main Center Surface
        val surfacePaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = backgroundColor.toArgb()
        }
        nativeCanvas.drawRoundRect(
            0f,
            0f,
            size.width,
            size.height,
            cornerPx,
            cornerPx,
            surfacePaint
        )
    }
}

/**
 * Custom Modifier for Circular 3D Neomorphic Surface (e.g., logo, buttons, risk gauge).
 */
fun Modifier.neoRaisedCircle(
    shadowOffset: Dp = 5.dp,
    blurRadius: Dp = 8.dp,
    backgroundColor: Color = NeoBg,
    darkShadowColor: Color = NeoDarkShadow,
    lightShadowColor: Color = NeoLightHighlight
): Modifier = this.drawBehind {
    val offsetPx = shadowOffset.toPx()
    val blurPx = blurRadius.toPx()
    val radiusPx = size.minDimension / 2f
    val cx = size.width / 2f
    val cy = size.height / 2f

    drawIntoCanvas { canvas ->
        val nativeCanvas = canvas.nativeCanvas

        // Dark Shadow
        val darkPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = darkShadowColor.toArgb()
            maskFilter = android.graphics.BlurMaskFilter(blurPx, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        nativeCanvas.drawCircle(cx + offsetPx, cy + offsetPx, radiusPx, darkPaint)

        // Light Highlight
        val lightPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = lightShadowColor.toArgb()
            maskFilter = android.graphics.BlurMaskFilter(blurPx, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        nativeCanvas.drawCircle(cx - offsetPx, cy - offsetPx, radiusPx, lightPaint)

        // Center Surface
        val surfacePaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = backgroundColor.toArgb()
        }
        nativeCanvas.drawCircle(cx, cy, radiusPx, surfacePaint)
    }
}

/**
 * Custom Modifier for Inset / Sunken 3D Neomorphic Surface (e.g. input fields, chart containers).
 */
fun Modifier.neoInset(
    cornerRadius: Dp = 16.dp,
    shadowOffset: Dp = 4.dp,
    blurRadius: Dp = 6.dp,
    backgroundColor: Color = NeoInsetBg,
    darkShadowColor: Color = NeoDarkShadow,
    lightShadowColor: Color = NeoLightHighlight
): Modifier = this.drawBehind {
    val cornerPx = cornerRadius.toPx()
    val offsetPx = shadowOffset.toPx()
    val blurPx = blurRadius.toPx()

    drawIntoCanvas { canvas ->
        val nativeCanvas = canvas.nativeCanvas

        // Base Inset Surface
        val bgPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = backgroundColor.toArgb()
        }
        nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, cornerPx, cornerPx, bgPaint)

        // Top-Left Inner Dark Shadow
        val darkPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = darkShadowColor.toArgb()
            maskFilter = android.graphics.BlurMaskFilter(blurPx, android.graphics.BlurMaskFilter.Blur.NORMAL)
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = offsetPx * 2.2f
        }
        nativeCanvas.save()
        val pathDark = android.graphics.Path()
        pathDark.addRoundRect(0f, 0f, size.width, size.height, cornerPx, cornerPx, android.graphics.Path.Direction.CW)
        nativeCanvas.clipPath(pathDark)
        nativeCanvas.drawRoundRect(-offsetPx, -offsetPx, size.width + offsetPx, size.height + offsetPx, cornerPx, cornerPx, darkPaint)
        nativeCanvas.restore()

        // Bottom-Right Inner Light Highlight
        val lightPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = lightShadowColor.toArgb()
            maskFilter = android.graphics.BlurMaskFilter(blurPx, android.graphics.BlurMaskFilter.Blur.NORMAL)
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = offsetPx * 2.2f
        }
        nativeCanvas.save()
        val pathLight = android.graphics.Path()
        pathLight.addRoundRect(0f, 0f, size.width, size.height, cornerPx, cornerPx, android.graphics.Path.Direction.CW)
        nativeCanvas.clipPath(pathLight)
        nativeCanvas.drawRoundRect(offsetPx, offsetPx, size.width + offsetPx * 2f, size.height + offsetPx * 2f, cornerPx, cornerPx, lightPaint)
        nativeCanvas.restore()
    }
}

@Composable
fun NeoRaisedSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    shadowOffset: Dp = 6.dp,
    blurRadius: Dp = 10.dp,
    backgroundColor: Color = NeoBg,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .neoRaised(
                cornerRadius = cornerRadius,
                shadowOffset = shadowOffset,
                blurRadius = blurRadius,
                backgroundColor = backgroundColor
            )
            .padding(contentPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
fun BehavioralWellGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    backgroundColor: Color = NeoBg,
    content: @Composable ColumnScope.() -> Unit
) {
    NeoRaisedSurface(
        modifier = modifier,
        cornerRadius = cornerRadius,
        backgroundColor = backgroundColor,
        content = content
    )
}

@Composable
fun NeoInsetSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    shadowOffset: Dp = 4.dp,
    blurRadius: Dp = 6.dp,
    backgroundColor: Color = NeoInsetBg,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .neoInset(
                cornerRadius = cornerRadius,
                shadowOffset = shadowOffset,
                blurRadius = blurRadius,
                backgroundColor = backgroundColor
            )
            .padding(contentPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
fun NeomorphicInsetContainer(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    NeoInsetSurface(
        modifier = modifier,
        cornerRadius = cornerRadius,
        content = content
    )
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
        // Outer Housing (Layer 1, 2, 3: Raised Circular Neomorphic Disc)
        Box(
            modifier = Modifier
                .size(220.dp)
                .neoRaisedCircle(shadowOffset = 8.dp, blurRadius = 14.dp, backgroundColor = NeoBg)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Layer 4 & 5: Inset Track + Colored Progress Arc
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 16.dp.toPx()

                // Layer 4: Inset Track Ring
                drawArc(
                    color = Color(0xFFC4C1DA),
                    startAngle = 140f,
                    sweepAngle = 260f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Layer 5: Active Score Arc
                drawArc(
                    color = stageColor,
                    startAngle = 140f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Layer 6: Central Value Area (Raised Inner Circle)
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .neoRaisedCircle(shadowOffset = 4.dp, blurRadius = 6.dp, backgroundColor = NeoBg),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${animatedScore.toInt()}",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimaryDark
                        )
                    )

                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(CircleShape)
                            .background(stageColor.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = stageLabel.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = stageColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    val safeConfidence = when {
                        confidence <= 1.0f -> confidence * 100.0f
                        else -> confidence
                    }.coerceIn(0.0f, 100.0f)

                    Text(
                        text = "Confidence ${safeConfidence.toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondaryDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TactileNeoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    containerColor: Color = NeoBg,
    contentColor: Color = PrimaryCyan,
    cornerRadius: Dp = 18.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val buttonModifier = if (isPressed) {
        Modifier.neoInset(cornerRadius = cornerRadius, shadowOffset = 3.dp, blurRadius = 4.dp, backgroundColor = NeoInsetBg)
    } else {
        Modifier.neoRaised(cornerRadius = cornerRadius, shadowOffset = 6.dp, blurRadius = 8.dp, backgroundColor = containerColor)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(buttonModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = contentColor,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = contentColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
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
    TactileNeoButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading,
        icon = icon
    )
}

@Composable
fun NeomorphicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None
) {
    NeoInsetSurface(
        modifier = modifier,
        cornerRadius = 14.dp,
        shadowOffset = 3.dp,
        blurRadius = 5.dp,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = TextSecondaryDark) },
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = TextPrimaryDark,
                unfocusedTextColor = TextPrimaryDark
            )
        )
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
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark, fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = PrimaryCyan,
                    fontWeight = FontWeight.ExtraBold
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neoRaised(
                cornerRadius = 28.dp,
                shadowOffset = 6.dp,
                blurRadius = 10.dp,
                backgroundColor = NeoBg
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationTab.values().forEach { tab ->
                val selected = (tab == currentTab)
                val color = if (selected) PrimaryCyan else TextSecondaryDark

                val itemModifier = if (selected) {
                    Modifier.neoInset(cornerRadius = 16.dp, shadowOffset = 3.dp, blurRadius = 4.dp, backgroundColor = NeoInsetBg)
                } else {
                    Modifier.clip(RoundedCornerShape(16.dp))
                }

                Box(
                    modifier = itemModifier
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
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
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
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
            .neoInset(cornerRadius = 20.dp, shadowOffset = 3.dp, blurRadius = 5.dp, backgroundColor = NeoInsetBg)
    )
}

