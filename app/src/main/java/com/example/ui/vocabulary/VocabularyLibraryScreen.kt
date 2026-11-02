package com.example.ui.vocabulary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LearningStatus
import com.example.data.model.VocabularyWord
import com.example.data.model.WordType
import com.example.data.model.WordWithReviewStates
import com.example.ui.components.ArticleBadge
import com.example.ui.components.LearningStatusBadge
import com.example.ui.components.LinguisticPills
import com.example.ui.components.WordTypeBadge

@Composable
fun VocabularyLibraryScreen(
    viewModel: VocabularyViewModel,
    onNavigateToImport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val filterState by viewModel.filterState.collectAsState()
    val wordsWithReviews by viewModel.wordsWithReviews.collectAsState()
    val topics by viewModel.allTopics.collectAsState()
    var wordToDelete by remember { mutableStateOf<VocabularyWord?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // Search & Filters Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = filterState.query,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("Wortschatz durchsuchen (Deutsch oder Arabisch)...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (filterState.query.isNotBlank()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Löschen")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vocabulary_search_field")
            )

            // Topics horizontal chips
            if (topics.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filterState.selectedTopic == null,
                            onClick = { viewModel.onTopicSelected(null) },
                            label = { Text("Alle Themen") }
                        )
                    }
                    items(topics) { topic ->
                        FilterChip(
                            selected = filterState.selectedTopic == topic,
                            onClick = { viewModel.onTopicSelected(topic) },
                            label = { Text(topic) }
                        )
                    }
                }
            }

            // Word types horizontal chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    FilterChip(
                        selected = filterState.selectedWordType == null,
                        onClick = { viewModel.onWordTypeSelected(null) },
                        label = { Text("Alle Wortarten") }
                    )
                }
                items(WordType.values()) { type ->
                    FilterChip(
                        selected = filterState.selectedWordType == type,
                        onClick = { viewModel.onWordTypeSelected(type) },
                        label = { Text(type.displayNameDe) }
                    )
                }
            }
        }

        // Header Count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${wordsWithReviews.size} Wörter gefunden",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (wordsWithReviews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = if (filterState.query.isNotBlank() || filterState.selectedTopic != null) {
                            "Keine passenden Vokabeln gefunden."
                        } else {
                            "Noch keine Vokabeln vorhanden."
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Importieren Sie eine JSON-Vokabelliste aus einem KI-Assistenten, um sofort zu starten.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onNavigateToImport,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Vokabeln importieren")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(wordsWithReviews, key = { it.word.id }) { item ->
                    WordItemCard(
                        item = item,
                        onDelete = { wordToDelete = item.word }
                    )
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }

        // Delete Confirmation Dialog
        if (wordToDelete != null) {
            val w = wordToDelete!!
            AlertDialog(
                onDismissRequest = { wordToDelete = null },
                title = { Text("Wort löschen?") },
                text = { Text("Möchten Sie '${w.german}' wirklich aus Ihrem Wortschatz entfernen?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteWord(w)
                            wordToDelete = null
                        }
                    ) {
                        Text("Löschen")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { wordToDelete = null }) {
                        Text("Abbrechen")
                    }
                }
            )
        }
    }
}

@Composable
fun WordItemCard(
    item: WordWithReviewStates,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val w = item.word

    // Determine overall learning status from review states
    val overallStatus = item.reviewStates.map { it.effectiveStatus }.minByOrNull { it.ordinal } ?: LearningStatus.NEW

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Article, German Word, WordType, Learning Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ArticleBadge(article = w.article)
                Text(
                    text = w.german,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                WordTypeBadge(wordType = w.wordType)
                LearningStatusBadge(status = overallStatus)
            }

            // Arabic Translation (RTL)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = w.arabic,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            // Linguistic pills summary
            LinguisticPills(word = w, modifier = Modifier.padding(top = 2.dp))

            // Expandable details (examples, conjugations, delete)
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    if (!w.exampleSentence.isNullOrBlank()) {
                        Text(
                            text = "Beispiel:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = w.exampleSentence,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    if (!w.exampleArabic.isNullOrBlank()) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Text(
                                text = w.exampleArabic,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Right,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Erstellt: Offline verfügbar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Wort löschen",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
