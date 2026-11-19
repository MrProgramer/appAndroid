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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.pergar.app.data.db.TaskItem
import ir.pergar.app.data.util.JalaliDate
import ir.pergar.app.data.util.daysUntilLabel
import ir.pergar.app.data.util.formatJalaliLong
import ir.pergar.app.data.util.formatTime
import ir.pergar.app.data.util.millisAtJalaliTime
import ir.pergar.app.data.util.toPersianDigits
import ir.pergar.app.ui.AppViewModel
import ir.pergar.app.ui.components.ConfirmDialog
import ir.pergar.app.ui.components.EmptyState
import ir.pergar.app.ui.components.GlassCard
import ir.pergar.app.ui.components.JalaliDatePickerDialog
import ir.pergar.app.ui.components.PergarButton
import ir.pergar.app.ui.components.PergarDialog
import ir.pergar.app.ui.components.PergarTextField
import ir.pergar.app.ui.components.ScreenFrame
import ir.pergar.app.ui.components.SegmentedControl
import ir.pergar.app.ui.components.SelectRow
import ir.pergar.app.ui.components.TinyTag
import ir.pergar.app.ui.components.iconForKey
import ir.pergar.app.ui.theme.PergarThemeInfo
import java.time.LocalTime

@Composable
fun TasksScreen(vm: AppViewModel, onBack: () -> Unit) {
    val extras = PergarThemeInfo.colors
    val tasks by vm.tasks.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<TaskItem?>(null) }
    val sorted = remember(tasks) {
        tasks.sortedWith(
            compareBy<TaskItem> { it.isDone }
                .thenByDescending { it.priority }
                .thenBy { it.dueAtMillis ?: Long.MAX_VALUE }
        )
    }
    val openCount = tasks.count { !it.isDone }

    ScreenFrame(
        title = "کارها و یادآورها",
        subtitle = "${toPersianDigits(openCount.toString())} کار باقی‌مانده",
        onBack = onBack,
        actions = {
            IconButton(onClick = { showAdd = true }) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "کار جدید",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (sorted.isEmpty()) {
                item {
                    EmptyState(
                        icon = iconForKey("task"),
                        title = "کاری ثبت نشده",
                        message = "پرداخت قسط، تمدید بیمه یا هر تعهد مالی را اینجا ثبت کنید تا یادآور بگیرید.",
                        actionLabel = "افزودن کار",
                        onAction = { showAdd = true }
                    )
                }
            }
            items(sorted, key = { it.id }) { task ->
                val priorityColor = when (task.priority) {
                    2 -> extras.danger
                    1 -> extras.warning
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                val isOverdue = task.dueAtMillis != null && !task.isDone &&
                    task.dueAtMillis < System.currentTimeMillis()
                GlassCard(padding = 12.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { vm.toggleTask(task) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (task.isDone) Icons.Rounded.CheckCircle
                                else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = if (task.isDone) "انجام شد" else "انجام نشده",
                                tint = if (task.isDone) extras.success else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (task.note.isNotBlank()) {
                                Text(
                                    text = task.note,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (task.dueAtMillis != null) {
                                Text(
                                    text = "${formatJalaliLong(task.dueAtMillis)} · ${formatTime(task.dueAtMillis)} · ${daysUntilLabel(task.dueAtMillis)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOverdue) extras.danger else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (task.priority > 0) {
                            TinyTag(
                                text = if (task.priority == 2) "فوری" else "مهم",
                                color = priorityColor
                            )
                        }
                        IconButton(
                            onClick = { pendingDelete = task },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "حذف",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        TaskDialog(
            onDismiss = { showAdd = false },
            onSave = { task ->
                vm.addTask(task)
                showAdd = false
            }
        )
    }
    pendingDelete?.let { task ->
        ConfirmDialog(
            title = "حذف کار",
            message = "کار «${task.title}» حذف شود؟",
            confirmLabel = "حذف",
            onConfirm = {
                vm.deleteTask(task)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun TaskDialog(
    onDismiss: () -> Unit,
    onSave: (TaskItem) -> Unit
) {
    val extras = PergarThemeInfo.colors
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var hasDue by remember { mutableStateOf(true) }
    var dueDate by remember { mutableStateOf(JalaliDate.today()) }
    var hour by remember { mutableStateOf(LocalTime.now().hour.toString()) }
    var minute by remember {
        mutableStateOf("%02d".format(LocalTime.now().minute))
    }
    var remind by remember { mutableStateOf(true) }
    var priority by remember { mutableIntStateOf(1) }
    var showDate by remember { mutableStateOf(false) }

    PergarDialog(title = "کار جدید", onDismiss = onDismiss) {
        PergarTextField(
            value = title,
            onValueChange = { title = it },
            label = "عنوان کار",
            placeholder = "مثلاً پرداخت قبض برق"
        )
        Spacer(Modifier.height(10.dp))
        PergarTextField(
            value = note,
            onValueChange = { note = it },
            label = "توضیح (اختیاری)",
            singleLine = false
        )
        Spacer(Modifier.height(10.dp))
        SegmentedControl(
            options = listOf("عادی", "مهم", "فوری"),
            selectedIndex = priority,
            onSelect = { priority = it }
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "زمان‌بندی",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "تاریخ و ساعت انجام کار",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = hasDue, onCheckedChange = { hasDue = it })
        }
        if (hasDue) {
            Spacer(Modifier.height(8.dp))
            SelectRow(
                label = "تاریخ",
                value = formatJalaliLong(dueDate.toEpochMillis()),
                onClick = { showDate = true }
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PergarTextField(
                    value = hour,
                    onValueChange = { input -> hour = input.filter { it.isDigit() }.take(2) },
                    label = "ساعت",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                PergarTextField(
                    value = minute,
                    onValueChange = { input -> minute = input.filter { it.isDigit() }.take(2) },
                    label = "دقیقه",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "یادآور",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "اعلان در زمان تعیین‌شده",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = remind, onCheckedChange = { remind = it })
            }
        }
        Spacer(Modifier.height(16.dp))
        PergarButton(
            text = "ذخیره کار",
            enabled = title.isNotBlank(),
            onClick = {
                val dueAt = if (hasDue) {
                    val h = (hour.toIntOrNull() ?: 0).coerceIn(0, 23)
                    val m = (minute.toIntOrNull() ?: 0).coerceIn(0, 59)
                    millisAtJalaliTime(dueDate, h, m)
                } else null
                onSave(
                    TaskItem(
                        title = title.trim(),
                        note = note.trim(),
                        dueAtMillis = dueAt,
                        remind = remind && dueAt != null,
                        isDone = false,
                        priority = priority
                    )
                )
            }
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "یادآورها فقط روی همین دستگاه ثبت می‌شوند.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showDate) {
        JalaliDatePickerDialog(
            initialMillis = dueDate.toEpochMillis(),
            onDismiss = { showDate = false },
            onSelect = { millis -> dueDate = JalaliDate.from(millis) }
        )
    }
}
