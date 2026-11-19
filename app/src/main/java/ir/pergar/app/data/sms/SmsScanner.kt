package ir.pergar.app.data.sms

import android.content.Context
import android.net.Uri

object SmsScanner {

    fun scanInbox(context: Context, limit: Int = 500): List<ParsedSms> {
        val results = mutableListOf<ParsedSms>()
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("address", "body", "date")
        try {
            context.contentResolver.query(uri, projection, null, null, "date DESC")?.use { cursor ->
                val addressIndex = cursor.getColumnIndexOrThrow("address")
                val bodyIndex = cursor.getColumnIndexOrThrow("body")
                val dateIndex = cursor.getColumnIndexOrThrow("date")
                while (cursor.moveToNext() && results.size < limit) {
                    val sender = cursor.getString(addressIndex) ?: ""
                    val body = cursor.getString(bodyIndex) ?: ""
                    if (body.isBlank()) continue
                    val date = cursor.getLong(dateIndex)
                    val parsed = SmsParser.parse(body, sender, date)
                    if (parsed.isUseful) results.add(parsed)
                }
            }
        } catch (_: SecurityException) {
            return emptyList()
        } catch (_: Exception) {
            return emptyList()
        }
        return results.sortedByDescending { it.epochMillis }
    }
}
