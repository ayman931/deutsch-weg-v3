package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.model.Article
import com.example.data.model.LearningStatus
import com.example.data.model.ReviewDirection
import com.example.data.model.WordType
import com.example.data.repository.ReviewRating

class Converters {
    @TypeConverter
    fun fromWordType(value: WordType?): String? = value?.name

    @TypeConverter
    fun toWordType(value: String?): WordType? = value?.let {
        try {
            WordType.valueOf(it)
        } catch (_: Exception) {
            WordType.from(it)
        }
    }

    @TypeConverter
    fun fromArticle(value: Article?): String? = value?.name

    @TypeConverter
    fun toArticle(value: String?): Article? = value?.let {
        try {
            Article.valueOf(it)
        } catch (_: Exception) {
            Article.from(it)
        }
    }

    @TypeConverter
    fun fromLearningStatus(value: LearningStatus?): String? = value?.name

    @TypeConverter
    fun toLearningStatus(value: String?): LearningStatus? = value?.let {
        try {
            LearningStatus.valueOf(it)
        } catch (_: Exception) {
            LearningStatus.NEW
        }
    }

    @TypeConverter
    fun fromReviewDirection(value: ReviewDirection?): String? = value?.name

    @TypeConverter
    fun toReviewDirection(value: String?): ReviewDirection? = value?.let {
        try {
            ReviewDirection.valueOf(it)
        } catch (_: Exception) {
            ReviewDirection.GERMAN_TO_ARABIC
        }
    }

    @TypeConverter
    fun fromReviewRating(value: ReviewRating?): String? = value?.name

    @TypeConverter
    fun toReviewRating(value: String?): ReviewRating? = value?.let {
        try {
            ReviewRating.valueOf(it)
        } catch (_: Exception) {
            ReviewRating.GOOD
        }
    }
}
