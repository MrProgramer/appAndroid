package ir.pergar.app.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.pergar.app.data.db.Budget
import ir.pergar.app.data.db.Category
import ir.pergar.app.data.insights.Analytics
import ir.pergar.app.data.model.TxnType
import ir.pergar.app.data.util.JalaliDate
import ir.pergar.app.data.util.formatCompactMoney
import ir.pergar.app.data.util.formatJalaliLong
import ir.pergar.app.data.util.formatMoney
import ir.pergar.app.data.util.toPersianDigits
import ir.pergar.app.ui.AppViewModel
import ir.pergar.app.ui.components.AmountField
import ir.pergar.app.ui.components.CategoryIcon
import ir.pergar.app.ui.components.ChoiceDialog
import ir.pergar.app.ui.components.DonutChart
import ir.pergar.app.ui.components.DonutSlice
import ir.pergar.app.ui.components.EmptyState
import ir.pergar.app.ui.components.GlassCard
import ir.pergar.app.ui.components.LabeledValueRow
import ir.pergar.app.ui.components.PergarButton
import ir.pergar.app.ui.components.PergarDialog
import ir.pergar.app.ui.components.ProgressBar
import ir.pergar.app.ui.components.ScreenFrame
import ir.pergar.app.ui.components.SelectRow
import ir.pergar.app.ui.components.iconForKey
import ir.pergar.app.ui.components.parseHexColor
import ir.pergar.app.ui.theme.PergarThemeInfo
import kotlin.math.abs

