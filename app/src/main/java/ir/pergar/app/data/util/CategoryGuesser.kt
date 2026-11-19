package ir.pergar.app.data.util

import ir.pergar.app.data.model.TxnType

object CategoryGuesser {

    private val expenseRules = listOf(
        listOf("رستوران", "کافه", "فست فود", "فست‌فود", "قهوه", "بیرون بر", "بیرون‌بر", "اسنپ فود", "اسنپ‌فود") to "خوراک و رستوران",
        listOf("داروخانه", "درمان", "بیمارستان", "کلینیک", "پزشک", "دندان", "آزمایشگاه") to "سلامت و درمان",
        listOf("تاکسی", "اسنپ", "تپسی", "بنزین", "پمپ بنزین", "مترو", "اتوبوس", "بلیت", "جابرب", "پیک موتوری") to "حمل‌ونقل",
        listOf("اینترنت", "همراه اول", "ایرانسل", "رایتل", "شارژ", "قبض", "برق", "گاز", "آب", "تلفن", "بیمه") to "قبوض و شارژ",
        listOf("مدرسه", "دانشگاه", "شهریه", "کتاب", "آموزش", "کلاس", "دوره") to "آموزش",
        listOf("اجاره", "مسکن", "شارژ ساختمان", "ودیعه") to "خانه و اجاره",
        listOf("سینما", "تفریح", "سفر", "هتل", "پرواز", "بازی", "پارک") to "تفریح و سفر",
        listOf("قسط", "وام", "اقساط") to "اقساط و وام",
        listOf("سوپر", "فروشگاه", "هایپر", "افق کوروش", "دیجی کالا", "دیجی‌کالا", "بامیلو", "خرید", "مایحتاج") to "خرید و مایحتاج"
    )

    private val incomeRules = listOf(
        listOf("حقوق", "دستمزد", "عیدی", "پاداش") to "حقوق و دستمزد",
        listOf("سود", "سپرده", "سرمایه گذاری", "سرمایه‌گذاری", "بورس") to "سود و سرمایه‌گذاری",
        listOf("هدیه", "کمک", "قرض") to "هدیه و کمک"
    )

    fun guess(text: String, type: TxnType): String? {
        val normalized = text.replace('\u200c', ' ')
        val rules = if (type == TxnType.INCOME) incomeRules + expenseRules else expenseRules
        for ((keywords, category) in rules) {
            if (keywords.any { normalized.contains(it.replace('\u200c', ' ')) }) return category
        }
        return null
    }
}
