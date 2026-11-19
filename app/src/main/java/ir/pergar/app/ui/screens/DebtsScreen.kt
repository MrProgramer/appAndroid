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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.pergar.app.data.db.Debt
import ir.pergar.app.data.model.DebtDirection
import ir.pergar.app.data.util.daysUntilLabel
import ir.pergar.app.data.util.formatJalaliLong
import ir.pergar.app.data.util.formatMoney
import ir.pergar.app.data.util.toPersianDigits
import ir.pergar.app.ui.AppViewModel
import ir.pergar.app.ui.components.AmountField
import ir.pergar.app.ui.components.CategoryIcon
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

@Composable
fun DebtsScreen(vm: AppViewModel, asToman: Boolean, onBack: () -> Unit) {
    val extras = PergarThemeInfo.colors
    val debts by vm.debts.collectAsState()
    var tabIndex by remember { mutableIntStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Debt?>(null) }
    var settleTarget by remember { mutableStateOf<Debt?>(null) }
    var pendingDelete by remember { mutableStateOf<Debt?>(null) }
    val direction = if (tabIndex == 0) DebtDirection.LENT else DebtDirection.BORROWED

    val activeLent = debts.filter { !it.isSettled && it.direction == DebtDirection.LENT }.sumOf { it.amountRial }
    val activeBorrowed = debts.filter { !it.isSettled && it.direction == DebtDirection.BORROWED }.sumOf { it.amountRial }
    val visible = debts
        .filter { it.direction == direction }
        .sortedWith(compareBy<Debt> { it.isSettled }.thenByDescending { it.createdAt })

    ScreenFrame(
        title = "طلب و بدهی",
        subtitle = "پیگیری حساب‌های بین افراد",
        onBack = onBack,
        actions = {
            IconButton(onClick = { showAdd = true }) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "ثبت جدید",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassCard(modifier = Modifier.weight(1f), padding = 12.dp) {
                        Text(
                            "طلب‌های باز",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            formatMoney(activeLent, withUnit = false, asToman = asToman),
                            style = MaterialTheme.typography.titleSmall,
                            color = extras.success
                        )
                    }
                    GlassCard(modifier = Modifier.weight(1f), padding = 12.dp) {
                        Text(
                            "بدهی‌های باز",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            formatMoney(activeBorrowed, withUnit = false, asToman = asToman),
                            style = MaterialTheme.typography.titleSmall,
                            color = extras.danger
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                SegmentedControl(
                    options = listOf("طلب از دیگران", "بدهی به دیگران"),
                    selectedIndex = tabIndex,
                    onSelect = { tabIndex = it }
                )
                Spacer(Modifier.height(8.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (visible.isEmpty()) {
                    item {
                        EmptyState(
                            icon = iconForKey("people"),
                            title = if (direction == DebtDirection.LENT) "طلبی ثبت نشده" else "بدهی‌ای ثبت نشده",
                            message = "وقتی به کسی پول می‌دهید یا قرض می‌گیرید، اینجا ثبت کنید. تا زمان تسویه، درآمد یا هزینه واقعی محسوب نمی‌شود.",
                            actionLabel = "ثبت مورد جدید",
                            onAction = { showAdd = true }
                        )
                    }
                }
                items(visible, key = { it.id }) { debt ->
                    val isLent = debt.direction == DebtDirection.LENT
                    val accent = if (isLent) extras.success else extras.danger
                    GlassCard(
                        padding = 14.dp,
                        onClick = { editing = debt }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryIcon(
                                iconKey = if (isLent) "person" else "people",
                                colorHex = if (isLent) "#22C55E" else "#EF4444",
                                size = 40.dp,
                                iconSize = 20.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = debt.person,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = listOfNotNull(
                                        if (isLent) "طلب" else "بدهی",
                                        debt.dueEpochMillis?.let { "سررسید ${formatJalaliLong(it)}" },
                                        debt.dueEpochMillis?.let { daysUntilLabel(it) }
                                    ).joinToString(" · "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = formatMoney(debt.amountRial, withUnit = false, asToman = asToman),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (debt.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else accent
                            )
                        }
                        if (debt.note.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = debt.note,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (debt.isSettled) {
                                TinyTag("تسویه شده", extras.success)
                            } else {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { settleTarget = debt }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isLent) "وصول شد" else "پرداخت شد",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = accent
                                    )
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            IconButton(
                                onClick = { pendingDelete = debt },
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
    }

    if (showAdd) {
        DebtDialog(
            initial = null,
            initialDirection = direction,
            asToman = asToman,
            onDismiss = { showAdd = false },
            onSave = { debt ->
                vm.saveDebt(debt, isNew = true)
                showAdd = false
            }
        )
    }
    editing?.let { debt ->
        DebtDialog(
            initial = debt,
            initialDirection = debt.direction,
            asToman = asToman,
            onDismiss = { editing = null },
            onSave = { updated ->
                vm.saveDebt(updated, isNew = false)
                editing = null
            }
        )
    }
    settleTarget?.let { debt ->
        val isLent = debt.direction == DebtDirection.LENT
        ConfirmDialog(
            title = if (isLent) "وصول طلب" else "پرداخت بدهی",
            message = if (isLent) {
                "${debt.person} مبلغ ${formatMoney(debt.amountRial, withUnit = true, asToman = asToman)} را پرداخت کرد؟ این مبلغ به عنوان «درآمد» واقعی ثبت می‌شود."
            } else {
                "مبلغ ${formatMoney(debt.amountRial, withUnit = true, asToman = asToman)} به ${debt.person} پرداخت شد؟ این مبلغ به عنوان «هزینه» واقعی ثبت می‌شود."
            },
            confirmLabel = if (isLent) "بله، وصول شد" else "بله، پرداخت شد",
            onConfirm = {
                vm.settleDebt(debt)
                settleTarget = null
            },
            onDismiss = { settleTarget = null }
        )
    }
    pendingDelete?.let { debt ->
        ConfirmDialog(
            title = "حذف مورد",
            message = "مورد «${debt.person}» حذف شود؟",
            confirmLabel = "حذف",
            onConfirm = {
                vm.deleteDebt(debt)
                pendingDelete = null
                editing = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun DebtDialog(
    initial: Debt?,
    initialDirection: DebtDirection,
    asToman: Boolean,
    onDismiss: () -> Unit,
    onSave: (Debt) -> Unit
) {
    var person by remember { mutableStateOf(initial?.person ?: "") }
    var digits by remember {
        mutableStateOf(
            initial?.let {
                val value = if (asToman) it.amountRial / 10 else it.amountRial
                value.toString()
            } ?: ""
        )
    }
    var direction by remember { mutableStateOf(initial?.direction ?: initialDirection) }
    var hasDue by remember { mutableStateOf(initial?.dueEpochMillis != null) }
    var due by remember { mutableStateOf(initial?.dueEpochMillis ?: System.currentTimeMillis()) }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var showDate by remember { mutableStateOf(false) }

    PergarDialog(
        title = if (initial == null) "ثبت طلب یا بدهی" else "ویرایش مورد",
        onDismiss = onDismiss
    ) {
        SegmentedControl(
            options = listOf("طلب از دیگران", "بدهی به دیگران"),
            selectedIndex = if (direction == DebtDirection.LENT) 0 else 1,
            onSelect = { direction = if (it == 0) DebtDirection.LENT else DebtDirection.BORROWED }
        )
        Spacer(Modifier.height(10.dp))
        PergarTextField(
            value = person,
            onValueChange = { person = it },
            label = "نام شخص",
            placeholder = "مثلاً آقای احمدی"
        )
        Spacer(Modifier.height(10.dp))
        AmountField(
            digits = digits,
            onDigitsChange = { digits = it },
            asToman = asToman,
            label = "مبلغ"
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "سررسید دارد",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "برای پیگیری و یادآوری زمان تسویه",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = hasDue, onCheckedChange = { hasDue = it })
        }
        if (hasDue) {
            Spacer(Modifier.height(8.dp))
            SelectRow(
                label = "تاریخ سررسید",
                value = formatJalaliLong(due),
                onClick = { showDate = true }
            )
        }
        Spacer(Modifier.height(10.dp))
        PergarTextField(
            value = note,
            onValueChange = { note = it },
            label = "توضیح (اختیاری)",
            singleLine = false
        )
        Spacer(Modifier.height(16.dp))
        PergarButton(
            text = "ذخیره",
            enabled = person.isNotBlank() && (digits.toLongOrNull() ?: 0L) > 0,
            onClick = {
                val amount = (digits.toLongOrNull() ?: 0L).let { if (asToman) it * 10 else it }
                onSave(
                    Debt(
                        id = initial?.id ?: 0,
                        person = person.trim(),
                        direction = direction,
                        amountRial = amount,
                        dueEpochMillis = if (hasDue) due else null,
                        note = note.trim(),
                        isSettled = initial?.isSettled ?: false,
                        createdAt = initial?.createdAt ?: System.currentTimeMillis()
                    )
                )
            }
        )
    }

    if (showDate) {
        JalaliDatePickerDialog(
            initialMillis = due,
            onDismiss = { showDate = false },
            onSelect = { due = it }
        )
    }
}
