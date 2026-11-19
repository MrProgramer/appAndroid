package ir.pergar.app.data.repo

import android.content.Context
import ir.pergar.app.data.db.Account
import ir.pergar.app.data.db.AppDatabase
import ir.pergar.app.data.db.Budget
import ir.pergar.app.data.db.Debt
import ir.pergar.app.data.db.Goal
import ir.pergar.app.data.db.Loan
import ir.pergar.app.data.db.SeedData
import ir.pergar.app.data.db.SplitGroup
import ir.pergar.app.data.db.SplitMember
import ir.pergar.app.data.db.TaskItem
import ir.pergar.app.data.db.TxnEntity
import ir.pergar.app.data.model.AccountKind
import ir.pergar.app.data.model.DebtDirection
import ir.pergar.app.data.model.SplitGroupWithMembers
import ir.pergar.app.data.model.TxnSource
import ir.pergar.app.data.model.TxnType
import ir.pergar.app.data.prefs.AppPrefs
import ir.pergar.app.data.sms.ParsedSms
import ir.pergar.app.data.util.JalaliDate
import ir.pergar.app.data.util.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.math.abs

sealed interface ImportResult {
    data class Imported(val transactionId: Long, val balanceUpdated: Boolean) : ImportResult
    data class Duplicate(val hash: String) : ImportResult
    data class Skipped(val reason: String) : ImportResult
}

