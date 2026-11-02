package com.example.data.model

enum class ReviewDirection(val label: String, val fromLanguage: String, val toLanguage: String) {
    GERMAN_TO_ARABIC("Deutsch → Arabisch", "Deutsch", "Arabisch"),
    ARABIC_TO_GERMAN("Arabisch → Deutsch", "Arabisch", "Deutsch")
}
