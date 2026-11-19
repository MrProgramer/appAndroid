package ir.pergar.app.data.db

import ir.pergar.app.data.model.AccountKind
import ir.pergar.app.data.model.TxnType

object SeedData {

    val categories = listOf(
        Category(name = "خوراک و رستوران", iconKey = "restaurant", colorHex = "#F97316", type = TxnType.EXPENSE),
        Category(name = "خرید و مایحتاج", iconKey = "shopping", colorHex = "#8B5CF6", type = TxnType.EXPENSE),
        Category(name = "حمل‌ونقل", iconKey = "car", colorHex = "#06B6D4", type = TxnType.EXPENSE),
        Category(name = "قبوض و شارژ", iconKey = "receipt", colorHex = "#F59E0B", type = TxnType.EXPENSE),
        Category(name = "سلامت و درمان", iconKey = "health", colorHex = "#EF4444", type = TxnType.EXPENSE),
        Category(name = "خانه و اجاره", iconKey = "home", colorHex = "#10B981", type = TxnType.EXPENSE),
        Category(name = "تفریح و سفر", iconKey = "fun", colorHex = "#EC4899", type = TxnType.EXPENSE),
        Category(name = "آموزش", iconKey = "education", colorHex = "#3B82F6", type = TxnType.EXPENSE),
        Category(name = "اقساط و وام", iconKey = "loan", colorHex = "#6366F1", type = TxnType.EXPENSE),
        Category(name = "سایر هزینه‌ها", iconKey = "more", colorHex = "#64748B", type = TxnType.EXPENSE),
        Category(name = "حقوق و دستمزد", iconKey = "salary", colorHex = "#22C55E", type = TxnType.INCOME),
        Category(name = "سود و سرمایه‌گذاری", iconKey = "trend", colorHex = "#14B8A6", type = TxnType.INCOME),
        Category(name = "هدیه و کمک", iconKey = "gift", colorHex = "#EAB308", type = TxnType.INCOME),
        Category(name = "سایر درآمدها", iconKey = "plus", colorHex = "#64748B", type = TxnType.INCOME)
    )

    val defaultAccount = Account(
        name = "کیف پول",
        bank = "",
        kind = AccountKind.CASH,
        colorHex = "#22C55E",
        balanceRial = 0,
        isDefault = true
    )

    val accountColors = listOf(
        "#4C7DFF", "#22D3EE", "#22C55E", "#F5B942", "#F97316", "#EC4899", "#8B5CF6", "#14B8A6"
    )

    val banks = listOf(
        "بانک ملت", "بانک ملی", "بانک صادرات", "بانک تجارت", "بانک سپه",
        "بانک کشاورزی", "بانک مسکن", "بانک پاسارگاد", "بانک سامان", "بانک پارسیان",
        "بانک رفاه", "بانک سرمایه", "بانک آینده", "بانک شهر", "بانک اقتصاد نوین",
        "بانک دی", "بانک سینا", "بانک کارآفرین", "بانک ایران زمین", "پست بانک",
        "بانک گردشگری", "بانک قوامین", "بانک مهر ایران", "بانک رسالت", "بلوبانک", "آی‌بان"
    )
}
