package com.example.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.LearningStatus
import com.example.data.repository.VocabularyRepository
import com.example.data.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DayActivity(
    val dayName: String,
    val count: Int,
    val heightRatio: Float
)

data class ProgressUiState(
    val totalWords: Int = 0,
    val newCount: Int = 0,
    val learningCount: Int = 0,
    val reviewCount: Int = 0,
    val masteredCount: Int = 0,
    val weeklyActivities: List<DayActivity> = emptyList(),
    val dailyAverage: Int = 0
)

class ProgressViewModel(
    private val repository: VocabularyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    val totalWordsFlow = repository.wordCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    init {
        loadProgressData()
    }

    fun loadProgressData() = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val allWords = repository.getAllWordsSnapshot()
        val allStates = repository.getAllReviewStatesSnapshot()
        val allLogs = repository.getAllReviewLogsSnapshot()

        var newC = 0
        var learningC = 0
        var reviewC = 0
        var masteredC = 0

        for (state in allStates) {
            when {
                state.status == LearningStatus.NEW -> newC++
                state.status == LearningStatus.LEARNING || state.status == LearningStatus.RELEARNING -> learningC++
                (state.status == LearningStatus.REVIEW || state.status == LearningStatus.MASTERED) && state.intervalDays >= 21 -> masteredC++
                else -> reviewC++
            }
        }

        // Calculate 7-day activities: Sat to Fri or last 7 days
        val dayNames = listOf("السبت", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")
        val activities = mutableListOf<DayActivity>()
        val oneDayMs = DateTimeUtils.DAY_IN_MS

        val startOfToday = DateTimeUtils.getStartOfLocalDay(now)
        var totalRecentReviews = 0

        for (i in 6 downTo 0) {
            val dayStart = startOfToday - (i * oneDayMs)
            val dayEnd = dayStart + oneDayMs - 1
            val count = allLogs.count { it.reviewTimestamp in dayStart..dayEnd }
            totalRecentReviews += count
            val dayIndex = ((dayStart / oneDayMs) % 7).toInt().let { if (it < 0) it + 7 else it }
            val name = dayNames[dayIndex % 7]
            activities.add(DayActivity(name, count, (count.toFloat() / 50f).coerceIn(0.15f, 1f)))
        }

        val dailyAvg = if (activities.isNotEmpty()) totalRecentReviews / 7 else 0

        _uiState.value = ProgressUiState(
            totalWords = allWords.size,
            newCount = newC,
            learningCount = learningC,
            reviewCount = reviewC,
            masteredCount = masteredC,
            weeklyActivities = activities,
            dailyAverage = dailyAvg
        )
    }

    class Factory(private val repository: VocabularyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProgressViewModel(repository) as T
        }
    }
}
