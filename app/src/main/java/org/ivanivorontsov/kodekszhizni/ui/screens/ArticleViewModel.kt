package org.ivanivorontsov.kodekszhizni.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ivanivorontsov.kodekszhizni.data.Article
import org.ivanivorontsov.kodekszhizni.data.Repository

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleViewModel(private val repo: Repository) : ViewModel() {
    private val query = MutableStateFlow("")
    val searchQuery: StateFlow<String> = query

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError

    val articles = query
        .flatMapLatest { repo.articles(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(q: String) {
        query.value = q
    }

    fun clearSaveError() {
        _saveError.value = null
    }

    suspend fun load(id: Long): Article? =
        if (id == 0L) null else repo.getArticle(id)

    fun save(article: Article, onDone: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                val id = repo.saveArticle(article)
                _saveError.value = null
                onDone(id)
            } catch (e: Exception) {
                _saveError.value = e.message ?: "Не удалось сохранить"
            }
        }
    }

    fun delete(id: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                repo.deleteArticle(id)
                _saveError.value = null
                onDone()
            } catch (e: Exception) {
                _saveError.value = e.message ?: "Не удалось удалить"
            }
        }
    }

    companion object {
        fun factory(repo: Repository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ArticleViewModel(repo) as T
        }
    }
}
