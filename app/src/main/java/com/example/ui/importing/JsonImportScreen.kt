package com.example.ui.importing

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.importer.ImportEntryPreview
import com.example.data.importer.ImportEntryStatus
import com.example.data.importer.ImportPreview
import com.example.ui.components.ArticleBadge
import com.example.ui.components.LinguisticPills
import com.example.ui.components.StatusBadge
import com.example.ui.components.WordTypeBadge

@Composable
fun JsonImportScreen(
    viewModel: ImportViewModel,
    onNavigateToLibrary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.loadFromFile(uri, context.contentResolver)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.preview != null) {
            ImportPreviewContent(
                preview = uiState.preview!!,
                filter = uiState.filter,
                onFilterChanged = viewModel::onFilterChanged,
                onToggleEntry = viewModel::toggleEntrySelection,
                onInspectEntry = viewModel::inspectEntry,
                onSelectAllValid = viewModel::selectAllValid,
                onSkipDuplicates = viewModel::skipAllDuplicates,
                onIncludeDistinctMeanings = viewModel::includeAllDistinctMeanings,
                onDeselectAll = viewModel::deselectAll,
                onClearPreview = viewModel::clearPreview,
                onRequestCommit = { viewModel.setConfirmDialogVisible(true) },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            ImportInputContent(
                rawInput = uiState.rawJsonInput,
                onInputChange = viewModel::onJsonInputChanged,
                onValidate = { viewModel.validateJson() },
                onSelectFile = {
                    filePickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                },
                onLoadSample = viewModel::loadSampleAiJson,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Inspection Dialog
        if (uiState.inspectedEntry != null) {
            EntryInspectionDialog(
                entry = uiState.inspectedEntry!!,
                onDismiss = { viewModel.inspectEntry(null) },
                onToggleSelection = {
                    viewModel.toggleEntrySelection(uiState.inspectedEntry!!.id)
                    viewModel.inspectEntry(null)
                }
            )
        }

        // Commit Confirmation Dialog
        if (uiState.showConfirmDialog && uiState.preview != null) {
            ImportConfirmationDialog(
                preview = uiState.preview!!,
                onConfirm = viewModel::commitImport,
                onDismiss = { viewModel.setConfirmDialogVisible(false) }
            )
        }

        // Success Dialog
        if (uiState.commitResult != null) {
            val result = uiState.commitResult!!
            AlertDialog(
                onDismissRequest = viewModel::clearCommitResult,
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = { Text("Import erfolgreich abgeschlossen") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(result.message)
                        Text(
                            "• Importierte Wörter: ${result.importedCount}\n" +
                                    "• Übersprungene Duplikate: ${result.skippedDuplicatesCount}\n" +
                                    "• Ungültige Einträge: ${result.failedCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Alle Wörter wurden offline in der lokalen Datenbank gespeichert und sind bereit für das Karteikarten-Training.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearCommitResult()
                            onNavigateToLibrary()
                        },
                        modifier = Modifier.testTag("success_goto_library_button")
                    ) {
                        Text("Zum Wortschatz")
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::clearCommitResult) {
                        Text("Schließen")
                    }
                }
            )
        }
    }
}

