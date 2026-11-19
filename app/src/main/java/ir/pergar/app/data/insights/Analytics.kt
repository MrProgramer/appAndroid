package ir.pergar.app.data.insights

import ir.pergar.app.data.db.Account
import ir.pergar.app.data.db.Category
import ir.pergar.app.data.db.TxnEntity
import ir.pergar.app.data.model.CategorySlice
import ir.pergar.app.data.model.MonthlyFlow
import ir.pergar.app.data.model.TxnType
import ir.pergar.app.data.util.JalaliDate

object Analytics {

    fun monthRange(date: JalaliDate = JalaliDate.today()): Pair<Long, Long> {
        val start = JalaliDate(date.year, date.month, 1)
        val end = start.addMonths(1)
        return start.toEpochMillis() to end.toEpochMillis()
    }

    fun monthRange(monthKey: String): Pair<Long, Long> {
        val parts = monthKey.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: JalaliDate.today().year
        val month = parts.getOrNull(1)?.toIntOrNull() ?: JalaliDate.today().month
        return monthRange(JalaliDate(year, month, 1))
    }

    fun sum(txns: List<TxnEntity>, type: TxnType, start: Long, end: Long): Long =
        txns.filter {
            it.type == type && it.dateEpochMillis >= start && it.dateEpochMillis < end
        }.sumOf { it.amountRial }

    fun totalBalance(accounts: List<Account>): Long = accounts.sumOf { it.balanceRial }

    fun monthlyFlows(
        txns: List<TxnEntity>,
        monthsBack: Int = 5,
        now: JalaliDate = JalaliDate.today()
    ): List<MonthlyFlow> {
        val result = mutableListOf<MonthlyFlow>()
        for (offset in monthsBack downTo 0) {
            val date = now.addMonths(-offset)
            val (start, end) = monthRange(date)
            result.add(
                MonthlyFlow(
                    monthKey = date.monthKey(),
                    label = date.monthName,
                    incomeRial = sum(txns, TxnType.INCOME, start, end),
                    expenseRial = sum(txns, TxnType.EXPENSE, start, end)
                )
            )
        }
        return result
    }

    fun categorySlices(
        txns: List<TxnEntity>,
        categories: List<Category>,
        type: TxnType,
        start: Long,
        end: Long
    ): List<CategorySlice> {
        val byCategory = txns
            .filter { it.type == type && it.dateEpochMillis >= start && it.dateEpochMillis < end }
            .groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amountRial } }

        return byCategory.mapNotNull { (catId, total) ->
            val category = categories.firstOrNull { it.id == catId }
                ?: categories.firstOrNull { it.name == "سایر هزینه‌ها" }
                ?: return@mapNotNull null
            CategorySlice(category, total)
        }.sortedByDescending { it.totalRial }
    }

    fun spentForCategory(
        txns: List<TxnEntity>,
        categoryId: Long?,
        start: Long,
        end: Long
    ): Long = txns.filter {
        it.type == TxnType.EXPENSE &&
            it.dateEpochMillis >= start && it.dateEpochMillis < end &&
            (categoryId == null || it.categoryId == categoryId)
    }.sumOf { it.amountRial }

    fun dailyExpenses(txns: List<TxnEntity>, start: Long, end: Long): Map<Int, Long> {
        val result = mutableMapOf<Int, Long>()
        txns.filter { it.type == TxnType.EXPENSE && it.dateEpochMillis >= start && it.dateEpochMillis < end }
            .forEach { txn ->
                val day = JalaliDate.from(txn.dateEpochMillis).day
                result[day] = (result[day] ?: 0L) + txn.amountRial
            }
        return result
    }
}
