package org.ivanivorontsov.kodekszhizni.worker

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build

/**
 * Plays the default phone ringtone in a loop until [stop] is called.
 * Used when an ACTIVE measure reaches endAt («мера окончена»).
 */
object RingtonePlayer {
    @Volatile private var ringtone: Ringtone? = null

    @Synchronized
    fun start(context: Context) {
        stop()
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: return
        val r = RingtoneManager.getRingtone(context.applicationContext, uri) ?: return
        if (Build.VERSION.SDK_INT >= 28) {
            r.isLooping = true
        }
        r.audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        ringtone = r
        try {
            r.play()
        } catch (_: Exception) {
            ringtone = null
        }
    }

    @Synchronized
    fun stop() {
        try {
            ringtone?.stop()
        } catch (_: Exception) {
        }
        ringtone = null
    }

    fun isPlaying(): Boolean = ringtone?.isPlaying == true
}