@Composable
fun ImportInputContent(
    rawInput: String,
    onInputChange: (String) -> Unit,
    onValidate: () -> Unit,
    onSelectFile: () -> Unit,
    onLoadSample: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "JSON Vokabelimport",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Importieren Sie strukturierte deutsche Vokabeln aus KI-Assistenten direkt in Deutsch Weg.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Unterstützt JSON-Array mit Feldern: german, wordType, article, plural, praeteritum, perfekt, isSeparable, isReflexive, rektion, arabic, exampleSentence, exampleArabic, topic.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        ScrollableTabRow(
            selectedTabIndex = activeTab,
            edgePadding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("JSON einfügen") },
                icon = { Icon(Icons.Default.ContentPaste, contentDescription = null) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Datei auswählen (.json)") },
                icon = { Icon(Icons.Default.FileOpen, contentDescription = null) }
            )
        }

        if (activeTab == 0) {
            // Text Paste Editor
            OutlinedTextField(
                value = rawInput,
                onValueChange = onInputChange,
                placeholder = {
                    Text(
                        "[\n  {\n    \"german\": \"entscheiden\",\n    \"wordType\": \"VERB\",\n    \"arabic\": \"يقرر\",\n    \"topic\": \"Alltag\"\n  }\n]",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .testTag("import_paste_input"),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onLoadSample,
                    modifier = Modifier.testTag("import_sample_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Beispiel-JSON laden")
                }

                if (rawInput.isNotBlank()) {
                    TextButton(onClick = { onInputChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Löschen")
                    }
                }
            }
        } else {
            // Document Picker Option
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FileOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Wählen Sie eine .json Datei von Ihrem Gerät",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Dokumente und Downloads werden unterstützt",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onSelectFile,
                        modifier = Modifier.testTag("import_pick_file_button")
                    ) {
                        Text("JSON Datei durchsuchen...")
                    }
                }
            }
        }

        if (!errorMessage.isNullOrBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onValidate,
            enabled = !isLoading && rawInput.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("import_validate_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Validierung läuft...")
            } else {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Validieren & Vorschau anzeigen", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportPreviewContent(
    preview: ImportPreview,
    filter: ImportFilter,
    onFilterChanged: (ImportFilter) -> Unit,
    onToggleEntry: (String) -> Unit,
    onInspectEntry: (ImportEntryPreview) -> Unit,
    onSelectAllValid: () -> Unit,
    onSkipDuplicates: () -> Unit,
    onIncludeDistinctMeanings: () -> Unit,
    onDeselectAll: () -> Unit,
    onClearPreview: () -> Unit,
    onRequestCommit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredEntries = when (filter) {
        ImportFilter.ALL -> preview.entries
        ImportFilter.VALID_ONLY -> preview.entries.filter { it.status == ImportEntryStatus.VALID || it.status == ImportEntryStatus.DIFFERENT_MEANING }
        ImportFilter.DUPLICATES -> preview.entries.filter { it.status == ImportEntryStatus.DUPLICATE_EXACT }
        ImportFilter.INVALID -> preview.entries.filter { it.status == ImportEntryStatus.INVALID }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Top action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Import-Vorschau",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${preview.selectedCount} von ${preview.totalCount} zum Import ausgewählt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onClearPreview) {
                Text("Zurück zum Editor")
            }
        }

        // Summary Stats Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(label = "Gesamt", value = preview.totalCount, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            StatCard(label = "Gültig", value = preview.validCount, color = Color(0xFF16A34A), modifier = Modifier.weight(1f))
            StatCard(label = "Duplikate", value = preview.duplicateCount, color = Color(0xFFD97706), modifier = Modifier.weight(1f))
            StatCard(label = "Ungültig", value = preview.invalidCount, color = Color(0xFFDC2626), modifier = Modifier.weight(1f))
        }

        // Filter chips
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ImportFilter.values().forEach { f ->
                val count = when (f) {
                    ImportFilter.ALL -> preview.totalCount
                    ImportFilter.VALID_ONLY -> preview.validCount
                    ImportFilter.DUPLICATES -> preview.duplicateCount
                    ImportFilter.INVALID -> preview.invalidCount
                }
                FilterChip(
                    selected = filter == f,
                    onClick = { onFilterChanged(f) },
                    label = { Text("${f.label} ($count)") }
                )
            }
        }

        // Batch toggles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TextButton(onClick = onSelectAllValid) {
                Text("Alle gültigen", fontSize = 12.sp)
            }
            if (preview.duplicateCount > 0) {
                TextButton(onClick = onSkipDuplicates) {
                    Text("Duplikate auslassen", fontSize = 12.sp)
                }
            }
            TextButton(onClick = onDeselectAll) {
                Text("Alle abwählen", fontSize = 12.sp)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))

        // Entries List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredEntries, key = { it.id }) { entry ->
                ImportEntryItemCard(
                    entry = entry,
                    onToggle = { onToggleEntry(entry.id) },
                    onInspect = { onInspectEntry(entry) }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Bottom commit bar
        Card(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${preview.selectedCount} Wörter ausgewählt",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Bereit für Offline-Speicherung",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onRequestCommit,
                    enabled = preview.selectedCount > 0,
                    modifier = Modifier.testTag("import_confirm_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Importieren (${preview.selectedCount})")
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$value", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
            Text(text = label, fontSize = 11.sp, color = color.copy(alpha = 0.9f))
        }
    }
}

