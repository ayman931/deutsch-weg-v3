package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class WordWithReviewStates(
    @Embedded val word: VocabularyWord,
    @Relation(
        parentColumn = "id",
        entityColumn = "wordId"
    )
    val reviewStates: List<ReviewState>
)
