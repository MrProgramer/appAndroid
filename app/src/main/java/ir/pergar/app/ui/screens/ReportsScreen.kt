package ir.pergar.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.pergar.app.data.insights.Analytics
import ir.pergar.app.data.model.TxnType
import ir.pergar.app.data.util.JalaliDate
import ir.pergar.app.data.util.formatCompactMoney
import ir.pergar.app.data.util.formatMoney
import ir.pergar.app.data.util.toPersianDigits
import ir.pergar.app.ui.AppViewModel
import ir.pergar.app.ui.components.CategoryIcon
import ir.pergar.app.ui.components.DonutChart
import ir.pergar.app.ui.components.DonutSlice
import ir.pergar.app.ui.components.EmptyState
import ir.pergar.app.ui.components.GlassCard
import ir.pergar.app.ui.components.IncomeExpenseChart
import ir.pergar.app.ui.components.ScreenFrame
import ir.pergar.app.ui.components.SparklineChart
import ir.pergar.app.ui.components.UsageBar
import ir.pergar.app.ui.components.iconForKey
import ir.pergar.app.ui.components.parseHexColor
import ir.pergar.app.ui.theme.PergarThemeInfo

@Composable
fun ReportsScreen(vm: AppViewModel, asToman: Boolean) {
    val extras = PergarThemeInfo.colors
    val txns by vm.transactions.collectAsState()
    val categories by vm.categories.collectAsState()
    var monthDate by remember { mutableStateOf(JalaliDate.today()) }
    val monthKey = monthDate.monthKey()
    val range = remember(monthKey) { Analytics.monthRange(monthKey) }
    val income = remember(txns, range) { Analytics.sum(txns, TxnType.INCOME, range.first, range.second) }
    val expense = remember(txns, range) { Analytics.sum(txns, TxnType.EXPENSE, range.first, range.second) }
    val savings = income - expense
    val flows = remember(txns, monthDate) { Analytics.monthlyFlows(txns, 5, monthDate) }
    val slices = remember(txns, categories, range) {
        Analytics.categorySlices(txns, categories, TxnType.EXPENSE, range.first, range.second)
    }
    val daily = remember(txns, monthKey) {
        val map = Analytics.dailyExpenses(txns, range.first, range.second)
        (1..monthDate.monthLength()).map { (map[it] ?: 0L).toFloat() }
    }
    val savingsRate = if (income > 0) (savings.toFloat() / income * 100).toInt() else 0

    ScreenFrame(
        title = "گزارش‌های مالی",
        subtitle = "تحلیل درآمد، هزینه و الگوی خرج"
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                GlassCard(padding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { monthDate = monthDate.addMonths(-1) }) {
                            Icon(Icons.Rounded.ChevronRight, contentDescription = "ماه قبل")
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${monthDate.monthName} ${toPersianDigits(monthDate.year.toString())}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "نرخ پس‌انداز: ${toPersianDigits(savingsRate.toString())}٪",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (savings >= 0) extras.success else extras.danger
                            )
                        }
                        IconButton(onClick = { monthDate = monthDate.addMonths(1) }) {
                            Icon(Icons.Rounded.ChevronLeft, contentDescription = "ماه بعد")
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryCell(
                        label = "درآمد",
                        value = formatCompactMoney(income, asToman),
                        color = extras.success,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCell(
                        label = "هزینه",
                        value = formatCompactMoney(expense, asToman),
                        color = extras.danger,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCell(
                        label = "پس‌انداز",
                        value = formatCompactMoney(savings, asToman),
                        color = if (savings >= 0) extras.gradientStart else extras.warning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                GlassCard(padding = 16.dp) {
                    Text(
                        text = "روند درآمد و هزینه",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    IncomeExpenseChart(flows = flows)
                }
            }

            item {
                GlassCard(padding = 16.dp) {
                    Text(
                        text = "روند خرج روزانه",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "مجموع هزینه هر روز ماه",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    SparklineChart(
                        values = daily,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                    )
                }
            }

            item {
                GlassCard(padding = 16.dp) {
                    Text(
                        text = "ترکیب هزینه‌ها",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))
                    if (slices.isEmpty()) {
                        EmptyState(
                            icon = iconForKey("pie"),
                            title = "هزینه‌ای در این ماه نیست",
                            message = "با ثبت خریدها، ترکیب دسته‌های هزینه نمایش داده می‌شود."
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            DonutChart(
                                slices = slices.map {
                                    DonutSlice(it.totalRial, parseHexColor(it.category.colorHex))
                                },
                                modifier = Modifier.size(172.dp),
                                strokeWidth = 22.dp
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = formatCompactMoney(expense, asToman),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "هزینه ماه",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        slices.take(6).forEach { slice ->
                            val percent = if (expense > 0) {
                                (slice.totalRial.toFloat() / expense * 100).toInt()
                            } else 0
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CategoryIcon(
                                    iconKey = slice.category.iconKey,
                                    colorHex = slice.category.colorHex,
                                    size = 34.dp,
                                    iconSize = 17.dp
                                )
                                Spacer(Modifier.width(9.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = slice.category.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${toPersianDigits(percent.toString())}٪",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    UsageBar(
                                        progress = percent / 100f,
                                        color = parseHexColor(slice.category.colorHex)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = formatMoney(slice.totalRial, withUnit = false, asToman = asToman),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            item {
                GlassCard(padding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "مانده ماه",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatMoney(savings, withUnit = true, asToman = asToman),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (savings >= 0) extras.success else extras.danger
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCell(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier, padding = 12.dp) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
