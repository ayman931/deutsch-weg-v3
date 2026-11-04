package com.example.ui.home

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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LearningStatus
import com.example.data.model.VocabularyWord
import com.example.data.repository.SettingsRepository
import com.example.data.repository.VocabularyRepository
import com.example.ui.review.FlashcardReviewViewModel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.ArticleDer
import com.example.ui.theme.ArticleDerSubtle
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.FeedbackCorrect
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
fun HomeScreen(
    vocabularyRepository: VocabularyRepository,
    reviewViewModel: FlashcardReviewViewModel,
    settingsRepository: SettingsRepository,
    onStartReview: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    DisposableEffect(ttsManager) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    val dueFlashcards by reviewViewModel.dueFlashcards.collectAsState()
    val wordsWithReviews by vocabularyRepository.wordsWithReviewStates.collectAsState(initial = emptyList())
    val maxNewCards by settingsRepository.maxNewCardsPerDay.collectAsState()

    var selectedSessionDuration by remember { mutableIntStateOf(10) }
    var selectedCardsCount by remember { mutableIntStateOf(25) }

    // Count review vs new in current queue
    val reviewCardsCount = dueFlashcards.count { it.reviewState.status != LearningStatus.NEW }
    val newCardsCount = dueFlashcards.count { it.reviewState.status == LearningStatus.NEW }
    val totalDue = dueFlashcards.size

    // Spotlight word: pick first available word or fallback
    val spotlightWord: VocabularyWord? = wordsWithReviews.firstOrNull()?.word

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(WarmBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Greeting & Streak Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "صباح الإنجاز",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "مرحباً بك مجدداً",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🔥", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "١٤ يوماً",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentAmber
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "متواصلة",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 2. Today's Progress Ring Sanctuary Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = PrimaryEspresso,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "حصاد اليوم",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = "A1 • B1",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }

                    // Central Circular Progress Ring
                    Box(
                        modifier = Modifier
                            .size(175.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background Track
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = SurfaceContainerHigh,
                            strokeWidth = 10.dp,
                            trackColor = Color.Transparent,
                            strokeCap = StrokeCap.Round
                        )

                        // Completed segment (teal)
                        CircularProgressIndicator(
                            progress = { 0.72f },
                            modifier = Modifier.fillMaxSize(),
                            color = FeedbackCorrect,
                            strokeWidth = 10.dp,
                            trackColor = Color.Transparent,
                            strokeCap = StrokeCap.Round
                        )

                        // Due segment overlay (amber)
                        val dueRatio = if (totalDue > 0) (totalDue.coerceAtMost(35) / 35f) else 0.4f
                        CircularProgressIndicator(
                            progress = { dueRatio },
                            modifier = Modifier.fillMaxSize(),
                            color = AccentAmber,
                            strokeWidth = 10.dp,
                            trackColor = Color.Transparent,
                            strokeCap = StrokeCap.Round
                        )

                        // Inner Ring Core Information
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (totalDue > 0) "$totalDue" else "35",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "بطاقة جاهزة",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryEspresso
                            )
                            Text(
                                text = "للتثبيت السريع",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Category Breakdown Pills (Grid of 2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Due cards pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(AccentAmber)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "مستحقة",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceContainer)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${reviewCardsCount.coerceAtLeast(25)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryEspresso
                                    )
                                }
                            }
                        }

                        // New cards pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(FeedbackCorrect)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "جديدة",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceContainer)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${newCardsCount.coerceAtLeast(10)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryEspresso
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Session Length Duration Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مدة الجلسة المقترحة",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "تكرار متباعد ذكي",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option 1: 5 mins (12 cards)
                    SessionChip(
                        minutes = 5,
                        cards = 12,
                        isSelected = selectedSessionDuration == 5,
                        onClick = {
                            selectedSessionDuration = 5
                            selectedCardsCount = 12
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Option 2: 10 mins (25 cards)
                    SessionChip(
                        minutes = 10,
                        cards = 25,
                        isSelected = selectedSessionDuration == 10,
                        onClick = {
                            selectedSessionDuration = 10
                            selectedCardsCount = 25
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Option 3: 20 mins (35 cards)
                    SessionChip(
                        minutes = 20,
                        cards = 35,
                        isSelected = selectedSessionDuration == 20,
                        onClick = {
                            selectedSessionDuration = 20
                            selectedCardsCount = 35
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Primary Call To Action Button (Full Width Espresso)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(PrimaryEspresso)
                    .clickable(onClick = onStartReview)
                    .padding(horizontal = 18.dp, vertical = 14.dp)
                    .testTag("home_start_review_button")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = OnPrimaryWarm,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "ابدأ المراجعة الآن",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnPrimaryWarm
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$selectedCardsCount",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnPrimaryWarm
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "بطاقة",
                                fontSize = 11.sp,
                                color = OnPrimaryWarm
                            )
                        }
                    }
                }
            }

            // 5. Daily Goal & Retentive Memory Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = FeedbackCorrect,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "هدف اليوم: 80% مكتمل",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "18 / 20 كلمة",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentAmber
                        )
                    }

                    // Progress track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(SurfaceContainerHigh)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.80f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(FeedbackCorrect)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "كلمتان متبقيتان لختام الهدف",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Wort des Tages",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 6. Micro Word Discovery Spotlight
            val displayGerman = spotlightWord?.german ?: "Weg"
            val displayArticle = spotlightWord?.article?.name?.lowercase() ?: "der"
            val displayPhonetic = "[veːk]"
            val displayArabic = spotlightWord?.arabic ?: "الطريق، السبيل، المسار"

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(SurfaceContainerLow)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(22.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(ArticleDerSubtle)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = displayArticle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArticleDer
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = displayGerman,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = displayPhonetic,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = displayArabic,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            ttsManager.speak("$displayArticle $displayGerman")
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard)
                            .border(1.dp, BorderSubtle, CircleShape)
                            .testTag("home_spotlight_tts_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "استمع للنطق",
                            tint = PrimaryEspresso,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionChip(
    minutes: Int,
    cards: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) PrimaryEspresso else SurfaceCard)
            .border(
                1.dp,
                if (isSelected) PrimaryEspresso else BorderSubtle,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$minutes دقائق",
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) OnPrimaryWarm else TextPrimary
            )
            Text(
                text = "$cards بطاقة",
                fontSize = 10.sp,
                color = if (isSelected) OnPrimaryWarm.copy(alpha = 0.8f) else TextSecondary
            )
        }
    }
}
