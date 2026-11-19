package ir.pergar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun iconForKey(key: String): ImageVector = when (key) {
    "restaurant" -> Icons.Rounded.Restaurant
    "shopping" -> Icons.Rounded.ShoppingBag
    "car" -> Icons.Rounded.DirectionsCar
    "receipt" -> Icons.Rounded.ReceiptLong
    "health" -> Icons.Rounded.MedicalServices
    "home" -> Icons.Rounded.Home
    "fun" -> Icons.Rounded.CardGiftcard
    "education" -> Icons.Rounded.School
    "loan" -> Icons.Rounded.AccountBalance
    "salary" -> Icons.Rounded.Payments
    "trend" -> Icons.Rounded.TrendingUp
    "gift" -> Icons.Rounded.CardGiftcard
    "plus" -> Icons.Rounded.Add
    "flag" -> Icons.Rounded.Flag
    "radar" -> Icons.Rounded.Radar
    "chart" -> Icons.Rounded.Insights
    "warning" -> Icons.Rounded.WarningAmber
    "target" -> Icons.Rounded.GpsFixed
    "calendar" -> Icons.Rounded.Event
    "pie" -> Icons.Rounded.PieChart
    "wallet" -> Icons.Rounded.Wallet
    "card" -> Icons.Rounded.CreditCard
    "savings" -> Icons.Rounded.Savings
    "sms" -> Icons.Rounded.Sms
    "people" -> Icons.Rounded.Group
    "task" -> Icons.Rounded.Checklist
    "light" -> Icons.Rounded.Lightbulb
    "security" -> Icons.Rounded.Security
    "upload" -> Icons.Rounded.CloudUpload
    "download" -> Icons.Rounded.CloudDownload
    "settings" -> Icons.Rounded.Settings
    "swap" -> Icons.Rounded.SwapHoriz
    "person" -> Icons.Rounded.Person
    else -> Icons.Rounded.MoreHoriz
}

@Composable
fun CategoryIcon(
    iconKey: String,
    colorHex: String,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp
) {
    val color = parseHexColor(colorHex)
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconForKey(iconKey),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(iconSize)
        )
    }
}

fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        Color(0xFF4C7DFF)
    }
}
