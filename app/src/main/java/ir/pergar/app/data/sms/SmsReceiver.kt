package ir.pergar.app.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import ir.pergar.app.PergarApp
import ir.pergar.app.data.repo.ImportResult
import ir.pergar.app.data.util.Notifications
import ir.pergar.app.data.util.formatMoney
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val body = messages.joinToString(separator = "") { it?.messageBody ?: "" }
        val sender = messages.firstOrNull()?.originatingAddress ?: ""
        val timestamp = messages.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()
        if (body.isBlank()) return

        val parsed = SmsParser.parse(body, sender, timestamp)
        if (!parsed.isUseful) return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as? PergarApp ?: return@launch
                val settings = app.prefs.settings.first()
                if (!settings.smsAutoImport) return@launch
                val result = app.repository.importParsedSms(parsed)
                if (result is ImportResult.Imported) {
                    val amountText = parsed.amountRial?.let { formatMoney(it) } ?: "به‌روزرسانی موجودی"
                    val detail = parsed.merchant?.takeIf { it.isNotBlank() } ?: body.take(120)
                    Notifications.showSmsImport(
                        context,
                        parsed.bank ?: "بانک",
                        amountText,
                        detail
                    )
                }
            } catch (_: Exception) {
                // never crash the SMS pipeline
            } finally {
                pending.finish()
            }
        }
    }
}
