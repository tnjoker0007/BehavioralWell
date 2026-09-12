package ai.behavioralwell.app.core.design

import androidx.compose.ui.graphics.Color

// Primary Theme Colors (Aligned with Web UI)
val DarkBg = Color(0xFF090D16)
val GlassSurface = Color(0xB2111827)        // rgba(17, 24, 39, 0.7)
val GlassSurfaceHover = Color(0xCC1E293B)   // rgba(30, 41, 59, 0.8)
val BorderGlass = Color(0x1AFFFFFF)          // rgba(255, 255, 255, 0.1)
val BorderGlassGlow = Color(0x4D06B6D4)      // rgba(6, 182, 212, 0.3)

// Accents & Gradients
val PrimaryCyan = Color(0xFF06B6D4)
val PrimaryCyanGlow = Color(0x4006B6D4)
val AccentPurple = Color(0xFF8B5CF6)
val AccentBlue = Color(0xFF3B82F6)

// Stage Risk Accent Colors
val Stage0Stable = Color(0xFF10B981)          // Emerald Green
val Stage1EarlyDev = Color(0xFF06B6D4)        // Cyan
val Stage2PersistentDev = Color(0xFFF59E0B)   // Amber
val Stage3ElevatedRisk = Color(0xFFF97316)    // Orange
val Stage4HighConcern = Color(0xFFF43F5E)     // Crimson Rose

// Neutral & Text Colors
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
val TextDimDark = Color(0xFF64748B)

// Modern Design Tokens & Aliases
val TextPrimary = TextPrimaryDark
val TextMuted = TextSecondaryDark
val GlassBorder = BorderGlass
val Stage0Color = Stage0Stable
val Stage1Color = Stage1EarlyDev
val Stage2Color = Stage2PersistentDev
val Stage3Color = Stage3ElevatedRisk
val Stage4Color = Stage4HighConcern

// Legacy aliases for backward compatibility
val PrimaryTeal = PrimaryCyan
val PrimaryTealDark = Color(0xFF0F766E)
val PrimaryTealLight = PrimaryCyan
val PrimaryTealContainer = Color(0xFF115E59)
val DarkSlateBackground = DarkBg
val SurfaceSlate = GlassSurface
val SurfaceSlateLight = GlassSurfaceHover
val BorderSlate = BorderGlass

