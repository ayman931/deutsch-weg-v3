package com.example.data.importer

import com.example.data.database.ReviewStateDao
import com.example.data.database.VocabularyDao
import com.example.data.model.Article
import com.example.data.model.LearningStatus
import com.example.data.model.ReviewDirection
import com.example.data.model.ReviewLog
import com.example.data.model.ReviewState
import com.example.data.model.VocabularyWord
import com.example.data.model.WordType
import com.example.data.repository.ReviewRating
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class BackupManager(
    private val vocabularyDao: VocabularyDao,
    private val reviewStateDao: ReviewStateDao
) {
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val words = vocabularyDao.getAllWordsSnapshot()
        val states = reviewStateDao.getAllReviewStatesSnapshot()
        val logs = reviewStateDao.getAllReviewLogsSnapshot()

        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val wordsArray = JSONArray()
        for (w in words) {
            val obj = JSONObject()
            obj.put("id", w.id)
            obj.put("german", w.german)
            obj.put("arabic", w.arabic)
            obj.put("article", w.article?.value ?: "")
            obj.put("wordType", w.wordType.name)
            obj.put("plural", w.plural ?: "")
            obj.put("praeteritum", w.praeteritum ?: "")
            obj.put("perfekt", w.perfekt ?: "")
            obj.put("isSeparable", w.isSeparable)
            obj.put("isReflexive", w.isReflexive)
            obj.put("rektion", w.rektion ?: "")
            obj.put("exampleSentence", w.exampleSentence ?: "")
            obj.put("exampleArabic", w.exampleArabic ?: "")
            obj.put("topic", w.topic)
            obj.put("createdAt", w.createdAt)
            wordsArray.put(obj)
        }
        root.put("words", wordsArray)

        val statesArray = JSONArray()
        for (s in states) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("wordId", s.wordId)
            obj.put("direction", s.direction.name)
            obj.put("status", s.status.name)
            obj.put("repetitionCount", s.repetitionCount)
            obj.put("lapses", s.lapses)
            obj.put("difficulty", s.difficulty)
            obj.put("stability", s.stability)
            obj.put("intervalDays", s.intervalDays)
            obj.put("nextReviewDate", s.nextReviewDate)
            if (s.lastReviewedDate != null) obj.put("lastReviewedDate", s.lastReviewedDate)
            if (s.easeFactor != null) obj.put("easeFactor", s.easeFactor)
            statesArray.put(obj)
        }
        root.put("reviewStates", statesArray)

        val logsArray = JSONArray()
        for (l in logs) {
            val obj = JSONObject()
            obj.put("id", l.id)
            obj.put("reviewStateId", l.reviewStateId)
            obj.put("rating", l.rating.name)
            obj.put("status", l.status.name)
            obj.put("difficulty", l.difficulty)
            obj.put("stability", l.stability)
            obj.put("elapsedDays", l.elapsedDays)
            obj.put("scheduledDays", l.scheduledDays)
            obj.put("reviewTimestamp", l.reviewTimestamp)
            logsArray.put(obj)
        }
        root.put("reviewLogs", logsArray)

        root.toString(2)
    }

    suspend fun restoreBackupJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("words")) {
                return@withContext Result.failure(IllegalArgumentException("Invalid backup JSON: 'words' list missing"))
            }

            val wordsArray = root.getJSONArray("words")
            val wordsList = mutableListOf<VocabularyWord>()
            for (i in 0 until wordsArray.length()) {
                val obj = wordsArray.getJSONObject(i)
                val typeName = obj.optString("wordType", "OTHER")
                val wordType = (try {
                    WordType.valueOf(typeName)
                } catch (_: Exception) {
                    WordType.from(typeName)
                }) ?: WordType.OTHER

                wordsList.add(
                    VocabularyWord(
                        id = obj.optLong("id", 0L),
                        german = obj.getString("german"),
                        wordType = wordType,
                        article = Article.from(obj.optString("article")),
                        plural = obj.optString("plural").takeIf { it.isNotEmpty() },
                        praeteritum = obj.optString("praeteritum").takeIf { it.isNotEmpty() },
                        perfekt = obj.optString("perfekt").takeIf { it.isNotEmpty() },
                        isSeparable = obj.optBoolean("isSeparable", false),
                        isReflexive = obj.optBoolean("isReflexive", false),
                        rektion = obj.optString("rektion").takeIf { it.isNotEmpty() },
                        arabic = obj.getString("arabic"),
                        exampleSentence = obj.optString("exampleSentence").takeIf { it.isNotEmpty() }
                            ?: obj.optString("exampleGerman").takeIf { it.isNotEmpty() },
                        exampleArabic = obj.optString("exampleArabic").takeIf { it.isNotEmpty() },
                        topic = obj.optString("topic", "Alltag"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val statesList = mutableListOf<ReviewState>()
            if (root.has("reviewStates")) {
                val statesArray = root.getJSONArray("reviewStates")
                for (i in 0 until statesArray.length()) {
                    val obj = statesArray.getJSONObject(i)
                    statesList.add(
                        ReviewState(
                            id = obj.optLong("id", 0L),
                            wordId = obj.getLong("wordId"),
                            direction = ReviewDirection.valueOf(obj.getString("direction")),
                            status = LearningStatus.valueOf(obj.getString("status")),
                            repetitionCount = obj.optInt("repetitionCount", 0),
                            lapses = obj.optInt("lapses", 0),
                            difficulty = obj.optDouble("difficulty", 0.0),
                            stability = obj.optDouble("stability", 0.0),
                            intervalDays = obj.optInt("intervalDays", 0),
                            nextReviewDate = obj.optLong("nextReviewDate", System.currentTimeMillis()),
                            lastReviewedDate = if (obj.has("lastReviewedDate") && !obj.isNull("lastReviewedDate")) obj.getLong("lastReviewedDate") else null,
                            easeFactor = if (obj.has("easeFactor") && !obj.isNull("easeFactor")) obj.getDouble("easeFactor").toFloat() else null
                        )
                    )
                }
            }

            val logsList = mutableListOf<ReviewLog>()
            if (root.has("reviewLogs")) {
                val logsArray = root.getJSONArray("reviewLogs")
                for (i in 0 until logsArray.length()) {
                    val obj = logsArray.getJSONObject(i)
                    logsList.add(
                        ReviewLog(
                            id = obj.optLong("id", 0L),
                            reviewStateId = obj.getLong("reviewStateId"),
                            rating = ReviewRating.valueOf(obj.getString("rating")),
                            status = LearningStatus.valueOf(obj.getString("status")),
                            difficulty = obj.optDouble("difficulty", 0.0),
                            stability = obj.optDouble("stability", 0.0),
                            elapsedDays = obj.optInt("elapsedDays", 0),
                            scheduledDays = obj.optInt("scheduledDays", 0),
                            reviewTimestamp = obj.optLong("reviewTimestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            // Clean existing and restore backup
            reviewStateDao.deleteAllReviewLogs()
            reviewStateDao.deleteAllReviewStates()
            vocabularyDao.deleteAllWords()

            vocabularyDao.restoreWords(wordsList)
            if (statesList.isNotEmpty()) {
                reviewStateDao.insertReviewStates(statesList)
            }
            if (logsList.isNotEmpty()) {
                reviewStateDao.insertReviewLogs(logsList)
            }

            Result.success(wordsList.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
