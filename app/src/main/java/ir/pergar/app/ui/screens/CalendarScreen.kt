package ir.pergar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.pergar.app.data.db.TxnEntity
import ir.pergar.app.data.insights.Analytics
import ir.pergar.app.data.model.TxnType
import ir.pergar.app.data.util.JalaliDate
import ir.pergar.app.data.util.formatCompactMoney
import ir.pergar.app.data.util.formatJalaliLong
import ir.pergar.app.data.util.formatTime
import ir.pergar.app.data.util.toPersianDigits
import ir.pergar.app.ui.AppViewModel
import ir.pergar.app.ui.components.EmptyState
import ir.pergar.app.ui.components.GlassCard
import ir.pergar.app.ui.components.ScreenFrame
import ir.pergar.app.ui.components.TransactionRow
import ir.pergar.app.ui.components.iconForKey
import ir.pergar.app.ui.theme.PergarThemeInfo

@Composable
fun CalendarScreen(vm: AppViewModel, asToman: Boolean, onBack: () -> Unit) {
    val extras = PergarThemeInfo.colors
    val txns by vm.transactions.collectAsState()
    val categories by vm.categories.collectAsState()
    val accounts by vm.accounts.collectAsState()
    var monthDate by remember { mutableStateOf(JalaliDate.today()) }
    val today = remember { JalaliDate.today() }
    var selectedDay by remember {
        mutableIntStateOf(if (monthDate.year == today.year && monthDate.month == today.month) today.day else 1)
    }
    LaunchedEffect(monthDate) {
        selectedDay = if (monthDate.year == today.year && monthDate.month == today.month) today.day else 1
    }

    val categoryMap = remember(categories) { categories.associateBy { it.id } }
    val accountMap = remember(accounts) { accounts.associateBy { it.id } }

    val monthTxns = remember(txns, monthDate) {
        txns.filter {
            val date = JalaliDate.from(it.dateEpochMillis)
            date.year == monthDate.year && date.month == monthDate.month
        }
    }
    val dailyExpense = remember(monthTxns) {
        monthTxns.filter { it.type == TxnType.EXPENSE }
            .groupBy { JalaliDate.from(it.dateEpochMillis).day }
            .mapValues { entry -> entry.value.sumOf { it.amountRial } }
    }
    val monthExpense = dailyExpense.values.sum()
    val dayTxns = remember(monthTxns, selectedDay) {
        monthTxns.filter { JalaliDate.from(it.dateEpochMillis).day == selectedDay }
            .sortedByDescending { it.dateEpochMillis }
    }
    val maxDaily = dailyExpense.values.maxOrNull() ?: 1L

    ScreenFrame(
        title = "تقویم مالی",
        subtitle = "نمای هزینه‌ها و رویدادهای ماه",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
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
                            text = "جمع هزینه: ${formatCompactMoney(monthExpense, asToman)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { monthDate = monthDate.addMonths(1) }) {
                        Icon(Icons.Rounded.ChevronLeft, contentDescription = "ماه بعد")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            GlassCard(padding = 12.dp) {
                Row(Modifier.fillMaxWidth()) {
                    JalaliDate.WEEKDAYS.forEach { dayName ->
                        Text(
                            text = dayName.take(1),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                val first = JalaliDate(monthDate.year, monthDate.month, 1)
                val cells = buildList {
                    repeat(first.weekdayIndex()) { add(null) }
                    for (day in 1..first.monthLength()) add(day)
                    while (size % 7 != 0) add(null)
                }
                cells.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEach { day ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(2.dp)
                                    .height(46.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day != null) {
                                    val isSelected = day == selectedDay
                                    val isToday = monthDate.year == today.year &&
                                        monthDate.month == today.month && day == today.day
                                    val expense = dailyExpense[day]
                                    val container = when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(container)
                                            .border(
                                                width = if (isToday && !isSelected) 1.dp else 0.dp,
                                                color = if (isToday && !isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                                else androidx.compose.ui.graphics.Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedDay = day },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = toPersianDigits(day.toString()),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                        if (expense != null) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 3.dp)
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                        else extras.danger.copy(
                                                            alpha = (0.35f + 0.65f * (expense.toFloat() / maxDaily)).coerceAtMost(1f)
                                                        )
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "رویدادهای ${toPersianDigits(selectedDay.toString())} ${monthDate.monthName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${toPersianDigits(dayTxns.size.toString())} تراکنش",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            GlassCard(padding = 10.dp) {
                if (dayTxns.isEmpty()) {
                    EmptyState(
                        icon = iconForKey("calendar"),
                        title = "رویدادی ثبت نشده",
                        message = "برای این روز تراکنشی وجود ندارد. از تقویم روز دیگری را انتخاب کنید."
                    )
                } else {
                    val dayExpense = dayTxns.filter { it.type == TxnType.EXPENSE }.sumOf { it.amountRial }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatJalaliLong(dayTxns.first().dateEpochMillis),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "خرج روز: ${formatCompactMoney(dayExpense, asToman)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = extras.danger
                        )
                    }
                    dayTxns.forEach { txn: TxnEntity ->
                        TransactionRow(
                            txn = txn,
                            category = txn.categoryId?.let { categoryMap[it] },
                            accountName = txn.accountId?.let { accountMap[it]?.name },
                            asToman = asToman
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "زمان ثبت: ${formatTime(System.currentTimeMillis())} · همه زمان‌ها به وقت محلی",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
