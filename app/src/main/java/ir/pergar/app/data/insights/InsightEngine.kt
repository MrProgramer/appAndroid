package ir.pergar.app.data.insights

import ir.pergar.app.data.db.Budget
import ir.pergar.app.data.db.Category
import ir.pergar.app.data.db.Debt
import ir.pergar.app.data.db.Goal
import ir.pergar.app.data.db.Loan
import ir.pergar.app.data.db.TxnEntity
import ir.pergar.app.data.model.DebtDirection
import ir.pergar.app.data.model.Insight
import ir.pergar.app.data.model.InsightTone
import ir.pergar.app.data.model.TxnType
import ir.pergar.app.data.util.JalaliDate
import ir.pergar.app.data.util.formatCompactMoney
import kotlin.math.roundToInt

data class RadarResult(val score: Int, val insights: List<Insight>, val savingsRate: Float)

object InsightEngine {

    fun analyze(
        txns: List<TxnEntity>,
        categories: List<Category>,
        budgets: List<Budget>,
        loans: List<Loan>,
        debts: List<Debt>,
        goals: List<Goal>
    ): RadarResult {
        val today = JalaliDate.today()
        val (start, end) = Analytics.monthRange(today)
        val (prevStart, prevEnd) = Analytics.monthRange(today.addMonths(-1))

        val income = Analytics.sum(txns, TxnType.INCOME, start, end)
        val expense = Analytics.sum(txns, TxnType.EXPENSE, start, end)
        val prevExpense = Analytics.sum(txns, TxnType.EXPENSE, prevStart, prevEnd)
        val savings = income - expense
        val savingsRate = if (income > 0) savings.toFloat() / income else 0f

        val insights = mutableListOf<Insight>()
        var score = 100

        if (income > 0) {
            if (savingsRate >= 0.2f) {
                insights.add(
                    Insight(
                        title = "پس‌انداز در مسیر درست",
                        message = "این ماه ${(savingsRate * 100).roundToInt()}٪ از درآمدت را پس‌انداز کرده‌ای. عالی است!",
                        tone = InsightTone.POSITIVE,
                        iconKey = "trend"
                    )
                )
            } else if (savingsRate >= 0f) {
                insights.add(
                    Insight(
                        title = "فرصت بهبود پس‌انداز",
                        message = "نرخ پس‌انداز این ماه ${(savingsRate * 100).roundToInt()}٪ است؛ هدف خوب، بالای ۲۰٪ است.",
                        tone = InsightTone.NEUTRAL,
                        iconKey = "target"
                    )
                )
                score -= 12
            } else {
                insights.add(
                    Insight(
                        title = "هزینه بیشتر از درآمد",
                        message = "این ماه ${formatCompactMoney(-savings)} بیشتر از درآمدت خرج کرده‌ای.",
                        tone = InsightTone.WARNING,
                        iconKey = "warning"
                    )
                )
                score -= 25
            }
        } else if (expense > 0) {
            insights.add(
                Insight(
                    title = "درآمدی ثبت نشده",
                    message = "برای این ماه درآمدی ثبت نکرده‌ای؛ ثبت درآمد تصویر دقیق‌تری می‌دهد.",
                    tone = InsightTone.NEUTRAL,
                    iconKey = "plus"
                )
            )
            score -= 10
        }

        val topSlices = Analytics.categorySlices(txns, categories, TxnType.EXPENSE, start, end)
        val top = topSlices.firstOrNull()
        if (top != null && expense > 0) {
            val share = (top.totalRial.toFloat() / expense * 100).roundToInt()
            insights.add(
                Insight(
                    title = "بیشترین هزینه: ${top.category.name}",
                    message = "${toPersian(share)}٪ از هزینه‌های این ماه در این دسته بوده است (${formatCompactMoney(top.totalRial)}).",
                    tone = if (share > 45) InsightTone.WARNING else InsightTone.NEUTRAL,
                    iconKey = top.category.iconKey
                )
            )
            if (share > 45) score -= 6
        }

        if (prevExpense > 0 && expense > 0) {
            val diff = expense - prevExpense
            val percent = (diff.toFloat() / prevExpense * 100).roundToInt()
            if (percent > 15) {
                insights.add(
                    Insight(
                        title = "رشد هزینه‌ها",
                        message = "هزینه‌های این ماه ${toPersian(percent)}٪ بیشتر از ماه گذشته است.",
                        tone = InsightTone.WARNING,
                        iconKey = "chart"
                    )
                )
                score -= 8
            } else if (percent < -10) {
                insights.add(
                    Insight(
                        title = "کاهش هوشمندانه هزینه",
                        message = "نسبت به ماه گذشته ${toPersian(-percent)}٪ کمتر خرج کرده‌ای؛ ادامه بده!",
                        tone = InsightTone.POSITIVE,
                        iconKey = "trend"
                    )
                )
            }
        }

        val smallExpenses = txns.filter {
            it.type == TxnType.EXPENSE && it.dateEpochMillis >= start && it.dateEpochMillis < end &&
                it.amountRial in 10_000..1_000_000
        }
        if (smallExpenses.isNotEmpty() && expense > 0) {
            val smallSum = smallExpenses.sumOf { it.amountRial }
            val share = (smallSum.toFloat() / expense * 100).roundToInt()
            if (share >= 15) {
                insights.add(
                    Insight(
                        title = "هزینه‌های خرد",
                        message = "${toPersian(smallExpenses.size)} خرید کوچک، ${toPersian(share)}٪ از هزینه‌ها را ساخته‌اند (${formatCompactMoney(smallSum)}).",
                        tone = InsightTone.NEUTRAL,
                        iconKey = "shopping"
                    )
                )
                score -= 5
            }
        }

        val currentBudgets = budgets.filter { it.monthKey == today.monthKey() }
        val overBudget = currentBudgets.count { budget ->
            Analytics.spentForCategory(txns, budget.categoryId, start, end) > budget.limitRial
        }
        if (overBudget > 0) {
            insights.add(
                Insight(
                    title = "بودجه رد شده",
                    message = "در ${toPersian(overBudget)} بودجه از محدودیت عبور کرده‌ای؛ برای کنترل هزینه‌ها برنامه بریز.",
                    tone = InsightTone.WARNING,
                    iconKey = "pie"
                )
            )
            score -= (overBudget * 8).coerceAtMost(24)
        }

        val upcoming = loans.filter { it.paidCount < it.installmentCount }.minByOrNull { l ->
            JalaliDate.from(l.firstDueEpochMillis).addMonths(l.paidCount).toEpochMillis()
        }
        if (upcoming != null) {
            val due = JalaliDate.from(upcoming.firstDueEpochMillis).addMonths(upcoming.paidCount)
            val daysLeft = ((due.toEpochMillis() - System.currentTimeMillis()) / 86_400_000L).toInt()
            if (daysLeft in 0..7) {
                insights.add(
                    Insight(
                        title = "قسط نزدیک است",
                        message = "قسط ${upcoming.title} ${if (daysLeft == 0) "امروز" else "${toPersian(daysLeft)} روز آینده"} سررسید می‌شود.",
                        tone = InsightTone.WARNING,
                        iconKey = "calendar"
                    )
                )
            }
        }

        val borrowed = debts.filter { !it.isSettled && it.direction == DebtDirection.BORROWED }.sumOf { it.amountRial }
        if (borrowed > 0 && income > 0 && borrowed > income) {
            insights.add(
                Insight(
                    title = "بدهی بیشتر از درآمد ماه",
                    message = "بدهی‌های باز تو از درآمد این ماه بیشتر است؛ اولویت را به تسویه بدهی بده.",
                    tone = InsightTone.WARNING,
                    iconKey = "warning"
                )
            )
            score -= 10
        }

        val bestGoal = goals.filter { it.savedRial < it.targetRial }.maxByOrNull { it.savedRial.toFloat() / it.targetRial }
        if (bestGoal != null) {
            val progress = (bestGoal.savedRial.toFloat() / bestGoal.targetRial * 100).roundToInt()
            if (progress > 0) {
                insights.add(
                    Insight(
                        title = "پیشرفت هدف «${bestGoal.title}»",
                        message = "تا اینجا ${toPersian(progress)}٪ هدف را پیش برده‌ای؛ ${formatCompactMoney(bestGoal.targetRial - bestGoal.savedRial)} باقی مانده.",
                        tone = InsightTone.POSITIVE,
                        iconKey = "flag"
                    )
                )
            }
        }

        if (insights.size < 2) {
            insights.add(
                Insight(
                    title = "رادار آماده تحلیل است",
                    message = "با ثبت چند تراکنش دیگر، پرگار دقیق‌تر الگوی مالی‌ات را تحلیل می‌کند.",
                    tone = InsightTone.NEUTRAL,
                    iconKey = "radar"
                )
            )
        }

        return RadarResult(
            score = score.coerceIn(0, 100),
            insights = insights.take(5),
            savingsRate = savingsRate
        )
    }

    private fun toPersian(value: Int): String {
        val digits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        return buildString { for (c in value.toString()) append(if (c in '0'..'9') digits[c - '0'] else c) }
    }
}
