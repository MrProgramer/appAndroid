package ir.pergar.app.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.pergar.app.data.db.TxnEntity
import ir.pergar.app.data.model.TxnSource
import ir.pergar.app.data.model.TxnType
import ir.pergar.app.data.util.formatCompactMoney
import ir.pergar.app.data.util.formatJalaliLong
import ir.pergar.app.data.util.toPersianDigits
import ir.pergar.app.data.voice.VoiceParser
import ir.pergar.app.ui.AppViewModel
import ir.pergar.app.ui.components.AmountField
import ir.pergar.app.ui.components.CategoryIcon
import ir.pergar.app.ui.components.ChoiceDialog
import ir.pergar.app.ui.components.GhostButton
import ir.pergar.app.ui.components.JalaliDatePickerDialog
import ir.pergar.app.ui.components.PergarButton
import ir.pergar.app.ui.components.PergarTextField
import ir.pergar.app.ui.components.SegmentedControl
import ir.pergar.app.ui.components.SelectRow
import ir.pergar.app.ui.theme.PergarThemeInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    vm: AppViewModel,
    asToman: Boolean,
    initialType: TxnType,
    onDismiss: () -> Unit,
    editTxn: TxnEntity? = null
) {
    val context = LocalContext.current
    val extras = PergarThemeInfo.colors
    val categories by vm.categories.collectAsState()
    val accounts by vm.accounts.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val typeOptions = listOf(TxnType.EXPENSE, TxnType.INCOME, TxnType.TRANSFER)
    val typeLabels = listOf("هزینه", "درآمد", "انتقال")

    var type by remember { mutableStateOf(editTxn?.type ?: initialType) }
    var amountDigits by remember {
        mutableStateOf(
            editTxn?.let { txn ->
                val value = if (asToman) txn.amountRial / 10 else txn.amountRial
                if (value > 0) value.toString() else ""
            } ?: ""
        )
    }
    var categoryId by remember { mutableStateOf(editTxn?.categoryId) }
    var accountId by remember { mutableStateOf(editTxn?.accountId) }
    var toAccountId by remember { mutableStateOf(editTxn?.toAccountId) }
    var dateMillis by remember { mutableLongStateOf(editTxn?.dateEpochMillis ?: System.currentTimeMillis()) }
    var note by remember {
        mutableStateOf(
            editTxn?.let { txn ->
                txn.note.takeIf { it.isNotBlank() && txn.source != TxnSource.SMS } ?: ""
            } ?: ""
        )
    }
    var merchant by remember { mutableStateOf(editTxn?.merchant ?: "") }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showToAccountPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var voiceHint by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(accounts) {
        if (accountId == null) {
            accountId = accounts.firstOrNull { it.isDefault }?.id ?: accounts.firstOrNull()?.id
        }
        if (toAccountId == null) {
            toAccountId = accounts.firstOrNull { it.id != accountId }?.id
        }
    }
    LaunchedEffect(type, categories) {
        if (type == TxnType.TRANSFER) {
            categoryId = null
        } else if (categories.none { it.id == categoryId && it.type == type }) {
            categoryId = categories.firstOrNull { it.type == type }?.id
        }
    }

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                val parsed = VoiceParser.parse(spoken)
                parsed.amountToman?.let { amountDigits = it.toString() }
                if (parsed.type != TxnType.TRANSFER) type = parsed.type
                parsed.categoryHint?.let { hint ->
                    categories.firstOrNull { it.name == hint && it.type == type }?.let {
                        categoryId = it.id
                    }
                }
                if (note.isBlank()) note = spoken.take(120)
                voiceHint = spoken
            }
        }
    }

    val selectedAccount = accounts.firstOrNull { it.id == accountId }
    val selectedToAccount = accounts.firstOrNull { it.id == toAccountId }
    val amountValue = amountDigits.toLongOrNull() ?: 0L
    val amountRial = if (asToman) amountValue * 10 else amountValue
    val canSave = amountRial > 0 && when (type) {
        TxnType.TRANSFER -> accountId != null && toAccountId != null && accountId != toAccountId
        else -> true
    }
    val typeCategories = remember(categories, type) { categories.filter { it.type == type } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = extras.glassBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 26.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (editTxn != null) "ویرایش تراکنش" else "ثبت تراکنش",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "بستن",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            SegmentedControl(
                options = typeLabels,
                selectedIndex = typeOptions.indexOf(type).coerceAtLeast(0),
                onSelect = { index -> type = typeOptions.getOrElse(index) { TxnType.EXPENSE } }
            )
            Spacer(Modifier.height(16.dp))
            AmountField(
                digits = amountDigits,
                onDigitsChange = { amountDigits = it },
                asToman = asToman,
                label = "مبلغ به ${if (asToman) "تومان" else "ریال"}"
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(50_000L, 100_000L, 200_000L, 500_000L).forEach { tomanValue ->
                    val display = formatCompactMoney(if (asToman) tomanValue * 10 else tomanValue, asToman = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, extras.glassBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                amountDigits = (if (asToman) tomanValue else tomanValue * 10).toString()
                            }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = display,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            GhostButton(
                text = "ثبت با صدا",
                icon = Icons.Rounded.Mic,
                onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR")
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "مثلاً: دویست هزار تومان خرید نان")
                    }
                    runCatching { voiceLauncher.launch(intent) }
                        .onFailure {
                            voiceHint = "تشخیص گفتار روی این دستگاه در دسترس نیست."
                        }
                },
                modifier = Modifier.fillMaxWidth()
            )
            voiceHint?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "شنیده شد: $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (type != TxnType.TRANSFER) {
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "دسته‌بندی",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                typeCategories.chunked(3).forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowCategories.forEach { cat ->
                            val selected = cat.id == categoryId
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                        else Color.Transparent
                                    )
                                    .clickable { categoryId = cat.id }
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .border(
                                            width = 2.dp,
                                            color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .padding(2.dp)
                                ) {
                                    CategoryIcon(
                                        iconKey = cat.iconKey,
                                        colorHex = cat.colorHex,
                                        size = 40.dp,
                                        iconSize = 20.dp
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    textAlign = TextAlign.Center,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        repeat(3 - rowCategories.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }

            Spacer(Modifier.height(12.dp))
            SelectRow(
                label = if (type == TxnType.TRANSFER) "از حساب" else "حساب",
                value = selectedAccount?.let { "${it.name}${if (it.cardLast4.isNotBlank()) " · ${it.cardLast4}" else ""}" }
                    ?: "انتخاب حساب",
                onClick = { showAccountPicker = true }
            )
            if (type == TxnType.TRANSFER) {
                Spacer(Modifier.height(8.dp))
                SelectRow(
                    label = "به حساب",
                    value = selectedToAccount?.let { "${it.name}${if (it.cardLast4.isNotBlank()) " · ${it.cardLast4}" else ""}" }
                        ?: "انتخاب حساب مقصد",
                    onClick = { showToAccountPicker = true }
                )
            }
            Spacer(Modifier.height(8.dp))
            SelectRow(
                label = "تاریخ",
                value = formatJalaliLong(dateMillis),
                onClick = { showDatePicker = true }
            )

            if (type != TxnType.TRANSFER) {
                Spacer(Modifier.height(12.dp))
                PergarTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = "فروشنده / طرف حساب (اختیاری)",
                    placeholder = "مثلاً فروشگاه رفاه"
                )
            }
            Spacer(Modifier.height(10.dp))
            PergarTextField(
                value = note,
                onValueChange = { note = it },
                label = "توضیح (اختیاری)",
                singleLine = false,
                placeholder = "جزئیات این تراکنش"
            )

            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (asToman) "مبلغ نهایی: ${formatCompactMoney(amountRial, true)} تومان"
                    else "مبلغ نهایی: ${formatCompactMoney(amountRial, false)} ریال",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = toPersianDigits(amountValue.toString()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
            PergarButton(
                text = if (editTxn != null) "ذخیره تغییرات" else "ثبت تراکنش",
                icon = Icons.Rounded.Check,
                enabled = canSave,
                onClick = {
                    if (canSave) {
                        val txn = TxnEntity(
                            id = editTxn?.id ?: 0,
                            type = type,
                            amountRial = amountRial,
                            categoryId = if (type == TxnType.TRANSFER) null else categoryId,
                            accountId = accountId,
                            toAccountId = if (type == TxnType.TRANSFER) toAccountId else null,
                            note = note.trim(),
                            merchant = merchant.trim(),
                            dateEpochMillis = dateMillis,
                            source = editTxn?.source ?: TxnSource.MANUAL,
                            smsHash = editTxn?.smsHash,
                            bank = editTxn?.bank ?: ""
                        )
                        if (editTxn != null) {
                            vm.updateTransaction(editTxn, txn) { onDismiss() }
                        } else {
                            vm.addTransaction(txn) { onDismiss() }
                        }
                    }
                }
            )
        }
    }

    if (showAccountPicker) {
        ChoiceDialog(
            title = "انتخاب حساب",
            options = accounts.map { "${it.name} (${formatCompactMoney(it.balanceRial, asToman)})" },
            selectedIndex = accounts.indexOfFirst { it.id == accountId },
            onDismiss = { showAccountPicker = false },
            onSelect = { index -> accountId = accounts.getOrNull(index)?.id }
        )
    }
    if (showToAccountPicker) {
        ChoiceDialog(
            title = "انتخاب حساب مقصد",
            options = accounts.map { it.name },
            selectedIndex = accounts.indexOfFirst { it.id == toAccountId },
            onDismiss = { showToAccountPicker = false },
            onSelect = { index -> toAccountId = accounts.getOrNull(index)?.id }
        )
    }
    if (showDatePicker) {
        JalaliDatePickerDialog(
            initialMillis = dateMillis,
            onDismiss = { showDatePicker = false },
            onSelect = { dateMillis = it }
        )
    }
}
