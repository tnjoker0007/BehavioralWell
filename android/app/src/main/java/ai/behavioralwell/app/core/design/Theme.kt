package ai.behavioralwell.app.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryTealLight,
    onPrimary = DarkSlateBackground,
    primaryContainer = PrimaryTealContainer,
    onPrimaryContainer = TextPrimaryDark,
    secondary = PrimaryTeal,
    onSecondary = TextPrimaryDark,
    background = DarkSlateBackground,
    onBackground = TextPrimaryDark,
    surface = SurfaceSlate,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceSlateLight,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderSlate
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
