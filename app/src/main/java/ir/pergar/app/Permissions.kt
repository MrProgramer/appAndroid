package ir.pergar.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

val SMS_PERMISSIONS = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
