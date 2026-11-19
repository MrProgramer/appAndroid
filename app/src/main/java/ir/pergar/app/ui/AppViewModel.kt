package ir.pergar.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ir.pergar.app.PergarApp
import ir.pergar.app.data.db.Account
import ir.pergar.app.data.db.AppDatabase
import ir.pergar.app.data.db.Budget
import ir.pergar.app.data.db.Debt
import ir.pergar.app.data.db.Goal
import ir.pergar.app.data.db.Loan
import ir.pergar.app.data.db.SplitGroup
import ir.pergar.app.data.db.SplitMember
import ir.pergar.app.data.db.TaskItem
import ir.pergar.app.data.db.TxnEntity
import ir.pergar.app.data.export.BackupManager
import ir.pergar.app.data.export.ExcelExporter
import ir.pergar.app.data.model.SplitGroupWithMembers
import ir.pergar.app.data.prefs.AppSettings
import ir.pergar.app.data.repo.ImportResult
import ir.pergar.app.data.sms.ParsedSms
import ir.pergar.app.data.sms.SmsScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as PergarApp
    private val prefs = app.prefs
    private val db = AppDatabase.get(application)
    val repository = app.repository

    val settings = prefs.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())
    val accounts = repository.accounts.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val transactions = repository.transactions.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val budgets = repository.budgets.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val loans = repository.loans.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val debts = repository.debts.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val goals = repository.goals.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val tasks = repository.tasks.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val splitGroups = repository.splitGroups.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<SplitGroupWithMembers>())

    // ---------- Settings ----------

    fun setTheme(mode: String) = viewModelScope.launch { prefs.setTheme(mode) }
    fun setAsToman(value: Boolean) = viewModelScope.launch { prefs.setAsToman(value) }
    fun setSmsAutoImport(value: Boolean) = viewModelScope.launch { prefs.setSmsAutoImport(value) }
    fun setBiometricLock(value: Boolean) = viewModelScope.launch { prefs.setBiometricLock(value) }
    fun setOnboardingDone(value: Boolean) = viewModelScope.launch { prefs.setOnboardingDone(value) }
    fun setHideBalance(value: Boolean) = viewModelScope.launch { prefs.setHideBalance(value) }

    // ---------- Transactions ----------

    fun addTransaction(txn: TxnEntity, onDone: (Long) -> Unit = {}) = viewModelScope.launch {
        val id = repository.addTransaction(txn)
        onDone(id)
    }

    fun updateTransaction(old: TxnEntity, new: TxnEntity, onDone: () -> Unit = {}) = viewModelScope.launch {
        repository.updateTransaction(old, new)
        onDone()
    }

    fun deleteTransaction(txn: TxnEntity) = viewModelScope.launch {
        repository.deleteTransaction(txn)
    }

    // ---------- Accounts ----------

    fun saveAccount(account: Account, isNew: Boolean, onDone: () -> Unit = {}) = viewModelScope.launch {
        if (isNew) repository.addAccount(account) else repository.updateAccount(account)
        onDone()
    }

    fun deleteAccount(account: Account) = viewModelScope.launch {
        repository.deleteAccount(account)
    }

    fun transfer(from: Account, to: Account, amountRial: Long) = viewModelScope.launch {
        repository.transferBetween(from, to, amountRial)
    }

    // ---------- Budgets ----------

    fun saveBudget(categoryId: Long?, limitRial: Long, monthKey: String, onDone: () -> Unit = {}) =
        viewModelScope.launch {
            repository.upsertBudget(categoryId, limitRial, monthKey)
            onDone()
        }

    fun deleteBudget(budget: Budget) = viewModelScope.launch { repository.deleteBudget(budget) }

    // ---------- Loans ----------

    fun saveLoan(loan: Loan, isNew: Boolean, onDone: () -> Unit = {}) = viewModelScope.launch {
        if (isNew) repository.addLoan(loan) else repository.updateLoan(loan)
        onDone()
    }

    fun deleteLoan(loan: Loan) = viewModelScope.launch { repository.deleteLoan(loan) }

    fun payInstallment(loan: Loan, onDone: () -> Unit = {}) = viewModelScope.launch {
        repository.payInstallment(loan)
        onDone()
    }

    // ---------- Debts ----------

    fun saveDebt(debt: Debt, isNew: Boolean, onDone: () -> Unit = {}) = viewModelScope.launch {
        if (isNew) repository.addDebt(debt) else repository.updateDebt(debt)
        onDone()
    }

    fun deleteDebt(debt: Debt) = viewModelScope.launch { repository.deleteDebt(debt) }

    fun settleDebt(debt: Debt, onDone: () -> Unit = {}) = viewModelScope.launch {
        repository.settleDebt(debt)
        onDone()
    }

    // ---------- Goals ----------

    fun saveGoal(goal: Goal, isNew: Boolean, onDone: () -> Unit = {}) = viewModelScope.launch {
        if (isNew) repository.addGoal(goal) else repository.updateGoal(goal)
        onDone()
    }

    fun deleteGoal(goal: Goal) = viewModelScope.launch { repository.deleteGoal(goal) }

    fun contributeGoal(goal: Goal, amountRial: Long, onDone: () -> Unit = {}) = viewModelScope.launch {
        repository.contributeGoal(goal, amountRial)
        onDone()
    }

    // ---------- Tasks ----------

    fun addTask(task: TaskItem, onDone: () -> Unit = {}) = viewModelScope.launch {
        repository.addTask(task)
        onDone()
    }

    fun toggleTask(task: TaskItem) = viewModelScope.launch { repository.toggleTask(task) }

    fun deleteTask(task: TaskItem) = viewModelScope.launch { repository.deleteTask(task) }

    // ---------- Splits ----------

    fun addSplitGroup(group: SplitGroup, members: List<SplitMember>, onDone: () -> Unit = {}) =
        viewModelScope.launch {
            repository.addSplitGroup(group, members)
            onDone()
        }

    fun settleSplitGroup(group: SplitGroup) = viewModelScope.launch { repository.settleSplitGroup(group) }

    fun deleteSplitGroup(group: SplitGroup) = viewModelScope.launch { repository.deleteSplitGroup(group) }

    // ---------- SMS ----------

    suspend fun scanSms(limit: Int = 300): List<ParsedSms> = withContext(Dispatchers.IO) {
        SmsScanner.scanInbox(app, limit)
    }

    fun importSms(
        items: List<ParsedSms>,
        onDone: (imported: Int, duplicates: Int, skipped: Int) -> Unit
    ) = viewModelScope.launch {
        var imported = 0
        var duplicates = 0
        var skipped = 0
        items.forEach { parsed ->
            when (repository.importParsedSms(parsed)) {
                is ImportResult.Imported -> imported++
                is ImportResult.Duplicate -> duplicates++
                is ImportResult.Skipped -> skipped++
            }
        }
        onDone(imported, duplicates, skipped)
    }

    // ---------- Export / Backup ----------

    fun exportExcel(uri: Uri, onResult: (Result<Int>) -> Unit) = viewModelScope.launch {
        val result = runCatching {
            withContext(Dispatchers.IO) {
                ExcelExporter.export(app, uri, transactions.value, categories.value, accounts.value)
            }
        }
        onResult(result)
    }

    fun backup(uri: Uri, onResult: (Result<Int>) -> Unit) = viewModelScope.launch {
        val result = runCatching {
            withContext(Dispatchers.IO) { BackupManager.export(app, uri, db) }
        }
        onResult(result)
    }

    fun restore(uri: Uri, onResult: (Result<Int>) -> Unit) = viewModelScope.launch {
        val result = runCatching {
            withContext(Dispatchers.IO) { BackupManager.restore(app, uri, db) }
        }
        onResult(result)
    }
}
