package com.example.ui.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.VocabularyWord
import com.example.data.model.WordType
import com.example.data.model.WordWithReviewStates
import com.example.data.repository.VocabularyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VocabularyFilterState(
    val query: String = "",
    val selectedTopic: String? = null,
    val selectedWordType: WordType? = null
)

class VocabularyViewModel(
    private val repository: VocabularyRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(VocabularyFilterState())
    val filterState: StateFlow<VocabularyFilterState> = _filterState

    val allTopics: StateFlow<List<String>> = repository.allTopics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wordsWithReviews: StateFlow<List<WordWithReviewStates>> = combine(
        repository.wordsWithReviewStates,
        _filterState
    ) { wordsWithStates, filters ->
        wordsWithStates.filter { item ->
            val matchesQuery = if (filters.query.isBlank()) true else {
                val q = filters.query.trim().lowercase()
                item.word.german.lowercase().contains(q) ||
                        item.word.arabic.contains(q) ||
                        (item.word.plural?.lowercase()?.contains(q) == true) ||
                        item.word.topic.lowercase().contains(q)
            }
            val matchesTopic = filters.selectedTopic == null || item.word.topic == filters.selectedTopic
            val matchesType = filters.selectedWordType == null || item.word.wordType == filters.selectedWordType
            matchesQuery && matchesTopic && matchesType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(q: String) {
        _filterState.update { it.copy(query = q) }
    }

    fun onTopicSelected(topic: String?) {
        _filterState.update { it.copy(selectedTopic = if (it.selectedTopic == topic) null else topic) }
    }

    fun onWordTypeSelected(type: WordType?) {
        _filterState.update { it.copy(selectedWordType = if (it.selectedWordType == type) null else type) }
    }

    fun deleteWord(word: VocabularyWord) {
        viewModelScope.launch {
            repository.deleteWord(word)
        }
    }

    fun updateWord(word: VocabularyWord) {
        viewModelScope.launch {
            repository.updateWord(word)
        }
    }

    class Factory(private val repository: VocabularyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VocabularyViewModel(repository) as T
        }
    }
}
