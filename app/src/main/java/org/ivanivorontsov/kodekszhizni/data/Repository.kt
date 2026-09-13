package org.ivanivorontsov.kodekszhizni.data

import kotlinx.coroutines.flow.Flow

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

    suspend fun startMeasure(measure: Measure): Long {
        require(measure.articleId > 0L) {
            "Мера должна быть привязана к закону. Сначала создайте закон."
        }
        val article = articleDao.getById(measure.articleId)
            ?: error("Выбранный закон не найден. Создайте закон и попробуйте снова.")
        // article used to validate existence
        @Suppress("UNUSED_VARIABLE")
        val _title = article.title
        return measureDao.insert(measure)
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

    suspend fun completeExpiredMeasures() {
        val now = System.currentTimeMillis()
        measureDao.expiredActive(now).forEach { m ->
            measureDao.update(
                m.copy(status = MeasureStatus.COMPLETED, completedAt = now)
            )
        }
    }
}
