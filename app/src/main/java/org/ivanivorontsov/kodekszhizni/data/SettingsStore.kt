package org.ivanivorontsov.kodekszhizni.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

private val Context.dataStore by preferencesDataStore("settings")

/** Same spirit as article lock: one edit every 30 days. */
val SCARY_CODEX_EDIT_LOCK_MS: Long = TimeUnit.DAYS.toMillis(30)

data class NotificationSettings(
    val enabled: Boolean = true,
    val morningHour: Int = 8,
    val morningMinute: Int = 0,
    val middayHour: Int = 13,
    val middayMinute: Int = 0,
    val eveningHour: Int = 20,
    val eveningMinute: Int = 0,
    val monthlyReviewEnabled: Boolean = true
)

data class ScaryCodexState(
    /** Custom body; blank means use built-in ScaryCodexText default. */
    val body: String = "",
    /** Epoch millis of last successful edit; 0 = never edited → editable. */
    val lastEditedAt: Long = 0L
)

/** Editable when never edited or 30 days have passed since last edit. */
fun canEditScaryCodex(lastEditedAt: Long, now: Long = System.currentTimeMillis()): Boolean =
    lastEditedAt == 0L || now >= lastEditedAt + SCARY_CODEX_EDIT_LOCK_MS

/** Whole days remaining until unlock (ceil). 0 if editable. */
fun daysUntilScaryEditUnlock(lastEditedAt: Long, now: Long = System.currentTimeMillis()): Int {
    if (canEditScaryCodex(lastEditedAt, now)) return 0
    val remaining = lastEditedAt + SCARY_CODEX_EDIT_LOCK_MS - now
    return ((remaining + TimeUnit.DAYS.toMillis(1) - 1) / TimeUnit.DAYS.toMillis(1)).toInt()
        .coerceAtLeast(1)
}

fun scaryCodexUnlockAt(lastEditedAt: Long): Long =
    if (lastEditedAt == 0L) 0L else lastEditedAt + SCARY_CODEX_EDIT_LOCK_MS

class SettingsStore(private val context: Context) {
    private val enabled = booleanPreferencesKey("notif_enabled")
    private val mH = intPreferencesKey("morning_h")
    private val mM = intPreferencesKey("morning_m")
    private val dH = intPreferencesKey("midday_h")
    private val dM = intPreferencesKey("midday_m")
    private val eH = intPreferencesKey("evening_h")
    private val eM = intPreferencesKey("evening_m")
    private val monthly = booleanPreferencesKey("monthly_review")
    private val scaryBody = stringPreferencesKey("scary_codex_body")
    private val scaryLastEdited = longPreferencesKey("scary_codex_last_edited_at")

    val settings: Flow<NotificationSettings> = context.dataStore.data.map { p ->
        NotificationSettings(
            enabled = p[enabled] ?: true,
            morningHour = p[mH] ?: 8,
            morningMinute = p[mM] ?: 0,
            middayHour = p[dH] ?: 13,
            middayMinute = p[dM] ?: 0,
            eveningHour = p[eH] ?: 20,
            eveningMinute = p[eM] ?: 0,
            monthlyReviewEnabled = p[monthly] ?: true
        )
    }

    val scaryCodex: Flow<ScaryCodexState> = context.dataStore.data.map { p ->
        ScaryCodexState(
            body = p[scaryBody] ?: "",
            lastEditedAt = p[scaryLastEdited] ?: 0L
        )
    }

    suspend fun update(transform: (NotificationSettings) -> NotificationSettings) {
        context.dataStore.edit { p ->
            val cur = NotificationSettings(
                enabled = p[enabled] ?: true,
                morningHour = p[mH] ?: 8,
                morningMinute = p[mM] ?: 0,
                middayHour = p[dH] ?: 13,
                middayMinute = p[dM] ?: 0,
                eveningHour = p[eH] ?: 20,
                eveningMinute = p[eM] ?: 0,
                monthlyReviewEnabled = p[monthly] ?: true
            )
            val n = transform(cur)
            p[enabled] = n.enabled
            p[mH] = n.morningHour
            p[mM] = n.morningMinute
            p[dH] = n.middayHour
            p[dM] = n.middayMinute
            p[eH] = n.eveningHour
            p[eM] = n.eveningMinute
            p[monthly] = n.monthlyReviewEnabled
        }
    }

    /**
     * Persists custom scary-codex text and stamps the monthly lock.
     * @return false if currently locked (no write).
     */
    suspend fun saveScaryCodexBody(body: String, now: Long = System.currentTimeMillis()): Boolean {
        var saved = false
        context.dataStore.edit { p ->
            val last = p[scaryLastEdited] ?: 0L
            if (!canEditScaryCodex(last, now)) return@edit
            p[scaryBody] = body
            p[scaryLastEdited] = now
            saved = true
        }
        return saved
    }

    /**
     * Resets to built-in default (empty stored body) and consumes the monthly edit lock.
     * @return false if currently locked (no write).
     */
    suspend fun resetScaryCodexToDefault(now: Long = System.currentTimeMillis()): Boolean {
        return saveScaryCodexBody("", now)
    }
}