@Composable
fun ImportEntryItemCard(
    entry: ImportEntryPreview,
    onToggle: () -> Unit,
    onInspect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (entry.status) {
                ImportEntryStatus.INVALID -> Color(0xFFFEF2F2)
                ImportEntryStatus.DUPLICATE_EXACT -> Color(0xFFFFFBEB)
                ImportEntryStatus.DIFFERENT_MEANING -> Color(0xFFF0F9FF)
                ImportEntryStatus.VALID -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInspect() }
            .testTag("entry_item_${entry.index}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = entry.isSelectedForImport,
                onCheckedChange = { onToggle() },
                enabled = entry.word != null,
                modifier = Modifier.testTag("entry_checkbox_${entry.index}")
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "#${entry.index}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (entry.word != null) {
                        ArticleBadge(article = entry.word.article)
                        Text(
                            text = entry.word.german,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        WordTypeBadge(wordType = entry.word.wordType)
                    } else {
                        Text(
                            text = "Ungültiger Eintrag",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    StatusBadge(status = entry.status)
                }

                if (entry.word != null) {
                    // Arabic Translation with explicit RTL direction
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(
                            text = entry.word.arabic,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        )
                    }

                    LinguisticPills(word = entry.word, modifier = Modifier.padding(top = 2.dp))
                }

                // Show warnings
                if (entry.warnings.isNotEmpty()) {
                    entry.warnings.forEach { warning ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = warning,
                                fontSize = 11.sp,
                                color = Color(0xFFB45309),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Show errors
                if (entry.errors.isNotEmpty()) {
                    entry.errors.forEach { err ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = err,
                                fontSize = 11.sp,
                                color = Color(0xFFB91C1C)
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onInspect) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Details ansehen",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EntryInspectionDialog(
    entry: ImportEntryPreview,
    onDismiss: () -> Unit,
    onToggleSelection: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eintrag #${entry.index}")
                StatusBadge(status = entry.status)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (entry.word != null) {
                    val w = entry.word

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ArticleBadge(article = w.article)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = w.german,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                WordTypeBadge(wordType = w.wordType)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Text(
                                    text = w.arabic,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Linguistic Details Table
                    Text("Linguistische Details:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    DetailRow(label = "Wortart", value = w.wordType.displayNameDe)
                    if (w.article != null) DetailRow(label = "Artikel", value = w.article.value)
                    if (!w.plural.isNullOrBlank()) DetailRow(label = "Plural", value = w.plural)
                    if (!w.praeteritum.isNullOrBlank()) DetailRow(label = "Präteritum", value = w.praeteritum)
                    if (!w.perfekt.isNullOrBlank()) DetailRow(label = "Perfekt", value = w.perfekt)
                    if (w.isSeparable) DetailRow(label = "Trennbar", value = "Ja")
                    if (w.isReflexive) DetailRow(label = "Reflexiv", value = "Ja (sich)")
                    if (!w.rektion.isNullOrBlank()) DetailRow(label = "Rektion", value = w.rektion)
                    DetailRow(label = "Thema / Topic", value = w.topic)

                    // Examples
                    if (!w.exampleSentence.isNullOrBlank()) {
                        Text("Beispielsatz (Deutsch):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = w.exampleSentence,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    if (!w.exampleArabic.isNullOrBlank()) {
                        Text("Beispielsatz (Arabisch):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Text(
                                text = w.exampleArabic,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Right,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 4.dp)
                            )
                        }
                    }
                }

                if (entry.errors.isNotEmpty()) {
                    Text("Validierungsfehler:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    entry.errors.forEach { err ->
                        Text("• $err", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }

                if (entry.warnings.isNotEmpty()) {
                    Text("Hinweise / Duplikate:", fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                    entry.warnings.forEach { warn ->
                        Text("• $warn", color = Color(0xFFB45309), fontSize = 12.sp)
                    }
                }

                Text("Rohdaten (JSON):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F172A))
                        .padding(8.dp)
                ) {
                    Text(
                        text = entry.rawPreview,
                        color = Color(0xFFE2E8F0),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }
        },
        confirmButton = {
            if (entry.word != null) {
                Button(onClick = onToggleSelection) {
                    Text(if (entry.isSelectedForImport) "Vom Import abwählen" else "Für Import auswählen")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ImportConfirmationDialog(
    preview: ImportPreview,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val selectedCount = preview.selectedCount
    val skippedCount = preview.totalCount - selectedCount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import bestätigen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Möchten Sie $selectedCount Vokabeleinträge in Deutsch Weg importieren?",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (skippedCount > 0) {
                    Text(
                        text = "• $skippedCount Einträge werden übersprungen (Duplikate oder abgewählt).",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD97706)
                    )
                }
                Text(
                    text = "• Jedes Wort erhält automatisch zwei adaptive Lernrichtungen: Deutsch → Arabisch und Arabisch → Deutsch.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• Alle Wörter sind sofort offline verfügbar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.testTag("import_commit_dialog_button")
            ) {
                Text("Jetzt $selectedCount Wörter importieren")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
