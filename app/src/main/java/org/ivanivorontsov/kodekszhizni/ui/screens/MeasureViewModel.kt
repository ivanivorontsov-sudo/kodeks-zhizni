package org.ivanivorontsov.kodekszhizni.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ivanivorontsov.kodekszhizni.data.Article
import org.ivanivorontsov.kodekszhizni.data.Measure
import org.ivanivorontsov.kodekszhizni.data.MeasurePresets
import org.ivanivorontsov.kodekszhizni.data.MeasureStatus
import org.ivanivorontsov.kodekszhizni.data.Repository
import java.util.concurrent.TimeUnit

class MeasureViewModel(private val repo: Repository) : ViewModel() {
    val measures = repo.measures()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val articles = repo.articles("")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _startError = MutableStateFlow<String?>(null)
    val startError: StateFlow<String?> = _startError

    init {
        viewModelScope.launch { repo.completeExpiredMeasures() }
    }

    fun clearStartError() {
        _startError.value = null
    }

    fun articleTitle(articleId: Long, articles: List<Article>): String =
        articles.find { it.id == articleId }?.title ?: "Закон удалён"

    fun start(
        typeId: String,
        title: String,
        description: String,
        durationHours: Long,
        endAtAbsolute: Long?,
        articleId: Long
    ) {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val end = when {
                    endAtAbsolute != null -> endAtAbsolute
                    durationHours > 0 -> now + TimeUnit.HOURS.toMillis(durationHours)
                    else -> null
                }
                val preset = MeasurePresets.find(typeId)
                repo.startMeasure(
                    Measure(
                        type = typeId,
                        title = title.ifBlank {
                            preset?.title ?: MeasurePresets.label(typeId)
                        },
                        description = description.ifBlank { preset?.description.orEmpty() },
                        articleId = articleId,
                        durationHours = durationHours,
                        startedAt = now,
                        endAt = end,
                        status = MeasureStatus.ACTIVE
                    )
                )
                _startError.value = null
            } catch (e: Exception) {
                _startError.value = e.message ?: "Не удалось создать меру"
            }
        }
    }

    fun stop(id: Long, completed: Boolean) {
        viewModelScope.launch { repo.stopMeasure(id, completed) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repo.deleteMeasure(id) }
    }

    companion object {
        fun factory(repo: Repository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MeasureViewModel(repo) as T
        }
    }
}
