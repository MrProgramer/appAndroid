package ir.pergar.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.pergar.app.data.db.Goal
import ir.pergar.app.data.util.JalaliDate
import ir.pergar.app.data.util.daysUntilLabel
import ir.pergar.app.data.util.formatJalaliLong
import ir.pergar.app.data.util.formatMoney
import ir.pergar.app.data.util.toPersianDigits
import ir.pergar.app.ui.AppViewModel
import ir.pergar.app.ui.components.AmountField
import ir.pergar.app.ui.components.CategoryIcon
import ir.pergar.app.ui.components.EmptyState
import ir.pergar.app.ui.components.GlassCard
import ir.pergar.app.ui.components.JalaliDatePickerDialog
import ir.pergar.app.ui.components.PergarButton
import ir.pergar.app.ui.components.PergarDialog
import ir.pergar.app.ui.components.PergarTextField
import ir.pergar.app.ui.components.ProgressBar
import ir.pergar.app.ui.components.ScreenFrame
import ir.pergar.app.ui.components.SelectRow
import ir.pergar.app.ui.components.StrokeRing
import ir.pergar.app.ui.components.TinyTag
import ir.pergar.app.ui.components.iconForKey
import ir.pergar.app.ui.theme.PergarThemeInfo

private val goalColors = listOf("#22C55E", "#4C7DFF", "#F5B942", "#EC4899", "#8B5CF6", "#06B6D4")

