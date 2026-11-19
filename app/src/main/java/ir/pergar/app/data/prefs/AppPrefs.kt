package ir.pergar.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "pergar_prefs")

data class AppSettings(
    val themeMode: String = "dark",
    val asToman: Boolean = true,
    val smsAutoImport: Boolean = true,
    val biometricLock: Boolean = false,
    val onboardingDone: Boolean = false,
    val hideBalance: Boolean = false
)

class AppPrefs(private val context: Context) {

    private object Keys {
        val theme = stringPreferencesKey("theme_mode")
        val asToman = booleanPreferencesKey("as_toman")
        val smsAuto = booleanPreferencesKey("sms_auto_import")
        val biometric = booleanPreferencesKey("biometric_lock")
        val onboarding = booleanPreferencesKey("onboarding_done")
        val hideBalance = booleanPreferencesKey("hide_balance")
    }

    val settings: Flow<AppSettings> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw e }
        .map { p ->
            AppSettings(
                themeMode = p[Keys.theme] ?: "dark",
                asToman = p[Keys.asToman] ?: true,
                smsAutoImport = p[Keys.smsAuto] ?: true,
                biometricLock = p[Keys.biometric] ?: false,
                onboardingDone = p[Keys.onboarding] ?: false,
                hideBalance = p[Keys.hideBalance] ?: false
            )
        }

    suspend fun setTheme(mode: String) = context.dataStore.edit { it[Keys.theme] = mode }
    suspend fun setAsToman(value: Boolean) = context.dataStore.edit { it[Keys.asToman] = value }
    suspend fun setSmsAutoImport(value: Boolean) = context.dataStore.edit { it[Keys.smsAuto] = value }
    suspend fun setBiometricLock(value: Boolean) = context.dataStore.edit { it[Keys.biometric] = value }
    suspend fun setOnboardingDone(value: Boolean) = context.dataStore.edit { it[Keys.onboarding] = value }
    suspend fun setHideBalance(value: Boolean) = context.dataStore.edit { it[Keys.hideBalance] = value }
}
