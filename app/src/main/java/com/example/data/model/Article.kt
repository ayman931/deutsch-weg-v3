package com.example.data.model

enum class Article(val value: String) {
    DER("der"),
    DIE("die"),
    DAS("das");

    companion object {
        fun from(raw: String?): Article? {
            if (raw.isNullOrBlank()) return null
            val clean = raw.trim().lowercase()
            return when (clean) {
                "der" -> DER
                "die" -> DIE
                "das" -> DAS
                else -> null
            }
        }
    }
}
