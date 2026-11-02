package com.example.data.model

enum class LearningStatus(val labelDe: String, val labelEn: String) {
    NEW("Neu", "New"),
    LEARNING("Lernen", "Learning"),
    REVIEW("Wiederholen", "Review"),
    RELEARNING("Wiederlernen", "Relearning"),
    MASTERED("Gemeistert", "Mastered")
}
