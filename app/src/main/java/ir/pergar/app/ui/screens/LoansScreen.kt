package ir.pergar.app.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.pergar.app.data.db.Account
import ir.pergar.app.data.db.Loan
import ir.pergar.app.data.db.SeedData
import ir.pergar.app.data.util.JalaliDate
import ir.pergar.app.data.util.daysUntilLabel
import ir.pergar.app.data.util.formatJalaliLong
import ir.pergar.app.data.util.formatMoney
import ir.pergar.app.data.util.toPersianDigits
import ir.pergar.app.ui.AppViewModel
import ir.pergar.app.ui.components.AmountField
import ir.pergar.app.ui.components.ChoiceDialog
import ir.pergar.app.ui.components.ConfirmDialog
import ir.pergar.app.ui.components.EmptyState
import ir.pergar.app.ui.components.GlassCard
import ir.pergar.app.ui.components.JalaliDatePickerDialog
import ir.pergar.app.ui.components.LabeledValueRow
import ir.pergar.app.ui.components.PergarButton
import ir.pergar.app.ui.components.PergarDialog
import ir.pergar.app.ui.components.PergarTextField
import ir.pergar.app.ui.components.ProgressBar
import ir.pergar.app.ui.components.ScreenFrame
import ir.pergar.app.ui.components.SelectRow
import ir.pergar.app.ui.components.iconForKey
import ir.pergar.app.ui.components.parseHexColor
import ir.pergar.app.ui.theme.PergarThemeInfo

