package ir.pergar.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ir.pergar.app.R

val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

val PergarTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 48.sp
    ),
    displayMedium = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 40.sp
    ),
    displaySmall = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 36.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, lineHeight = 30.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 21.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 23.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp
    )
)
