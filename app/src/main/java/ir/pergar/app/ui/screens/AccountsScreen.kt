package ir.pergar.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.SwapHoriz
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.widget.Toast
import ir.pergar.app.data.db.Account
import ir.pergar.app.data.db.SeedData
import ir.pergar.app.data.model.AccountKind
import ir.pergar.app.data.util.formatMoney
import ir.pergar.app.data.util.toPersianDigits
import ir.pergar.app.ui.AppViewModel
import ir.pergar.app.ui.components.AmountField
import ir.pergar.app.ui.components.AnimatedAmount
import ir.pergar.app.ui.components.ChoiceDialog
import ir.pergar.app.ui.components.ConfirmDialog
import ir.pergar.app.ui.components.EmptyState
import ir.pergar.app.ui.components.GhostButton
import ir.pergar.app.ui.components.GlassCard
import ir.pergar.app.ui.components.GradientCard
import ir.pergar.app.ui.components.PergarButton
import ir.pergar.app.ui.components.PergarDialog
import ir.pergar.app.ui.components.PergarTextField
import ir.pergar.app.ui.components.ScreenFrame
import ir.pergar.app.ui.components.SegmentedControl
import ir.pergar.app.ui.components.SelectRow
import ir.pergar.app.ui.components.parseHexColor
import ir.pergar.app.ui.components.pressScale
import ir.pergar.app.ui.theme.PergarThemeInfo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable as clickableModifier