@Composable
fun BudgetsScreen(vm: AppViewModel, asToman: Boolean) {
    val extras = PergarThemeInfo.colors
    val budgets by vm.budgets.collectAsState()
    val categories by vm.categories.collectAsState()
    val txns by vm.transactions.collectAsState()

    var monthDate by remember { mutableStateOf(JalaliDate.today()) }
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Budget?>(null) }
    val monthKey = monthDate.monthKey()
    val monthRange = remember(monthKey) { Analytics.monthRange(monthKey) }
    val monthBudgets = remember(budgets, monthKey) { budgets.filter { it.monthKey == monthKey } }
    val expenseCategories = remember(categories) { categories.filter { it.type == TxnType.EXPENSE } }
    val categoryMap = remember(categories) { categories.associateBy { it.id } }

    fun spentFor(budget: Budget): Long = Analytics.spentForCategory(
        txns = txns,
        categoryId = budget.categoryId,
        start = monthRange.first,
        end = monthRange.second
    )

    val totalLimit = monthBudgets.sumOf { it.limitRial }
    val totalSpent = monthBudgets.sumOf { spentFor(it) }
    val monthExpense = Analytics.sum(txns, TxnType.EXPENSE, monthRange.first, monthRange.second)
    val overallBudget = monthBudgets.firstOrNull { it.categoryId == null }
    val remaining = if (overallBudget != null) overallBudget.limitRial - monthExpense else totalLimit - totalSpent

    ScreenFrame(
        title = "بودجه‌بندی",
        subtitle = "کنترل هوشمند هزینه‌های ماه",
        actions = {
            IconButton(onClick = { showAdd = true }) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "بودجه جدید",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                text = "${toPersianDigits(monthBudgets.size.toString())} بودجه فعال",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { monthDate = monthDate.addMonths(1) }) {
                            Icon(Icons.Rounded.ChevronLeft, contentDescription = "ماه بعد")
                        }
                    }
                }
            }

            item {
                GlassCard(padding = 18.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DonutChart(
                            slices = if (monthBudgets.isEmpty()) emptyList() else monthBudgets.map { budget ->
                                val category = budget.categoryId?.let { categoryMap[it] }
                                DonutSlice(
                                    value = spentFor(budget),
                                    color = category?.let { parseHexColor(it.colorHex) } ?: extras.gold
                                )
                            },
                            modifier = Modifier.size(132.dp),
                            strokeWidth = 16.dp
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formatCompactMoney(monthExpense, asToman),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "خرج ماه",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            LabeledValueRow(
                                label = "بودجه کل",
                                value = if (totalLimit > 0) formatMoney(totalLimit, withUnit = false, asToman = asToman) else "—"
                            )
                            Spacer(Modifier.height(6.dp))
                            LabeledValueRow(
                                label = "مانده",
                                value = if (totalLimit > 0) formatMoney(remaining, withUnit = false, asToman = asToman) else "—",
                                valueColor = if (remaining < 0) extras.danger else extras.success
                            )
                            Spacer(Modifier.height(6.dp))
                            LabeledValueRow(
                                label = "مصرف‌شده",
                                value = formatMoney(totalSpent, withUnit = false, asToman = asToman)
                            )
                            Spacer(Modifier.height(10.dp))
                            ProgressBar(
                                progress = if (overallBudget != null && overallBudget.limitRial > 0) {
                                    monthExpense.toFloat() / overallBudget.limitRial
                                } else if (totalLimit > 0) {
                                    totalSpent.toFloat() / totalLimit
                                } else 0f,
                                color = when {
                                    remaining < 0 -> extras.danger
                                    totalLimit > 0 && totalSpent.toFloat() / totalLimit > 0.85f -> extras.warning
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                    }
                }
            }

            if (monthBudgets.isEmpty()) {
                item {
                    EmptyState(
                        icon = iconForKey("pie"),
                        title = "بودجه‌ای برای این ماه نیست",
                        message = "برای دسته‌های اصلی هزینه سقف تعیین کنید تا رادار مالی هشدار دهد.",
                        actionLabel = "ساخت بودجه",
                        onAction = { showAdd = true }
                    )
                }
            } else {
                items(monthBudgets, key = { it.id }) { budget ->
                    val category = budget.categoryId?.let { categoryMap[it] }
                    val spent = if (budget.categoryId == null) monthExpense else spentFor(budget)
                    val progress = if (budget.limitRial > 0) spent.toFloat() / budget.limitRial else 0f
                    val color = when {
                        progress > 1f -> extras.danger
                        progress > 0.85f -> extras.warning
                        else -> category?.let { parseHexColor(it.colorHex) } ?: MaterialTheme.colorScheme.primary
                    }
                    GlassCard(
                        padding = 14.dp,
                        onClick = { editing = budget }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryIcon(
                                iconKey = category?.iconKey ?: "pie",
                                colorHex = category?.colorHex ?: "#F5B942",
                                size = 42.dp,
                                iconSize = 21.dp
                            )
                            Spacer(Modifier.width(11.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = category?.name ?: "کل هزینه‌های ماه",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${formatMoney(spent, withUnit = false, asToman = asToman)} از ${formatMoney(budget.limitRial, withUnit = false, asToman = asToman)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${toPersianDigits((progress * 100).toInt().toString())}٪",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = color
                                )
                                Text(
                                    text = if (spent > budget.limitRial) {
                                        "${formatCompactMoney(abs(budget.limitRial - spent), asToman)} بیشتر"
                                    } else {
                                        "${formatCompactMoney(budget.limitRial - spent, asToman)} باقی"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (spent > budget.limitRial) extras.danger else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        ProgressBar(progress = progress, color = color)
                    }
                }
            }

            item {
                GlassCard(
                    padding = 14.dp,
                    onClick = { showAdd = true }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "افزودن بودجه برای دسته دیگر",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        BudgetDialog(
            initial = null,
            monthLabel = formatJalaliLong(monthDate.toEpochMillis()),
            categories = expenseCategories,
            asToman = asToman,
            onDismiss = { showAdd = false },
            onSave = { categoryId, limitRial ->
                vm.saveBudget(categoryId, limitRial, monthKey)
                showAdd = false
            }
        )
    }
    editing?.let { budget ->
        BudgetDialog(
            initial = budget,
            monthLabel = formatJalaliLong(monthDate.toEpochMillis()),
            categories = expenseCategories,
            asToman = asToman,
            onDismiss = { editing = null },
            onSave = { categoryId, limitRial ->
                vm.saveBudget(categoryId, limitRial, monthKey)
                editing = null
            },
            onDelete = {
                vm.deleteBudget(budget)
                editing = null
            }
        )
    }
}

@Composable
private fun BudgetDialog(
    initial: Budget?,
    monthLabel: String,
    categories: List<Category>,
    asToman: Boolean,
    onDismiss: () -> Unit,
    onSave: (categoryId: Long?, limitRial: Long) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val options = remember(categories) { listOf("کل هزینه‌های ماه") + categories.map { it.name } }
    var categoryIndex by remember(initial, categories) {
        mutableIntStateOf(
            categories.indexOfFirst { it.id == initial?.categoryId }
                .let { if (it >= 0) it + 1 else 0 }
        )
    }
    var digits by remember(initial) {
        mutableStateOf(
            initial?.let {
                val value = if (asToman) it.limitRial / 10 else it.limitRial
                value.toString()
            } ?: ""
        )
    }
    var showChoices by remember { mutableStateOf(false) }
    val selectedCategory = categories.getOrNull(categoryIndex - 1)

    PergarDialog(
        title = if (initial == null) "بودجه جدید" else "ویرایش بودجه",
        onDismiss = onDismiss
    ) {
        Text(
            text = monthLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        if (initial == null) {
            SelectRow(
                label = "دسته‌بندی",
                value = options.getOrElse(categoryIndex) { options.first() },
                onClick = { showChoices = true }
            )
        } else {
            LabeledValueRow(
                label = "دسته‌بندی",
                value = selectedCategory?.name ?: "کل هزینه‌های ماه"
            )
        }
        Spacer(Modifier.height(12.dp))
        AmountField(
            digits = digits,
            onDigitsChange = { digits = it },
            asToman = asToman,
            label = "سقف بودجه"
        )
        Spacer(Modifier.height(16.dp))
        PergarButton(
            text = "ذخیره بودجه",
            onClick = {
                val amount = (digits.toLongOrNull() ?: 0L).let { if (asToman) it * 10 else it }
                if (amount > 0) {
                    onSave(if (categoryIndex == 0) null else selectedCategory?.id, amount)
                }
            }
        )
        if (onDelete != null) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "حذف این بودجه",
                    style = MaterialTheme.typography.labelMedium,
                    color = PergarThemeInfo.colors.danger,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onDelete)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }

    if (showChoices) {
        ChoiceDialog(
            title = "دسته‌بندی بودجه",
            options = options,
            selectedIndex = categoryIndex,
            onDismiss = { showChoices = false },
            onSelect = { categoryIndex = it }
        )
    }
}
