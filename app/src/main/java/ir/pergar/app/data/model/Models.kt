package ir.pergar.app.data.model

enum class TxnType { INCOME, EXPENSE, TRANSFER }

enum class TxnSource { MANUAL, SMS, VOICE, AUTO }

enum class AccountKind { BANK, CASH, WALLET }

enum class DebtDirection { LENT, BORROWED }

data class SplitGroupWithMembers(
    val group: SplitGroup,
    val members: List<SplitMember>
)

data class MonthlyFlow(
    val monthKey: String,
    val label: String,
    val incomeRial: Long,
    val expenseRial: Long
)

data class CategorySlice(
    val category: Category,
    val totalRial: Long
)

data class Insight(
    val title: String,
    val message: String,
    val tone: InsightTone,
    val iconKey: String
)

enum class InsightTone { POSITIVE, NEUTRAL, WARNING }
