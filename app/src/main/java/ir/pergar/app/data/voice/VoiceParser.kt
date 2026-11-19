package ir.pergar.app.data.voice

import ir.pergar.app.data.model.TxnType
import ir.pergar.app.data.util.CategoryGuesser
import ir.pergar.app.data.util.toEnglishDigits

data class VoiceResult(
    val rawText: String,
    val amountToman: Long?,
    val type: TxnType,
    val categoryHint: String?,
    val spokenDateHint: Pair<Int, Int>?
)

object VoiceParser {

    private val numberWords: Map<String, Long> = buildMap {
        put("صفر", 0)
        put("یک", 1); put("دو", 2); put("سه", 3); put("چهار", 4); put("پنج", 5)
        put("شش", 6); put("هفت", 7); put("هشت", 8); put("نه", 9)
        put("ده", 10); put("یازده", 11); put("دوازده", 12); put("سیزده", 13)
        put("چهارده", 14); put("پانزده", 15); put("شانزده", 16); put("هفده", 17)
        put("هجده", 18); put("نوزده", 19)
        put("بیست", 20); put("سی", 30); put("چهل", 40); put("پنجاه", 50)
        put("شصت", 60); put("هفتاد", 70); put("هشتاد", 80); put("نود", 90)
        put("صد", 100); put("یکصد", 100); put("دویست", 200); put("سیصد", 300)
        put("چهارصد", 400); put("پانصد", 500); put("ششصد", 600); put("هفتصد", 700)
        put("هشتصد", 800); put("نهصد", 900)
        put("نیم", 0)
    }

    private val scaleWords = mapOf(
        "هزار" to 1_000L,
        "میلیون" to 1_000_000L,
        "میلیارد" to 1_000_000_000L
    )

    private val expenseHints = listOf("خریدم", "خرید", "خرج", "هزینه", "پرداخت", "دادم", "پرداختم", "کشیدم")
    private val incomeHints = listOf("درآمد", "واریز", "گرفتم", "حقوق", "فروختم", "دریافت", "رسید")

    fun parse(rawText: String): VoiceResult {
        val normalized = rawText
            .replace('\u200c', ' ')
            .replace('،', ' ')
            .replace('٫', ' ')
            .trim()
        val english = toEnglishDigits(normalized)

        val amount = amountFromDigits(english) ?: amountFromWords(normalized)

        val expIndex = expenseHints.map { normalized.indexOf(it) }.filter { it >= 0 }.minOrNull()
        val incIndex = incomeHints.map { normalized.indexOf(it) }.filter { it >= 0 }.minOrNull()
        val type = when {
            incIndex != null && (expIndex == null || incIndex < expIndex) -> TxnType.INCOME
            expIndex != null -> TxnType.EXPENSE
            else -> TxnType.EXPENSE
        }

        val category = CategoryGuesser.guess(normalized, type)
        val dateHint = parseSpokenDate(normalized)

        return VoiceResult(
            rawText = rawText,
            amountToman = amount,
            type = type,
            categoryHint = category,
            spokenDateHint = dateHint
        )
    }

    private fun amountFromDigits(text: String): Long? {
        val scaled = Regex("""(\d[\d,]{0,12})\s*(هزار|میلیون|میلیارد)""").find(text)
        if (scaled != null) {
            val base = scaled.groupValues[1].replace(",", "").toLongOrNull() ?: return null
            val multiplier = scaleWords[scaled.groupValues[2]] ?: 1L
            return base * multiplier
        }
        val plain = Regex("""\d[\d,]{2,}""").find(text)?.value?.replace(",", "")?.toLongOrNull()
        return plain
    }

    private fun amountFromWords(text: String): Long? {
        val tokens = text.split(Regex("\\s+")).map { it.trim('.', '!', '?', ':', ';', '،') }.filter { it.isNotEmpty() }
        var total = 0L
        var current = 0L
        var sawAny = false
        for (token in tokens) {
            when {
                token == "و" -> Unit
                numberWords.containsKey(token) -> {
                    current += numberWords[token] ?: 0L
                    sawAny = true
                }
                scaleWords.containsKey(token) -> {
                    if (current == 0L) current = 1L
                    total += current * (scaleWords[token] ?: 1L)
                    current = 0L
                    sawAny = true
                }
            }
        }
        if (!sawAny) return null
        val value = total + current
        return value.takeIf { it > 0 }
    }

    private fun parseSpokenDate(text: String): Pair<Int, Int>? {
        val dayMonth = Regex("""(\d{1,2})\s*(فروردین|اردیبهشت|خرداد|تیر|مرداد|شهریور|مهر|آبان|آذر|دی|بهمن|اسفند)""").find(text)
        if (dayMonth != null) {
            val day = dayMonth.groupValues[1].toIntOrNull() ?: return null
            val month = listOf(
                "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
                "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
            ).indexOf(dayMonth.groupValues[2]) + 1
            if (month > 0) return day to month
        }
        return null
    }
}
