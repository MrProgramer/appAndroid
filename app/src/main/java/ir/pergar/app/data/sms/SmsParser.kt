package ir.pergar.app.data.sms

import ir.pergar.app.data.model.TxnType
import ir.pergar.app.data.util.CategoryGuesser
import ir.pergar.app.data.util.toEnglishDigits
import java.security.MessageDigest

data class ParsedSms(
    val rawBody: String,
    val sender: String,
    val epochMillis: Long,
    val bank: String?,
    val amountRial: Long?,
    val balanceRial: Long?,
    val type: TxnType?,
    val cardLast4: String?,
    val merchant: String?,
    val categoryHint: String?,
    val confidence: Float,
    val isTransaction: Boolean,
    val hash: String
) {
    val isUseful: Boolean
        get() = (isTransaction && amountRial != null && amountRial > 0) || (balanceRial != null && balanceRial >= 0)
}

object SmsParser {

    private val bankShortNames = listOf(
        "ملت", "ملی", "صادرات", "تجارت", "سپه", "کشاورزی", "مسکن", "پاسارگاد", "سامان",
        "پارسیان", "رفاه", "سرمایه", "آینده", "شهر", "اقتصاد نوین", "دی", "سینا", "کارآفرین",
        "ایران زمین", "پست بانک", "گردشگری", "قوامین", "مهر ایران", "رسالت", "بلو", "آی بانک", "آی‌بان"
    )

    private val otpWords = listOf("رمز", "کد تایید", "کد تأیید", "otp", "رمز پویا", "رمز یکبار", "کد فعالسازی", "کد فعال‌سازی")
    private val expenseWords = listOf("خرید", "برداشت", "پرداخت", "کسر")
    private val incomeWords = listOf("واریز", "به حساب شما", "افزایش موجودی", "دریافت", "وجه به حساب")

    private val numberRegex = Regex("""\d[\d,]{2,}""")
    private val currencyRegex = Regex("""^\s*(ریال|تومان)""")
    private val balanceRegex = Regex("""(?:مانده|موجودی|بالانس|موجودي)[^\d\n]{0,25}([\d,]{3,})""")

