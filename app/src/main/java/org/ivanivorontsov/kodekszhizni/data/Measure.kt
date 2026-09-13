package org.ivanivorontsov.kodekszhizni.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MeasureStatus {
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
    /** Duration in hours; 0 means until endAt only */
    val durationHours: Long = 0,
    val startedAt: Long = System.currentTimeMillis(),
    /** Absolute end timestamp; null if open-ended until stopped */
    val endAt: Long? = null,
    val status: MeasureStatus = MeasureStatus.ACTIVE,
    val completedAt: Long? = null
)
