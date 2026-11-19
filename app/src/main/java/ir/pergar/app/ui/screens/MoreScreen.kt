package ir.pergar.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.pergar.app.ui.Routes
import ir.pergar.app.ui.components.FeatureTile
import ir.pergar.app.ui.components.GlassCard
import ir.pergar.app.ui.components.ScreenFrame
import ir.pergar.app.ui.components.iconForKey
import ir.pergar.app.ui.theme.PergarThemeInfo

private data class MoreItem(
    val title: String,
    val subtitle: String,
    val iconKey: String,
    val route: String,
    val accent: Color
)

@Composable
fun MoreScreen(onOpen: (String) -> Unit) {
    val extras = PergarThemeInfo.colors
    val items = listOf(
        MoreItem("حساب‌ها", "کارت‌ها و کیف پول", "wallet", Routes.ACCOUNTS, extras.gradientStart),
        MoreItem("مرکز پیامک", "اسکن و ثبت خودکار", "sms", Routes.SMS, extras.gradientEnd),
        MoreItem("اقساط و وام", "سررسید و پرداخت", "loan", Routes.LOANS, extras.gold),
        MoreItem("طلب و بدهی", "تسویه با اشخاص", "people", Routes.DEBTS, extras.warning),
        MoreItem("اهداف مالی", "برنامه‌ریز پس‌انداز", "flag", Routes.GOALS, extras.success),
        MoreItem("کارها و یادآورها", "پیگیری تعهدات", "task", Routes.TASKS, extras.gradientStart),
        MoreItem("تقسیم هزینه", "دونگی و تسویه", "people", Routes.SPLIT, extras.gradientEnd),
        MoreItem("تقویم مالی", "نمای ماهانه شمسی", "calendar", Routes.CALENDAR, extras.gold),
        MoreItem("رادار مالی", "تحلیل هوشمند", "radar", Routes.RADAR, extras.success),
        MoreItem("آموزش مالی", "مفاهیم کاربردی", "light", Routes.EDUCATION, extras.warning),
        MoreItem("تنظیمات", "ظاهر، امنیت، داده", "settings", Routes.SETTINGS, MaterialTheme.colorScheme.onSurfaceVariant),
        MoreItem("درباره پرگار", "نسخه و سازنده", "security", Routes.SETTINGS, MaterialTheme.colorScheme.primary)
    )

    ScreenFrame(
        title = "همه امکانات",
        subtitle = "پرگار، دستیار مالی روزمره شما"
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items.chunked(2)) { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowItems.forEach { item ->
                        FeatureTile(
                            icon = iconForKey(item.iconKey),
                            title = item.title,
                            subtitle = item.subtitle,
                            accent = item.accent,
                            onClick = { onOpen(item.route) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
            item {
                Spacer(Modifier.height(4.dp))
                GlassCard(padding = 16.dp) {
                    Text(
                        text = "پرگار",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "مدیریت هوشمند امور مالی شخصی — نسخه ۱.۰.۰",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "طراحی و توسعه: کوروش پرگار\nهمه داده‌ها روی دستگاه شما نگهداری می‌شود.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}
