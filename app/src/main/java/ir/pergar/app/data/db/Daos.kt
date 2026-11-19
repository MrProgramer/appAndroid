package ir.pergar.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY isDefault DESC, id ASC")
    fun observeAll(): Flow<List<Account>>

    @Query("SELECT * FROM accounts ORDER BY isDefault DESC, id ASC")
    suspend fun getAll(): List<Account>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun byId(id: Long): Account?

    @Insert
    suspend fun insert(account: Account): Long

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("UPDATE accounts SET balanceRial = balanceRial + :delta WHERE id = :id")
    suspend fun addBalance(id: Long, delta: Long)

    @Query("UPDATE accounts SET balanceRial = :balance WHERE id = :id")
    suspend fun setBalance(id: Long, balance: Long)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY type ASC, id ASC")
    fun observeAll(): Flow<List<Category>>

    @Query("SELECT * FROM categories ORDER BY type ASC, id ASC")
    suspend fun getAll(): List<Category>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Insert
    suspend fun insert(category: Category): Long

    @Insert
    suspend fun insertAll(categories: List<Category>)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateEpochMillis DESC")
    fun observeAll(): Flow<List<TxnEntity>>

    @Query("SELECT * FROM transactions ORDER BY dateEpochMillis DESC")
    suspend fun getAll(): List<TxnEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun byId(id: Long): TxnEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: TxnEntity): Long

    @Update
    suspend fun update(entity: TxnEntity)

    @Delete
    suspend fun delete(entity: TxnEntity)

    @Query("SELECT COUNT(*) FROM transactions WHERE smsHash = :hash")
    suspend fun countByHash(hash: String): Int
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY id ASC")
    fun observeAll(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets")
    suspend fun getAll(): List<Budget>

    @Insert
    suspend fun insert(budget: Budget): Long

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)
}

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY firstDueEpochMillis ASC")
    fun observeAll(): Flow<List<Loan>>

    @Query("SELECT * FROM loans")
    suspend fun getAll(): List<Loan>

    @Insert
    suspend fun insert(loan: Loan): Long

    @Update
    suspend fun update(loan: Loan)

    @Delete
    suspend fun delete(loan: Loan)
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY isSettled ASC, createdAt DESC")
    fun observeAll(): Flow<List<Debt>>

    @Query("SELECT * FROM debts")
    suspend fun getAll(): List<Debt>

    @Insert
    suspend fun insert(debt: Debt): Long

    @Update
    suspend fun update(debt: Debt)

    @Delete
    suspend fun delete(debt: Debt)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Goal>>

    @Query("SELECT * FROM goals")
    suspend fun getAll(): List<Goal>

    @Insert
    suspend fun insert(goal: Goal): Long

    @Update
    suspend fun update(goal: Goal)

    @Delete
    suspend fun delete(goal: Goal)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isDone ASC, dueAtMillis IS NULL, dueAtMillis ASC")
    fun observeAll(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks")
    suspend fun getAll(): List<TaskItem>

    @Insert
    suspend fun insert(task: TaskItem): Long

    @Update
    suspend fun update(task: TaskItem)

    @Delete
    suspend fun delete(task: TaskItem)
}

@Dao
interface SplitDao {
    @Query("SELECT * FROM split_groups ORDER BY dateMillis DESC")
    fun observeGroups(): Flow<List<SplitGroup>>

    @Query("SELECT * FROM split_members")
    fun observeMembers(): Flow<List<SplitMember>>

    @Query("SELECT * FROM split_groups")
    suspend fun getAllGroups(): List<SplitGroup>

    @Query("SELECT * FROM split_members")
    suspend fun getAllMembers(): List<SplitMember>

    @Insert
    suspend fun insertGroup(group: SplitGroup): Long

    @Insert
    suspend fun insertMembers(members: List<SplitMember>)

    @Update
    suspend fun updateGroup(group: SplitGroup)

    @Delete
    suspend fun deleteGroup(group: SplitGroup)
}
