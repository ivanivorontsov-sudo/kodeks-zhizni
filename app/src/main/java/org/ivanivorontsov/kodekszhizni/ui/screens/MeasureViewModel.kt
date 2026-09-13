package org.ivanivorontsov.kodekszhizni.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
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
import org.ivanivorontsov.kodekszhizni.worker.MeasureEndCompleter
import org.ivanivorontsov.kodekszhizni.worker.MeasureEndScheduler
import java.util.concurrent.TimeUnit

class MeasureViewModel(
    app: Application,
    private val repo: Repository
) : AndroidViewModel(app) {
    val measures = repo.measures()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val articles = repo.articles("")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _startError = MutableStateFlow<String?>(null)
    val startError: StateFlow<String?> = _startError

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast

    init {
        viewModelScope.launch {
            MeasureEndCompleter.sweepExpiredAndRing(getApplication())
            MeasureEndScheduler.rescheduleAll(
                getApplication(),
                repo.activeMeasuresWithEnd()
            )
        }
    }

    fun clearStartError() {
        _startError.value = null
    }

    fun clearToast() {
        _toast.value = null
    }

    fun articleTitle(articleId: Long, articles: List<Article>): String =
        articles.find { it.id == articleId }?.title ?: "Закон удалён"

    /**
     * Saves measure as ARMED (pending). Does not start the timer.
     * [durationHours] / optional [endAtAbsolute] used only to derive planned durationHours.
     */
    fun arm(
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
                val plannedHours = when {
                    durationHours > 0 -> durationHours
                    endAtAbsolute != null && endAtAbsolute > now -> {
                        val ms = endAtAbsolute - now
                        TimeUnit.MILLISECONDS.toHours(ms).coerceAtLeast(1L)
                    }
                    else -> 0L
                }
                val preset = MeasurePresets.find(typeId)
                repo.addArmedMeasure(
                    Measure(
                        type = typeId,
                        title = title.ifBlank {
                            preset?.title ?: MeasurePresets.label(typeId)
                        },
                        description = description.ifBlank { preset?.description.orEmpty() },
                        articleId = articleId,
                        durationHours = plannedHours,
                        status = MeasureStatus.ARMED,
                        violationCount = 0
                    )
                )
                _startError.value = null
            } catch (e: Exception) {
                _startError.value = e.message ?: "Не удалось создать меру"
            }
        }
    }

    fun recordViolation(id: Long) {
        viewModelScope.launch {
            val before = repo.getMeasure(id) ?: return@launch
            val wasActive = before.status == MeasureStatus.ACTIVE
            val updated = repo.recordViolation(id) ?: return@launch
            if (updated.status == MeasureStatus.ACTIVE && updated.endAt != null) {
                MeasureEndScheduler.schedule(getApplication(), updated)
            }
            if (wasActive) {
                _toast.value = "Нарушение записано (мера уже идёт)"
            }
        }
    }

    fun stop(id: Long, completed: Boolean) {
        viewModelScope.launch {
            MeasureEndScheduler.cancel(getApplication(), id)
            repo.stopMeasure(id, completed)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            MeasureEndScheduler.cancel(getApplication(), id)
            repo.deleteMeasure(id)
        }
    }

    companion object {
        fun factory(repo: Repository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return MeasureViewModel(app, repo) as T
            }
        }
    }
}
