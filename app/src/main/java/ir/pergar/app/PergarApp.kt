package ir.pergar.app

import android.app.Application
import ir.pergar.app.data.db.AppDatabase
import ir.pergar.app.data.prefs.AppPrefs
import ir.pergar.app.data.repo.FinanceRepository
import ir.pergar.app.data.util.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PergarApp : Application() {

    lateinit var prefs: AppPrefs
        private set

    lateinit var repository: FinanceRepository
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        prefs = AppPrefs(this)
        repository = FinanceRepository(this, AppDatabase.get(this), prefs)
        Notifications.createChannels(this)
        appScope.launch {
            repository.ensureSeeded()
        }
    }
}
