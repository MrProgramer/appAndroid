package ir.pergar.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Account::class,
        Category::class,
        TxnEntity::class,
        Budget::class,
        Loan::class,
        Debt::class,
        Goal::class,
        TaskItem::class,
        SplitGroup::class,
        SplitMember::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun loanDao(): LoanDao
    abstract fun debtDao(): DebtDao
    abstract fun goalDao(): GoalDao
    abstract fun taskDao(): TaskDao
    abstract fun splitDao(): SplitDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pergar.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
