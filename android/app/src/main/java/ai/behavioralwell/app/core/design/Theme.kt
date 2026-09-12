package ai.behavioralwell.app.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryCyan,
    onPrimary = DarkBg,
    primaryContainer = PrimaryCyanGlow,
    onPrimaryContainer = TextPrimaryDark,
    secondary = AccentPurple,
    onSecondary = TextPrimaryDark,
    background = DarkBg,
    onBackground = TextPrimaryDark,
    surface = GlassSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = GlassSurfaceHover,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderGlass
)

@Composable
fun BehavioralWellTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