@Composable
fun LoansScreen(vm: AppViewModel, asToman: Boolean, onBack: () -> Unit) {
    val extras = PergarThemeInfo.colors
    val context = LocalContext.current
    val loans by vm.loans.collectAsState()
    val accounts by vm.accounts.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Loan?>(null) }
    var pendingDelete by remember { mutableStateOf<Loan?>(null) }
    var paying by remember { mutableStateOf<Loan?>(null) }

    val active = loans.filter { it.paidCount < it.installmentCount }
    val remainingTotal = active.sumOf { (it.installmentCount - it.paidCount).toLong() * it.installmentRial }

    ScreenFrame(
        title = "اقساط و وام‌ها",
        subtitle = "پیگیری سررسید و پرداخت اقساط",
        onBack = onBack,
        actions = {
            IconButton(onClick = { showAdd = true }) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "وام جدید",
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
                                text = "مانده اقساط فعال",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = formatMoney(remainingTotal, withUnit = true, asToman = asToman),
                                style = MaterialTheme.typography.headlineSmall,
                                color = extras.gold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${toPersianDigits(active.size.toString())} وام فعال",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${toPersianDigits(loans.size.toString())} وام ثبت‌شده",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (loans.isEmpty()) {
                item {
                    EmptyState(
                        icon = iconForKey("loan"),
                        title = "وامی ثبت نشده",
                        message = "اقساط و وام‌های خود را اضافه کنید تا سررسیدها و یادآورها را دنبال کنید.",
                        actionLabel = "افزودن وام",
                        onAction = { showAdd = true }
                    )
                }
            } else {
                items(loans, key = { it.id }) { loan ->
                    val done = loan.paidCount >= loan.installmentCount
                    val progress = if (loan.installmentCount > 0) {
                        loan.paidCount.toFloat() / loan.installmentCount
                    } else 0f
                    val nextDue = vm.repository.nextInstallmentMillis(loan)
                    GlassCard(
                        padding = 14.dp,
                        onClick = { editing = loan }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ir.pergar.app.ui.components.CategoryIcon(
                                iconKey = "loan",
                                colorHex = if (done) "#22C55E" else "#6366F1",
                                size = 42.dp,
                                iconSize = 21.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = loan.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = listOfNotNull(
                                        loan.bank.takeIf { it.isNotBlank() },
                                        "قسط ${formatMoney(loan.installmentRial, withUnit = false, asToman = asToman)}"
                                    ).joinToString(" · "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (done) {
                                Text(
                                    text = "تسویه شد",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = extras.success
                                )
                            } else {
                                IconButton(
                                    onClick = { pendingDelete = loan },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Delete,
                                        contentDescription = "حذف وام",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        ProgressBar(
                            progress = progress,
                            color = if (done) extras.success else MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${toPersianDigits(loan.paidCount.toString())} از ${toPersianDigits(loan.installmentCount.toString())} قسط",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.weight(1f))
                            if (!done && nextDue != null) {
                                Text(
                                    text = "${formatJalaliLong(nextDue)} · ${daysUntilLabel(nextDue)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = extras.gold
                                )
                            }
                        }
                        if (!done) {
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PergarButton(
                                    text = "پرداخت قسط ${toPersianDigits((loan.paidCount + 1).toString())}",
                                    onClick = { paying = loan },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "اصلی: ${formatMoney(loan.principalRial, withUnit = false, asToman = asToman)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        LoanDialog(
            initial = null,
            accounts = accounts,
            asToman = asToman,
            onDismiss = { showAdd = false },
            onSave = { loan ->
                vm.saveLoan(loan, isNew = true)
                showAdd = false
            }
        )
    }
    editing?.let { loan ->
        LoanDialog(
            initial = loan,
            accounts = accounts,
            asToman = asToman,
            onDismiss = { editing = null },
            onSave = { updated ->
                vm.saveLoan(updated, isNew = false)
                editing = null
            },
            onDelete = { pendingDelete = loan }
        )
    }
    paying?.let { loan ->
        ConfirmDialog(
            title = "ثبت پرداخت قسط",
            message = "قسط ${toPersianDigits((loan.paidCount + 1).toString())} به مبلغ ${formatMoney(loan.installmentRial, withUnit = true, asToman = asToman)} پرداخت شده و به عنوان هزینه «اقساط و وام» ثبت شود؟",
            confirmLabel = "بله، پرداخت شد",
            onConfirm = {
                vm.payInstallment(loan) {
                    Toast.makeText(context, "قسط با موفقیت ثبت شد", Toast.LENGTH_SHORT).show()
                }
                paying = null
            },
            onDismiss = { paying = null }
        )
    }
    pendingDelete?.let { loan ->
        ConfirmDialog(
            title = "حذف وام",
            message = "وام «${loan.title}» حذف شود؟ تراکنش‌های پرداخت‌شده حذف نمی‌شوند.",
            confirmLabel = "حذف",
            onConfirm = {
                vm.deleteLoan(loan)
                pendingDelete = null
                editing = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun LoanDialog(
    initial: Loan?,
    accounts: List<Account>,
    asToman: Boolean,
    onDismiss: () -> Unit,
    onSave: (Loan) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val extras = PergarThemeInfo.colors
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var bank by remember { mutableStateOf(initial?.bank ?: "") }
    var principalDigits by remember {
        mutableStateOf(
            initial?.let {
                val value = if (asToman) it.principalRial / 10 else it.principalRial
                value.toString()
            } ?: ""
        )
    }
    var installmentDigits by remember {
        mutableStateOf(
            initial?.let {
                val value = if (asToman) it.installmentRial / 10 else it.installmentRial
                value.toString()
            } ?: ""
        )
    }
    var countText by remember { mutableStateOf(initial?.installmentCount?.toString() ?: "") }
    var firstDue by remember {
        mutableStateOf(initial?.firstDueEpochMillis ?: JalaliDate.today().toEpochMillis())
    }
    var accountId by remember { mutableStateOf(initial?.accountId) }
    var remind by remember { mutableStateOf(initial?.reminderEnabled ?: true) }
    var showBank by remember { mutableStateOf(false) }
    var showAccount by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }

    PergarDialog(
        title = if (initial == null) "وام جدید" else "ویرایش وام",
        onDismiss = onDismiss
    ) {
        PergarTextField(
            value = title,
            onValueChange = { title = it },
            label = "عنوان وام",
            placeholder = "مثلاً وام مسکن"
        )
        Spacer(Modifier.height(10.dp))
        SelectRow(
            label = "بانک",
            value = bank.ifBlank { "انتخاب بانک" },
            onClick = { showBank = true }
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AmountField(
                digits = principalDigits,
                onDigitsChange = { principalDigits = it },
                asToman = asToman,
                label = "مبلغ کل وام",
                modifier = Modifier.weight(1f)
            )
            AmountField(
                digits = installmentDigits,
                onDigitsChange = { installmentDigits = it },
                asToman = asToman,
                label = "مبلغ هر قسط",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        PergarTextField(
            value = countText,
            onValueChange = { input -> countText = input.filter { it.isDigit() }.take(3) },
            label = "تعداد اقساط",
            keyboardType = KeyboardType.Number
        )
        Spacer(Modifier.height(10.dp))
        SelectRow(
            label = "سررسید اولین قسط",
            value = formatJalaliLong(firstDue),
            onClick = { showDate = true }
        )
        Spacer(Modifier.height(8.dp))
        SelectRow(
            label = "حساب پرداخت",
            value = accounts.firstOrNull { it.id == accountId }?.name ?: "بدون حساب",
            onClick = { showAccount = true }
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "یادآور سررسید",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "اعلان در روز سررسید هر قسط",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = remind, onCheckedChange = { remind = it })
        }
        Spacer(Modifier.height(16.dp))
        PergarButton(
            text = "ذخیره وام",
            enabled = title.isNotBlank() && (countText.toIntOrNull() ?: 0) > 0,
            onClick = {
                val principal = (principalDigits.toLongOrNull() ?: 0L).let { if (asToman) it * 10 else it }
                val installment = (installmentDigits.toLongOrNull() ?: 0L).let { if (asToman) it * 10 else it }
                onSave(
                    Loan(
                        id = initial?.id ?: 0,
                        title = title.trim(),
                        bank = bank,
                        principalRial = principal,
                        installmentRial = installment,
                        installmentCount = (countText.toIntOrNull() ?: 1).coerceIn(1, 360),
                        paidCount = initial?.paidCount ?: 0,
                        firstDueEpochMillis = firstDue,
                        accountId = accountId,
                        reminderEnabled = remind
                    )
                )
            }
        )
        if (onDelete != null) {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "حذف این وام",
                    style = MaterialTheme.typography.labelMedium,
                    color = extras.danger,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onDelete() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }

    if (showBank) {
        ChoiceDialog(
            title = "بانک",
            options = SeedData.banks,
            selectedIndex = SeedData.banks.indexOf(bank),
            onDismiss = { showBank = false },
            onSelect = { bank = SeedData.banks.getOrElse(it) { "" } }
        )
    }
    if (showAccount) {
        ChoiceDialog(
            title = "حساب پرداخت",
            options = listOf("بدون حساب") + accounts.map { it.name },
            selectedIndex = accounts.indexOfFirst { it.id == accountId } + 1,
            onDismiss = { showAccount = false },
            onSelect = { index -> accountId = if (index == 0) null else accounts.getOrNull(index - 1)?.id }
        )
    }
    if (showDate) {
        JalaliDatePickerDialog(
            initialMillis = firstDue,
            onDismiss = { showDate = false },
            onSelect = { firstDue = it }
        )
    }
}
