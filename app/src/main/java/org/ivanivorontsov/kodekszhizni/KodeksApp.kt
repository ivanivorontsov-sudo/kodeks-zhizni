package org.ivanivorontsov.kodekszhizni

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.ivanivorontsov.kodekszhizni.data.AppDatabase
import org.ivanivorontsov.kodekszhizni.data.Repository
import org.ivanivorontsov.kodekszhizni.data.SettingsStore
import org.ivanivorontsov.kodekszhizni.worker.MeasureEndCompleter
import org.ivanivorontsov.kodekszhizni.worker.MeasureEndNotifier
import org.ivanivorontsov.kodekszhizni.worker.MeasureEndScheduler
import org.ivanivorontsov.kodekszhizni.worker.NotificationHelper
import org.ivanivorontsov.kodekszhizni.worker.NotificationScheduler

class KodeksApp : Application() {
    lateinit var repository: Repository
        private set
    lateinit var settingsStore: SettingsStore
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)
        repository = Repository(db.articleDao(), db.measureDao())
        settingsStore = SettingsStore(this)
        NotificationHelper.ensureChannels(this)
        MeasureEndNotifier.ensureChannel(this)
        scope.launch {
            // Complete overdue + ring if any expired while app was dead
            MeasureEndCompleter.sweepExpiredAndRing(this@KodeksApp)
            MeasureEndScheduler.rescheduleAll(
                this@KodeksApp,
                repository.activeMeasuresWithEnd()
            )
            val settings = settingsStore.settings.first()
            NotificationScheduler.reschedule(this@KodeksApp, settings)
        }
    }
}
