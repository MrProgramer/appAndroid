package ir.pergar.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.pergar.app.data.db.Category
import ir.pergar.app.data.db.TxnEntity
import ir.pergar.app.data.model.TxnSource
import ir.pergar.app.data.model.TxnType
import ir.pergar.app.data.util.formatMoney
import ir.pergar.app.data.util.formatTime
import ir.pergar.app.data.util.relativeDayLabel
import ir.pergar.app.ui.theme.PergarThemeInfo

@Composable
fun TransactionRow(
    txn: TxnEntity,
    category: Category?,
    accountName: String?,
    asToman: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val extras = PergarThemeInfo.colors
    val isIncome = txn.type == TxnType.INCOME
    val isTransfer = txn.type == TxnType.TRANSFER
    val amountColor = when {
        isIncome -> extras.success
        isTransfer -> MaterialTheme.colorScheme.secondary
        else -> extras.danger
    }
    val sign = when (txn.type) {
        TxnType.INCOME -> "+"
        TxnType.EXPENSE -> "−"
        TxnType.TRANSFER -> ""
    }
    val typeLabel = when (txn.type) {
        TxnType.INCOME -> "درآمد"
        TxnType.EXPENSE -> "هزینه"
        TxnType.TRANSFER -> "انتقال"
    }
    val sourceLabel = when (txn.source) {
        TxnSource.SMS -> "پیامک"
        TxnSource.VOICE -> "صدا"
        TxnSource.AUTO -> "خودکار"
        TxnSource.MANUAL -> null
    }
    val title = txn.merchant.takeIf { it.isNotBlank() }
        ?: txn.note.takeIf { it.isNotBlank() }?.take(42)
        ?: category?.name
        ?: typeLabel
    val subtitle = listOfNotNull(
        category?.name?.takeIf { it != title },
        accountName,
        sourceLabel
    ).joinToString(" · ")

    val baseModifier = modifier
        .fillMaxWidth()
    Row(
        modifier = (if (onClick != null) baseModifier.clickable(onClick = onClick) else baseModifier)
            .clip(RoundedCornerShape(16.dp))
            .padding(horizontal = 4.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryIcon(
            iconKey = category?.iconKey ?: if (isTransfer) "swap" else "receipt",
            colorHex = category?.colorHex ?: "#4C7DFF",
            size = 42.dp,
            iconSize = 21.dp
        )
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$sign${formatMoney(txn.amountRial, withUnit = false, asToman = asToman)}",
                style = MaterialTheme.typography.labelLarge,
                color = amountColor
            )
            Text(
                text = "${relativeDayLabel(txn.dateEpochMillis)} · ${formatTime(txn.dateEpochMillis)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DayHeader(
    label: String,
    totalRial: Long,
    asToman: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 2.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.weight(1f))
        if (totalRial != 0L) {
            Text(
                text = "خرج روز: ${formatMoney(totalRial, withUnit = false, asToman = asToman)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
