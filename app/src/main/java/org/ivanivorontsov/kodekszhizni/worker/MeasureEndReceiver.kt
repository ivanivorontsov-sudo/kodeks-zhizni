package org.ivanivorontsov.kodekszhizni.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ivanivorontsov.kodekszhizni.KodeksApp
import org.ivanivorontsov.kodekszhizni.data.MeasureStatus
import java.util.concurrent.ConcurrentHashMap

/**
 * AlarmManager callback when a measure's endAt is reached.
 */
class MeasureEndReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_MEASURE_END) return
        val measureId = intent.getLongExtra(EXTRA_MEASURE_ID, -1L)
        if (measureId < 0L) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MeasureEndCompleter.completeAndRing(context.applicationContext, measureId)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_MEASURE_END = "org.ivanivorontsov.kodekszhizni.ACTION_MEASURE_END"
        const val EXTRA_MEASURE_ID = "measure_id"
    }
}

/**
 * Shared completion + ringtone/notification path for AlarmManager and WorkManager.
 */
object MeasureEndCompleter {
    /** Deduplicate AlarmManager + WorkManager double-fire within a short window. */
    private val recentlyRung = ConcurrentHashMap<Long, Long>()

    private fun claimRing(measureId: Long): Boolean {
        val now = System.currentTimeMillis()
        recentlyRung.entries.removeIf { now - it.value > 60_000L }
        val prev = recentlyRung.putIfAbsent(measureId, now)
        return prev == null
    }

    suspend fun completeAndRing(context: Context, measureId: Long) {
        val app = context.applicationContext as? KodeksApp
        val repo = app?.repository ?: return
        val m = repo.getMeasure(measureId) ?: return
        if (m.status != MeasureStatus.ACTIVE) return
        val endAt = m.endAt
        if (endAt != null && endAt > System.currentTimeMillis() + 2_000L) {
            // Not yet due (spurious early fire)
            return
        }
        MeasureEndScheduler.cancel(context, measureId)
        repo.stopMeasure(measureId, completed = true)
        val done = repo.getMeasure(measureId) ?: return
        if (!claimRing(measureId)) return
        val articleTitle = repo.getArticle(done.articleId)?.title ?: "Закон удалён"
        MeasureEndNotifier.notifyAndLaunch(
            context = context,
            measureId = done.id,
            measureTitle = done.title,
            lawTitle = articleTitle,
            violationCount = done.violationCount
        )
    }

    /** Complete any overdue measures and ring for the most recent one. */
    suspend fun sweepExpiredAndRing(context: Context) {
        val app = context.applicationContext as? KodeksApp ?: return
        val completed = app.repository.completeExpiredMeasures()
        if (completed.isEmpty()) return
        val done = completed.maxByOrNull { it.completedAt ?: 0L } ?: return
        MeasureEndScheduler.cancel(context, done.id)
        if (!claimRing(done.id)) return
        val articleTitle = app.repository.getArticle(done.articleId)?.title ?: "Закон удалён"
        MeasureEndNotifier.notifyAndLaunch(
            context = context,
            measureId = done.id,
            measureTitle = done.title,
            lawTitle = articleTitle,
            violationCount = done.violationCount
        )
    }
}
