package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vocabulary_words",
    indices = [
        Index(value = ["german"]),
        Index(value = ["topic"]),
        Index(value = ["wordType"])
    ]
)
data class VocabularyWord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val german: String,
    val wordType: WordType,
    val article: Article? = null,
    val plural: String? = null,
    val praeteritum: String? = null,
    val perfekt: String? = null,
    val isSeparable: Boolean = false,
    val isReflexive: Boolean = false,
    val rektion: String? = null,
    val arabic: String,
    val exampleSentence: String? = null,
    val exampleArabic: String? = null,
    val topic: String = "Alltag",
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Normalized key used for duplicate identity:
     * lowercase trimmed german lemma, normalized word type, and trimmed arabic meaning.
     */
    fun normalizedIdentity(): String {
        return "${german.trim().lowercase()}|${wordType.name}|${arabic.trim().lowercase()}"
    }

    fun normalizedGermanLemma(): String {
        return german.trim().lowercase()
    }
}
