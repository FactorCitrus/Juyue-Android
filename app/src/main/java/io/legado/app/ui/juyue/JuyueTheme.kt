package io.legado.app.ui.juyue

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class JuyuePalette(
    val background: Color,
    val surface: Color,
    val elevated: Color,
    val ink: Color,
    val secondaryInk: Color,
    val divider: Color,
    val accent: Color,
    val onAccent: Color,
    val danger: Color,
    val success: Color,
    val warning: Color,
    val isDark: Boolean,
)

val JuyueLightPalette = JuyuePalette(
    background = Color(0xFFD9D4C3),
    surface = Color(0xFFE7E1D1),
    elevated = Color(0xFFF1ECDE),
    ink = Color(0xFF171715),
    secondaryInk = Color(0xFF6F6A5E),
    divider = Color(0xFF9A9485),
    accent = Color(0xFFB33A2B),
    onAccent = Color(0xFFF6F1E4),
    danger = Color(0xFFB33A2B),
    success = Color(0xFF3F6844),
    warning = Color(0xFF8D5F27),
    isDark = false,
)

val JuyueDarkPalette = JuyuePalette(
    background = Color(0xFF191A18),
    surface = Color(0xFF232420),
    elevated = Color(0xFF2B2C27),
    ink = Color(0xFFE9E1D2),
    secondaryInk = Color(0xFF9C9588),
    divider = Color(0xFF4A4841),
    accent = Color(0xFFD05A49),
    onAccent = Color(0xFF191A18),
    danger = Color(0xFFD05A49),
    success = Color(0xFF6E9A73),
    warning = Color(0xFFD2A24C),
    isDark = true,
)

private val JuyueTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 13.sp,
        lineHeight = 19.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.8.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        lineHeight = 15.sp,
    ),
)

@Composable
fun JuyueTheme(content: @Composable () -> Unit) {
    val palette = if (isSystemInDarkTheme()) JuyueDarkPalette else JuyueLightPalette
    JuyueTheme(palette = palette, content = content)
}

@Composable
fun JuyueTheme(
    palette: JuyuePalette,
    content: @Composable () -> Unit,
) {
    val scheme = if (palette.isDark) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            background = palette.background,
            onBackground = palette.ink,
            surface = palette.surface,
            onSurface = palette.ink,
            surfaceVariant = palette.elevated,
            onSurfaceVariant = palette.secondaryInk,
            outline = palette.divider,
            error = palette.danger,
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = palette.onAccent,
            background = palette.background,
            onBackground = palette.ink,
            surface = palette.surface,
            onSurface = palette.ink,
            surfaceVariant = palette.elevated,
            onSurfaceVariant = palette.secondaryInk,
            outline = palette.divider,
            error = palette.danger,
        )
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = JuyueTypography,
        content = content,
    )
}
