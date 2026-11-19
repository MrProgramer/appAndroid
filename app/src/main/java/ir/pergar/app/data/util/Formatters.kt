package ir.pergar.app.data.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.abs

private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
private const val THOUSANDS = '٬'

fun toPersianDigits(text: String): String = buildString(text.length) {
    for (c in text) {
        append(if (c in '0'..'9') PERSIAN_DIGITS[c - '0'] else c)
    }
}

fun toEnglishDigits(text: String): String = buildString(text.length) {
    for (c in text) {
        when (c) {
            in '۰'..'۹' -> append(('0' + (c - '۰')))
            in '٠'..'٩' -> append(('0' + (c - '٠')))
            else -> append(c)
        }
    }
}

fun groupDigits(value: Long): String {
    val negative = value < 0
    val digits = abs(value).toString()
    val sb = StringBuilder()
    for (i in digits.indices) {
        if (i > 0 && (digits.length - i) % 3 == 0) sb.append(THOUSANDS)
        sb.append(digits[i])
    }
    return (if (negative) "−" else "") + toPersianDigits(sb.toString())
}

fun formatMoney(rial: Long, withUnit: Boolean = true, asToman: Boolean = true): String {
    val value = if (asToman) rial / 10 else rial
    val text = groupDigits(value)
    return if (withUnit) "$text ${if (asToman) "تومان" else "ریال"}" else text
}

fun formatCompactMoney(rial: Long, asToman: Boolean = true): String {
    val value = if (asToman) rial / 10 else rial
    val absValue = abs(value)
    val (scaled, unit) = when {
        absValue >= 1_000_000_000L -> value / 100_000_000.0 to "میلیارد"
        absValue >= 1_000_000L -> value / 100_000.0 to "میلیون"
        absValue >= 1_000L -> value / 100.0 to "هزار"
        else -> value.toDouble() to ""
    }
    val rounded = if (scaled == scaled.toLong().toDouble()) {
        scaled.toLong().toString()
    } else {
        String.format("%.1f", scaled).replace('.', '٫')
    }
    return (toPersianDigits(rounded) + " " + unit).trim()
}

fun formatJalaliLong(epochMillis: Long): String {
    val date = JalaliDate.from(epochMillis)
    return "${JalaliDate.WEEKDAYS[date.weekdayIndex()]} ${toPersianDigits(date.day.toString())} ${date.monthName} ${toPersianDigits(date.year.toString())}"
}

fun formatJalaliShort(epochMillis: Long): String {
    val date = JalaliDate.from(epochMillis)
    return "${toPersianDigits(date.day.toString())} ${date.monthName}"
}

fun formatJalaliNumeric(epochMillis: Long): String {
    val date = JalaliDate.from(epochMillis)
    return toPersianDigits("%04d/%02d/%02d".format(date.year, date.month, date.day))
}

fun formatTime(epochMillis: Long): String {
    val time = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalTime()
    return toPersianDigits("%02d:%02d".format(time.hour, time.minute))
}

fun formatMonthLabel(monthKey: String): String {
    val parts = monthKey.split("-")
    if (parts.size != 2) return monthKey
    val month = parts[1].toIntOrNull() ?: return monthKey
    return JalaliDate.MONTH_NAMES.getOrNull(month - 1) + " " + toPersianDigits(parts[0])
}

fun relativeDayLabel(epochMillis: Long): String {
    val zone = ZoneId.systemDefault()
    val target = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
    val today = LocalDate.now(zone)
    return when (target) {
        today -> "امروز"
        today.minusDays(1) -> "دیروز"
        else -> formatJalaliShort(epochMillis)
    }
}

fun daysUntilLabel(targetMillis: Long): String {
    val zone = ZoneId.systemDefault()
    val target = Instant.ofEpochMilli(targetMillis).atZone(zone).toLocalDate()
    val today = LocalDate.now(zone)
    val days = java.time.temporal.ChronoUnit.DAYS.between(today, target)
    return when {
        days == 0L -> "امروز"
        days == 1L -> "فردا"
        days > 1 -> "${toPersianDigits(days.toString())} روز آینده"
        days == -1L -> "دیروز"
        else -> "${toPersianDigits(abs(days).toString())} روز گذشته"
    }
}

fun millisAtJalaliTime(date: JalaliDate, hour: Int, minute: Int): Long {
    val (gy, gm, gd) = Jalali.toGregorian(date.year, date.month, date.day)
    return LocalDate.of(gy, gm, gd)
        .atTime(LocalTime.of(hour, minute))
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}
