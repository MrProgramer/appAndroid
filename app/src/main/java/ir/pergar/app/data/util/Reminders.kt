package ir.pergar.app.data.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import ir.pergar.app.PergarApp
import ir.pergar.app.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ReminderScheduler {

    fun schedule(context: Context, id: Int, title: String, message: String, atMillis: Long) {
        if (atMillis <= System.currentTimeMillis()) return
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("id", id)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()
            if (canExact) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pending)
            } else {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pending)
            }
        } catch (_: SecurityException) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pending)
        }
    }

    fun cancel(context: Context, id: Int) {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        manager.cancel(pending)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "یادآور پرگار"
        val message = intent.getStringExtra("message") ?: ""
        val id = intent.getIntExtra("id", (System.currentTimeMillis() % 90_000).toInt() + 1_000)
        Notifications.showReminder(context, id, title, message)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.get(context)
                val now = System.currentTimeMillis()
                db.taskDao().getAll()
                    .filter { it.remind && !it.isDone }
                    .forEach { task ->
                        val due = task.dueAtMillis
                        if (due != null && due > now) {
                            ReminderScheduler.schedule(
                                context,
                                20_000 + task.id.toInt(),
                                "یادآور پرگار",
                                task.title,
                                due
                            )
                        }
                    }
                db.loanDao().getAll().forEach { loan ->
                    if (!loan.reminderEnabled || loan.paidCount >= loan.installmentCount) return@forEach
                    val base = JalaliDate.from(loan.firstDueEpochMillis)
                    val due = base.addMonths(loan.paidCount).toEpochMillis()
                    if (due > now) {
                        ReminderScheduler.schedule(
                            context,
                            10_000 + loan.id.toInt(),
                            "قسط ${loan.title}",
                            "سررسید قسط ${loan.paidCount + 1} از ${loan.installmentCount}",
                            due
                        )
                    }
                }
            } catch (_: Exception) {
                // ignore rescheduling failures after reboot
            } finally {
                pending.finish()
            }
        }
    }
}
