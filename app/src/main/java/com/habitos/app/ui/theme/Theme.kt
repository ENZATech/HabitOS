package com.habitos.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ---- Palette ----
val Background       = Color(0xFF0C0A09)
val Surface          = Color(0xFF171412)
val NavBar           = Color(0xFF141210)
val Border           = Color(0xFF292524)
val AccentEmerald    = Color(0xFF10B981)
val AccentTeal       = Color(0xFF0D9488)
val Amber            = Color(0xFFF59E0B)
val Rose             = Color(0xFFF43F5E)
val OnSurface        = Color(0xFFFFFAF7)
val OnSurfaceMuted   = Color(0xFF9CA3AF)
val OnSurfaceDim     = Color(0xFF6B7280)

private val HabitOSDarkColorScheme = darkColorScheme(
    primary           = AccentEmerald,
    secondary         = AccentTeal,
    tertiary          = Amber,
    background        = Background,
    surface           = Surface,
    surfaceVariant    = NavBar,
    onBackground      = OnSurface,
    onSurface         = OnSurface,
    onSurfaceVariant  = OnSurfaceMuted,
    outline           = Border,
    error             = Rose,
    primaryContainer  = Color(0xFF064E3B),
    onPrimaryContainer= OnSurface,
)

@Composable
fun HabitOSTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HabitOSDarkColorScheme,
        typography  = HabitOSTypography,
        content     = content
    )
}
