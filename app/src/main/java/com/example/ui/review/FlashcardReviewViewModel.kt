package com.example.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.LearningStatus
import com.example.data.model.ReviewDirection
import com.example.data.model.ReviewState
import com.example.data.model.VocabularyWord
import com.example.data.repository.ReviewRating
import com.example.data.repository.SettingsRepository
import com.example.data.repository.VocabularyRepository
import com.example.data.util.DateTimeUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FlashcardItem(
    val word: VocabularyWord,
    val reviewState: ReviewState,
    val isInSessionRelearning: Boolean = false
)

data class ReviewUiState(
    val direction: ReviewDirection = ReviewDirection.GERMAN_TO_ARABIC,
    val isFlipped: Boolean = false,
    val currentIndex: Int = 0,
    val completedCount: Int = 0,
    val isSubmitting: Boolean = false
)

class FlashcardReviewViewModel(
    private val repository: VocabularyRepository,
    private val settingsRepository: SettingsRepository? = null,
    val maxNewCardsPerDay: Int = settingsRepository?.getMaxNewCardsPerDay() ?: 20,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private val _dueFlashcards = MutableStateFlow<List<FlashcardItem>>(emptyList())
    val dueFlashcards: StateFlow<List<FlashcardItem>> = _dueFlashcards.asStateFlow()

    private var isSavingReview = false

    init {
        buildSessionQueue(_uiState.value.direction)
    }

    fun buildSessionQueue(direction: ReviewDirection) = viewModelScope.launch(dispatcher) {
        val now = System.currentTimeMillis()
        val endOfToday = DateTimeUtils.getEndOfLocalDay(now)
        val startOfToday = DateTimeUtils.getStartOfLocalDay(now)

        val allWords = repository.getWordsWithReviewStatesSnapshot()
        val dueReviewCards = mutableListOf<FlashcardItem>()
        val newCards = mutableListOf<FlashcardItem>()

        for (item in allWords) {
            val state = item.reviewStates.find { it.direction == direction } ?: continue
            if (state.status == LearningStatus.NEW) {
                newCards.add(FlashcardItem(item.word, state))
            } else if (state.nextReviewDate <= endOfToday) {
                dueReviewCards.add(FlashcardItem(item.word, state))
            }
        }

        // Order due review cards by nextReviewDate
        dueReviewCards.sortBy { it.reviewState.nextReviewDate }

        // Count review_logs rows with status == NEW and reviewTimestamp >= start of today (local time), per direction
        val reviewedNewToday = repository.countNewCardsReviewedSince(direction, startOfToday)
        val dailyLimit = settingsRepository?.getMaxNewCardsPerDay() ?: maxNewCardsPerDay
        val remainingNewQuota = maxOf(0, dailyLimit - reviewedNewToday)

        val frozenQueue = dueReviewCards + newCards.take(remainingNewQuota)

        _dueFlashcards.value = frozenQueue
        _uiState.update {
            it.copy(
                direction = direction,
                currentIndex = 0,
                completedCount = 0,
                isFlipped = false,
                isSubmitting = false
            )
        }
    }

    fun setDirection(direction: ReviewDirection) {
        buildSessionQueue(direction)
    }

    fun flipCard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun answerCard(item: FlashcardItem, rating: ReviewRating) = viewModelScope.launch(dispatcher) {
        if (isSavingReview) return@launch
        isSavingReview = true
        _uiState.update { it.copy(isSubmitting = true) }
        try {
            // 1. Only persist to FSRS if this is not an in-session relearning drill
            if (!item.isInSessionRelearning) {
                repository.recordReview(item.reviewState, rating)
            }

            // 2. In-session relearning: when answered AGAIN, re-queue card after about 5-8 cards
            if (rating == ReviewRating.AGAIN) {
                val currentQueue = _dueFlashcards.value.toMutableList()
                val curIndex = _uiState.value.currentIndex
                val offset = 6 // about 5-8 other cards
                val targetIndex = minOf(currentQueue.size, curIndex + 1 + offset)
                currentQueue.add(targetIndex, item.copy(isInSessionRelearning = true))
                _dueFlashcards.value = currentQueue
            }

            // 3. Advance to next card
            _uiState.update {
                it.copy(
                    isFlipped = false,
                    completedCount = it.completedCount + 1,
                    currentIndex = it.currentIndex + 1,
                    isSubmitting = false
                )
            }
        } finally {
            isSavingReview = false
            _uiState.update { it.copy(isSubmitting = false) }
        }
    }

    fun restartSession() {
        buildSessionQueue(_uiState.value.direction)
    }

    class Factory(
        private val repository: VocabularyRepository,
        private val settingsRepository: SettingsRepository? = null,
        private val maxNewCardsPerDay: Int = settingsRepository?.getMaxNewCardsPerDay() ?: 20
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FlashcardReviewViewModel(
                repository = repository,
                settingsRepository = settingsRepository,
                maxNewCardsPerDay = maxNewCardsPerDay
            ) as T
        }
    }
}
