package com.example.data.fsrs

import com.example.data.model.LearningStatus
import com.example.data.model.ReviewLog
import com.example.data.model.ReviewState
import com.example.data.repository.ReviewRating

data class FsrsScheduleResult(
    val updatedState: ReviewState,
    val log: ReviewLog,
    val retrievability: Double
)

interface FsrsScheduler {
    val desiredRetention: Double
    val parameters: DoubleArray

    fun schedule(
        currentState: ReviewState,
        rating: ReviewRating,
        now: Long = System.currentTimeMillis()
    ): FsrsScheduleResult
}
