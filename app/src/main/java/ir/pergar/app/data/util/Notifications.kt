package ir.pergar.app.data.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ir.pergar.app.MainActivity
import ir.pergar.app.R

object Notifications {

    const val CHANNEL_ALERTS = "pergar_alerts"
    const val CHANNEL_REMINDERS = "pergar_reminders"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val alerts = NotificationChannel(
            CHANNEL_ALERTS,
            "ثبت خودکار تراکنش",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "وقتی تراکنشی از پیامک بانکی ثبت می‌شود" }
        val reminders = NotificationChannel(
            CHANNEL_REMINDERS,
            "یادآورها",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "یادآور کارها و سررسید اقساط" }
        manager.createNotificationChannel(alerts)
        manager.createNotificationChannel(reminders)
    }

    private fun canNotify(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun contentIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showSmsImport(context: Context, bank: String, amountText: String, detail: String) {
        if (!canNotify(context)) return
        val builder = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$bank • $amountText")
            .setContentText(detail.ifBlank { "تراکنش از پیامک بانکی ثبت شد" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(detail))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent(context))
        runCatching {
            NotificationManagerCompat.from(context).notify(
                (System.currentTimeMillis() % 100_000).toInt(),
                builder.build()
            )
        }
    }

    fun showReminder(context: Context, id: Int, title: String, message: String) {
        if (!canNotify(context)) return
        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent(context))
        runCatching {
            NotificationManagerCompat.from(context).notify(id, builder.build())
        }
    }
}
