package ir.pergar.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ir.pergar.app.data.util.JalaliDate
import ir.pergar.app.data.util.formatMoney
import ir.pergar.app.data.util.toEnglishDigits
import ir.pergar.app.data.util.toPersianDigits
import ir.pergar.app.ui.theme.PergarThemeInfo

@Composable
fun PergarDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val extras = PergarThemeInfo.colors
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 580.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, extras.glassBorder, RoundedCornerShape(28.dp))
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "بستن",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun PergarTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    val extras = PergarThemeInfo.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        singleLine = singleLine,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = extras.glassBorder,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
fun AmountField(
    digits: String,
    onDigitsChange: (String) -> Unit,
    asToman: Boolean,
    modifier: Modifier = Modifier,
    label: String = "مبلغ"
) {
    val extras = PergarThemeInfo.colors
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = toPersianDigits(digits),
            onValueChange = { input ->
                onDigitsChange(toEnglishDigits(input).filter { it in '0'..'9' }.take(15))
            },
            label = { Text(label) },
            suffix = {
                Text(
                    if (asToman) "تومان" else "ریال",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = extras.glassBorder,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        if (digits.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            val value = digits.toLongOrNull() ?: 0L
            Text(
                text = formatMoney(if (asToman) value * 10 else value, withUnit = true, asToman = asToman),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = PergarThemeInfo.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            val background by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent,
                label = "segmentBg"
            )
            val foreground by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "segmentFg"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(background)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelMedium,
                    color = foreground,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SelectRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = PergarThemeInfo.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, extras.glassBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Icon(
            Icons.Rounded.ChevronLeft,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ChoiceDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    PergarDialog(title = title, onDismiss = onDismiss) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
                    )
                    .clickable {
                        onSelect(index)
                        onDismiss()
                    }
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (selected) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "تأیید",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val extras = PergarThemeInfo.colors
    PergarDialog(title = title, onDismiss = onDismiss) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GhostButton(
                text = "انصراف",
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(extras.danger)
                    .clickable(onClick = onConfirm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = confirmLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun JalaliDatePickerDialog(
    initialMillis: Long,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit
) {
    val initial = remember(initialMillis) { JalaliDate.from(initialMillis) }
    var year by remember { mutableIntStateOf(initial.year) }
    var month by remember { mutableIntStateOf(initial.month) }
    var day by remember { mutableIntStateOf(initial.day) }
    val today = remember { JalaliDate.today() }
    val primary = MaterialTheme.colorScheme.primary

    PergarDialog(title = "انتخاب تاریخ شمسی", onDismiss = onDismiss) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                month -= 1
                if (month < 1) {
                    month = 12
                    year -= 1
                }
                day = day.coerceAtMost(JalaliDate(year, month, 1).monthLength())
            }) {
                Icon(Icons.Rounded.ChevronRight, contentDescription = "ماه قبل")
            }
            Text(
                text = "${JalaliDate.MONTH_NAMES[month - 1]} ${toPersianDigits(year.toString())}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                month += 1
                if (month > 12) {
                    month = 1
                    year += 1
                }
                day = day.coerceAtMost(JalaliDate(year, month, 1).monthLength())
            }) {
                Icon(Icons.Rounded.ChevronLeft, contentDescription = "ماه بعد")
            }
        }

        Row(Modifier.fillMaxWidth()) {
            listOf("ش", "ی", "د", "س", "چ", "پ", "ج").forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(6.dp))

        val first = JalaliDate(year, month, 1)
        val offset = first.weekdayIndex()
        val daysInMonth = first.monthLength()
        val cells = buildList {
            repeat(offset) { add(null) }
            for (d in 1..daysInMonth) add(d)
            while (size % 7 != 0) add(null)
        }
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { d ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (d != null) {
                            val isSelected = d == day
                            val isToday = today.year == year && today.month == month && today.day == d
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> primary
                                            isToday -> primary.copy(alpha = 0.12f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { day = d },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = toPersianDigits(d.toString()),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> primary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GhostButton(
                text = "انصراف",
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )
            PergarButton(
                text = "تأیید تاریخ",
                onClick = {
                    onSelect(JalaliDate(year, month, day).toEpochMillis())
                    onDismiss()
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
