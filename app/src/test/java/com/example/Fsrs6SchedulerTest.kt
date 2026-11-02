package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.fsrs.Fsrs6Scheduler
import com.example.data.model.Article
import com.example.data.model.LearningStatus
import com.example.data.model.ReviewDirection
import com.example.data.model.ReviewState
import com.example.data.model.VocabularyWord
import com.example.data.model.WordType
import com.example.data.repository.ReviewRating
import com.example.data.repository.VocabularyRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Fsrs6SchedulerTest {

    private lateinit var scheduler: Fsrs6Scheduler
    private lateinit var database: AppDatabase
    private lateinit var repository: VocabularyRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = VocabularyRepositoryImpl(
            vocabularyDao = database.vocabularyDao(),
            reviewStateDao = database.reviewStateDao(),
            fsrsScheduler = Fsrs6Scheduler(desiredRetention = 0.90)
        )
        scheduler = Fsrs6Scheduler(desiredRetention = 0.90)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun test90PercentDesiredRetentionConfiguration() {
        assertEquals(0.90, scheduler.desiredRetention, 0.0001)
        assertEquals(21, scheduler.parameters.size)

        // At desiredRetention = 0.90, nextInterval(S) exactly equals S (rounded)
        val stability = 14.0
        val interval = scheduler.nextInterval(stability)
        assertEquals(14, interval)

        // Higher desired retention (e.g. 95%) forces shorter intervals to prevent forgetting
        val strictScheduler = Fsrs6Scheduler(desiredRetention = 0.95)
        val strictInterval = strictScheduler.nextInterval(stability)
        assertTrue(strictInterval < interval)

        // Lower desired retention (e.g. 80%) allows longer intervals
        val relaxedScheduler = Fsrs6Scheduler(desiredRetention = 0.80)
        val relaxedInterval = relaxedScheduler.nextInterval(stability)
        assertTrue(relaxedInterval > interval)
    }

    @Test
    fun testAllFourReviewRatingsOnFirstReview() {
        val initialNewState = ReviewState(
            id = 1,
            wordId = 100,
            direction = ReviewDirection.GERMAN_TO_ARABIC,
            status = LearningStatus.NEW,
            repetitionCount = 0,
            lapses = 0,
            difficulty = 0.0,
            stability = 0.0
        )
        val now = 1700000000000L

        // 1. AGAIN
        val resultAgain = scheduler.schedule(initialNewState, ReviewRating.AGAIN, now)
        assertEquals(scheduler.parameters[0], resultAgain.updatedState.stability, 0.001)
        assertEquals(scheduler.initDifficulty(1), resultAgain.updatedState.difficulty, 0.001)
        assertEquals(1, resultAgain.updatedState.lapses)
        assertEquals(LearningStatus.LEARNING, resultAgain.updatedState.status)
        assertEquals(1, resultAgain.updatedState.intervalDays)

        // 2. HARD
        val resultHard = scheduler.schedule(initialNewState, ReviewRating.HARD, now)
        assertEquals(scheduler.parameters[1], resultHard.updatedState.stability, 0.001)
        assertEquals(scheduler.initDifficulty(2), resultHard.updatedState.difficulty, 0.001)
        assertEquals(0, resultHard.updatedState.lapses)
        assertEquals(LearningStatus.LEARNING, resultHard.updatedState.status)
        assertEquals(1, resultHard.updatedState.intervalDays)

        // 3. GOOD
        val resultGood = scheduler.schedule(initialNewState, ReviewRating.GOOD, now)
        assertEquals(scheduler.parameters[2], resultGood.updatedState.stability, 0.001)
        assertEquals(scheduler.initDifficulty(3), resultGood.updatedState.difficulty, 0.001)
        assertEquals(0, resultGood.updatedState.lapses)
        assertEquals(LearningStatus.LEARNING, resultGood.updatedState.status)
        assertEquals(2, resultGood.updatedState.intervalDays) // 2.3065 rounded to 2

        // 4. EASY
        val resultEasy = scheduler.schedule(initialNewState, ReviewRating.EASY, now)
        assertEquals(scheduler.parameters[3], resultEasy.updatedState.stability, 0.001)
        assertEquals(scheduler.initDifficulty(4), resultEasy.updatedState.difficulty, 0.001)
        assertEquals(0, resultEasy.updatedState.lapses)
        assertEquals(LearningStatus.REVIEW, resultEasy.updatedState.status)
        assertEquals(8, resultEasy.updatedState.intervalDays) // 8.2956 rounded to 8
    }

    @Test
    fun testSuccessfulRecallProgression() {
        var state = ReviewState(
            id = 2,
            wordId = 101,
            direction = ReviewDirection.GERMAN_TO_ARABIC,
            status = LearningStatus.NEW,
            repetitionCount = 0,
            lapses = 0
        )
        var currentTime = 1700000000000L

        // Review 1: Good
        val r1 = scheduler.schedule(state, ReviewRating.GOOD, currentTime)
        state = r1.updatedState
        assertEquals(1, state.repetitionCount)
        val s1 = state.stability

        // Review 2: after s1 days, rated Good
        currentTime += (state.intervalDays * 86_400_000L)
        val r2 = scheduler.schedule(state, ReviewRating.GOOD, currentTime)
        state = r2.updatedState
        assertEquals(2, state.repetitionCount)
        assertEquals(LearningStatus.REVIEW, state.status)
        val s2 = state.stability
        assertTrue("Stability must increase upon successful recall: s2 ($s2) > s1 ($s1)", s2 > s1)

        // Review 3: after interval, rated Easy
        currentTime += (state.intervalDays * 86_400_000L)
        val r3 = scheduler.schedule(state, ReviewRating.EASY, currentTime)
        state = r3.updatedState
        val s3 = state.stability
        assertTrue("Stability must increase further: s3 ($s3) > s2 ($s2)", s3 > s2)
        assertEquals(0, state.lapses)
    }

    @Test
    fun testLapsesAndRelearningState() {
        val matureState = ReviewState(
            id = 3,
            wordId = 102,
            direction = ReviewDirection.GERMAN_TO_ARABIC,
            status = LearningStatus.REVIEW,
            repetitionCount = 5,
            lapses = 0,
            difficulty = 3.5,
            stability = 30.0,
            intervalDays = 30,
            lastReviewedDate = 1700000000000L
        )

        val reviewTime = 1700000000000L + (30 * 86_400_000L)

        // User lapses on this mature review card: rates AGAIN
        val result = scheduler.schedule(matureState, ReviewRating.AGAIN, reviewTime)
        val lapsedState = result.updatedState

        // Must transition to RELEARNING
        assertEquals(LearningStatus.RELEARNING, lapsedState.status)
        // Lapses must increment
        assertEquals(1, lapsedState.lapses)
        assertTrue("Stability must decrease after lapse: ${lapsedState.stability} < 30.0", lapsedState.stability < 30.0)
        assertEquals(scheduler.nextInterval(lapsedState.stability), lapsedState.intervalDays)
        assertTrue("Lapse interval should be short (1-3 days)", lapsedState.intervalDays in 1..3)

        // Next review after lapse: rated Good
        val recoveryTime = reviewTime + (1 * 86_400_000L)
        val recoveryResult = scheduler.schedule(lapsedState, ReviewRating.GOOD, recoveryTime)
        // Should graduate back to REVIEW
        assertEquals(LearningStatus.REVIEW, recoveryResult.updatedState.status)
        assertEquals(1, recoveryResult.updatedState.lapses)
    }

    @Test
    fun testIndependentSchedulingForBothReviewDirections() = runBlocking {
        val word = VocabularyWord(
            german = "verstehen",
            wordType = WordType.VERB,
            arabic = "يفهم",
            topic = "Alltag"
        )
        val wordId = database.vocabularyDao().insertWord(word)

        val deToArState = ReviewState(
            wordId = wordId,
            direction = ReviewDirection.GERMAN_TO_ARABIC,
            status = LearningStatus.NEW
        )
        val arToDeState = ReviewState(
            wordId = wordId,
            direction = ReviewDirection.ARABIC_TO_GERMAN,
            status = LearningStatus.NEW
        )

        val idDeToAr = database.reviewStateDao().insertReviewState(deToArState)
        val idArToDe = database.reviewStateDao().insertReviewState(arToDeState)

        val state1 = database.reviewStateDao().getReviewStatesForWord(wordId).find { it.id == idDeToAr }!!
        val state2 = database.reviewStateDao().getReviewStatesForWord(wordId).find { it.id == idArToDe }!!

        // Practice German -> Arabic: Easy recognition
        repository.recordReview(state1, ReviewRating.EASY)

        // Practice Arabic -> German: Hard / Lapse in production
        repository.recordReview(state2, ReviewRating.AGAIN)

        val updatedStates = database.reviewStateDao().getReviewStatesForWord(wordId)
        val updatedDeToAr = updatedStates.find { it.direction == ReviewDirection.GERMAN_TO_ARABIC }!!
        val updatedArToDe = updatedStates.find { it.direction == ReviewDirection.ARABIC_TO_GERMAN }!!

        // Assert independent memory states
        assertEquals(LearningStatus.REVIEW, updatedDeToAr.status)
        assertEquals(0, updatedDeToAr.lapses)
        assertTrue(updatedDeToAr.stability > 8.0)
        assertEquals(8, updatedDeToAr.intervalDays)

        assertEquals(LearningStatus.LEARNING, updatedArToDe.status)
        assertEquals(1, updatedArToDe.lapses)
        assertTrue(updatedArToDe.stability < 1.0)
        assertEquals(1, updatedArToDe.intervalDays)

        assertNotEquals(updatedDeToAr.difficulty, updatedArToDe.difficulty)
        assertNotEquals(updatedDeToAr.stability, updatedArToDe.stability)
    }

    @Test
    fun testReviewLogRecordedInDatabase() = runBlocking {
        val word = VocabularyWord(
            german = "Haus",
            wordType = WordType.NOUN,
            article = Article.DAS,
            arabic = "بيت",
            topic = "Wohnen"
        )
        val wordId = repository.insertWord(word)
        val state = ReviewState(
            wordId = wordId,
            direction = ReviewDirection.GERMAN_TO_ARABIC,
            status = LearningStatus.NEW
        )
        val stateId = database.reviewStateDao().insertReviewState(state)
        val loadedState = database.reviewStateDao().getReviewStatesForWord(wordId).first()

        repository.recordReview(loadedState, ReviewRating.GOOD)

        val logs = repository.getReviewLogs(stateId)
        assertEquals(1, logs.size)
        val log = logs.first()
        assertEquals(ReviewRating.GOOD, log.rating)
        assertEquals(LearningStatus.NEW, log.status)
        assertTrue(log.stability > 0.0)
        assertTrue(log.difficulty > 0.0)
    }

    @Test
    fun testDatabaseMigrationFromV1ToV2PreservesData() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("test_migration.db")
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `vocabulary_words` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `german` TEXT NOT NULL,
                            `wordType` TEXT NOT NULL,
                            `article` TEXT,
                            `plural` TEXT,
                            `praeteritum` TEXT,
                            `perfekt` TEXT,
                            `isSeparable` INTEGER NOT NULL,
                            `isReflexive` INTEGER NOT NULL,
                            `rektion` TEXT,
                            `arabic` TEXT NOT NULL,
                            `exampleSentence` TEXT,
                            `exampleArabic` TEXT,
                            `topic` TEXT NOT NULL,
                            `createdAt` INTEGER NOT NULL
                        )
                    """.trimIndent())

                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `review_states` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `wordId` INTEGER NOT NULL,
                            `direction` TEXT NOT NULL,
                            `status` TEXT NOT NULL,
                            `repetitionCount` INTEGER NOT NULL,
                            `easeFactor` REAL NOT NULL,
                            `intervalDays` INTEGER NOT NULL,
                            `nextReviewDate` INTEGER NOT NULL,
                            `lastReviewedDate` INTEGER,
                            FOREIGN KEY(`wordId`) REFERENCES `vocabulary_words`(`id`) ON DELETE CASCADE
                        )
                    """.trimIndent())

                    db.execSQL("INSERT INTO vocabulary_words (id, german, wordType, arabic, topic, isSeparable, isReflexive, createdAt) " +
                            "VALUES (1, 'reisen', 'VERB', 'يسافر', 'Urlaub', 0, 0, 1600000000)")
                    db.execSQL("INSERT INTO review_states (id, wordId, direction, status, repetitionCount, easeFactor, intervalDays, nextReviewDate) " +
                            "VALUES (1, 1, 'GERMAN_TO_ARABIC', 'REVIEW', 2, 2.5, 3, 1600100000)")
                }

                override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                    if (oldVersion < 2) {
                        AppDatabase.MIGRATION_1_2.migrate(db)
                    }
                }
            })
            .build()

        val helper = androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory().create(config)
        val db = helper.writableDatabase

        // Execute migration 1 -> 2
        AppDatabase.MIGRATION_1_2.migrate(db)

        // Verify V1 data is preserved intact
        val cursorVocab = db.query("SELECT german, arabic FROM vocabulary_words WHERE id = 1")
        assertTrue(cursorVocab.moveToFirst())
        assertEquals("reisen", cursorVocab.getString(0))
        assertEquals("يسافر", cursorVocab.getString(1))
        cursorVocab.close()

        val cursorReview = db.query("SELECT easeFactor, difficulty, stability, lapses FROM review_states WHERE id = 1")
        assertTrue(cursorReview.moveToFirst())
        assertEquals(2.5f, cursorReview.getFloat(0), 0.001f) // Legacy easeFactor preserved
        assertEquals(0.0, cursorReview.getDouble(1), 0.001)  // New difficulty column initialized to default
        assertEquals(0.0, cursorReview.getDouble(2), 0.001)  // New stability column initialized to default
        assertEquals(0, cursorReview.getInt(3))              // New lapses column initialized to 0
        cursorReview.close()

        // Verify review_logs table exists and is writable
        db.execSQL("INSERT INTO review_logs (reviewStateId, rating, status, difficulty, stability, elapsedDays, scheduledDays, reviewTimestamp) " +
                "VALUES (1, 'GOOD', 'REVIEW', 5.0, 3.0, 3, 6, 1700000000)")
        val cursorLog = db.query("SELECT count(*) FROM review_logs")
        assertTrue(cursorLog.moveToFirst())
        assertEquals(1, cursorLog.getInt(0))
        cursorLog.close()

        db.close()
    }

    @Test
    fun testSameDayReviewFormula() {
        val baseState = ReviewState(
            id = 10,
            wordId = 200,
            direction = ReviewDirection.GERMAN_TO_ARABIC,
            status = LearningStatus.REVIEW,
            repetitionCount = 2,
            lapses = 0,
            difficulty = 3.0,
            stability = 5.0,
            intervalDays = 5,
            lastReviewedDate = 1700000000000L
        )

        // Review on the exact same day (elapsedDays = 0.0)
        val sameDayTime = 1700000000000L + 3600_000L // 1 hour later
        val resultSameDayGood = scheduler.schedule(baseState, ReviewRating.GOOD, sameDayTime)
        val goodState = resultSameDayGood.updatedState

        // Expected same day formula: S' = S * exp(w[17] * (G - 3 + w[18])) * S^(-w[19])
        // For G=3: w17=0.5425, w18=0.0912, w19=0.0658
        // S' = 5.0 * exp(0.5425 * 0.0912) * 5.0^(-0.0658) = 5.0 * 1.050720 * 0.899661 = 4.7265
        assertEquals(4.7265, goodState.stability, 0.05)
        assertEquals(LearningStatus.REVIEW, goodState.status)

        // Same day review with EASY (G=4):
        val resultSameDayEasy = scheduler.schedule(baseState, ReviewRating.EASY, sameDayTime)
        // S' = 5.0 * exp(0.5425 * 1.0912) * 5.0^(-0.0658) = 5.0 * 1.807557 * 0.899661 = 8.1309
        assertTrue("Easy same-day rating must significantly increase stability", resultSameDayEasy.updatedState.stability > goodState.stability)
        assertEquals(8.13, resultSameDayEasy.updatedState.stability, 0.1)
    }

    @Test
    fun testLongReviewIntervalsAndLowRetrievability() {
        val matureState = ReviewState(
            id = 11,
            wordId = 201,
            direction = ReviewDirection.GERMAN_TO_ARABIC,
            status = LearningStatus.REVIEW,
            repetitionCount = 3,
            lapses = 0,
            difficulty = 4.0,
            stability = 10.0,
            intervalDays = 10,
            lastReviewedDate = 1700000000000L
        )

        // Long overdue: reviewed 100 days later (10x stability)
        val overdueTime = 1700000000000L + (100L * 86_400_000L)
        val resultOverdueGood = scheduler.schedule(matureState, ReviewRating.GOOD, overdueTime)

        // Retrievability should have dropped well below 0.90
        assertTrue("Retrievability for 10x interval must be below 0.75", resultOverdueGood.retrievability < 0.75)
        assertTrue("Retrievability must be non-negative", resultOverdueGood.retrievability > 0.0)

        // Successfully recalling long-overdue card produces substantial memory reinforcement
        assertTrue("Stability should increase after recalling an overdue card", resultOverdueGood.updatedState.stability > matureState.stability)
        assertEquals(LearningStatus.REVIEW, resultOverdueGood.updatedState.status)
    }

    @Test
    fun testBoundaryAndInvalidInputs() {
        // 1. Invalid parameter array size
        try {
            Fsrs6Scheduler(parameters = DoubleArray(19))
            org.junit.Assert.fail("Should throw IllegalArgumentException for wrong parameter count")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("21 parameters"))
        }

        // 2. Desired retention out of bounds (< 0.70)
        try {
            Fsrs6Scheduler(desiredRetention = 0.65)
            org.junit.Assert.fail("Should throw IllegalArgumentException for desired retention < 0.70")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Desired retention"))
        }

        // 3. Desired retention out of bounds (> 0.99)
        try {
            Fsrs6Scheduler(desiredRetention = 1.0)
            org.junit.Assert.fail("Should throw IllegalArgumentException for desired retention > 0.99")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Desired retention"))
        }

        // 4. Stability <= 0 in nextInterval
        assertEquals(1, scheduler.nextInterval(0.0))
        assertEquals(1, scheduler.nextInterval(-5.0))

        // 5. Zero / negative elapsed days in retrievability
        assertEquals(1.0, scheduler.retrievability(0.0, 10.0), 0.0001)
        assertEquals(1.0, scheduler.retrievability(-2.0, 10.0), 0.0001)
    }

    @Test
    fun testMigratedCardPreservesReviewStatusOnGoodFirstReview() {
        // Card migrated from V1 with REVIEW status, but stability = 0.0 and difficulty = 0.0
        val migratedCard = ReviewState(
            id = 12,
            wordId = 202,
            direction = ReviewDirection.GERMAN_TO_ARABIC,
            status = LearningStatus.REVIEW,
            repetitionCount = 3,
            lapses = 0,
            difficulty = 0.0,
            stability = 0.0,
            intervalDays = 5,
            nextReviewDate = 1700000000000L,
            easeFactor = 2.5f
        )

        // User reviews migrated card with GOOD
        val result = scheduler.schedule(migratedCard, ReviewRating.GOOD, 1700000000000L)
        val state = result.updatedState

        // Must remain in REVIEW (not demoted to LEARNING)
        assertEquals(LearningStatus.REVIEW, state.status)
        // Must obtain valid initial FSRS-6 stability and difficulty
        assertEquals(scheduler.parameters[2], state.stability, 0.001) // 2.3065
        assertEquals(scheduler.initDifficulty(3), state.difficulty, 0.001)
        assertEquals(0, state.lapses)
        assertEquals(4, state.repetitionCount)
        assertTrue(state.intervalDays >= 1)
    }

    @Test
    fun testMigratedCardTransitionsToRelearningOnAgainFirstReview() {
        // Card migrated from V1 with REVIEW status, but stability = 0.0 and difficulty = 0.0
        val migratedCard = ReviewState(
            id = 13,
            wordId = 203,
            direction = ReviewDirection.GERMAN_TO_ARABIC,
            status = LearningStatus.REVIEW,
            repetitionCount = 4,
            lapses = 0,
            difficulty = 0.0,
            stability = 0.0,
            intervalDays = 7,
            nextReviewDate = 1700000000000L,
            easeFactor = 2.4f
        )

        // User fails on migrated card: rates AGAIN
        val result = scheduler.schedule(migratedCard, ReviewRating.AGAIN, 1700000000000L)
        val state = result.updatedState

        // Must transition to RELEARNING
        assertEquals(LearningStatus.RELEARNING, state.status)
        assertEquals(1, state.lapses)
        assertEquals(scheduler.parameters[0], state.stability, 0.001) // 0.212
        assertEquals(scheduler.initDifficulty(1), state.difficulty, 0.001)
        assertEquals(5, state.repetitionCount)
    }
}
