package ai.behavioralwell.app.core.design

import androidx.compose.ui.graphics.Color

// Neomorphic Primary Color System
val NeoBg = Color(0xFFE8E6F4)
val NeoSurface = Color(0xFFE8E6F4)
val NeoShadowDark = Color(0xFFC4C1DA)
val NeoShadowLight = Color(0xFFFFFFFF)

// Legacy alias compatibility
val DarkBg = NeoBg
val GlassSurface = NeoSurface
val GlassSurfaceHover = Color(0xFFDFDCF0)
val BorderGlass = Color(0x33C4C1DA)
val BorderGlassGlow = Color(0x4D7C3AED)

// Accents & Gradients
val PrimaryCyan = Color(0xFF7C3AED)          // Deep Violet
val PrimaryCyanGlow = Color(0x407C3AED)
val AccentPurple = Color(0xFF9333EA)
val AccentBlue = Color(0xFF2563EB)
val AccentTeal = Color(0xFF0D9488)

// Stage Risk Accent Colors
val Stage0Stable = Color(0xFF10B981)          // Emerald Green
val Stage1EarlyDev = Color(0xFF0284C7)        // Sky Blue
val Stage2PersistentDev = Color(0xFFD97706)   // Amber
val Stage3ElevatedRisk = Color(0xFFEA580C)    // Orange
val Stage4HighConcern = Color(0xFFE11D48)     // Crimson Rose

// High Contrast Text Colors
val TextPrimaryDark = Color(0xFF2B273D)       // Deep Slate
val TextSecondaryDark = Color(0xFF6E6A86)     // Muted Slate
val TextDimDark = Color(0xFF8E8A9F)

// Modern Design Tokens & Aliases
val TextPrimary = TextPrimaryDark
val TextMuted = TextSecondaryDark
val GlassBorder = BorderGlass
val Stage0Color = Stage0Stable
val Stage1Color = Stage1EarlyDev
val Stage2Color = Stage2PersistentDev
val Stage3Color = Stage3ElevatedRisk
val Stage4Color = Stage4HighConcern

// Legacy aliases
val PrimaryTeal = PrimaryCyan
val PrimaryTealDark = Color(0xFF5B21B6)
val PrimaryTealLight = PrimaryCyan
val PrimaryTealContainer = Color(0xFFEDE9FE)
val DarkSlateBackground = NeoBg
val SurfaceSlate = NeoSurface
val SurfaceSlateLight = GlassSurfaceHover
val BorderSlate = BorderGlass
