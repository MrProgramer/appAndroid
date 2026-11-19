package ir.pergar.app

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import ir.pergar.app.data.prefs.AppSettings
import ir.pergar.app.ui.PergarRoot
import ir.pergar.app.ui.components.GhostButton
import ir.pergar.app.ui.components.PergarBackground
import ir.pergar.app.ui.components.PergarButton
import ir.pergar.app.ui.components.PergarLogo
import ir.pergar.app.ui.theme.PergarTheme

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as PergarApp

        setContent {
            val settings by app.prefs.settings.collectAsState(initial = AppSettings())
            var unlocked by rememberSaveable { mutableStateOf(false) }

            LaunchedEffect(settings.hideBalance) {
                if (settings.hideBalance) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            PergarTheme(themeMode = settings.themeMode) {
                if (settings.biometricLock && !unlocked) {
                    LockScreen(onUnlock = { unlocked = true })
                } else {
                    PergarRoot(settings = settings)
                }
            }
        }
    }
}

private fun Context.findFragmentActivity(): FragmentActivity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is FragmentActivity) return current
        current = current.baseContext
    }
    return null
}

@Composable
private fun LockScreen(onUnlock: () -> Unit) {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    var attempt by remember { mutableIntStateOf(1) }
    var message by remember { mutableStateOf<String?>(null) }
    var unavailable by remember { mutableStateOf(false) }
    val primary = MaterialTheme.colorScheme.primary

    LaunchedEffect(attempt) {
        message = null
        if (activity == null) {
            unavailable = true
            message = "قفل بیومتریک در این محیط در دسترس نیست."
            return@LaunchedEffect
        }
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val can = BiometricManager.from(context).canAuthenticate(authenticators)
        if (can != BiometricManager.BIOMETRIC_SUCCESS) {
            unavailable = true
            message = "برای استفاده از قفل، اثر انگشت یا قفل صفحه دستگاه را فعال کنید."
            return@LaunchedEffect
        }
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onUnlock()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    message = if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_CANCELED
                    ) {
                        "برای ورود، هویت خود را تأیید کنید."
                    } else {
                        errString.toString()
                    }
                }

                override fun onAuthenticationFailed() {
                    message = "تأیید نشد؛ دوباره تلاش کنید."
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("ورود به پرگار")
            .setSubtitle("هویت خود را برای باز کردن قفل تأیید کنید")
            .setAllowedAuthenticators(authenticators)
            .build()
        prompt.authenticate(info)
    }

    PergarBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PergarLogo(size = 96.dp)
            Spacer(Modifier.height(22.dp))
            Text(
                text = "پرگار قفل است",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message ?: "برای ادامه، هویت خود را تأیید کنید.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(26.dp))
            PergarButton(
                text = "تأیید هویت",
                onClick = { attempt++ },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            if (unavailable) {
                Spacer(Modifier.height(12.dp))
                GhostButton(text = "ورود موقت بدون قفل", onClick = onUnlock)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "امنیت اطلاعات پرگار با بیومتریک محافظت می‌شود",
                style = MaterialTheme.typography.labelSmall,
                color = primary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
