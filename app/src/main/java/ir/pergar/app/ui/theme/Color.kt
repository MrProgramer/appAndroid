package ir.pergar.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Brand palette — trust blue + profit green over a deep cinematic navy.
val Navy950 = Color(0xFF070B14)
val Navy900 = Color(0xFF0B1220)
val Navy850 = Color(0xFF0E1526)
val Navy800 = Color(0xFF131D33)
val Navy700 = Color(0xFF1B2740)

val Blue400 = Color(0xFF5B8CFF)
val Blue500 = Color(0xFF3D74FF)
val Blue600 = Color(0xFF2657E0)
val Cyan400 = Color(0xFF22D3EE)
val Teal400 = Color(0xFF2DD4A7)
val Green400 = Color(0xFF34D399)
val Green500 = Color(0xFF22C55E)
val Gold400 = Color(0xFFF5B942)
val Orange400 = Color(0xFFFB923C)
val Red400 = Color(0xFFFB7185)
val Red500 = Color(0xFFEF4444)

val LightBg = Color(0xFFF3F6FC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEAF0FB)
val LightOutline = Color(0xFFD5DEED)
val TextDark = Color(0xFF0B1220)
val TextMutedLight = Color(0xFF5B6B85)

val TextLight = Color(0xFFF3F7FF)
val TextMutedDark = Color(0xFF9AA9C2)
val TextFaintDark = Color(0xFF6B7A94)

@Immutable
data class PergarColors(
    val success: Color,
    val warning: Color,
    val danger: Color,
    val gold: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val gradientWarmStart: Color,
    val gradientWarmEnd: Color,
    val glassBorder: Color,
    val glassFill: Color,
    val positiveSoft: Color,
    val negativeSoft: Color,
    val chartIncome: Color,
    val chartExpense: Color,
    val available: Color,
    val isDark: Boolean
)

val DarkPergar = PergarColors(
    success = Green400,
    warning = Color(0xFFFBBF24),
    danger = Red400,
    gold = Gold400,
    gradientStart = Color(0xFF2F6BFF),
    gradientEnd = Color(0xFF18C7D8),
    gradientWarmStart = Color(0xFFF5B942),
    gradientWarmEnd = Color(0xFFF97316),
    glassBorder = Color(0x1AFFFFFF),
    glassFill = Color(0x0FFFFFFF),
    positiveSoft = Color(0x1F34D399),
    negativeSoft = Color(0x1FFB7185),
    chartIncome = Green400,
    chartExpense = Color(0xFF6F8BFF),
    available = Green400,
    isDark = true
)

val LightPergar = PergarColors(
    success = Color(0xFF0E9F6E),
    warning = Color(0xFFD97706),
    danger = Color(0xFFE11D48),
    gold = Color(0xFFD97706),
    gradientStart = Color(0xFF1D4ED8),
    gradientEnd = Color(0xFF0E7490),
    gradientWarmStart = Color(0xFFD97706),
    gradientWarmEnd = Color(0xFFEA580C),
    glassBorder = Color(0x14000000),
    glassFill = Color(0xCCFFFFFF),
    positiveSoft = Color(0x1A0E9F6E),
    negativeSoft = Color(0x1AE11D48),
    chartIncome = Color(0xFF0E9F6E),
    chartExpense = Color(0xFF3B82F6),
    available = Color(0xFF0E9F6E),
    isDark = false
)

val LocalPergarColors = staticCompositionLocalOf { DarkPergar }
