package org.ivanivorontsov.kodekszhizni.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MeasureStatus {
    /** Saved and bound to a law; waiting for «Нарушение» to start the timer. */
    ARMED,
    ACTIVE,
    COMPLETED,
    STOPPED
}

@Entity(tableName = "measures")
data class Measure(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Preset id string (see [MeasurePresets]); safer than enum for Room evolution. */
    val type: String,
    val title: String,
    val description: String = "",
    /** FK to articles.id — every measure must be bound to a law. */
    val articleId: Long,
    /** Planned punishment length in hours; used when violation starts the timer. 0 = open-ended. */
    val durationHours: Long = 0,
    /** When ACTIVE: timer start. When ARMED: creation time for sorting (not a running timer). */
    val startedAt: Long = System.currentTimeMillis(),
    /** Absolute end timestamp while ACTIVE; null if open-ended or not yet started. */
    val endAt: Long? = null,
    val status: MeasureStatus = MeasureStatus.ARMED,
    val completedAt: Long? = null,
    /** How many times «Нарушение» was recorded for this measure row. */
    val violationCount: Int = 0
)