@Composable
fun AccountsScreen(vm: AppViewModel, asToman: Boolean, onBack: () -> Unit) {
    val extras = PergarThemeInfo.colors
    val context = LocalContext.current
    val accounts by vm.accounts.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Account?>(null) }
    var showTransfer by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Account?>(null) }

    ScreenFrame(
        title = "حساب‌ها و کیف پول‌ها",
        subtitle = "مدیریت نقدینگی و کارت‌های بانکی",
        onBack = onBack,
        actions = {
            IconButton(onClick = { showAdd = true }) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "حساب جدید",
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
                                text = "جمع موجودی",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            AnimatedAmount(
                                amountRial = accounts.sumOf { it.balanceRial },
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                asToman = asToman
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${toPersianDigits(accounts.size.toString())} حساب",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            GhostButton(
                                text = "انتقال",
                                icon = Icons.Rounded.SwapHoriz,
                                onClick = {
                                    if (accounts.size < 2) {
                                        Toast.makeText(
                                            context,
                                            "برای انتقال حداقل دو حساب لازم است",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        showTransfer = true
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (accounts.isEmpty()) {
                item {
                    EmptyState(
                        icon = ir.pergar.app.ui.components.iconForKey("wallet"),
                        title = "حسابی ثبت نشده",
                        message = "یک کارت بانکی یا کیف پول اضافه کنید.",
                        actionLabel = "افزودن حساب",
                        onAction = { showAdd = true }
                    )
                }
            } else {
                items(accounts, key = { it.id }) { account ->
                    val color = parseHexColor(account.colorHex)
                    val interaction = remember { MutableInteractionSource() }
                    GradientCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pressScale(interaction)
                            .clickable(
                                interactionSource = interaction,
                                indication = null
                            ) { editing = account },
                        colors = listOf(color, lerp(color, Color(0xFF07101F), 0.45f))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable { }
                                    .padding(0.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = ir.pergar.app.ui.components.iconForKey(
                                        when (account.kind) {
                                            AccountKind.CASH -> "wallet"
                                            AccountKind.WALLET -> "savings"
                                            AccountKind.BANK -> "card"
                                        }
                                    ),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = account.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = listOfNotNull(
                                        account.bank.takeIf { it.isNotBlank() },
                                        when (account.kind) {
                                            AccountKind.BANK -> "حساب بانکی"
                                            AccountKind.CASH -> "نقدی"
                                            AccountKind.WALLET -> "کیف پول"
                                        }
                                    ).joinToString(" · "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                            if (account.isDefault) {
                                Text(
                                    text = "پیش‌فرض",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.16f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Icon(
                                Icons.Rounded.ChevronLeft,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(Modifier.height(18.dp))
                        Text(
                            text = "موجودی",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(2.dp))
                        AnimatedAmount(
                            amountRial = account.balanceRial,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            asToman = asToman
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (account.cardLast4.isNotBlank()) "کارت •••• ${toPersianDigits(account.cardLast4)}" else "بدون شماره کارت ثبت‌شده",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
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
                            text = "افزودن حساب یا کیف پول",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        AccountDialog(
            initial = null,
            colorHex = SeedData.accountColors[accounts.size % SeedData.accountColors.size],
            asToman = asToman,
            onDismiss = { showAdd = false },
            onSave = { account ->
                vm.saveAccount(account, isNew = true)
                showAdd = false
            }
        )
    }
    editing?.let { account ->
        AccountDialog(
            initial = account,
            colorHex = account.colorHex,
            asToman = asToman,
            onDismiss = { editing = null },
            onSave = { updated ->
                vm.saveAccount(updated, isNew = false)
                editing = null
            },
            onDelete = { pendingDelete = account }
        )
    }
    pendingDelete?.let { account ->
        ConfirmDialog(
            title = "حذف حساب",
            message = "حساب «${account.name}» حذف شود؟ تراکنش‌های قبلی حفظ می‌شوند اما به حسابی متصل نخواهند بود.",
            confirmLabel = "حذف حساب",
            onConfirm = {
                vm.deleteAccount(account)
                pendingDelete = null
                editing = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
    if (showTransfer) {
        TransferDialog(
            accounts = accounts,
            asToman = asToman,
            onDismiss = { showTransfer = false },
            onTransfer = { from, to, amountRial ->
                vm.transfer(from, to, amountRial)
                showTransfer = false
            }
        )
    }
}

@Composable
private fun AccountDialog(
    initial: Account?,
    colorHex: String,
    asToman: Boolean,
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val extras = PergarThemeInfo.colors
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var bank by remember { mutableStateOf(initial?.bank ?: "") }
    var kind by remember { mutableStateOf(initial?.kind ?: AccountKind.BANK) }
    var cardLast4 by remember { mutableStateOf(initial?.cardLast4 ?: "") }
    var balanceDigits by remember {
        mutableStateOf(
            initial?.let {
                val value = if (asToman) it.balanceRial / 10 else it.balanceRial
                value.toString()
            } ?: ""
        )
    }
    var showBankPicker by remember { mutableStateOf(false) }
    var showKindPicker by remember { mutableStateOf(false) }
    val kinds = listOf(AccountKind.BANK, AccountKind.CASH, AccountKind.WALLET)
    val kindLabels = listOf("بانکی", "نقدی", "کیف پول")

    PergarDialog(
        title = if (initial == null) "حساب جدید" else "ویرایش حساب",
        onDismiss = onDismiss
    ) {
        PergarTextField(
            value = name,
            onValueChange = { name = it },
            label = "نام حساب",
            placeholder = "مثلاً ملت — کارت اصلی"
        )
        Spacer(Modifier.height(10.dp))
        SegmentedControl(
            options = kindLabels,
            selectedIndex = kinds.indexOf(kind).coerceAtLeast(0),
            onSelect = { kind = kinds[it] }
        )
        Spacer(Modifier.height(10.dp))
        if (kind == AccountKind.BANK) {
            SelectRow(
                label = "بانک",
                value = bank.ifBlank { "انتخاب بانک" },
                onClick = { showBankPicker = true }
            )
            Spacer(Modifier.height(8.dp))
            PergarTextField(
                value = cardLast4,
                onValueChange = { input ->
                    cardLast4 = input.filter { it.isDigit() }.take(4)
                },
                label = "چهار رقم آخر کارت (اختیاری)",
                keyboardType = KeyboardType.Number
            )
            Spacer(Modifier.height(10.dp))
        }
        AmountField(
            digits = balanceDigits,
            onDigitsChange = { balanceDigits = it },
            asToman = asToman,
            label = "موجودی فعلی"
        )
        Spacer(Modifier.height(16.dp))
        PergarButton(
            text = "ذخیره حساب",
            enabled = name.isNotBlank(),
            onClick = {
                val balance = (balanceDigits.toLongOrNull() ?: 0L).let { if (asToman) it * 10 else it }
                onSave(
                    Account(
                        id = initial?.id ?: 0,
                        name = name.trim(),
                        bank = if (kind == AccountKind.BANK) bank else "",
                        kind = kind,
                        cardLast4 = if (kind == AccountKind.BANK) cardLast4 else "",
                        colorHex = colorHex,
                        balanceRial = balance,
                        isDefault = initial?.isDefault ?: false
                    )
                )
            }
        )
        if (onDelete != null) {
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "حذف این حساب",
                    style = MaterialTheme.typography.labelMedium,
                    color = extras.danger,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickableModifier { onDelete() }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }

    if (showBankPicker) {
        ChoiceDialog(
            title = "انتخاب بانک",
            options = SeedData.banks,
            selectedIndex = SeedData.banks.indexOf(bank),
            onDismiss = { showBankPicker = false },
            onSelect = { bank = SeedData.banks.getOrElse(it) { "" } }
        )
    }
}

@Composable
private fun TransferDialog(
    accounts: List<Account>,
    asToman: Boolean,
    onDismiss: () -> Unit,
    onTransfer: (Account, Account, Long) -> Unit
) {
    var fromId by remember { mutableStateOf(accounts.firstOrNull()?.id) }
    var toId by remember { mutableStateOf(accounts.getOrNull(1)?.id) }
    var digits by remember { mutableStateOf("") }
    var showFrom by remember { mutableStateOf(false) }
    var showTo by remember { mutableStateOf(false) }
    val from = accounts.firstOrNull { it.id == fromId }
    val to = accounts.firstOrNull { it.id == toId }
    val amountRial = (digits.toLongOrNull() ?: 0L).let { if (asToman) it * 10 else it }

    PergarDialog(title = "انتقال بین حساب‌ها", onDismiss = onDismiss) {
        SelectRow(
            label = "از حساب",
            value = from?.name ?: "انتخاب حساب",
            onClick = { showFrom = true }
        )
        Spacer(Modifier.height(8.dp))
        SelectRow(
            label = "به حساب",
            value = to?.name ?: "انتخاب حساب",
            onClick = { showTo = true }
        )
        Spacer(Modifier.height(12.dp))
        AmountField(
            digits = digits,
            onDigitsChange = { digits = it },
            asToman = asToman,
            label = "مبلغ انتقال"
        )
        Spacer(Modifier.height(16.dp))
        PergarButton(
            text = "انتقال",
            enabled = amountRial > 0 && from != null && to != null && from.id != to.id,
            onClick = {
                if (from != null && to != null && amountRial > 0 && from.id != to.id) {
                    onTransfer(from, to, amountRial)
                }
            }
        )
    }

    if (showFrom) {
        ChoiceDialog(
            title = "از حساب",
            options = accounts.map { "${it.name} — ${formatMoney(it.balanceRial, withUnit = false, asToman = asToman)}" },
            selectedIndex = accounts.indexOfFirst { it.id == fromId },
            onDismiss = { showFrom = false },
            onSelect = { fromId = accounts.getOrNull(it)?.id }
        )
    }
    if (showTo) {
        ChoiceDialog(
            title = "به حساب",
            options = accounts.map { it.name },
            selectedIndex = accounts.indexOfFirst { it.id == toId },
            onDismiss = { showTo = false },
            onSelect = { toId = accounts.getOrNull(it)?.id }
        )
    }
}
