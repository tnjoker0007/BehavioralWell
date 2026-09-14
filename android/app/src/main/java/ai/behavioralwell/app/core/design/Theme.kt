package ai.behavioralwell.app.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NeomorphicColorScheme = lightColorScheme(
    primary = PrimaryCyan,
    onPrimary = NeoBg,
    primaryContainer = PrimaryCyanGlow,
    onPrimaryContainer = TextPrimaryDark,
    secondary = AccentPurple,
    onSecondary = TextPrimaryDark,
    background = NeoBg,
    onBackground = TextPrimaryDark,
    surface = NeoSurface,
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
        colorScheme = NeomorphicColorScheme,
        typography = Typography,
        content = content
    )
}
