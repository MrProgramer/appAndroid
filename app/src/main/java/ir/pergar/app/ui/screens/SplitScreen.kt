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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import ir.pergar.app.data.db.SplitGroup
import ir.pergar.app.data.db.SplitMember
import ir.pergar.app.data.model.SplitGroupWithMembers
import ir.pergar.app.data.util.formatJalaliLong
import ir.pergar.app.data.util.formatMoney
import ir.pergar.app.data.util.toPersianDigits
import ir.pergar.app.ui.AppViewModel
import ir.pergar.app.ui.components.AmountField
import ir.pergar.app.ui.components.CategoryIcon
import ir.pergar.app.ui.components.ConfirmDialog
import ir.pergar.app.ui.components.EmptyState
import ir.pergar.app.ui.components.GlassCard
import ir.pergar.app.ui.components.PergarButton
import ir.pergar.app.ui.components.PergarDialog
import ir.pergar.app.ui.components.PergarTextField
import ir.pergar.app.ui.components.ScreenFrame
import ir.pergar.app.ui.components.TinyTag
import ir.pergar.app.ui.components.WideInfoCard
import ir.pergar.app.ui.components.iconForKey
import ir.pergar.app.ui.theme.PergarThemeInfo

@Composable
fun SplitScreen(vm: AppViewModel, asToman: Boolean, onBack: () -> Unit) {
    val extras = PergarThemeInfo.colors
    val groups by vm.splitGroups.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var settleTarget by remember { mutableStateOf<SplitGroupWithMembers?>(null) }
    var pendingDelete by remember { mutableStateOf<SplitGroupWithMembers?>(null) }
    val active = groups.filter { !it.group.isSettled }
    val openTotal = active.sumOf { it.group.totalRial }

    ScreenFrame(
        title = "تقسیم هزینه",
        subtitle = "دونگی و تسویه گروهی",
        onBack = onBack,
        actions = {
            IconButton(onClick = { showAdd = true }) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "تقسیم جدید",
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
                    Text(
                        text = "هزینه‌های تقسیم‌شده در جریان",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = formatMoney(openTotal, withUnit = true, asToman = asToman),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${toPersianDigits(active.size.toString())} گروه باز · ${toPersianDigits(groups.size.toString())} گروه",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (groups.isEmpty()) {
                item {
                    EmptyState(
                        icon = iconForKey("people"),
                        title = "تقسیمی ثبت نشده",
                        message = "هزینه مشترک رستوران یا سفر را ثبت کنید و سهم هر نفر را محاسبه کنید.",
                        actionLabel = "تقسیم هزینه",
                        onAction = { showAdd = true }
                    )
                }
            }

            items(groups, key = { it.group.id }) { item ->
                val group = item.group
                GlassCard(padding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryIcon(
                            iconKey = "people",
                            colorHex = if (group.isSettled) "#22C55E" else "#4C7DFF",
                            size = 40.dp,
                            iconSize = 20.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = group.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${formatJalaliLong(group.dateMillis)} · پرداخت‌کننده: ${group.payerName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatMoney(group.totalRial, withUnit = false, asToman = asToman),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (group.isSettled) {
                                TinyTag("تسویه شده", extras.success)
                            } else {
                                TinyTag("باز", extras.gold)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    item.members.forEach { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatMoney(member.shareRial, withUnit = false, asToman = asToman),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "هر نفر ${formatMoney(if (item.members.isNotEmpty()) item.members.first().shareRial else 0, withUnit = true, asToman = asToman)} به ${group.payerName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!group.isSettled) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { settleTarget = item }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = extras.success,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = "تسویه",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = extras.success
                                )
                            }
                        }
                        IconButton(
                            onClick = { pendingDelete = item },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "حذف",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }

            item {
                WideInfoCard(
                    icon = iconForKey("light"),
                    title = "تقسیم منصفانه",
                    message = "مبلغ کل به‌طور مساوی بین افراد تقسیم می‌شود. برای مبالغ غیرقابل تقسیم، باقی‌مانده به نفر اول اضافه می‌شود.",
                    accent = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showAdd) {
        SplitDialog(
            asToman = asToman,
            onDismiss = { showAdd = false },
            onSave = { group, members ->
                vm.addSplitGroup(group, members)
                showAdd = false
            }
        )
    }
    settleTarget?.let { item ->
        ConfirmDialog(
            title = "تسویه گروه",
            message = "گروه «${item.group.title}» تسویه‌شده علامت‌گذاری شود؟ این عملیات تراکنش جدیدی ایجاد نمی‌کند.",
            confirmLabel = "تسویه شد",
            onConfirm = {
                vm.settleSplitGroup(item.group)
                settleTarget = null
            },
            onDismiss = { settleTarget = null }
        )
    }
    pendingDelete?.let { item ->
        ConfirmDialog(
            title = "حذف گروه",
            message = "گروه «${item.group.title}» و اعضای آن حذف شوند؟",
            confirmLabel = "حذف",
            onConfirm = {
                vm.deleteSplitGroup(item.group)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun SplitDialog(
    asToman: Boolean,
    onDismiss: () -> Unit,
    onSave: (SplitGroup, List<SplitMember>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var payer by remember { mutableStateOf("") }
    var digits by remember { mutableStateOf("") }
    var names by remember { mutableStateOf(listOf("", "")) }
    val amountRial = (digits.toLongOrNull() ?: 0L).let { if (asToman) it * 10 else it }
    val validNames = names.map { it.trim() }.filter { it.isNotBlank() }
    val share = if (validNames.isNotEmpty()) amountRial / validNames.size else 0L

    PergarDialog(title = "تقسیم هزینه جدید", onDismiss = onDismiss) {
        PergarTextField(
            value = title,
            onValueChange = { title = it },
            label = "عنوان",
            placeholder = "مثلاً شام رستوران"
        )
        Spacer(Modifier.height(10.dp))
        AmountField(
            digits = digits,
            onDigitsChange = { digits = it },
            asToman = asToman,
            label = "مبلغ کل"
        )
        Spacer(Modifier.height(10.dp))
        PergarTextField(
            value = payer,
            onValueChange = { payer = it },
            label = "پرداخت‌کننده",
            placeholder = "نام کسی که پرداخت کرد"
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "افراد (به‌جز پرداخت‌کننده)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        names.forEachIndexed { index, name ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                PergarTextField(
                    value = name,
                    onValueChange = { input ->
                        names = names.toMutableList().also { it[index] = input }
                    },
                    label = "نام نفر ${toPersianDigits((index + 1).toString())}",
                    modifier = Modifier.weight(1f)
                )
                if (names.size > 1) {
                    IconButton(onClick = {
                        names = names.filterIndexed { i, _ -> i != index }
                    }) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "حذف نفر",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { names = names + "" }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "افزودن نفر",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (amountRial > 0 && validNames.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "سهم هر نفر: ${formatMoney(share, withUnit = true, asToman = asToman)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(14.dp))
        PergarButton(
            text = "ثبت تقسیم",
            enabled = title.isNotBlank() && payer.isNotBlank() && amountRial > 0 && validNames.isNotEmpty(),
            onClick = {
                val members = validNames.mapIndexed { index, name ->
                    SplitMember(
                        groupId = 0,
                        name = name,
                        shareRial = share + if (index == 0) amountRial % validNames.size else 0L
                    )
                }
                onSave(
                    SplitGroup(
                        title = title.trim(),
                        totalRial = amountRial,
                        payerName = payer.trim()
                    ),
                    members
                )
            }
        )
    }
}
