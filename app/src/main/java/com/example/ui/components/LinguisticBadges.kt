package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.importer.ImportEntryStatus
import com.example.data.model.Article
import com.example.data.model.LearningStatus
import com.example.data.model.VocabularyWord
import com.example.data.model.WordType

val ColorArticleDer = Color(0xFF1D65D4) // Blue (Masculine)
val ColorArticleDie = Color(0xFFD63346) // Red (Feminine)
val ColorArticleDas = Color(0xFF1B806A) // Teal (Neuter)

@Composable
fun ArticleBadge(article: Article?, modifier: Modifier = Modifier) {
    if (article == null) return
    val (color, text) = when (article) {
        Article.DER -> ColorArticleDer to "der"
        Article.DIE -> ColorArticleDie to "die"
        Article.DAS -> ColorArticleDas to "das"
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WordTypeBadge(wordType: WordType, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (wordType) {
        WordType.NOUN -> Color(0xFFE0E7FF) to Color(0xFF3730A3)
        WordType.VERB -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        WordType.ADJECTIVE -> Color(0xFFDCFCE7) to Color(0xFF166534)
        WordType.ADVERB -> Color(0xFFF3E8FF) to Color(0xFF6B21A8)
        WordType.PREPOSITION -> Color(0xFFFFEDD5) to Color(0xFF9A3412)
        WordType.CONJUNCTION -> Color(0xFFCCFBF1) to Color(0xFF115E59)
        WordType.PRONOUN -> Color(0xFFFCE7F3) to Color(0xFF9D174D)
        WordType.PHRASE -> Color(0xFFE2E8F0) to Color(0xFF334155)
        WordType.OTHER -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = wordType.displayNameDe,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LinguisticPills(word: VocabularyWord, modifier: Modifier = Modifier) {
    FlowRow(modifier = modifier) {
        if (!word.plural.isNullOrBlank()) {
            Pill(label = "Pl: ${word.plural}", color = Color(0xFF475569))
        }
        if (!word.praeteritum.isNullOrBlank()) {
            Pill(label = "Prät: ${word.praeteritum}", color = Color(0xFF0369A1))
        }
        if (!word.perfekt.isNullOrBlank()) {
            Pill(label = "Perfekt: ${word.perfekt}", color = Color(0xFF0F766E))
        }
        if (word.isSeparable) {
            Pill(label = "trennbar", color = Color(0xFF7C3AED))
        }
        if (word.isReflexive) {
            Pill(label = "reflexiv (sich)", color = Color(0xFFC026D3))
        }
        if (!word.rektion.isNullOrBlank()) {
            Pill(label = "Rektion: ${word.rektion}", color = Color(0xFFD97706))
        }
        if (word.topic.isNotBlank()) {
            Pill(label = word.topic, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun Pill(label: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(end = 4.dp, bottom = 4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatusBadge(status: ImportEntryStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, text) = when (status) {
        ImportEntryStatus.VALID -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), "Gültig")
        ImportEntryStatus.DUPLICATE_EXACT -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), "Duplikat")
        ImportEntryStatus.DIFFERENT_MEANING -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), "Neue Bedeutung")
        ImportEntryStatus.INVALID -> Triple(Color(0xFFFEE2E2), Color(0xFFB91C1C), "Ungültig")
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LearningStatusBadge(status: LearningStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        LearningStatus.NEW -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
        LearningStatus.LEARNING -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        LearningStatus.REVIEW -> Color(0xFFEDE9FE) to Color(0xFF6D28D9)
        LearningStatus.RELEARNING -> Color(0xFFFFEDD5) to Color(0xFFC2410C)
        LearningStatus.MASTERED -> Color(0xFFDCFCE7) to Color(0xFF15803D)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = status.labelDe,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
