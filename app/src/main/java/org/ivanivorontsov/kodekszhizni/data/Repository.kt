package org.ivanivorontsov.kodekszhizni.data

import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class Repository(
    private val articleDao: ArticleDao,
    private val measureDao: MeasureDao
) {
    fun articles(query: String): Flow<List<Article>> =
        if (query.isBlank()) articleDao.observeAll() else articleDao.search(query.trim())

    suspend fun getArticle(id: Long) = articleDao.getById(id)

    suspend fun articleCount(): Int = articleDao.count()

    /**
     * Inserts a new law with a 30-day lock, or updates an unlocked law.
     * @throws IllegalStateException if trying to update/delete a locked article.
     */
    suspend fun saveArticle(article: Article): Long {
        return if (article.id == 0L) {
            val now = System.currentTimeMillis()
            val created = if (article.createdAt > 0L) article.createdAt else now
            articleDao.insert(
                article.copy(
                    createdAt = created,
                    updatedAt = now,
                    isSeed = false,
                    lockedUntil = if (article.lockedUntil > 0L) {
                        article.lockedUntil
                    } else {
                        computeLockedUntil(created)
                    }
                )
            )
        } else {
            val existing = articleDao.getById(article.id)
                ?: error("Статья не найдена")
            if (existing.isLocked()) {
                error("Закон заблокирован до окончания срока (30 дней). Правка запрещена.")
            }
            articleDao.update(
                article.copy(
                    updatedAt = System.currentTimeMillis(),
                    lockedUntil = existing.lockedUntil,
                    createdAt = existing.createdAt,
                    isSeed = existing.isSeed
                )
            )
            article.id
        }
    }

    suspend fun deleteArticle(id: Long) {
        val existing = articleDao.getById(id) ?: return
        if (existing.isLocked()) {
            error("Закон заблокирован до окончания срока (30 дней). Удаление запрещено.")
        }
        articleDao.deleteById(id)
    }

    suspend fun randomArticle() = articleDao.randomOne()

    fun measures(): Flow<List<Measure>> = measureDao.observeAll()

    suspend fun getMeasure(id: Long) = measureDao.getById(id)

    suspend fun activeMeasuresWithEnd(): List<Measure> = measureDao.activeWithEnd()

    /**
     * Saves a measure as ARMED/pending — bound to a law, not yet running.
     * [durationHours] is the planned length only; startedAt/endAt timer fields are not set as running.
     */
    suspend fun addArmedMeasure(measure: Measure): Long {
        require(measure.articleId > 0L) {
            "Мера должна быть привязана к закону. Сначала создайте закон."
        }
        articleDao.getById(measure.articleId)
            ?: error("Выбранный закон не найден. Создайте закон и попробуйте снова.")
        val now = System.currentTimeMillis()
        return measureDao.insert(
            measure.copy(
                status = MeasureStatus.ARMED,
                startedAt = now, // creation/sort time only; not a running timer
                endAt = null,
                completedAt = null,
                violationCount = measure.violationCount.coerceAtLeast(0)
            )
        )
    }

    /**
     * Records a violation and starts (or restarts after COMPLETED/STOPPED) the measure timer.
     * If already ACTIVE: only increments [Measure.violationCount], does not reset the timer.
     * @return updated measure, or null if not found
     */
    suspend fun recordViolation(id: Long): Measure? {
        val m = measureDao.getById(id) ?: return null
        val now = System.currentTimeMillis()
        val newCount = m.violationCount + 1
        val updated = when (m.status) {
            MeasureStatus.ACTIVE -> {
                // Count only; do not restart timer
                m.copy(violationCount = newCount)
            }
            MeasureStatus.ARMED, MeasureStatus.COMPLETED, MeasureStatus.STOPPED -> {
                val end = if (m.durationHours > 0) {
                    now + TimeUnit.HOURS.toMillis(m.durationHours)
                } else {
                    null // open-ended until manual stop
                }
                m.copy(
                    violationCount = newCount,
                    status = MeasureStatus.ACTIVE,
                    startedAt = now,
                    endAt = end,
                    completedAt = null
                )
            }
        }
        measureDao.update(updated)
        return updated
    }

    suspend fun stopMeasure(id: Long, completed: Boolean) {
        val m = measureDao.getById(id) ?: return
        measureDao.update(
            m.copy(
                status = if (completed) MeasureStatus.COMPLETED else MeasureStatus.STOPPED,
                completedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteMeasure(id: Long) = measureDao.deleteById(id)

    /**
     * Marks expired ACTIVE measures as COMPLETED.
     * @return list of measures that just completed (for ringtone / notification hooks)
     */
    suspend fun completeExpiredMeasures(): List<Measure> {
        val now = System.currentTimeMillis()
        val expired = measureDao.expiredActive(now)
        return expired.map { m ->
            val done = m.copy(status = MeasureStatus.COMPLETED, completedAt = now)
            measureDao.update(done)
            done
        }
    }
}
