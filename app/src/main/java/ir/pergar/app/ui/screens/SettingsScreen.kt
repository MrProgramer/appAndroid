package ir.pergar.app.ui.screens

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.pergar.app.SMS_PERMISSIONS
import ir.pergar.app.data.prefs.AppSettings
import ir.pergar.app.hasPermission
import ir.pergar.app.ui.AppViewModel
import ir.pergar.app.ui.components.CategoryIcon
import ir.pergar.app.ui.components.ConfirmDialog
import ir.pergar.app.ui.components.GlassCard
import ir.pergar.app.ui.components.PergarDialog
import ir.pergar.app.ui.components.ScreenFrame
import ir.pergar.app.ui.components.SectionHeader
import ir.pergar.app.ui.components.SegmentedControl
import ir.pergar.app.ui.components.WideInfoCard
import ir.pergar.app.ui.components.iconForKey
import ir.pergar.app.ui.theme.PergarThemeInfo

private const val EXCEL_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

@Composable
fun SettingsScreen(
    vm: AppViewModel,
    settings: AppSettings,
    onBack: () -> Unit,
    onOpenSms: () -> Unit
) {
    val context = LocalContext.current
    val extras = PergarThemeInfo.colors
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            vm.backup(uri) { result ->
                val message = result.fold(
                    onSuccess = { count -> "پشتیبان‌گیری انجام شد · $count رکورد" },
                    onFailure = { "خطا در پشتیبان‌گیری: ${it.message ?: "نامشخص"}" }
                )
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            vm.restore(uri) { result ->
                val message = result.fold(
                    onSuccess = { count -> "بازیابی انجام شد · $count رکورد" },
                    onFailure = { "خطا در بازیابی: ${it.message ?: "نامشخص"}" }
                )
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    val excelLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(EXCEL_MIME)
    ) { uri ->
        if (uri != null) {
            vm.exportExcel(uri) { result ->
                val message = result.fold(
                    onSuccess = { count -> "فایل اکسل ساخته شد · $count تراکنش" },
                    onFailure = { "خطا در خروجی: ${it.message ?: "نامشخص"}" }
                )
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    ScreenFrame(
        title = "تنظیمات",
        subtitle = "ظاهر، امنیت و پشتیبان‌گیری",
        onBack = onBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SectionHeader(title = "ظاهر برنامه")
                GlassCard(padding = 14.dp) {
                    Text(
                        text = "پوسته",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "حالت نمایش رنگی برنامه",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    SegmentedControl(
                        options = listOf("تیره", "روشن", "سیستم"),
                        selectedIndex = when (settings.themeMode) {
                            "light" -> 1
                            "dark" -> 0
                            else -> 2
                        },
                        onSelect = { index ->
                            vm.setTheme(
                                when (index) {
                                    0 -> "dark"
                                    1 -> "light"
                                    else -> "system"
                                }
                            )
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    SwitchRow(
                        iconKey = "trend",
                        title = "نمایش به تومان",
                        subtitle = "اگر خاموش باشد مبالغ به ریال نمایش داده می‌شود",
                        checked = settings.asToman,
                        onCheckedChange = { vm.setAsToman(it) }
                    )
                    SwitchRow(
                        iconKey = "security",
                        title = "مخفی کردن موجودی",
                        subtitle = "نمایش ستاره به‌جای مبلغ و جلوگیری از اسکرین‌شات",
                        checked = settings.hideBalance,
                        onCheckedChange = { vm.setHideBalance(it) }
                    )
                }
            }

            item {
                SectionHeader(title = "پیامک بانکی")
                GlassCard(padding = 14.dp) {
                    SwitchRow(
                        iconKey = "sms",
                        title = "ثبت خودکار از پیامک",
                        subtitle = "خواندن پیامک‌های بانکی و تشخیص تراکنش",
                        checked = settings.smsAutoImport,
                        onCheckedChange = { vm.setSmsAutoImport(it) }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenSms() }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryIcon(
                            iconKey = "sms",
                            colorHex = "#4C7DFF",
                            size = 38.dp,
                            iconSize = 18.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "مرکز پیامک‌ها",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "اسکن و بررسی پیامک‌های بانکی",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Rounded.ChevronLeft,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    val smsGranted = context.hasPermission(Manifest.permission.READ_SMS)
                    if (!smsGranted) {
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    smsPermissionLauncher.launch(SMS_PERMISSIONS)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "اجازه دسترسی به پیامک داده نشده — برای فعال‌سازی بزنید",
                                style = MaterialTheme.typography.labelSmall,
                                color = extras.gold
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "امنیت")
                GlassCard(padding = 14.dp) {
                    SwitchRow(
                        iconKey = "security",
                        title = "قفل برنامه",
                        subtitle = "ورود با اثر انگشت یا رمز دستگاه",
                        checked = settings.biometricLock,
                        onCheckedChange = { enabled ->
                            if (!enabled) {
                                vm.setBiometricLock(false)
                            } else {
                                val manager = BiometricManager.from(context)
                                val can = manager.canAuthenticate(
                                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                                )
                                if (can == BiometricManager.BIOMETRIC_SUCCESS) {
                                    vm.setBiometricLock(true)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "روی این دستگاه اثر انگشت یا قفل صفحه تنظیم نشده است",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    )
                    WideInfoCard(
                        icon = iconForKey("security"),
                        title = "حریم خصوصی",
                        message = "همه داده‌ها فقط روی گوشی شما ذخیره می‌شوند. پیامک‌ها روی دستگاه پردازش می‌شوند و هیچ اطلاعاتی به سرور ارسال نمی‌شود.",
                        accent = extras.success
                    )
                }
            }

            item {
                SectionHeader(title = "داده‌ها")
                GlassCard(padding = 14.dp) {
                    ActionRow(
                        iconKey = "download",
                        title = "پشتیبان‌گیری",
                        subtitle = "ذخیره فایل JSON از تمام اطلاعات",
                        onClick = { backupLauncher.launch("pergar-backup.json") }
                    )
                    ActionRow(
                        iconKey = "upload",
                        title = "بازیابی",
                        subtitle = "بازگردانی اطلاعات از فایل پشتیبان",
                        onClick = { showRestoreConfirm = true }
                    )
                    ActionRow(
                        iconKey = "chart",
                        title = "خروجی اکسل",
                        subtitle = "دریافت فایل XLSX از تراکنش‌ها",
                        onClick = { excelLauncher.launch("pergar-export.xlsx") }
                    )
                }
            }

            item {
                SectionHeader(title = "درباره")
                GlassCard(padding = 16.dp) {
                    Text(
                        text = "پرگار",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "مدیریت هوشمند امور مالی شخصی · نسخه ۱.۰.۰",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "ساخته‌شده توسط کوروش پرگار",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showPrivacy = true }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سیاست حریم خصوصی",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            Icons.Rounded.ChevronLeft,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showRestoreConfirm) {
        ConfirmDialog(
            title = "بازیابی اطلاعات",
            message = "داده‌های فعلی با محتوای فایل پشتیبان جایگزین می‌شوند. قبل از ادامه از اطلاعات فعلی پشتیبان بگیرید.",
            confirmLabel = "انتخاب فایل",
            onConfirm = {
                showRestoreConfirm = false
                restoreLauncher.launch(arrayOf("application/json", "*/*"))
            },
            onDismiss = { showRestoreConfirm = false }
        )
    }

    if (showPrivacy) {
        PergarDialog(title = "حریم خصوصی", onDismiss = { showPrivacy = false }) {
            Text(
                text = "پرگار هیچ حساب کاربری و سروری ندارد. تمام تراکنش‌ها، بودجه‌ها و پیامک‌های خوانده‌شده فقط در حافظه داخلی همین دستگاه ذخیره می‌شوند.\n\n" +
                    "دسترسی به پیامک تنها برای خواندن پیامک‌های بانکی و به‌صورت محلی انجام می‌شود و هیچ پیامکی ارسال یا آپلود نمی‌شود.\n\n" +
                    "با حذف برنامه، تمام اطلاعات پاک می‌شود؛ پس قبل از آن از بخش «پشتیبان‌گیری» استفاده کنید.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SwitchRow(
    iconKey: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryIcon(iconKey = iconKey, colorHex = "#4C7DFF", size = 38.dp, iconSize = 18.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun ActionRow(
    iconKey: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryIcon(iconKey = iconKey, colorHex = "#22D3EE", size = 38.dp, iconSize = 18.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Rounded.ChevronLeft,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
