package ir.pergar.app.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ir.pergar.app.data.model.AccountKind
import ir.pergar.app.data.model.DebtDirection
import ir.pergar.app.data.model.TxnSource
import ir.pergar.app.data.model.TxnType

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val bank: String = "",
    val kind: AccountKind = AccountKind.BANK,
    val cardLast4: String = "",
    val colorHex: String = "#4C7DFF",
    val balanceRial: Long = 0,
    val isDefault: Boolean = false
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconKey: String,
    val colorHex: String,
    val type: TxnType,
    val isDefault: Boolean = true
)

@Entity(
    tableName = "transactions",
    indices = [Index("smsHash", unique = true), Index("dateEpochMillis")]
)
data class TxnEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TxnType,
    val amountRial: Long,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val toAccountId: Long? = null,
    val note: String = "",
    val merchant: String = "",
    val dateEpochMillis: Long = System.currentTimeMillis(),
    val source: TxnSource = TxnSource.MANUAL,
    val smsHash: String? = null,
    val bank: String = ""
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long? = null,
    val limitRial: Long,
    val monthKey: String
)

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val bank: String = "",
    val principalRial: Long,
    val installmentRial: Long,
    val installmentCount: Int,
    val paidCount: Int = 0,
    val firstDueEpochMillis: Long,
    val accountId: Long? = null,
    val reminderEnabled: Boolean = true
)

@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val person: String,
    val direction: DebtDirection,
    val amountRial: Long,
    val dueEpochMillis: Long? = null,
    val note: String = "",
    val isSettled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetRial: Long,
    val savedRial: Long = 0,
    val targetEpochMillis: Long? = null,
    val iconKey: String = "flag",
    val colorHex: String = "#22C55E",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String = "",
    val dueAtMillis: Long? = null,
    val remind: Boolean = false,
    val isDone: Boolean = false,
    val priority: Int = 0
)

@Entity(tableName = "split_groups")
data class SplitGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val totalRial: Long,
    val payerName: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val isSettled: Boolean = false,
    val note: String = ""
)

@Entity(
    tableName = "split_members",
    foreignKeys = [
        ForeignKey(
            entity = SplitGroup::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId")]
)
data class SplitMember(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val name: String,
    val shareRial: Long
)
