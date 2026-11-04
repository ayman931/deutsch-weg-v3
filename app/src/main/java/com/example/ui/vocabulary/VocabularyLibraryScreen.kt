package com.example.ui.vocabulary

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Article
import com.example.data.model.LearningStatus
import com.example.data.model.VocabularyWord
import com.example.data.model.WordType
import com.example.data.model.WordWithReviewStates
import com.example.ui.components.LinguisticPills
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.ArticleDas
import com.example.ui.theme.ArticleDasSubtle
import com.example.ui.theme.ArticleDer
import com.example.ui.theme.ArticleDerSubtle
import com.example.ui.theme.ArticleDie
import com.example.ui.theme.ArticleDieSubtle
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
fun VocabularyLibraryScreen(
    viewModel: VocabularyViewModel,
    onNavigateToImport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    DisposableEffect(ttsManager) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    val filterState by viewModel.filterState.collectAsState()
    val wordsWithReviews by viewModel.wordsWithReviews.collectAsState()
    val topics by viewModel.allTopics.collectAsState()
    var wordToDelete by remember { mutableStateOf<VocabularyWord?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(WarmBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                // 1. Search Bar
                OutlinedTextField(
                    value = filterState.query,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = {
                        Text(
                            "ابحث عن كلمة ألمانية أو معنى...",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = TextSecondary
                        )
                    },
                    trailingIcon = {
                        if (filterState.query.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "مسح",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryEspresso,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("vocabulary_search_field")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Filter Bar: Topics Button + Horizontal Word Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Topics Filter Button
                    item {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                                .clickable {
                                    // Cycle or clear topic
                                    if (topics.isNotEmpty()) {
                                        val currentIndex = topics.indexOf(filterState.selectedTopic)
                                        val nextTopic = if (currentIndex < topics.size - 1) topics[currentIndex + 1] else null
                                        viewModel.onTopicSelected(nextTopic)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FilterAlt,
                                    contentDescription = null,
                                    tint = PrimaryEspresso,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (filterState.selectedTopic != null) filterState.selectedTopic!! else "المواضيع (${topics.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PrimaryEspresso
                                )
                            }
                        }
                    }

                    // Chip: All
                    item {
                        val isAll = filterState.selectedWordType == null
                        FilterChipItem(
                            label = "الكل",
                            isSelected = isAll,
                            onClick = { viewModel.onWordTypeSelected(null) }
                        )
                    }

                    // Word Type Chips
                    item {
                        val isSelected = filterState.selectedWordType == WordType.NOUN
                        FilterChipItem(
                            label = "أسماء (Nomen)",
                            isSelected = isSelected,
                            onClick = { viewModel.onWordTypeSelected(if (isSelected) null else WordType.NOUN) }
                        )
                    }
                    item {
                        val isSelected = filterState.selectedWordType == WordType.VERB
                        FilterChipItem(
                            label = "أفعال (Verben)",
                            isSelected = isSelected,
                            onClick = { viewModel.onWordTypeSelected(if (isSelected) null else WordType.VERB) }
                        )
                    }
                    item {
                        val isSelected = filterState.selectedWordType == WordType.ADJECTIVE
                        FilterChipItem(
                            label = "صفات (Adjektive)",
                            isSelected = isSelected,
                            onClick = { viewModel.onWordTypeSelected(if (isSelected) null else WordType.ADJECTIVE) }
                        )
                    }
                    item {
                        val isSelected = filterState.selectedWordType == WordType.PHRASE
                        FilterChipItem(
                            label = "حروف وعبارات",
                            isSelected = isSelected,
                            onClick = { viewModel.onWordTypeSelected(if (isSelected) null else WordType.PHRASE) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Library Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AccentAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مجموع الكلمات: ${wordsWithReviews.size} مفردة",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(FeedbackCorrect)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "متقنة",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(AccentAmber)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "قيد التدريب",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Vocabulary Cards List
                if (wordsWithReviews.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = PrimaryEspresso,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Text(
                                text = "لا توجد كلمات مطابقة",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "أضف كلمات جديدة من خلال الاستيراد السريع أو قم بتغيير كلمات البحث.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = onNavigateToImport,
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryEspresso,
                                    contentColor = OnPrimaryWarm
                                )
                            ) {
                                Text("استيراد كلمات جديدة", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(wordsWithReviews, key = { it.word.id }) { item ->
                            WarmVocabularyCardRow(
                                item = item,
                                onSpeak = {
                                    val art = item.word.article?.name?.lowercase()?.let { "$it " } ?: ""
                                    ttsManager.speak("$art${item.word.german}")
                                },
                                onDelete = { wordToDelete = item.word }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(84.dp))
                        }
                    }
                }
            }

            // 5. FAB (Primary Espresso)
            FloatingActionButton(
                onClick = onNavigateToImport,
                containerColor = PrimaryEspresso,
                contentColor = OnPrimaryWarm,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
                    .testTag("library_add_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "إضافة بطاقة جديدة",
                    modifier = Modifier.size(28.dp)
                )
            }

            // Delete Dialog
            if (wordToDelete != null) {
                val w = wordToDelete!!
                AlertDialog(
                    onDismissRequest = { wordToDelete = null },
                    title = { Text("حذف الكلمة؟", fontWeight = FontWeight.Bold) },
                    text = { Text("هل تريد بالتأكيد حذف '${w.german}' من المكتبة؟") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteWord(w)
                                wordToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("حذف")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { wordToDelete = null }) {
                            Text("إلغاء")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) PrimaryEspresso else SurfaceCard)
            .border(1.dp, if (isSelected) PrimaryEspresso else BorderSubtle, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) OnPrimaryWarm else TextSecondary
        )
    }
}

@Composable
fun WarmVocabularyCardRow(
    item: WordWithReviewStates,
    onSpeak: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val w = item.word
    val overallStatus = item.reviewStates.map { it.effectiveStatus }.minByOrNull { it.ordinal } ?: LearningStatus.NEW

    val articleColor = when (w.article) {
        Article.DER -> ArticleDer
        Article.DIE -> ArticleDie
        Article.DAS -> ArticleDas
        null -> PrimaryEspresso
    }
    val articleSubtleBg = when (w.article) {
        Article.DER -> ArticleDerSubtle
        Article.DIE -> ArticleDieSubtle
        Article.DAS -> ArticleDasSubtle
        null -> SurfaceContainerLow
    }

    val statusDotColor = when (overallStatus) {
        LearningStatus.MASTERED -> FeedbackCorrect
        LearningStatus.REVIEW -> ArticleDer
        LearningStatus.LEARNING, LearningStatus.RELEARNING -> AccentAmber
        LearningStatus.NEW -> Color(0xFFA89F95)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(26.dp))
            .clickable { isExpanded = !isExpanded }
            .padding(16.dp)
            .testTag("vocab_card_${w.id}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: German + Article + Phonetic (LTR)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (w.article != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(articleSubtleBg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = w.article.name.lowercase(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = articleColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = w.german,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            if (!w.plural.isNullOrBlank()) {
                                Text(
                                    text = "Pl: ${w.plural}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // Right: Arabic + Status Dot + CEFR Tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = w.arabic,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = w.topic,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(statusDotColor)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "A1",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                    }

                    // Pronounce Button
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "استمع للنطق",
                            tint = PrimaryEspresso,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Expanded Details View
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerLow)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!w.exampleSentence.isNullOrBlank()) {
                        Column {
                            Text("مثال توضيحي:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryEspresso)
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(w.exampleSentence, fontSize = 13.sp, color = TextPrimary)
                            }
                            if (!w.exampleArabic.isNullOrBlank()) {
                                Text(w.exampleArabic, fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }

                    LinguisticPills(word = w)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("حذف الكلمة", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
