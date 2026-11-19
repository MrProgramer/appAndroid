package ir.pergar.app.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.pergar.app.SMS_PERMISSIONS
import ir.pergar.app.data.model.TxnType
import ir.pergar.app.data.sms.ParsedSms
import ir.pergar.app.data.util.formatJalaliShort
import ir.pergar.app.data.util.formatMoney
import ir.pergar.app.data.util.formatTime
import ir.pergar.app.data.util.toPersianDigits
import ir.pergar.app.hasPermission
import ir.pergar.app.ui.AppViewModel
import ir.pergar.app.ui.components.CategoryIcon
import ir.pergar.app.ui.components.EmptyState
import ir.pergar.app.ui.components.GlassCard
import ir.pergar.app.ui.components.PergarButton
import ir.pergar.app.ui.components.ScreenFrame
import ir.pergar.app.ui.components.StaggeredItem
import ir.pergar.app.ui.components.TinyTag
import ir.pergar.app.ui.components.iconForKey
import ir.pergar.app.ui.theme.PergarThemeInfo
import kotlinx.coroutines.launch

@Composable
fun SmsCenterScreen(vm: AppViewModel, asToman: Boolean, onBack: () -> Unit) {
    val context = LocalContext.current
    val extras = PergarThemeInfo.colors
    val scope = rememberCoroutineScope()
    val transactions by vm.transactions.collectAsState()
    val existingHashes = remember(transactions) {
        transactions.mapNotNull { it.smsHash }.toHashSet()
    }

    var granted by remember { mutableStateOf(context.hasPermission(Manifest.permission.READ_SMS)) }
    var scanning by remember { mutableStateOf(false) }
    var scanned by remember { mutableStateOf(false) }
    var items by remember { mutableStateOf<List<ParsedSms>>(emptyList()) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var summary by remember { mutableStateOf<Triple<Int, Int, Int>?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        granted = context.hasPermission(Manifest.permission.READ_SMS)
    }

    val startScan: () -> Unit = {
        if (!scanning && granted) {
            scanning = true
            summary = null
            scope.launch {
                val found = runCatching { vm.scanSms() }.getOrDefault(emptyList())
                items = found.filter { it.isUseful }.sortedByDescending { it.epochMillis }
                selected = items
                    .filter { it.isTransaction && it.hash !in existingHashes }
                    .map { it.hash }
                    .toSet()
                scanned = true
                scanning = false
            }
        }
    }

    LaunchedEffect(Unit) {
        if (granted) startScan()
    }

    val selectable = items.filter { it.isTransaction }
    val selectedItems = items.filter { it.hash in selected }
    val allSelected = selectable.isNotEmpty() && selectable.all { it.hash in selected }

    ScreenFrame(
        title = "مرکز پیامک‌ها",
        subtitle = "خواندن و ثبت تراکنش‌های بانکی",
        onBack = onBack,
        actions = {
            IconButton(
                onClick = {
                    if (!granted) permissionLauncher.launch(SMS_PERMISSIONS) else startScan()
                }
            ) {
                Icon(
                    Icons.Rounded.Refresh,
                    contentDescription = "اسکن مجدد",
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
            if (!granted) {
                item {
                    GlassCard(padding = 16.dp) {
                        Text(
                            text = "دسترسی به پیامک لازم است",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "پرگار فقط پیامک‌های بانکی را روی همین گوشی می‌خواند. هیچ پیامکی ارسال یا آپلود نمی‌شود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        PergarButton(
                            text = "اجازه دسترسی",
                            onClick = { permissionLauncher.launch(SMS_PERMISSIONS) }
                        )
                    }
                }
            }

            summary?.let { (imported, duplicates, skipped) ->
                item {
                    GlassCard(padding = 14.dp) {
                        Text(
                            text = "نتیجه ثبت",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                            ResultChip("ثبت شد", imported, extras.success)
                            ResultChip("تکراری", duplicates, extras.warning)
                            ResultChip("رد شد", skipped, MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (scanned && items.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${toPersianDigits(items.size.toString())} پیامک بانکی",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selected = if (allSelected) emptySet()
                                    else selectable.map { it.hash }.toSet()
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (allSelected) "لغو انتخاب" else "انتخاب همه",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (scanned && items.isEmpty()) {
                item {
                    EmptyState(
                        icon = iconForKey("sms"),
                        title = "پیامک بانکی پیدا نشد",
                        message = "پیامک‌های بانکی خوانده‌شده‌ای در صندوق ورودی پیدا نشد. اگر مطمئن هستید پیامک بانکی دارید، دوباره اسکن کنید.",
                        actionLabel = "اسکن مجدد",
                        onAction = { startScan() }
                    )
                }
            }

            items(items, key = { it.hash }) { parsed ->
                val duplicate = parsed.hash in existingHashes
                val isSelected = parsed.hash in selected
                StaggeredItem(index = 0) {
                    SmsRow(
                        parsed = parsed,
                        asToman = asToman,
                        duplicate = duplicate,
                        selected = isSelected,
                        selectable = parsed.isTransaction,
                        onToggle = {
                            if (parsed.isTransaction) {
                                selected = if (isSelected) selected - parsed.hash
                                else selected + parsed.hash
                            }
                        }
                    )
                }
            }

            if (items.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    PergarButton(
                        text = if (scanning) "در حال اسکن..."
                        else if (selectedItems.isEmpty()) "تراکنشی انتخاب نشده"
                        else "ثبت ${toPersianDigits(selectedItems.size.toString())} تراکنش",
                        enabled = selectedItems.isNotEmpty() && !scanning,
                        onClick = {
                            vm.importSms(selectedItems) { imported, duplicates, skipped ->
                                summary = Triple(imported, duplicates, skipped)
                                selected = emptySet()
                            }
                        }
                    )
                }
            }

            item {
                Text(
                    text = "مبلغ‌ها از پیامک استخراج می‌شوند؛ در صورت نیاز می‌توانید تراکنش را پس از ثبت ویرایش کنید.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ResultChip(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Column {
        Text(
            text = toPersianDigits(count.toString()),
            style = MaterialTheme.typography.titleMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SmsRow(
    parsed: ParsedSms,
    asToman: Boolean,
    duplicate: Boolean,
    selected: Boolean,
    selectable: Boolean,
    onToggle: () -> Unit
) {
    val extras = PergarThemeInfo.colors
    val typeColor = when (parsed.type) {
        TxnType.INCOME -> extras.success
        TxnType.EXPENSE -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    GlassCard(
        padding = 12.dp,
        onClick = if (selectable) onToggle else null
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CategoryIcon(
                iconKey = parsed.categoryHint ?: "sms",
                colorHex = when (parsed.type) {
                    TxnType.INCOME -> "#22C55E"
                    TxnType.EXPENSE -> "#EF4444"
                    else -> "#4C7DFF"
                },
                size = 40.dp,
                iconSize = 20.dp
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = parsed.bank ?: parsed.sender,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(6.dp))
                    when {
                        duplicate -> TinyTag("قبلاً ثبت شده", extras.warning)
                        parsed.isTransaction -> TinyTag(
                            if (parsed.type == TxnType.INCOME) "درآمد" else "هزینه",
                            typeColor
                        )
                        else -> TinyTag("موجودی", MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    text = listOfNotNull(
                        parsed.merchant,
                        parsed.cardLast4?.let { "کارت ${toPersianDigits(it)}" }
                    ).joinToString(" · ").ifBlank { "پیامک بانکی" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatJalaliShort(parsed.epochMillis)} · ${formatTime(parsed.epochMillis)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = parsed.amountRial?.let {
                        formatMoney(it, withUnit = false, asToman = asToman)
                    } ?: "—",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (parsed.amountRial != null) typeColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
                parsed.balanceRial?.let {
                    Text(
                        text = "مانده ${formatMoney(it, withUnit = false, asToman = asToman)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                if (selectable) {
                    Icon(
                        imageVector = if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = if (selected) "انتخاب شده" else "انتخاب نشده",
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(18.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = parsed.rawBody,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
