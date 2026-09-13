package org.ivanivorontsov.kodekszhizni.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.concurrent.TimeUnit

/** Duration of the edit/delete lock after creating a law (article). */
val ARTICLE_LOCK_DURATION_MS: Long = TimeUnit.DAYS.toMillis(30)

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val tags: String = "",
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSeed: Boolean = false,
    /** Epoch millis until which edit/delete are forbidden. */
    val lockedUntil: Long = 0L
)

fun Article.isLocked(now: Long = System.currentTimeMillis()): Boolean =
    lockedUntil > 0L && now < lockedUntil

/** Whole days remaining until unlock (ceil). 0 if unlocked. */
fun Article.daysLeft(now: Long = System.currentTimeMillis()): Int {
    if (!isLocked(now)) return 0
    val remaining = lockedUntil - now
    return ((remaining + TimeUnit.DAYS.toMillis(1) - 1) / TimeUnit.DAYS.toMillis(1)).toInt()
        .coerceAtLeast(1)
}

fun computeLockedUntil(createdAt: Long = System.currentTimeMillis()): Long =
    createdAt + ARTICLE_LOCK_DURATION_MS
