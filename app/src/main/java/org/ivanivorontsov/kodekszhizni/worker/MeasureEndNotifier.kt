package org.ivanivorontsov.kodekszhizni.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.ivanivorontsov.kodekszhizni.MeasureEndedActivity
import org.ivanivorontsov.kodekszhizni.R

object MeasureEndNotifier {
    const val CHANNEL_MEASURE_END = "measure_end"
    const val NOTIFICATION_BASE = 7000

    fun ensureChannel(context: Context) {
        val mgr = context.getSystemService(NotificationManager::class.java)
        val ch = NotificationChannel(
            CHANNEL_MEASURE_END,
            context.getString(R.string.channel_measure_end),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_measure_end_desc)
            setBypassDnd(true)
            enableVibration(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        mgr.createNotificationChannel(ch)
    }

    fun notifyAndLaunch(
        context: Context,
        measureId: Long,
        measureTitle: String,
        lawTitle: String,
        violationCount: Int
    ) {
        ensureChannel(context)
        RingtonePlayer.start(context)

        val fullIntent = Intent(context, MeasureEndedActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MeasureEndedActivity.EXTRA_MEASURE_ID, measureId)
            putExtra(MeasureEndedActivity.EXTRA_MEASURE_TITLE, measureTitle)
            putExtra(MeasureEndedActivity.EXTRA_LAW_TITLE, lawTitle)
            putExtra(MeasureEndedActivity.EXTRA_VIOLATION_COUNT, violationCount)
        }

        // Try full-screen activity immediately
        try {
            context.startActivity(fullIntent)
        } catch (_: Exception) {
        }

        val pi = PendingIntent.getActivity(
            context,
            (NOTIFICATION_BASE + measureId).toInt(),
            fullIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val body = "«$measureTitle» · закон: $lawTitle · нарушений: $violationCount"
        val n = NotificationCompat.Builder(context, CHANNEL_MEASURE_END)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.measure_ended_title))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pi)
            .setFullScreenIntent(pi, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
        try {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_BASE + measureId.toInt().and(0xFFFF), n)
        } catch (_: SecurityException) {
        }
    }

    fun cancelNotification(context: Context, measureId: Long) {
        NotificationManagerCompat.from(context)
            .cancel(NOTIFICATION_BASE + measureId.toInt().and(0xFFFF))
    }
}
