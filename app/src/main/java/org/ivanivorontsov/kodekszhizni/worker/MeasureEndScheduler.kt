package org.ivanivorontsov.kodekszhizni.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import org.ivanivorontsov.kodekszhizni.data.Measure
import org.ivanivorontsov.kodekszhizni.data.MeasureStatus
import java.util.concurrent.TimeUnit

/**
 * Schedules exact end-of-measure alarms (AlarmManager) plus a WorkManager backup.
 */
object MeasureEndScheduler {
    private const val WORK_PREFIX = "measure_end_"

    fun schedule(context: Context, measure: Measure) {
        val endAt = measure.endAt ?: return
        if (measure.status != MeasureStatus.ACTIVE) return
        val delay = endAt - System.currentTimeMillis()
        if (delay <= 0L) {
            // Already due — fire completion path immediately via WorkManager
            enqueueWork(context, measure.id, 0L)
            return
        }
        scheduleAlarm(context, measure.id, endAt)
        enqueueWork(context, measure.id, delay)
    }

    fun cancel(context: Context, measureId: Long) {
        cancelAlarm(context, measureId)
        WorkManager.getInstance(context).cancelUniqueWork(WORK_PREFIX + measureId)
    }

    fun rescheduleAll(context: Context, activeWithEnd: List<Measure>) {
        activeWithEnd.forEach { schedule(context, it) }
    }

    private fun scheduleAlarm(context: Context, measureId: Long, endAt: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(context, measureId)
        try {
            if (Build.VERSION.SDK_INT >= 31) {
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAt, pi)
                } else {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAt, pi)
                }
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAt, pi)
            }
        } catch (_: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAt, pi)
        }
    }

    private fun cancelAlarm(context: Context, measureId: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, measureId))
    }

    private fun pendingIntent(context: Context, measureId: Long): PendingIntent {
        val intent = Intent(context, MeasureEndReceiver::class.java).apply {
            action = MeasureEndReceiver.ACTION_MEASURE_END
            putExtra(MeasureEndReceiver.EXTRA_MEASURE_ID, measureId)
        }
        return PendingIntent.getBroadcast(
            context,
            measureId.toInt().and(0x7FFFFFFF),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun enqueueWork(context: Context, measureId: Long, delayMs: Long) {
        val req = OneTimeWorkRequestBuilder<MeasureEndWorker>()
            .setInitialDelay(delayMs.coerceAtLeast(0L), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(MeasureEndWorker.KEY_MEASURE_ID to measureId))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_PREFIX + measureId,
            ExistingWorkPolicy.REPLACE,
            req
        )
    }
}
