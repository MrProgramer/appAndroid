package ir.pergar.app.data.export

import android.content.Context
import android.net.Uri
import ir.pergar.app.data.db.Account
import ir.pergar.app.data.db.AppDatabase
import ir.pergar.app.data.db.Budget
import ir.pergar.app.data.db.Category
import ir.pergar.app.data.db.Debt
import ir.pergar.app.data.db.Goal
import ir.pergar.app.data.db.Loan
import ir.pergar.app.data.db.SplitGroup
import ir.pergar.app.data.db.SplitMember
import ir.pergar.app.data.db.TaskItem
import ir.pergar.app.data.db.TxnEntity
import ir.pergar.app.data.model.AccountKind
import ir.pergar.app.data.model.DebtDirection
import ir.pergar.app.data.model.TxnSource
import ir.pergar.app.data.model.TxnType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object BackupManager {

    suspend fun export(context: Context, uri: Uri, db: AppDatabase): Int = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "Pergar")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        root.put("accounts", JSONArray().also { array ->
            db.accountDao().getAll().forEach { e -> array.put(accountJson(e)) }
        })
        root.put("categories", JSONArray().also { array ->
            db.categoryDao().getAll().forEach { e -> array.put(categoryJson(e)) }
        })
        val txns = db.transactionDao().getAll()
        root.put("transactions", JSONArray().also { array ->
            txns.forEach { e -> array.put(txnJson(e)) }
        })
        root.put("budgets", JSONArray().also { array ->
            db.budgetDao().getAll().forEach { e -> array.put(budgetJson(e)) }
        })
        root.put("loans", JSONArray().also { array ->
            db.loanDao().getAll().forEach { e -> array.put(loanJson(e)) }
        })
        root.put("debts", JSONArray().also { array ->
            db.debtDao().getAll().forEach { e -> array.put(debtJson(e)) }
        })
        root.put("goals", JSONArray().also { array ->
            db.goalDao().getAll().forEach { e -> array.put(goalJson(e)) }
        })
        root.put("tasks", JSONArray().also { array ->
            db.taskDao().getAll().forEach { e -> array.put(taskJson(e)) }
        })
        root.put("splitGroups", JSONArray().also { array ->
            db.splitDao().getAllGroups().forEach { e -> array.put(splitGroupJson(e)) }
        })
        root.put("splitMembers", JSONArray().also { array ->
            db.splitDao().getAllMembers().forEach { e -> array.put(splitMemberJson(e)) }
        })

        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(root.toString(2).toByteArray(Charsets.UTF_8))
        } ?: throw IOException("امکان نوشتن فایل پشتیبان وجود ندارد")
        txns.size
    }

    suspend fun restore(context: Context, uri: Uri, db: AppDatabase): Int = withContext(Dispatchers.IO) {
        val text = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes().toString(Charsets.UTF_8)
        } ?: throw IOException("امکان خواندن فایل پشتیبان وجود ندارد")

        val root = JSONObject(text)
        if (root.optString("app") != "Pergar") {
            throw IllegalArgumentException("این فایل، پشتیبان پرگار نیست")
        }

        db.clearAllTables()

        var count = 0
        db.accountDao().let { dao ->
            root.optJSONArray("accounts")?.let { array ->
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    dao.insert(
                        Account(
                            id = o.optLong("id"),
                            name = o.optString("name"),
                            bank = o.optString("bank"),
                            kind = runCatching { AccountKind.valueOf(o.optString("kind")) }.getOrDefault(AccountKind.BANK),
                            cardLast4 = o.optString("cardLast4"),
                            colorHex = o.optString("colorHex", "#4C7DFF"),
                            balanceRial = o.optLong("balanceRial"),
                            isDefault = o.optBoolean("isDefault")
                        )
                    )
                    count++
                }
            }
        }
        db.categoryDao().let { dao ->
            root.optJSONArray("categories")?.let { array ->
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    dao.insert(
                        Category(
                            id = o.optLong("id"),
                            name = o.optString("name"),
                            iconKey = o.optString("iconKey"),
                            colorHex = o.optString("colorHex"),
                            type = runCatching { TxnType.valueOf(o.optString("type")) }.getOrDefault(TxnType.EXPENSE),
                            isDefault = o.optBoolean("isDefault", true)
                        )
                    )
                    count++
                }
            }
        }
        db.transactionDao().let { dao ->
            root.optJSONArray("transactions")?.let { array ->
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    dao.insert(
                        TxnEntity(
                            id = o.optLong("id"),
                            type = runCatching { TxnType.valueOf(o.optString("type")) }.getOrDefault(TxnType.EXPENSE),
                            amountRial = o.optLong("amountRial"),
                            categoryId = o.longOrNull("categoryId"),
                            accountId = o.longOrNull("accountId"),
                            toAccountId = o.longOrNull("toAccountId"),
                            note = o.optString("note"),
                            merchant = o.optString("merchant"),
                            dateEpochMillis = o.optLong("dateEpochMillis"),
                            source = runCatching { TxnSource.valueOf(o.optString("source")) }.getOrDefault(TxnSource.MANUAL),
                            smsHash = o.stringOrNull("smsHash"),
                            bank = o.optString("bank")
                        )
                    )
                    count++
                }
            }
        }
        db.budgetDao().let { dao ->
            root.optJSONArray("budgets")?.let { array ->
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    dao.insert(Budget(id = o.optLong("id"), categoryId = o.longOrNull("categoryId"), limitRial = o.optLong("limitRial"), monthKey = o.optString("monthKey")))
                    count++
                }
            }
        }
        db.loanDao().let { dao ->
            root.optJSONArray("loans")?.let { array ->
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    dao.insert(
                        Loan(
                            id = o.optLong("id"),
                            title = o.optString("title"),
                            bank = o.optString("bank"),
                            principalRial = o.optLong("principalRial"),
                            installmentRial = o.optLong("installmentRial"),
                            installmentCount = o.optInt("installmentCount"),
                            paidCount = o.optInt("paidCount"),
                            firstDueEpochMillis = o.optLong("firstDueEpochMillis"),
                            accountId = o.longOrNull("accountId"),
                            reminderEnabled = o.optBoolean("reminderEnabled", true)
                        )
                    )
                    count++
                }
            }
        }
        db.debtDao().let { dao ->
            root.optJSONArray("debts")?.let { array ->
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    dao.insert(
                        Debt(
                            id = o.optLong("id"),
                            person = o.optString("person"),
                            direction = runCatching { DebtDirection.valueOf(o.optString("direction")) }.getOrDefault(DebtDirection.LENT),
                            amountRial = o.optLong("amountRial"),
                            dueEpochMillis = o.longOrNull("dueEpochMillis"),
                            note = o.optString("note"),
                            isSettled = o.optBoolean("isSettled"),
                            createdAt = o.optLong("createdAt")
                        )
                    )
                    count++
                }
            }
        }
        db.goalDao().let { dao ->
            root.optJSONArray("goals")?.let { array ->
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    dao.insert(
                        Goal(
                            id = o.optLong("id"),
                            title = o.optString("title"),
                            targetRial = o.optLong("targetRial"),
                            savedRial = o.optLong("savedRial"),
                            targetEpochMillis = o.longOrNull("targetEpochMillis"),
                            iconKey = o.optString("iconKey", "flag"),
                            colorHex = o.optString("colorHex", "#22C55E"),
                            createdAt = o.optLong("createdAt")
                        )
                    )
                    count++
                }
            }
        }
        db.taskDao().let { dao ->
            root.optJSONArray("tasks")?.let { array ->
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    dao.insert(
                        TaskItem(
                            id = o.optLong("id"),
                            title = o.optString("title"),
                            note = o.optString("note"),
                            dueAtMillis = o.longOrNull("dueAtMillis"),
                            remind = o.optBoolean("remind"),
                            isDone = o.optBoolean("isDone"),
                            priority = o.optInt("priority")
                        )
                    )
                    count++
                }
            }
        }
        db.splitDao().let { dao ->
            root.optJSONArray("splitGroups")?.let { array ->
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    dao.insertGroup(
                        SplitGroup(
                            id = o.optLong("id"),
                            title = o.optString("title"),
                            totalRial = o.optLong("totalRial"),
                            payerName = o.optString("payerName"),
                            dateMillis = o.optLong("dateMillis"),
                            isSettled = o.optBoolean("isSettled"),
                            note = o.optString("note")
                        )
                    )
                    count++
                }
            }
            root.optJSONArray("splitMembers")?.let { array ->
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    dao.insertMembers(
                        listOf(
                            SplitMember(
                                id = o.optLong("id"),
                                groupId = o.optLong("groupId"),
                                name = o.optString("name"),
                                shareRial = o.optLong("shareRial")
                            )
                        )
                    )
                    count++
                }
            }
        }
        count
    }

    private fun JSONObject.longOrNull(key: String): Long? =
        if (isNull(key)) null else optLong(key)

    private fun JSONObject.stringOrNull(key: String): String? =
        if (isNull(key)) null else optString(key)

    private fun accountJson(e: Account) = JSONObject().apply {
        put("id", e.id); put("name", e.name); put("bank", e.bank); put("kind", e.kind.name)
        put("cardLast4", e.cardLast4); put("colorHex", e.colorHex); put("balanceRial", e.balanceRial)
        put("isDefault", e.isDefault)
    }

    private fun categoryJson(e: Category) = JSONObject().apply {
        put("id", e.id); put("name", e.name); put("iconKey", e.iconKey)
        put("colorHex", e.colorHex); put("type", e.type.name); put("isDefault", e.isDefault)
    }

    private fun txnJson(e: TxnEntity) = JSONObject().apply {
        put("id", e.id); put("type", e.type.name); put("amountRial", e.amountRial)
        put("categoryId", e.categoryId ?: JSONObject.NULL)
        put("accountId", e.accountId ?: JSONObject.NULL)
        put("toAccountId", e.toAccountId ?: JSONObject.NULL)
        put("note", e.note); put("merchant", e.merchant); put("dateEpochMillis", e.dateEpochMillis)
        put("source", e.source.name); put("smsHash", e.smsHash ?: JSONObject.NULL); put("bank", e.bank)
    }

    private fun budgetJson(e: Budget) = JSONObject().apply {
        put("id", e.id); put("categoryId", e.categoryId ?: JSONObject.NULL)
        put("limitRial", e.limitRial); put("monthKey", e.monthKey)
    }

    private fun loanJson(e: Loan) = JSONObject().apply {
        put("id", e.id); put("title", e.title); put("bank", e.bank); put("principalRial", e.principalRial)
        put("installmentRial", e.installmentRial); put("installmentCount", e.installmentCount)
        put("paidCount", e.paidCount); put("firstDueEpochMillis", e.firstDueEpochMillis)
        put("accountId", e.accountId ?: JSONObject.NULL); put("reminderEnabled", e.reminderEnabled)
    }

    private fun debtJson(e: Debt) = JSONObject().apply {
        put("id", e.id); put("person", e.person); put("direction", e.direction.name)
        put("amountRial", e.amountRial); put("dueEpochMillis", e.dueEpochMillis ?: JSONObject.NULL)
        put("note", e.note); put("isSettled", e.isSettled); put("createdAt", e.createdAt)
    }

    private fun goalJson(e: Goal) = JSONObject().apply {
        put("id", e.id); put("title", e.title); put("targetRial", e.targetRial); put("savedRial", e.savedRial)
        put("targetEpochMillis", e.targetEpochMillis ?: JSONObject.NULL)
        put("iconKey", e.iconKey); put("colorHex", e.colorHex); put("createdAt", e.createdAt)
    }

    private fun taskJson(e: TaskItem) = JSONObject().apply {
        put("id", e.id); put("title", e.title); put("note", e.note)
        put("dueAtMillis", e.dueAtMillis ?: JSONObject.NULL); put("remind", e.remind)
        put("isDone", e.isDone); put("priority", e.priority)
    }

    private fun splitGroupJson(e: SplitGroup) = JSONObject().apply {
        put("id", e.id); put("title", e.title); put("totalRial", e.totalRial)
        put("payerName", e.payerName); put("dateMillis", e.dateMillis)
        put("isSettled", e.isSettled); put("note", e.note)
    }

    private fun splitMemberJson(e: SplitMember) = JSONObject().apply {
        put("id", e.id); put("groupId", e.groupId); put("name", e.name); put("shareRial", e.shareRial)
    }
}
