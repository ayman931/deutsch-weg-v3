package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.LearningStatus
import com.example.data.model.ReviewDirection
import com.example.data.model.ReviewState
import com.example.data.model.VocabularyWord
import com.example.data.model.WordWithReviewStates
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary_words ORDER BY createdAt DESC")
    fun getAllWords(): Flow<List<VocabularyWord>>

    @Query("SELECT * FROM vocabulary_words ORDER BY id ASC")
    suspend fun getAllWordsSnapshot(): List<VocabularyWord>

    @Query("SELECT * FROM vocabulary_words WHERE topic = :topic ORDER BY createdAt DESC")
    fun getWordsByTopic(topic: String): Flow<List<VocabularyWord>>

    @Query("SELECT * FROM vocabulary_words WHERE id = :id LIMIT 1")
    suspend fun getWordById(id: Long): VocabularyWord?

    @Query("SELECT * FROM vocabulary_words WHERE LOWER(TRIM(german)) = LOWER(TRIM(:lemma))")
    suspend fun findWordsByLemma(lemma: String): List<VocabularyWord>

    @Query("SELECT DISTINCT topic FROM vocabulary_words ORDER BY topic ASC")
    fun getAllTopics(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM vocabulary_words")
    fun getWordCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWord(word: VocabularyWord): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWords(words: List<VocabularyWord>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewStates(states: List<ReviewState>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreWords(words: List<VocabularyWord>): List<Long>

    @Update
    suspend fun updateWord(word: VocabularyWord)

    @Delete
    suspend fun deleteWord(word: VocabularyWord)

    @Query("DELETE FROM vocabulary_words")
    suspend fun deleteAllWords()

    @Transaction
    @Query("SELECT * FROM vocabulary_words ORDER BY createdAt DESC")
    fun getWordsWithReviewStates(): Flow<List<WordWithReviewStates>>

    @Transaction
    @Query("SELECT * FROM vocabulary_words ORDER BY createdAt DESC")
    suspend fun getWordsWithReviewStatesSnapshot(): List<WordWithReviewStates>

    /**
     * Batch import transaction:
     * Inserts words and generates initial independent review state for GERMAN_TO_ARABIC
     * with NEW status and FSRS-6 initial baseline (difficulty=0.0, stability=0.0, lapses=0).
     * ARABIC_TO_GERMAN will be created once GERMAN_TO_ARABIC reaches REVIEW status.
     */
    @Transaction
    suspend fun batchImportWithReviewStates(
        words: List<VocabularyWord>,
        directions: List<ReviewDirection> = listOf(
            ReviewDirection.GERMAN_TO_ARABIC
        )
    ): List<Long> {
        val insertedIds = mutableListOf<Long>()
        val reviewStatesToInsert = mutableListOf<ReviewState>()
        val now = System.currentTimeMillis()

        for (word in words) {
            val wordId = insertWord(word)
            insertedIds.add(wordId)
            for (direction in directions) {
                reviewStatesToInsert.add(
                    ReviewState(
                        wordId = wordId,
                        direction = direction,
                        status = LearningStatus.NEW,
                        repetitionCount = 0,
                        lapses = 0,
                        difficulty = 0.0,
                        stability = 0.0,
                        intervalDays = 0,
                        nextReviewDate = now,
                        lastReviewedDate = null,
                        easeFactor = null
                    )
                )
            }
        }

        if (reviewStatesToInsert.isNotEmpty()) {
            insertReviewStates(reviewStatesToInsert)
        }

        return insertedIds
    }
}
