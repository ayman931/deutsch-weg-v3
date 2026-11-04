package com.example.ui.review

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Article
import com.example.data.model.ReviewDirection
import com.example.data.model.WordType
import com.example.data.repository.ReviewRating
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.ArticleDas
import com.example.ui.theme.ArticleDasSubtle
import com.example.ui.theme.ArticleDer
import com.example.ui.theme.ArticleDerSubtle
import com.example.ui.theme.ArticleDie
import com.example.ui.theme.ArticleDieSubtle
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.FeedbackCorrect
import com.example.ui.theme.FeedbackIncorrect
import com.example.ui.theme.OnPrimaryWarm
import com.example.ui.theme.PrimaryEspresso
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmBackground
import com.example.util.TtsManager

@Composable
fun FlashcardReviewScreen(
    viewModel: FlashcardReviewViewModel,
    onNavigateToImport: () -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    DisposableEffect(ttsManager) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val flashcards by viewModel.dueFlashcards.collectAsState()
    var isGrammarExpanded by remember { mutableStateOf(false) }
    var isBookmarked by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(WarmBackground)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Progress Header & Controls Tracker
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close button & Session title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCard)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .testTag("review_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إنهاء الجلسة",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "جلسة التكرار المتباعد",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            val currentPos = if (flashcards.isNotEmpty()) (uiState.currentIndex + 1).coerceAtMost(flashcards.size) else 0
                            Text(
                                text = "$currentPos / ${flashcards.size}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    // Audio Pronounce & Bookmark & Direction
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Pronounce current word
                        IconButton(
                            onClick = {
                                if (flashcards.isNotEmpty() && uiState.currentIndex < flashcards.size) {
                                    val currentCard = flashcards[uiState.currentIndex]
                                    val articleStr = currentCard.word.article?.name?.lowercase()?.let { "$it " } ?: ""
                                    ttsManager.speak("$articleStr${currentCard.word.german}")
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCard)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .testTag("review_header_tts_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "استمع للنطق الصوتي",
                                tint = PrimaryEspresso,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Bookmark toggle
                        IconButton(
                            onClick = { isBookmarked = !isBookmarked },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCard)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "حفظ الكلمة في المفضلة",
                                tint = if (isBookmarked) AccentAmber else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Direction Toggle
                        FilterChip(
                            selected = uiState.direction == ReviewDirection.GERMAN_TO_ARABIC,
                            onClick = {
                                val nextDir = if (uiState.direction == ReviewDirection.GERMAN_TO_ARABIC)
                                    ReviewDirection.ARABIC_TO_GERMAN
                                else
                                    ReviewDirection.GERMAN_TO_ARABIC
                                viewModel.setDirection(nextDir)
                            },
                            label = {
                                Text(
                                    if (uiState.direction == ReviewDirection.GERMAN_TO_ARABIC) "DE→AR" else "AR→DE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryEspresso,
                                selectedLabelColor = OnPrimaryWarm,
                                containerColor = SurfaceCard,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = uiState.direction == ReviewDirection.GERMAN_TO_ARABIC,
                                borderColor = BorderSubtle
                            ),
                            modifier = Modifier.testTag("direction_toggle_chip")
                        )
                    }
                }

                // Multi-segmented Retention Progress Bar
                val progressFraction = if (flashcards.isNotEmpty()) {
                    ((uiState.currentIndex).toFloat() / flashcards.size.toFloat()).coerceIn(0f, 1f)
                } else 0f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SurfaceContainerHigh)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(FeedbackCorrect)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (flashcards.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = FeedbackCorrect,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            text = "لا توجد بطاقات مستحقة الآن",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "لقد أكملت جميع بطاقات اليوم! يمكنك استيراد كلمات جديدة أو تغيير الاتجاه لمواصلة الممارسة.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onNavigateToImport,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryEspresso,
                                contentColor = OnPrimaryWarm
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("empty_import_button")
                        ) {
                            Text("استيراد بطاقات جديدة", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (uiState.currentIndex >= flashcards.size) {
                // Completed Session State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(FeedbackCorrect.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = FeedbackCorrect,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        Text(
                            text = "اكتملت جلسة المذاكرة بنجاح!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "أتممت مراجعة ${uiState.completedCount} بطاقة. وفقاً لجدول FSRS-6 الذكي، ستتم جدولة البطاقات تلقائياً في الأوقات المثالية لتثبيتها في الذاكرة.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = viewModel::restartSession,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryEspresso,
                                contentColor = OnPrimaryWarm
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("restart_session_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("بدء جلسة جديدة", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Active Card View
                val currentItem = flashcards[uiState.currentIndex]
                val word = currentItem.word

                val articleColor = when (word.article) {
                    Article.DER -> ArticleDer
                    Article.DIE -> ArticleDie
                    Article.DAS -> ArticleDas
                    null -> PrimaryEspresso
                }
                val articleSubtleBg = when (word.article) {
                    Article.DER -> ArticleDerSubtle
                    Article.DIE -> ArticleDieSubtle
                    Article.DAS -> ArticleDasSubtle
                    null -> SurfaceContainerHigh
                }

                val typeLabel = when (word.wordType) {
                    WordType.NOUN -> when (word.article) {
                        Article.DER -> "اسم مذكّر"
                        Article.DIE -> "اسم مؤنث"
                        Article.DAS -> "اسم محايد"
                        null -> "اسم"
                    }
                    WordType.VERB -> "فعل"
                    WordType.ADJECTIVE -> "صفة"
                    WordType.ADVERB -> "ظرف"
                    WordType.PREPOSITION -> "حرف جر"
                    WordType.CONJUNCTION -> "حرف عطف"
                    WordType.PRONOUN -> "ضمير"
                    WordType.PHRASE -> "عبارة شائعة"
                    WordType.OTHER -> "مفردة لغوية"
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Hero Flashcard Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(28.dp))
                            .padding(20.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Top Meta Tags inside Card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (word.article != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(articleSubtleBg)
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(articleColor)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = word.article.name.lowercase(),
                                                    color = articleColor,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(SurfaceContainerHigh)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = typeLabel,
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceContainer)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "B1",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentAmber
                                    )
                                }
                            }

                            // German Target Word Presentation (LTR)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (word.article != null) {
                                            Text(
                                                text = "${word.article.name.lowercase()} ",
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Black,
                                                color = articleColor
                                            )
                                        }
                                        Text(
                                            text = word.german,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TextPrimary
                                        )
                                    }
                                    if (!word.plural.isNullOrBlank()) {
                                        Text(
                                            text = "Pl: ${word.plural}",
                                            fontSize = 13.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = TextSecondary,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Central Reveal / Meaning Card
                            if (!uiState.isFlipped) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(SurfaceContainerLow)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                                        .clickable { viewModel.flipCard() }
                                        .padding(vertical = 24.dp, horizontal = 16.dp)
                                        .testTag("review_reveal_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.TouchApp,
                                            contentDescription = null,
                                            tint = PrimaryEspresso,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "انقر لإظهار المعنى والتفاصيل",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryEspresso
                                        )
                                        Text(
                                            text = "قيّم حفظك بعد مراجعة المعنى",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            } else {
                                // Revealed Content Container
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Primary Arabic Meaning
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(SurfaceElevated)
                                            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "المعنى العربي",
                                                    fontSize = 12.sp,
                                                    color = TextSecondary
                                                )
                                                Text(
                                                    text = word.arabic,
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(SurfaceContainerHigh),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.VpnKey,
                                                    contentDescription = null,
                                                    tint = PrimaryEspresso,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Real Contextual Example sentence
                                    if (!word.exampleSentence.isNullOrBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(SurfaceContainerLow)
                                                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                                                .padding(14.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.RecordVoiceOver,
                                                            contentDescription = null,
                                                            tint = PrimaryEspresso,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "مثال توضيحي",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = PrimaryEspresso
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { ttsManager.speak(word.exampleSentence) },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.VolumeDown,
                                                            contentDescription = "استمع للمثال",
                                                            tint = TextSecondary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }

                                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                    Text(
                                                        text = word.exampleSentence,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextPrimary
                                                    )
                                                }

                                                if (!word.exampleArabic.isNullOrBlank()) {
                                                    Text(
                                                        text = word.exampleArabic,
                                                        fontSize = 13.sp,
                                                        color = TextSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Expandable Grammar Morphological Sheet Accordion
                                    val hasGrammarDetails = !word.plural.isNullOrBlank() || !word.perfekt.isNullOrBlank() || !word.praeteritum.isNullOrBlank()
                                    if (hasGrammarDetails) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(SurfaceElevated)
                                                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                                                .clickable { isGrammarExpanded = !isGrammarExpanded }
                                                .padding(12.dp)
                                        ) {
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.AccountTree,
                                                            contentDescription = null,
                                                            tint = PrimaryEspresso,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "التفاصيل النحوية",
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = TextPrimary
                                                        )
                                                    }
                                                    Icon(
                                                        imageVector = if (isGrammarExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                        contentDescription = null,
                                                        tint = TextSecondary
                                                    )
                                                }

                                                AnimatedVisibility(visible = isGrammarExpanded) {
                                                    Column(
                                                        modifier = Modifier.padding(top = 10.dp),
                                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            if (!word.plural.isNullOrBlank()) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .clip(RoundedCornerShape(10.dp))
                                                                        .background(SurfaceContainer)
                                                                        .padding(8.dp)
                                                                ) {
                                                                    Column {
                                                                        Text("صيغة الجمع (Plural)", fontSize = 10.sp, color = TextSecondary)
                                                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                                            Text(word.plural, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            if (!word.perfekt.isNullOrBlank()) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .clip(RoundedCornerShape(10.dp))
                                                                        .background(SurfaceContainer)
                                                                        .padding(8.dp)
                                                                ) {
                                                                    Column {
                                                                        Text("المضارع التام (Perfekt)", fontSize = 10.sp, color = TextSecondary)
                                                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                                            Text(word.perfekt, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Visual Micro-Insight Pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ArticleDerSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = ArticleDer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "حيلة التذكر اللغوي",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryEspresso
                                )
                                Text(
                                    text = "الكلمات المنتهية بـ (-el) غالباً ما تأخذ أداة التذكير der.",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // 4 SRS Answer Action Buttons (Active Thumb Targets)
                if (uiState.isFlipped) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مدى سهولة التذكر:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Text(
                                text = "FSRS-6 Scheduler",
                                fontSize = 11.sp,
                                color = PrimaryEspresso
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Again (Wiederholen)
                            SrsActionButton(
                                title = "إعادة",
                                subtitle = "< ١ دقيقة",
                                dotColor = FeedbackIncorrect,
                                enabled = !uiState.isSubmitting,
                                onClick = { viewModel.answerCard(currentItem, ReviewRating.AGAIN) },
                                modifier = Modifier.weight(1f).testTag("rating_button_again")
                            )

                            // 2. Hard (Schwer)
                            SrsActionButton(
                                title = "صعب",
                                subtitle = "٢ يوم",
                                dotColor = AccentAmber,
                                enabled = !uiState.isSubmitting,
                                onClick = { viewModel.answerCard(currentItem, ReviewRating.HARD) },
                                modifier = Modifier.weight(1f).testTag("rating_button_hard")
                            )

                            // 3. Good (Gut)
                            SrsActionButton(
                                title = "جيد",
                                subtitle = "٤ أيام",
                                dotColor = PrimaryEspresso,
                                enabled = !uiState.isSubmitting,
                                onClick = { viewModel.answerCard(currentItem, ReviewRating.GOOD) },
                                modifier = Modifier.weight(1f).testTag("rating_button_good")
                            )

                            // 4. Easy (Einfach)
                            SrsActionButton(
                                title = "سهل",
                                subtitle = "٧ أيام",
                                dotColor = FeedbackCorrect,
                                enabled = !uiState.isSubmitting,
                                onClick = { viewModel.answerCard(currentItem, ReviewRating.EASY) },
                                modifier = Modifier.weight(1f).testTag("rating_button_easy")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SrsActionButton(
    title: String,
    subtitle: String,
    dotColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = dotColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
