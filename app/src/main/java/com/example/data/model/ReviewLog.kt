package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.repository.ReviewRating

@Entity(
    tableName = "review_logs",
    foreignKeys = [
        ForeignKey(
            entity = ReviewState::class,
            parentColumns = ["id"],
            childColumns = ["reviewStateId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["reviewStateId"]),
        Index(value = ["reviewTimestamp"])
    ]
)
data class ReviewLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reviewStateId: Long,
    val rating: ReviewRating,
    val status: LearningStatus,
    val difficulty: Double,
    val stability: Double,
    val elapsedDays: Int,
    val scheduledDays: Int,
    val reviewTimestamp: Long = System.currentTimeMillis()
)
