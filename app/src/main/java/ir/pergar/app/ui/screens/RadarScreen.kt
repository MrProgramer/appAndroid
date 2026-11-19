package ir.pergar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.pergar.app.data.insights.Analytics
import ir.pergar.app.data.insights.InsightEngine
import ir.pergar.app.data.model.InsightTone
import ir.pergar.app.data.model.TxnType
import ir.pergar.app.data.util.formatCompactMoney
import ir.pergar.app.data.util.toPersianDigits
import ir.pergar.app.ui.AppViewModel
import ir.pergar.app.ui.components.GlassCard
import ir.pergar.app.ui.components.ScoreGauge
import ir.pergar.app.ui.components.ScreenFrame
import ir.pergar.app.ui.components.WideInfoCard
import ir.pergar.app.ui.components.iconForKey
import ir.pergar.app.ui.theme.PergarThemeInfo
import kotlin.math.roundToInt

@Composable
fun RadarScreen(vm: AppViewModel, asToman: Boolean, onBack: () -> Unit) {
    val extras = PergarThemeInfo.colors
    val txns by vm.transactions.collectAsState()
    val categories by vm.categories.collectAsState()
    val budgets by vm.budgets.collectAsState()
    val loans by vm.loans.collectAsState()
    val debts by vm.debts.collectAsState()
    val goals by vm.goals.collectAsState()
    val radar = remember(txns, categories, budgets, loans, debts, goals) {
        InsightEngine.analyze(txns, categories, budgets, loans, debts, goals)
    }
    val today = remember { ir.pergar.app.data.util.JalaliDate.today() }
    val range = remember(today) { Analytics.monthRange(today) }
    val income = Analytics.sum(txns, TxnType.INCOME, range.first, range.second)
    val expense = Analytics.sum(txns, TxnType.EXPENSE, range.first, range.second)
    val primaryColor = MaterialTheme.colorScheme.primary

    fun toneColor(tone: InsightTone): Color = when (tone) {
        InsightTone.POSITIVE -> extras.success
        InsightTone.WARNING -> extras.warning
        InsightTone.NEUTRAL -> primaryColor
    }

    ScreenFrame(
        title = "رادار مالی",
        subtitle = "تحلیل الگوی مالی و هشدارهای هوشمند",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            GlassCard(padding = 20.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ScoreGauge(
                        score = radar.score,
                        modifier = Modifier.size(184.dp),
                        strokeWidth = 15.dp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = when {
                            radar.score >= 80 -> "وضعیت مالی بسیار خوب"
                            radar.score >= 60 -> "وضعیت مالی متعادل"
                            radar.score >= 40 -> "نیازمند توجه"
                            else -> "هشدار مالی"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "نرخ پس‌انداز این ماه: ${toPersianDigits((radar.savingsRate * 100).roundToInt().toString())}٪",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        RadarStat("درآمد ماه", formatCompactMoney(income, asToman), extras.success, Modifier.weight(1f))
                        RadarStat("هزینه ماه", formatCompactMoney(expense, asToman), extras.danger, Modifier.weight(1f))
                        RadarStat(
                            "پس‌انداز",
                            formatCompactMoney(income - expense, asToman),
                            if (income >= expense) extras.gradientStart else extras.warning,
                            Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "بینش‌های هوشمند",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))

            radar.insights.forEach { insight ->
                val color = toneColor(insight.tone)
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    padding = 14.dp
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(color.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = iconForKey(insight.iconKey),
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = insight.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = insight.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            WideInfoCard(
                icon = iconForKey("security"),
                title = "رادار چگونه کار می‌کند؟",
                message = "پرگار با بررسی تراکنش‌ها، بودجه‌ها، اقساط و بدهی‌های شما، نرخ پس‌انداز و نقاط ریسک را تحلیل می‌کند. این تحلیل‌ها روی دستگاه شما انجام می‌شود.",
                accent = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RadarStat(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}
