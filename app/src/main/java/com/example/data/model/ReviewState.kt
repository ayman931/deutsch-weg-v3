package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "review_states",
    foreignKeys = [
        ForeignKey(
            entity = VocabularyWord::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["wordId", "direction"], unique = true),
        Index(value = ["nextReviewDate"]),
        Index(value = ["status"])
    ]
)
data class ReviewState(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wordId: Long,
    val direction: ReviewDirection,
    val status: LearningStatus = LearningStatus.NEW,
    val repetitionCount: Int = 0,
    val lapses: Int = 0,
    val difficulty: Double = 0.0,
    val stability: Double = 0.0,
    val intervalDays: Int = 0,
    val nextReviewDate: Long = System.currentTimeMillis(),
    val lastReviewedDate: Long? = null,
    val easeFactor: Float? = null // Legacy SM-2 parameter preserved for backward-compatible migration
)
