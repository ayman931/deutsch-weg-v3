package com.example.data.model

enum class WordType(val displayNameDe: String, val displayNameEn: String) {
    NOUN("Nomen", "Noun"),
    VERB("Verb", "Verb"),
    ADJECTIVE("Adjektiv", "Adjective"),
    ADVERB("Adverb", "Adverb"),
    PREPOSITION("Präposition", "Preposition"),
    CONJUNCTION("Konjunktion", "Conjunction"),
    PRONOUN("Pronomen", "Pronoun"),
    PHRASE("Redewendung", "Phrase"),
    OTHER("Sonstiges", "Other");

    companion object {
        fun from(raw: String?): WordType? {
            if (raw.isNullOrBlank()) return null
            val clean = raw.trim().uppercase()
            return when (clean) {
                "NOUN", "NOMEN", "SUBSTANTIV" -> NOUN
                "VERB", "VERBEN" -> VERB
                "ADJECTIVE", "ADJEKTIV" -> ADJECTIVE
                "ADVERB" -> ADVERB
                "PREPOSITION", "PRÄPOSITION", "PRAEPOSITION" -> PREPOSITION
                "CONJUNCTION", "KONJUNKTION" -> CONJUNCTION
                "PRONOUN", "PRONOMEN" -> PRONOUN
                "PHRASE", "REDEWENDUNG", "IDIOM", "EXPRESSION" -> PHRASE
                "OTHER", "SONSTIGES" -> OTHER
                else -> null
            }
        }
    }
}
