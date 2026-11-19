package ir.pergar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.unit.dp
import ir.pergar.app.data.db.TxnEntity
import ir.pergar.app.data.model.TxnType
import ir.pergar.app.data.util.JalaliDate
import ir.pergar.app.data.util.formatJalaliLong
import ir.pergar.app.data.util.formatMoney
import ir.pergar.app.data.util.toPersianDigits
import ir.pergar.app.ui.AppViewModel
import ir.pergar.app.ui.components.ConfirmDialog
import ir.pergar.app.ui.components.DayHeader
import ir.pergar.app.ui.components.EmptyState
import ir.pergar.app.ui.components.PergarTextField
import ir.pergar.app.ui.components.ScreenFrame
import ir.pergar.app.ui.components.SegmentedControl
import ir.pergar.app.ui.components.TransactionRow
import ir.pergar.app.ui.components.iconForKey
import ir.pergar.app.ui.theme.PergarThemeInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    vm: AppViewModel,
    asToman: Boolean,
    onAdd: (TxnType) -> Unit
) {
    val extras = PergarThemeInfo.colors
    val txns by vm.transactions.collectAsState()
    val categories by vm.categories.collectAsState()
    val accounts by vm.accounts.collectAsState()
    var query by remember { mutableStateOf("") }
    var filterIndex by remember { mutableIntStateOf(0) }
    var editTxn by remember { mutableStateOf<TxnEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<TxnEntity?>(null) }

    val filters = listOf("همه", "هزینه", "درآمد", "انتقال")
    val types = listOf(null, TxnType.EXPENSE, TxnType.INCOME, TxnType.TRANSFER)
    val categoryMap = remember(categories) { categories.associateBy { it.id } }
    val accountMap = remember(accounts) { accounts.associateBy { it.id } }

    val filtered = remember(txns, query, filterIndex, categoryMap) {
        val typeFilter = types[filterIndex]
        txns
            .filter { typeFilter == null || it.type == typeFilter }
            .filter { txn ->
                if (query.isBlank()) return@filter true
                val categoryName = txn.categoryId?.let { categoryMap[it]?.name } ?: ""
                txn.merchant.contains(query, ignoreCase = true) ||
                    txn.note.contains(query, ignoreCase = true) ||
                    categoryName.contains(query, ignoreCase = true)
            }
            .sortedByDescending { it.dateEpochMillis }
    }
    val grouped = remember(filtered) {
        filtered.groupBy { JalaliDate.from(it.dateEpochMillis).isoKey() }
    }

    ScreenFrame(
        title = "تراکنش‌ها",
        subtitle = "${toPersianDigits(filtered.size.toString())} مورد در این نمای",
        actions = {
            IconButton(onClick = { onAdd(TxnType.EXPENSE) }) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "ثبت تراکنش",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                PergarTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = "جستجو در تراکنش‌ها",
                    leadingIcon = Icons.Rounded.Search
                )
                Spacer(Modifier.height(10.dp))
                SegmentedControl(
                    options = filters,
                    selectedIndex = filterIndex,
                    onSelect = { filterIndex = it }
                )
                Spacer(Modifier.height(6.dp))
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp)
            ) {
                if (grouped.isEmpty()) {
                    item {
                        EmptyState(
                            icon = iconForKey("receipt"),
                            title = "تراکنشی پیدا نشد",
                            message = if (query.isBlank()) {
                                "با ثبت خرید و درآمد یا اسکن پیامک‌های بانکی شروع کنید."
                            } else {
                                "عبارت دیگری جستجو کنید یا فیلتر را تغییر دهید."
                            },
                            actionLabel = "ثبت تراکنش",
                            onAction = { onAdd(TxnType.EXPENSE) }
                        )
                    }
                }
                grouped.forEach { (isoKey, dayTxns) ->
                    item(key = isoKey) {
                        val first = dayTxns.first()
                        val dayExpense = dayTxns.filter { it.type == TxnType.EXPENSE }.sumOf { it.amountRial }
                        DayHeader(
                            label = formatJalaliLong(first.dateEpochMillis),
                            totalRial = dayExpense,
                            asToman = asToman
                        )
                    }
                    items(dayTxns, key = { it.id }) { txn ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value != SwipeToDismissBoxValue.Settled) {
                                    pendingDelete = txn
                                    false
                                } else {
                                    true
                                }
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(extras.danger.copy(alpha = 0.16f))
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Rounded.Delete,
                                        contentDescription = "حذف",
                                        tint = extras.danger,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.001f))
                            ) {
                                TransactionRow(
                                    txn = txn,
                                    category = txn.categoryId?.let { categoryMap[it] },
                                    accountName = txn.accountId?.let { accountMap[it]?.name },
                                    asToman = asToman,
                                    onClick = { editTxn = txn }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    editTxn?.let { txn ->
        AddTransactionSheet(
            vm = vm,
            asToman = asToman,
            initialType = txn.type,
            editTxn = txn,
            onDismiss = { editTxn = null }
        )
    }

    pendingDelete?.let { txn ->
        ConfirmDialog(
            title = "حذف تراکنش",
            message = "این تراکنش حذف شود؟ اثر آن روی موجودی حساب نیز برگشت داده می‌شود. مبلغ: ${formatMoney(txn.amountRial, withUnit = true, asToman = asToman)}",
            confirmLabel = "حذف",
            onConfirm = {
                vm.deleteTransaction(txn)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}
