package com.example.data.importer

import com.example.data.model.Article
import com.example.data.model.VocabularyWord
import com.example.data.model.WordType
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class VocabularyJsonImporter {

    /**
     * Parses and validates raw JSON string against existing database vocabulary entries.
     */
    fun parseAndValidate(
        rawJson: String,
        existingWords: List<VocabularyWord> = emptyList()
    ): ImportPreview {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) {
            return ImportPreview(
                globalError = "JSON input is empty. Please paste JSON content or select a .json file."
            )
        }

        val jsonArray: JSONArray
        try {
            if (trimmed.startsWith("{") && !trimmed.startsWith("[")) {
                return ImportPreview(
                    globalError = "Invalid JSON structure: The root element must be a JSON array [...], not a single JSON object {...}."
                )
            }
            jsonArray = JSONArray(trimmed)
        } catch (e: JSONException) {
            return ImportPreview(
                globalError = "Invalid JSON syntax: ${e.localizedMessage ?: "Failed to parse JSON"}"
            )
        } catch (e: Exception) {
            return ImportPreview(
                globalError = "Failed to parse JSON input: ${e.localizedMessage ?: "Unknown error"}"
            )
        }

        if (jsonArray.length() == 0) {
            return ImportPreview(
                globalError = "The JSON array is empty. No vocabulary entries found."
            )
        }

        val entries = mutableListOf<ImportEntryPreview>()

        // Track exact identities and lemmas for duplicate detection
        // Format of exact identity: "german_lemma|WORD_TYPE|arabic_meaning"
        val existingExactIdentities = existingWords.map { it.normalizedIdentity() }.toSet()
        val existingLemmasMap = existingWords.groupBy(
            keySelector = { it.normalizedGermanLemma() },
            valueTransform = { it.arabic.trim() }
        )

        val seenInCurrentBatchExact = mutableSetOf<String>()
        val seenInCurrentBatchLemmas = mutableMapOf<String, String>()

        var validCount = 0
        var duplicateCount = 0
        var invalidCount = 0

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.opt(i)
            val index = i + 1

            if (item !is JSONObject) {
                invalidCount++
                entries.add(
                    ImportEntryPreview(
                        index = index,
                        status = ImportEntryStatus.INVALID,
                        word = null,
                        errors = listOf("Entry #$index is not a JSON object (found ${item?.javaClass?.simpleName ?: "null"})."),
                        rawPreview = item?.toString() ?: "null",
                        isSelectedForImport = false
                    )
                )
                continue
            }

            val parseResult = parseSingleEntry(item, index)
            val errors = parseResult.errors.toMutableList()
            val warnings = parseResult.warnings.toMutableList()

            if (errors.isNotEmpty() || parseResult.word == null) {
                invalidCount++
                entries.add(
                    ImportEntryPreview(
                        index = index,
                        status = ImportEntryStatus.INVALID,
                        word = null,
                        errors = errors,
                        warnings = warnings,
                        rawPreview = item.toString(2),
                        isSelectedForImport = false
                    )
                )
                continue
            }

            val word = parseResult.word
            val exactIdentity = word.normalizedIdentity()
            val lemma = word.normalizedGermanLemma()

            // Check for exact duplicate
            val isExactDuplicateInDb = existingExactIdentities.contains(exactIdentity)
            val isExactDuplicateInBatch = seenInCurrentBatchExact.contains(exactIdentity)

            if (isExactDuplicateInDb || isExactDuplicateInBatch) {
                duplicateCount++
                val source = if (isExactDuplicateInDb) "existing database" else "earlier entry in this file"
                warnings.add("Exact duplicate found in $source ('${word.german}' - '${word.arabic}').")
                entries.add(
                    ImportEntryPreview(
                        index = index,
                        status = ImportEntryStatus.DUPLICATE_EXACT,
                        word = word,
                        errors = emptyList(),
                        warnings = warnings,
                        rawPreview = item.toString(2),
                        isSelectedForImport = false // By default, skip exact duplicates
                    )
                )
                continue
            }

            // Check if lemma already exists with a DIFFERENT meaning
            val existingMeaningsInDb = existingLemmasMap[lemma] ?: emptyList()
            val existingMeaningInBatch = seenInCurrentBatchLemmas[lemma]

            if (existingMeaningsInDb.isNotEmpty() || existingMeaningInBatch != null) {
                val otherMeanings = (existingMeaningsInDb + listOfNotNull(existingMeaningInBatch))
                    .distinct()
                    .filter { it.lowercase() != word.arabic.trim().lowercase() }

                if (otherMeanings.isNotEmpty()) {
                    warnings.add(
                        "German word '${word.german}' already exists with different meaning(s): " +
                                otherMeanings.joinToString(", ") { "'$it'" } +
                                ". Distinct meaning will be kept."
                    )
                }
            }

            seenInCurrentBatchExact.add(exactIdentity)
            seenInCurrentBatchLemmas[lemma] = word.arabic.trim()

            validCount++
            val status = if (warnings.any { it.contains("already exists with different meaning") }) {
                ImportEntryStatus.DIFFERENT_MEANING
            } else {
                ImportEntryStatus.VALID
            }

            entries.add(
                ImportEntryPreview(
                    index = index,
                    status = status,
                    word = word,
                    errors = emptyList(),
                    warnings = warnings,
                    rawPreview = item.toString(2),
                    isSelectedForImport = true
                )
            )
        }

        return ImportPreview(
            totalCount = jsonArray.length(),
            validCount = validCount,
            duplicateCount = duplicateCount,
            invalidCount = invalidCount,
            entries = entries,
            globalError = null
        )
    }

    private data class SingleEntryResult(
        val word: VocabularyWord?,
        val errors: List<String>,
        val warnings: List<String>
    )

    private fun parseSingleEntry(obj: JSONObject, index: Int): SingleEntryResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // 1. German (required non-blank)
        val german = optCleanString(obj, "german")
        if (german.isNullOrBlank()) {
            errors.add("Missing or blank required field 'german'.")
        }

        // 2. Arabic (required non-blank)
        val arabic = optCleanString(obj, "arabic")
        if (arabic.isNullOrBlank()) {
            errors.add("Missing or blank required field 'arabic'.")
        }

        // 3. WordType (required, must normalize)
        val rawWordType = optCleanString(obj, "wordType")
        val wordType: WordType?
        if (rawWordType.isNullOrBlank()) {
            errors.add("Missing required field 'wordType'.")
            wordType = null
        } else {
            wordType = WordType.from(rawWordType)
            if (wordType == null) {
                errors.add(
                    "Unknown wordType '$rawWordType'. Supported: NOUN, VERB, ADJECTIVE, ADVERB, PREPOSITION, CONJUNCTION, PRONOUN, PHRASE, OTHER."
                )
            }
        }

        // 4. Article (nullable)
        val rawArticle = optCleanString(obj, "article")
        val article = if (rawArticle != null) {
            val parsedArticle = Article.from(rawArticle)
            if (parsedArticle == null && wordType == WordType.NOUN) {
                warnings.add("Unrecognized article '$rawArticle' for noun; article set to null.")
            }
            parsedArticle
        } else {
            null
        }

        // 5. Plural (nullable)
        val plural = optCleanString(obj, "plural")

        // 6. Präteritum & Perfekt (nullable)
        val praeteritum = optCleanString(obj, "praeteritum")
        val perfekt = optCleanString(obj, "perfekt")

        // 7. Boolean flags (isSeparable, isReflexive)
        val isSeparable = optSafeBoolean(obj, "isSeparable")
        val isReflexive = optSafeBoolean(obj, "isReflexive")

        // 8. Rektion (nullable)
        val rektion = optCleanString(obj, "rektion")

        // 9. Example sentence & translation (nullable)
        val exampleSentence = optCleanString(obj, "exampleSentence")
        val exampleArabic = optCleanString(obj, "exampleArabic")

        // 10. Topic (nullable, default "Alltag")
        val rawTopic = optCleanString(obj, "topic")
        val topic = if (rawTopic.isNullOrBlank()) "Alltag" else rawTopic.trim()

        if (errors.isNotEmpty() || german.isNullOrBlank() || arabic.isNullOrBlank() || wordType == null) {
            return SingleEntryResult(word = null, errors = errors, warnings = warnings)
        }

        val word = VocabularyWord(
            german = german.trim(),
            wordType = wordType,
            article = article,
            plural = plural,
            praeteritum = praeteritum,
            perfekt = perfekt,
            isSeparable = isSeparable,
            isReflexive = isReflexive,
            rektion = rektion,
            arabic = arabic.trim(),
            exampleSentence = exampleSentence,
            exampleArabic = exampleArabic,
            topic = topic
        )

        return SingleEntryResult(word = word, errors = errors, warnings = warnings)
    }

    private fun optCleanString(obj: JSONObject, key: String): String? {
        if (!obj.has(key) || obj.isNull(key)) return null
        val value = obj.opt(key) ?: return null
        val str = value.toString().trim()
        if (str.isEmpty() || str.equals("null", ignoreCase = true)) return null
        return str
    }

    private fun optSafeBoolean(obj: JSONObject, key: String): Boolean {
        if (!obj.has(key) || obj.isNull(key)) return false
        val opt = obj.opt(key)
        return when (opt) {
            is Boolean -> opt
            is String -> opt.trim().equals("true", ignoreCase = true)
            is Number -> opt.toInt() != 0
            else -> false
        }
    }
}
