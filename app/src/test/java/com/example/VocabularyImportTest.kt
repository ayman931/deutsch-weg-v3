package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.importer.ImportEntryStatus
import com.example.data.importer.VocabularyJsonImporter
import com.example.data.model.Article
import com.example.data.model.LearningStatus
import com.example.data.model.ReviewDirection
import com.example.data.model.VocabularyWord
import com.example.data.model.WordType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VocabularyImportTest {

    private lateinit var database: AppDatabase
    private val importer = VocabularyJsonImporter()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testValidJsonParsingAndPreservationOfLinguisticData() {
        val json = """
        [
          {
            "german": "entscheiden",
            "wordType": "VERB",
            "article": null,
            "plural": null,
            "praeteritum": "entschied",
            "perfekt": "hat entschieden",
            "isSeparable": false,
            "isReflexive": true,
            "rektion": "über + Akk",
            "arabic": "يقرر",
            "exampleSentence": "Ich muss mich entscheiden.",
            "exampleArabic": "يجب علي أن أقرر.",
            "topic": "Alltag"
          }
        ]
        """.trimIndent()

        val preview = importer.parseAndValidate(json)
        assertNull(preview.globalError)
        assertEquals(1, preview.totalCount)
        assertEquals(1, preview.validCount)
        assertEquals(0, preview.invalidCount)
        assertEquals(0, preview.duplicateCount)

        val entry = preview.entries.first()
        assertEquals(ImportEntryStatus.VALID, entry.status)
        assertNotNull(entry.word)

        val word = entry.word!!
        assertEquals("entscheiden", word.german)
        assertEquals(WordType.VERB, word.wordType)
        assertNull(word.article)
        assertNull(word.plural)
        assertEquals("entschied", word.praeteritum)
        assertEquals("hat entschieden", word.perfekt)
        assertFalse(word.isSeparable)
        assertTrue(word.isReflexive)
        assertEquals("über + Akk", word.rektion)
        assertEquals("يقرر", word.arabic)
        assertEquals("Ich muss mich entscheiden.", word.exampleSentence)
        assertEquals("يجب علي أن أقرر.", word.exampleArabic)
        assertEquals("Alltag", word.topic)
    }

    @Test
    fun testNullFieldsPreservation() {
        val json = """
        [
          {
            "german": "Haus",
            "wordType": "NOUN",
            "article": "das",
            "plural": "die Häuser",
            "praeteritum": null,
            "perfekt": null,
            "isSeparable": false,
            "isReflexive": false,
            "rektion": null,
            "arabic": "بيت",
            "exampleSentence": null,
            "exampleArabic": null,
            "topic": "Wohnen"
          }
        ]
        """.trimIndent()

        val preview = importer.parseAndValidate(json)
        val word = preview.entries.first().word
        assertNotNull(word)
        assertEquals("Haus", word!!.german)
        assertEquals(Article.DAS, word.article)
        assertEquals("die Häuser", word.plural)
        assertNull(word.praeteritum)
        assertNull(word.perfekt)
        assertNull(word.rektion)
        assertNull(word.exampleSentence)
        assertNull(word.exampleArabic)
    }

    @Test
    fun testMissingRequiredFields() {
        val missingGerman = """
        [
          {
            "wordType": "NOUN",
            "arabic": "سيارة"
          }
        ]
        """.trimIndent()

        val preview1 = importer.parseAndValidate(missingGerman)
        assertEquals(1, preview1.invalidCount)
        val entry1 = preview1.entries.first()
        assertEquals(ImportEntryStatus.INVALID, entry1.status)
        assertTrue(entry1.errors.any { it.contains("german") })

        val missingArabic = """
        [
          {
            "german": "Auto",
            "wordType": "NOUN"
          }
        ]
        """.trimIndent()

        val preview2 = importer.parseAndValidate(missingArabic)
        val entry2 = preview2.entries.first()
        assertEquals(ImportEntryStatus.INVALID, entry2.status)
        assertTrue(entry2.errors.any { it.contains("arabic") })

        val missingWordType = """
        [
          {
            "german": "Auto",
            "arabic": "سيارة"
          }
        ]
        """.trimIndent()

        val preview3 = importer.parseAndValidate(missingWordType)
        val entry3 = preview3.entries.first()
        assertEquals(ImportEntryStatus.INVALID, entry3.status)
        assertTrue(entry3.errors.any { it.contains("wordType") })
    }

    @Test
    fun testInvalidJsonSyntaxAndStructures() {
        // Syntax error
        val malformed = "[ { this is not valid json "
        val previewMalformed = importer.parseAndValidate(malformed)
        assertNotNull(previewMalformed.globalError)
        assertTrue(previewMalformed.globalError!!.contains("syntax", ignoreCase = true))

        // Root is object instead of array
        val rootObject = """{"german": "Auto", "wordType": "NOUN", "arabic": "سيارة"}"""
        val previewObject = importer.parseAndValidate(rootObject)
        assertNotNull(previewObject.globalError)
        assertTrue(previewObject.globalError!!.contains("JSON array"))

        // Empty array
        val emptyArray = "[]"
        val previewEmpty = importer.parseAndValidate(emptyArray)
        assertNotNull(previewEmpty.globalError)
        assertTrue(previewEmpty.globalError!!.contains("empty"))

        // Unknown wordType
        val unknownType = """[{"german": "Hallo", "wordType": "ALIEN_TYPE", "arabic": "مرحبا"}]"""
        val previewUnknown = importer.parseAndValidate(unknownType)
        assertEquals(1, previewUnknown.invalidCount)
        assertTrue(previewUnknown.entries.first().errors.any { it.contains("Unknown wordType") })
    }

    @Test
    fun testDuplicateDetectionExactAndDifferentMeaning() {
        val existingWords = listOf(
            VocabularyWord(
                id = 1,
                german = "Bank",
                wordType = WordType.NOUN,
                article = Article.DIE,
                arabic = "بنك / مصرف",
                topic = "Finanzen"
            )
        )

        val json = """
        [
          {
            "german": "Bank",
            "wordType": "NOUN",
            "article": "die",
            "arabic": "بنك / مصرف",
            "topic": "Finanzen"
          },
          {
            "german": "Bank",
            "wordType": "NOUN",
            "article": "die",
            "arabic": "مقعد حديقة",
            "topic": "Natur"
          }
        ]
        """.trimIndent()

        val preview = importer.parseAndValidate(json, existingWords)
        assertEquals(2, preview.totalCount)

        // First item: exact duplicate of existing database item
        val exactDup = preview.entries[0]
        assertEquals(ImportEntryStatus.DUPLICATE_EXACT, exactDup.status)
        assertFalse(exactDup.isSelectedForImport)
        assertTrue(exactDup.warnings.any { it.contains("Exact duplicate") })

        // Second item: same German lemma but different meaning
        val diffMeaning = preview.entries[1]
        assertEquals(ImportEntryStatus.DIFFERENT_MEANING, diffMeaning.status)
        assertTrue(diffMeaning.isSelectedForImport)
        assertTrue(diffMeaning.warnings.any { it.contains("different meaning") })
    }

    @Test
    fun testPartialImportsAllowed() {
        val json = """
        [
          {
            "german": "lernen",
            "wordType": "VERB",
            "arabic": "يتعلم",
            "topic": "Bildung"
          },
          {
            "german": "defekt",
            "wordType": "INVALID_TYPE",
            "arabic": "معطل"
          },
          {
            "german": "Buch",
            "wordType": "NOUN",
            "article": "das",
            "arabic": "كتاب",
            "topic": "Bildung"
          }
        ]
        """.trimIndent()

        val preview = importer.parseAndValidate(json)
        assertEquals(3, preview.totalCount)
        assertEquals(2, preview.validCount)
        assertEquals(1, preview.invalidCount)

        val selected = preview.entries.filter { it.isSelectedForImport && it.word != null }
        assertEquals(2, selected.size)
        assertEquals("lernen", selected[0].word!!.german)
        assertEquals("Buch", selected[1].word!!.german)
    }

    @Test
    fun testDatabaseBatchTransactionAndReviewStatesCreation() = runBlocking {
        val words = listOf(
            VocabularyWord(
                german = "verstehen",
                wordType = WordType.VERB,
                arabic = "يفهم",
                topic = "Bildung"
            ),
            VocabularyWord(
                german = "Freund",
                wordType = WordType.NOUN,
                article = Article.DER,
                arabic = "صديق",
                topic = "Menschen"
            )
        )

        val insertedIds = database.vocabularyDao().batchImportWithReviewStates(words)
        assertEquals(2, insertedIds.size)

        // Verify words in database
        val allWords = database.vocabularyDao().getAllWordsSnapshot()
        assertEquals(2, allWords.size)

        // Verify only GERMAN_TO_ARABIC review state is created initially at import
        val wordsWithStates = database.vocabularyDao().getWordsWithReviewStatesSnapshot()
        assertEquals(2, wordsWithStates.size)

        for (item in wordsWithStates) {
            val states = item.reviewStates
            assertEquals(1, states.size)
            assertEquals(ReviewDirection.GERMAN_TO_ARABIC, states.first().direction)

            for (state in states) {
                // Must be initialized with NEW learning status and unfabricated history
                assertEquals(LearningStatus.NEW, state.status)
                assertEquals(0, state.repetitionCount)
                assertEquals(0, state.intervalDays)
                assertEquals(0, state.lapses)
                assertEquals(0.0, state.difficulty, 0.001)
                assertEquals(0.0, state.stability, 0.001)
                assertNull(state.lastReviewedDate)
            }
        }
    }
}