@Composable
fun GoalsScreen(vm: AppViewModel, asToman: Boolean, onBack: () -> Unit) {
    val extras = PergarThemeInfo.colors
    val goals by vm.goals.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Goal?>(null) }
    var contributeTarget by remember { mutableStateOf<Goal?>(null) }
    var pendingDelete by remember { mutableStateOf<Goal?>(null) }

    val totalSaved = goals.sumOf { it.savedRial }
    val totalTarget = goals.sumOf { it.targetRial }

    ScreenFrame(
        title = "برنامه‌ریز مالی",
        subtitle = "اهداف و پس‌انداز هدفمند",
        onBack = onBack,
        actions = {
            IconButton(onClick = { showAdd = true }) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "هدف جدید",
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
                GlassCard(padding = 16.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "پس‌انداز اهداف",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = formatMoney(totalSaved, withUnit = true, asToman = asToman),
                                style = MaterialTheme.typography.headlineSmall,
                                color = extras.success
                            )
                            Text(
                                text = "از ${formatMoney(totalTarget, withUnit = true, asToman = asToman)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StrokeRing(
                            progress = if (totalTarget > 0) totalSaved.toFloat() / totalTarget else 0f,
                            color = extras.success,
                            modifier = Modifier.size(68.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    ProgressBar(
                        progress = if (totalTarget > 0) totalSaved.toFloat() / totalTarget else 0f,
                        color = extras.success
                    )
                }
            }

            if (goals.isEmpty()) {
                item {
                    EmptyState(
                        icon = iconForKey("flag"),
                        title = "هنوز هدفی تعریف نشده",
                        message = "برای سفر، خرید بزرگ یا پس‌انداز اضطراری هدف بسازید و پیشرفت را دنبال کنید.",
                        actionLabel = "ساخت هدف",
                        onAction = { showAdd = true }
                    )
                }
            } else {
                items(goals, key = { it.id }) { goal ->
                    val progress = if (goal.targetRial > 0) {
                        (goal.savedRial.toFloat() / goal.targetRial).coerceIn(0f, 1f)
                    } else 0f
                    val completed = goal.savedRial >= goal.targetRial
                    GlassCard(
                        padding = 14.dp,
                        onClick = { editing = goal }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryIcon(
                                iconKey = goal.iconKey,
                                colorHex = goal.colorHex,
                                size = 44.dp,
                                iconSize = 22.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = goal.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${formatMoney(goal.savedRial, withUnit = false, asToman = asToman)} از ${formatMoney(goal.targetRial, withUnit = false, asToman = asToman)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (completed) {
                                TinyTag("تکمیل شد", extras.success)
                            } else {
                                Text(
                                    text = "${toPersianDigits((progress * 100).toInt().toString())}٪",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        ProgressBar(
                            progress = progress,
                            color = if (completed) extras.success else MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (goal.targetEpochMillis != null) {
                                    "مهلت: ${formatJalaliLong(goal.targetEpochMillis)} · ${daysUntilLabel(goal.targetEpochMillis)}"
                                } else {
                                    "بدون مهلت زمانی"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!completed) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { contributeTarget = goal }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(3.dp))
                                    Text(
                                        text = "پس‌انداز",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        GoalDialog(
            initial = null,
            colorHex = goalColors[goals.size % goalColors.size],
            asToman = asToman,
            onDismiss = { showAdd = false },
            onSave = { goal ->
                vm.saveGoal(goal, isNew = true)
                showAdd = false
            }
        )
    }
    editing?.let { goal ->
        GoalDialog(
            initial = goal,
            colorHex = goal.colorHex,
            asToman = asToman,
            onDismiss = { editing = null },
            onSave = { updated ->
                vm.saveGoal(updated, isNew = false)
                editing = null
            },
            onDelete = { pendingDelete = goal }
        )
    }
    contributeTarget?.let { goal ->
        ContributeDialog(
            goal = goal,
            asToman = asToman,
            onDismiss = { contributeTarget = null },
            onContribute = { amount ->
                vm.contributeGoal(goal, amount)
                contributeTarget = null
            },
            remaining = (goal.targetRial - goal.savedRial).coerceAtLeast(0)
        )
    }
    pendingDelete?.let { goal ->
        ir.pergar.app.ui.components.ConfirmDialog(
            title = "حذف هدف",
            message = "هدف «${goal.title}» حذف شود؟",
            confirmLabel = "حذف",
            onConfirm = {
                vm.deleteGoal(goal)
                pendingDelete = null
                editing = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun ContributeDialog(
    goal: Goal,
    asToman: Boolean,
    remaining: Long,
    onDismiss: () -> Unit,
    onContribute: (Long) -> Unit
) {
    var digits by remember { mutableStateOf("") }
    PergarDialog(title = "افزودن به هدف «${goal.title}»", onDismiss = onDismiss) {
        Text(
            text = "مانده تا هدف: ${formatMoney(remaining, withUnit = true, asToman = asToman)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        AmountField(
            digits = digits,
            onDigitsChange = { digits = it },
            asToman = asToman,
            label = "مبلغ پس‌انداز"
        )
        Spacer(Modifier.height(16.dp))
        PergarButton(
            text = "افزودن به پس‌انداز",
            enabled = (digits.toLongOrNull() ?: 0L) > 0,
            onClick = {
                val amount = (digits.toLongOrNull() ?: 0L).let { if (asToman) it * 10 else it }
                onContribute(amount)
            }
        )
    }
}

@Composable
private fun GoalDialog(
    initial: Goal?,
    colorHex: String,
    asToman: Boolean,
    onDismiss: () -> Unit,
    onSave: (Goal) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val extras = PergarThemeInfo.colors
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var targetDigits by remember {
        mutableStateOf(
            initial?.let {
                val value = if (asToman) it.targetRial / 10 else it.targetRial
                value.toString()
            } ?: ""
        )
    }
    var hasDeadline by remember { mutableStateOf(initial?.targetEpochMillis != null) }
    var deadline by remember { mutableStateOf(initial?.targetEpochMillis ?: System.currentTimeMillis()) }
    var showDate by remember { mutableStateOf(false) }

    PergarDialog(
        title = if (initial == null) "هدف مالی جدید" else "ویرایش هدف",
        onDismiss = onDismiss
    ) {
        PergarTextField(
            value = title,
            onValueChange = { title = it },
            label = "عنوان هدف",
            placeholder = "مثلاً سفر به شمال"
        )
        Spacer(Modifier.height(10.dp))
        AmountField(
            digits = targetDigits,
            onDigitsChange = { targetDigits = it },
            asToman = asToman,
            label = "مبلغ هدف"
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "مهلت زمانی",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "برای پیگیری و نمایش پیشرفت",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = hasDeadline, onCheckedChange = { hasDeadline = it })
        }
        if (hasDeadline) {
            Spacer(Modifier.height(8.dp))
            SelectRow(
                label = "تاریخ هدف",
                value = formatJalaliLong(deadline),
                onClick = { showDate = true }
            )
        }
        Spacer(Modifier.height(16.dp))
        PergarButton(
            text = "ذخیره هدف",
            enabled = title.isNotBlank() && (targetDigits.toLongOrNull() ?: 0L) > 0,
            onClick = {
                val target = (targetDigits.toLongOrNull() ?: 0L).let { if (asToman) it * 10 else it }
                onSave(
                    Goal(
                        id = initial?.id ?: 0,
                        title = title.trim(),
                        targetRial = target,
                        savedRial = initial?.savedRial ?: 0,
                        targetEpochMillis = if (hasDeadline) deadline else null,
                        iconKey = initial?.iconKey ?: "flag",
                        colorHex = colorHex,
                        createdAt = initial?.createdAt ?: System.currentTimeMillis()
                    )
                )
            }
        )
        if (onDelete != null) {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onDelete() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = null,
                        tint = extras.danger,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "حذف هدف",
                        style = MaterialTheme.typography.labelMedium,
                        color = extras.danger
                    )
                }
            }
        }
    }

    if (showDate) {
        JalaliDatePickerDialog(
            initialMillis = deadline,
            onDismiss = { showDate = false },
            onSelect = { deadline = it }
        )
    }
}
