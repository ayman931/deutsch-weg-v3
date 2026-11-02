package com.example.data.repository

import com.example.data.database.ReviewStateDao
import com.example.data.database.VocabularyDao
import com.example.data.fsrs.Fsrs6Scheduler
import com.example.data.fsrs.FsrsScheduleResult
import com.example.data.fsrs.FsrsScheduler
import com.example.data.model.ReviewDirection
import com.example.data.model.ReviewLog
import com.example.data.model.ReviewState
import com.example.data.model.VocabularyWord
import com.example.data.model.WordWithReviewStates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

enum class ReviewRating(val label: String, val grade: Int) {
    AGAIN("Nochmal", 1),
    HARD("Schwer", 2),
    GOOD("Gut", 3),
    EASY("Einfach", 4)
}

interface VocabularyRepository {
    val allWords: Flow<List<VocabularyWord>>
    val allTopics: Flow<List<String>>
    val wordCount: Flow<Int>
    val wordsWithReviewStates: Flow<List<WordWithReviewStates>>
    val fsrsScheduler: FsrsScheduler

    suspend fun getAllWordsSnapshot(): List<VocabularyWord>
    suspend fun getWordsWithReviewStatesSnapshot(): List<WordWithReviewStates>
    fun getWordsByTopic(topic: String): Flow<List<VocabularyWord>>
    suspend fun insertWord(word: VocabularyWord): Long
    suspend fun batchImport(words: List<VocabularyWord>): List<Long>
    suspend fun deleteWord(word: VocabularyWord)
    suspend fun recordReview(state: ReviewState, rating: ReviewRating): FsrsScheduleResult
    suspend fun getReviewLogs(reviewStateId: Long): List<ReviewLog>
}

class VocabularyRepositoryImpl(
    private val vocabularyDao: VocabularyDao,
    private val reviewStateDao: ReviewStateDao,
    override val fsrsScheduler: FsrsScheduler = Fsrs6Scheduler(desiredRetention = 0.90)
) : VocabularyRepository {

    override val allWords: Flow<List<VocabularyWord>> = vocabularyDao.getAllWords()
    override val allTopics: Flow<List<String>> = vocabularyDao.getAllTopics()
    override val wordCount: Flow<Int> = vocabularyDao.getWordCount()
    override val wordsWithReviewStates: Flow<List<WordWithReviewStates>> =
        vocabularyDao.getWordsWithReviewStates()

    override suspend fun getAllWordsSnapshot(): List<VocabularyWord> = withContext(Dispatchers.IO) {
        vocabularyDao.getAllWordsSnapshot()
    }

    override suspend fun getWordsWithReviewStatesSnapshot(): List<WordWithReviewStates> =
        withContext(Dispatchers.IO) {
            vocabularyDao.getWordsWithReviewStatesSnapshot()
        }

    override fun getWordsByTopic(topic: String): Flow<List<VocabularyWord>> =
        vocabularyDao.getWordsByTopic(topic)

    override suspend fun insertWord(word: VocabularyWord): Long = withContext(Dispatchers.IO) {
        vocabularyDao.insertWord(word)
    }

    override suspend fun batchImport(words: List<VocabularyWord>): List<Long> =
        withContext(Dispatchers.IO) {
            vocabularyDao.batchImportWithReviewStates(words)
        }

    override suspend fun deleteWord(word: VocabularyWord) = withContext(Dispatchers.IO) {
        vocabularyDao.deleteWord(word)
    }

    override suspend fun recordReview(state: ReviewState, rating: ReviewRating): FsrsScheduleResult =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val result = fsrsScheduler.schedule(state, rating, now)
            reviewStateDao.updateReviewState(result.updatedState)
            reviewStateDao.insertReviewLog(result.log)
            result
        }

    override suspend fun getReviewLogs(reviewStateId: Long): List<ReviewLog> =
        withContext(Dispatchers.IO) {
            reviewStateDao.getLogsForReviewState(reviewStateId)
        }
}