    fun normalize(raw: String): String {
        return toEnglishDigits(raw)
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('ۀ', 'ه')
            .replace('\u200c', ' ')
            .replace('٫', ',')
            .replace('٬', ',')
            .replace('،', ',')
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun parse(body: String, sender: String, epochMillis: Long): ParsedSms {
        val text = normalize(body)
        val bank = detectBank(text, sender)
        val isOtp = otpWords.any { text.contains(it, ignoreCase = true) }
        val type = if (isOtp) null else detectType(text)
        val balance = extractBalance(text, type)
        val amountRial = if (type != null && !isOtp) extractAmount(text) else null
        val isTransaction = !isOtp && type != null && amountRial != null && amountRial > 0
        val merchant = extractMerchant(text)
        val category = if (isTransaction) {
            CategoryGuesser.guess(text + " " + (merchant ?: ""), type ?: TxnType.EXPENSE)
        } else null

        var confidence = 0f
        if (bank != null) confidence += 0.25f
        if (amountRial != null) confidence += 0.35f
        if (type != null) confidence += 0.2f
        if (balance != null) confidence += 0.15f
        if (merchant != null) confidence += 0.05f
        confidence = confidence.coerceAtMost(1f)

        return ParsedSms(
            rawBody = body,
            sender = sender,
            epochMillis = epochMillis,
            bank = bank,
            amountRial = amountRial,
            balanceRial = balance,
            type = type,
            cardLast4 = extractCardLast4(text),
            merchant = merchant,
            categoryHint = category,
            confidence = confidence,
            isTransaction = isTransaction,
            hash = hashOf(sender, epochMillis, body)
        )
    }

    private fun detectBank(text: String, sender: String): String? {
        for (name in bankShortNames) {
            if (text.contains("بانک $name") || text.contains("بانک$name")) return "بانک $name"
        }
        for (name in bankShortNames) {
            if (sender.contains(name)) return "بانک $name"
        }
        if (text.contains("بلوبانک") || sender.contains("blubank", true)) return "بلوبانک"
        if (text.contains("آی‌بان") || text.contains("آی بانک")) return "آی‌بان"
        return null
    }

    private fun detectType(text: String): TxnType? {
        val expenseIndex = expenseWords.map { text.indexOf(it) }.filter { it >= 0 }.minOrNull()
        val incomeIndex = incomeWords.map { text.indexOf(it) }.filter { it >= 0 }.minOrNull()
        return when {
            expenseIndex != null && (incomeIndex == null || expenseIndex <= incomeIndex) -> TxnType.EXPENSE
            incomeIndex != null -> TxnType.INCOME
            text.contains("انتقال") -> {
                if (text.contains("به حساب شما") || text.contains("به کارت شما")) TxnType.INCOME
                else TxnType.EXPENSE
            }
            else -> null
        }
    }

    private fun extractBalance(text: String, type: TxnType?): Long? {
        val match = balanceRegex.find(text) ?: return null
        val value = match.groupValues[1].replace(",", "").toLongOrNull() ?: return null
        if (value <= 0) return null
        val after = text.substring(match.range.last + 1)
        val scaled = if (after.take(12).contains("تومان")) value * 10 else value
        if (type == null && scaled < 1000) return null
        return scaled
    }

    private fun extractAmount(text: String): Long? {
        val hits = numberRegex.findAll(text).map { match ->
            val raw = match.value
            val digits = raw.replace(",", "")
            Triple(raw, digits.toLongOrNull() ?: 0L, match.range)
        }.filter { it.second > 0 && it.first.replace(",", "").length in 3..13 }.toList()

        if (hits.isEmpty()) return null

        val best = hits.maxByOrNull { (raw, value, range) ->
            var score = 0
            val before = text.substring(maxOf(0, range.first - 35), range.first)
            val after = text.substring(minOf(text.length, range.last + 1), minOf(text.length, range.last + 14))
            if (Regex("""(مبلغ|به مبلغ|بابت)\s*:?\s*$""").containsMatchIn(before)) score += 4
            if (Regex("""(برداشت|واریز|خرید|پرداخت|انتقال|کسر)\s*:?\s*$""").containsMatchIn(before)) score += 3
            if (currencyRegex.containsMatchIn(after)) score += 3
            if (value >= 10_000) score += 1
            if (raw.replace(",", "").length >= 16) score -= 8
            if (Regex("""(کارت|حساب|شبا|شناسه|شماره)\s*:?\s*$""").containsMatchIn(before)) score -= 5
            score
        } ?: return null

        val (_, value, range) = best
        val after = text.substring(minOf(text.length, range.last + 1), minOf(text.length, range.last + 14))
        return if (after.contains("تومان")) value * 10 else value
    }

    private fun extractCardLast4(text: String): String? {
        Regex("""\d{4}[-* ]*\*{4}[-* ]*\*{4}[-* ]*(\d{4})""").find(text)?.let {
            return it.groupValues[1]
        }
        Regex("""[*•xX]{4,}\s*(\d{4})(?!\d)""").find(text)?.let {
            return it.groupValues[1]
        }
        Regex("""\b(6\d{15})\b""").find(text)?.let {
            return it.groupValues[1].takeLast(4)
        }
        Regex("""(?:کارت|حساب)[^\d\n]{0,25}(\d{4})(?!\d)""").find(text)?.let {
            return it.groupValues[1]
        }
        return null
    }

    private fun extractMerchant(text: String): String? {
        val match = Regex("""(?:خرید|پرداخت)[^,.;]{0,25}?(?:از|در)\s+([^,.;\d]{2,40})""").find(text)
        val value = match?.groupValues?.get(1)?.trim() ?: return null
        val cleaned = value.substringBefore(" مبلغ").substringBefore(" برداشت").trim()
        return cleaned.takeIf { it.length >= 2 }
    }

    fun hashOf(sender: String, epochMillis: Long, body: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$sender|$epochMillis|$body".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it.toInt() and 0xFF) }
    }
}
