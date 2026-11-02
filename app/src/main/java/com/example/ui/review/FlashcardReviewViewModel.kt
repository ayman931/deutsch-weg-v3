package com.example.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ReviewDirection
import com.example.data.model.ReviewState
import com.example.data.model.VocabularyWord
import com.example.data.model.WordWithReviewStates
import com.example.data.repository.ReviewRating
import com.example.data.repository.VocabularyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FlashcardItem(
    val word: VocabularyWord,
    val reviewState: ReviewState
)

data class ReviewUiState(
    val direction: ReviewDirection = ReviewDirection.GERMAN_TO_ARABIC,
    val isFlipped: Boolean = false,
    val currentIndex: Int = 0,
    val completedCount: Int = 0
)

class FlashcardReviewViewModel(
    private val repository: VocabularyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState

    val dueFlashcards: StateFlow<List<FlashcardItem>> = combine(
        repository.wordsWithReviewStates,
        _uiState
    ) { allWords, state ->
        val list = mutableListOf<FlashcardItem>()
        for (item in allWords) {
            val matchingState = item.reviewStates.find { it.direction == state.direction }
            if (matchingState != null) {
                list.add(FlashcardItem(item.word, matchingState))
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setDirection(direction: ReviewDirection) {
        _uiState.update { it.copy(direction = direction, currentIndex = 0, isFlipped = false) }
    }

    fun flipCard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun answerCard(item: FlashcardItem, rating: ReviewRating) {
        viewModelScope.launch {
            repository.recordReview(item.reviewState, rating)
            _uiState.update {
                it.copy(
                    isFlipped = false,
                    completedCount = it.completedCount + 1,
                    currentIndex = it.currentIndex + 1
                )
            }
        }
    }

    fun restartSession() {
        _uiState.update { it.copy(currentIndex = 0, isFlipped = false) }
    }

    class Factory(private val repository: VocabularyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FlashcardReviewViewModel(repository) as T
        }
    }
}