class FinanceRepository(
    private val context: Context,
    private val db: AppDatabase,
    val prefs: AppPrefs
) {
    val accounts: Flow<List<Account>> = db.accountDao().observeAll()
    val categories = db.categoryDao().observeAll()
    val transactions = db.transactionDao().observeAll()
    val budgets = db.budgetDao().observeAll()
    val loans = db.loanDao().observeAll()
    val debts = db.debtDao().observeAll()
    val goals = db.goalDao().observeAll()
    val tasks = db.taskDao().observeAll()

    val splitGroups: Flow<List<SplitGroupWithMembers>> = combine(
        db.splitDao().observeGroups(),
        db.splitDao().observeMembers()
    ) { groups, members ->
        groups.map { group ->
            SplitGroupWithMembers(group, members.filter { it.groupId == group.id })
        }
    }

    suspend fun ensureSeeded() {
        if (db.categoryDao().count() == 0) {
            db.categoryDao().insertAll(SeedData.categories)
            db.accountDao().insert(SeedData.defaultAccount)
        }
    }

    // ---------- Transactions ----------

    suspend fun addTransaction(txn: TxnEntity): Long {
        val id = db.transactionDao().insert(txn)
        if (id != -1L) applyEffect(txn, 1)
        return id
    }

    suspend fun updateTransaction(old: TxnEntity, new: TxnEntity) {
        applyEffect(old, -1)
        db.transactionDao().update(new)
        applyEffect(new, 1)
    }

    suspend fun deleteTransaction(txn: TxnEntity) {
        applyEffect(txn, -1)
        db.transactionDao().delete(txn)
    }

    private suspend fun applyEffect(txn: TxnEntity, sign: Int) {
        val amount = txn.amountRial * sign
        when (txn.type) {
            TxnType.INCOME -> txn.accountId?.let { db.accountDao().addBalance(it, amount) }
            TxnType.EXPENSE -> txn.accountId?.let { db.accountDao().addBalance(it, -amount) }
            TxnType.TRANSFER -> {
                txn.accountId?.let { db.accountDao().addBalance(it, -amount) }
                txn.toAccountId?.let { db.accountDao().addBalance(it, amount) }
            }
        }
    }

    // ---------- Accounts ----------

    suspend fun addAccount(account: Account): Long = db.accountDao().insert(account)

    suspend fun updateAccount(account: Account) = db.accountDao().update(account)

    suspend fun deleteAccount(account: Account) = db.accountDao().delete(account)

    suspend fun transferBetween(from: Account, to: Account, amountRial: Long, note: String = "انتقال بین حساب‌ها") {
        addTransaction(
            TxnEntity(
                type = TxnType.TRANSFER,
                amountRial = amountRial,
                accountId = from.id,
                toAccountId = to.id,
                note = note,
                source = TxnSource.MANUAL
            )
        )
    }

    // ---------- Categories / Budgets ----------

    suspend fun upsertBudget(categoryId: Long?, limitRial: Long, monthKey: String) {
        val existing = db.budgetDao().getAll().firstOrNull {
            it.categoryId == categoryId && it.monthKey == monthKey
        }
        if (existing != null) {
            db.budgetDao().update(existing.copy(limitRial = limitRial))
        } else {
            db.budgetDao().insert(Budget(categoryId = categoryId, limitRial = limitRial, monthKey = monthKey))
        }
    }

    suspend fun deleteBudget(budget: Budget) = db.budgetDao().delete(budget)

    // ---------- Loans ----------

    suspend fun addLoan(loan: Loan): Long {
        val id = db.loanDao().insert(loan)
        scheduleLoanReminder(loan.copy(id = id))
        return id
    }

    suspend fun updateLoan(loan: Loan) = db.loanDao().update(loan)

    suspend fun deleteLoan(loan: Loan) = db.loanDao().delete(loan)

    suspend fun payInstallment(loan: Loan) {
        if (loan.paidCount >= loan.installmentCount) return
        val categoryId = resolveCategory("اقساط و وام", TxnType.EXPENSE)
        addTransaction(
            TxnEntity(
                type = TxnType.EXPENSE,
                amountRial = loan.installmentRial,
                categoryId = categoryId,
                accountId = loan.accountId,
                note = "قسط ${loan.paidCount + 1} از ${loan.installmentCount} — ${loan.title}",
                merchant = loan.bank,
                bank = loan.bank,
                source = TxnSource.MANUAL
            )
        )
        val updated = loan.copy(paidCount = loan.paidCount + 1)
        db.loanDao().update(updated)
        scheduleLoanReminder(updated)
    }

    private fun scheduleLoanReminder(loan: Loan) {
        if (!loan.reminderEnabled || loan.paidCount >= loan.installmentCount) return
        val nextDue = nextInstallmentMillis(loan)
        if (nextDue != null && nextDue > System.currentTimeMillis()) {
            ReminderScheduler.schedule(
                context,
                id = 10_000 + loan.id.toInt(),
                title = "قسط ${loan.title}",
                message = "سررسید قسط ${loan.paidCount + 1} از ${loan.installmentCount}",
                atMillis = nextDue
            )
        }
    }

    fun nextInstallmentMillis(loan: Loan): Long? {
        val base = JalaliDate.from(loan.firstDueEpochMillis)
        return base.addMonths(loan.paidCount).toEpochMillis()
    }

    // ---------- Debts ----------

    suspend fun addDebt(debt: Debt): Long = db.debtDao().insert(debt)

    suspend fun updateDebt(debt: Debt) = db.debtDao().update(debt)

    suspend fun deleteDebt(debt: Debt) = db.debtDao().delete(debt)

    suspend fun settleDebt(debt: Debt) {
        if (debt.isSettled) return
        val isReceive = debt.direction == DebtDirection.LENT
        val type = if (isReceive) TxnType.INCOME else TxnType.EXPENSE
        val categoryId = resolveCategory(if (isReceive) "سایر درآمدها" else "سایر هزینه‌ها", type)
        addTransaction(
            TxnEntity(
                type = type,
                amountRial = debt.amountRial,
                categoryId = categoryId,
                note = if (isReceive) "وصول طلب از ${debt.person}" else "پرداخت بدهی به ${debt.person}",
                merchant = debt.person,
                source = TxnSource.MANUAL
            )
        )
        db.debtDao().update(debt.copy(isSettled = true))
    }

    // ---------- Goals ----------

    suspend fun addGoal(goal: Goal): Long = db.goalDao().insert(goal)

    suspend fun updateGoal(goal: Goal) = db.goalDao().update(goal)

    suspend fun deleteGoal(goal: Goal) = db.goalDao().delete(goal)

    suspend fun contributeGoal(goal: Goal, amountRial: Long) {
        db.goalDao().update(goal.copy(savedRial = (goal.savedRial + amountRial).coerceAtMost(goal.targetRial)))
    }

    // ---------- Tasks ----------

    suspend fun addTask(task: TaskItem): Long {
        val id = db.taskDao().insert(task)
        if (task.remind && task.dueAtMillis != null) {
            ReminderScheduler.schedule(
                context,
                id = 20_000 + id.toInt(),
                title = "یادآور پرگار",
                message = task.title,
                atMillis = task.dueAtMillis
            )
        }
        return id
    }

    suspend fun toggleTask(task: TaskItem) {
        val updated = task.copy(isDone = !task.isDone)
        db.taskDao().update(updated)
        if (updated.isDone) {
            ReminderScheduler.cancel(context, 20_000 + task.id.toInt())
        } else if (updated.remind && updated.dueAtMillis != null) {
            ReminderScheduler.schedule(context, 20_000 + task.id.toInt(), "یادآور پرگار", updated.title, updated.dueAtMillis)
        }
    }

    suspend fun deleteTask(task: TaskItem) {
        ReminderScheduler.cancel(context, 20_000 + task.id.toInt())
        db.taskDao().delete(task)
    }

    // ---------- Splits ----------

    suspend fun addSplitGroup(group: SplitGroup, members: List<SplitMember>): Long {
        val groupId = db.splitDao().insertGroup(group)
        db.splitDao().insertMembers(members.map { it.copy(groupId = groupId) })
        return groupId
    }

    suspend fun settleSplitGroup(group: SplitGroup) {
        db.splitDao().updateGroup(group.copy(isSettled = true))
    }

    suspend fun deleteSplitGroup(group: SplitGroup) = db.splitDao().deleteGroup(group)

    // ---------- SMS ----------

    suspend fun importParsedSms(parsed: ParsedSms): ImportResult {
        if (!parsed.isUseful) return ImportResult.Skipped("پیام قابل تشخیص نبود")
        if (db.transactionDao().countByHash(parsed.hash) > 0) {
            return ImportResult.Duplicate(parsed.hash)
        }
        val account = resolveAccountForSms(parsed)
        val isRecent = abs(System.currentTimeMillis() - parsed.epochMillis) < 48L * 60 * 60 * 1000

        if (parsed.isTransaction && parsed.amountRial != null && parsed.type != null) {
            val categoryId = resolveCategory(parsed.categoryHint, parsed.type)
            val txn = TxnEntity(
                type = parsed.type,
                amountRial = parsed.amountRial,
                categoryId = categoryId,
                accountId = account?.id,
                note = parsed.rawBody.take(180),
                merchant = parsed.merchant ?: "",
                dateEpochMillis = parsed.epochMillis,
                source = TxnSource.SMS,
                smsHash = parsed.hash,
                bank = parsed.bank ?: ""
            )
            val id = db.transactionDao().insert(txn)
            if (id == -1L) return ImportResult.Duplicate(parsed.hash)
            val reportedBalance = parsed.balanceRial
            val balanceUsed = reportedBalance != null && account != null && isRecent
            if (balanceUsed && reportedBalance != null && account != null) {
                db.accountDao().setBalance(account.id, reportedBalance)
            } else {
                applyEffect(txn, 1)
            }
            return ImportResult.Imported(id, balanceUsed)
        }

        if (parsed.balanceRial != null && account != null) {
            if (!isRecent) return ImportResult.Skipped("پیام قدیمی؛ موجودی تغییر نکرد")
            db.accountDao().setBalance(account.id, parsed.balanceRial)
            return ImportResult.Imported(-1, true)
        }
        return ImportResult.Skipped("اطلاعات کافی برای ساخت تراکنش پیدا نشد")
    }

    private suspend fun resolveAccountForSms(parsed: ParsedSms): Account? {
        val all = db.accountDao().getAll()
        val last4 = parsed.cardLast4?.takeIf { it.length == 4 }
        if (last4 != null) {
            all.firstOrNull { it.cardLast4 == last4 }?.let { return it }
        }
        val bank = parsed.bank
        if (bank != null) {
            val sameBank = all.firstOrNull { it.bank == bank }
            if (sameBank != null) {
                if (last4 != null && sameBank.cardLast4.isBlank()) {
                    val updated = sameBank.copy(cardLast4 = last4)
                    db.accountDao().update(updated)
                    return updated
                }
                if (last4 == null || sameBank.cardLast4 == last4) return sameBank
            }
            val color = SeedData.accountColors[all.size % SeedData.accountColors.size]
            val id = db.accountDao().insert(
                Account(
                    name = bank,
                    bank = bank,
                    kind = AccountKind.BANK,
                    cardLast4 = last4 ?: "",
                    colorHex = color
                )
            )
            return db.accountDao().byId(id)
        }
        return all.firstOrNull { it.isDefault } ?: all.firstOrNull()
    }

    private suspend fun resolveCategory(name: String?, type: TxnType): Long? {
        val all = db.categoryDao().getAll()
        if (name != null) {
            all.firstOrNull { it.name == name && it.type == type }?.let { return it.id }
        }
        val fallback = if (type == TxnType.INCOME) "سایر درآمدها" else "سایر هزینه‌ها"
        return all.firstOrNull { it.name == fallback }?.id
            ?: all.firstOrNull { it.type == type }?.id
    }
}
