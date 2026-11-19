package ir.pergar.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val DarkScheme = darkColorScheme(
    primary = Blue400,
    onPrimary = Color(0xFF04122E),
    primaryContainer = Color(0xFF1B2E5C),
    onPrimaryContainer = Color(0xFFD9E4FF),
    secondary = Cyan400,
    onSecondary = Color(0xFF04222A),
    secondaryContainer = Color(0xFF0F3440),
    onSecondaryContainer = Color(0xFFC8F6FF),
    tertiary = Gold400,
    onTertiary = Color(0xFF241A02),
    tertiaryContainer = Color(0xFF3C2E08),
    onTertiaryContainer = Color(0xFFFFE7AF),
    background = Navy950,
    onBackground = TextLight,
    surface = Navy850,
    onSurface = TextLight,
    surfaceVariant = Navy800,
    onSurfaceVariant = TextMutedDark,
    error = Red400,
    onError = Color(0xFF2B0710),
    errorContainer = Color(0xFF3D1018),
    onErrorContainer = Color(0xFFFFD9DD),
    outline = Color(0xFF2A3752),
    outlineVariant = Color(0xFF1B2740),
    scrim = Color(0xCC000000),
    inverseSurface = Color(0xFFEDF2FB),
    inverseOnSurface = Navy900,
    inversePrimary = Blue600
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF1F4ED8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE6FF),
    onPrimaryContainer = Color(0xFF0A1F52),
    secondary = Color(0xFF0891B2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6F4FA),
    onSecondaryContainer = Color(0xFF03313A),
    tertiary = Color(0xFFB45309),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE9C7),
    onTertiaryContainer = Color(0xFF3A2503),
    background = LightBg,
    onBackground = TextDark,
    surface = LightSurface,
    onSurface = TextDark,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextMutedLight,
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFFE1E1),
    onErrorContainer = Color(0xFF450A0A),
    outline = LightOutline,
    outlineVariant = Color(0xFFE8EEF9),
    scrim = Color(0x99000000),
    inverseSurface = Navy900,
    inverseOnSurface = TextLight,
    inversePrimary = Blue400
)

val PergarShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

object PergarThemeInfo {
    val colors: PergarColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPergarColors.current
}

@Composable
fun PergarTheme(
    themeMode: String = "dark",
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val scheme = if (dark) DarkScheme else LightScheme
    val extras = if (dark) DarkPergar else LightPergar
    CompositionLocalProvider(
        LocalPergarColors provides extras,
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = PergarTypography,
            shapes = PergarShapes,
            content = content
        )
    }
}
