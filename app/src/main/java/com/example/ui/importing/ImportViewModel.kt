package com.example.ui.importing

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.importer.ImportCommitResult
import com.example.data.importer.ImportEntryPreview
import com.example.data.importer.ImportEntryStatus
import com.example.data.importer.ImportPreview
import com.example.data.importer.VocabularyJsonImporter
import com.example.data.repository.VocabularyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

enum class ImportFilter(val label: String) {
    ALL("Alle"),
    VALID_ONLY("Gültig"),
    DUPLICATES("Duplikate"),
    INVALID("Ungültig")
}

data class ImportUiState(
    val rawJsonInput: String = "",
    val preview: ImportPreview? = null,
    val isLoading: Boolean = false,
    val filter: ImportFilter = ImportFilter.ALL,
    val inspectedEntry: ImportEntryPreview? = null,
    val showConfirmDialog: Boolean = false,
    val commitResult: ImportCommitResult? = null,
    val errorMessage: String? = null
)

class ImportViewModel(
    private val repository: VocabularyRepository,
    private val importer: VocabularyJsonImporter = VocabularyJsonImporter()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun onJsonInputChanged(text: String) {
        _uiState.update { it.copy(rawJsonInput = text, errorMessage = null) }
    }

    fun onFilterChanged(filter: ImportFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun inspectEntry(entry: ImportEntryPreview?) {
        _uiState.update { it.copy(inspectedEntry = entry) }
    }

    fun setConfirmDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showConfirmDialog = visible) }
    }

    fun clearCommitResult() {
        _uiState.update { it.copy(commitResult = null) }
    }

    fun clearPreview() {
        _uiState.update {
            it.copy(
                preview = null,
                inspectedEntry = null,
                showConfirmDialog = false,
                commitResult = null,
                errorMessage = null
            )
        }
    }

    fun validateJson(customInput: String? = null) {
        val inputToParse = customInput ?: _uiState.value.rawJsonInput
        if (inputToParse.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Bitte fügen Sie JSON ein oder wählen Sie eine Datei aus."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val existingWords = repository.getAllWordsSnapshot()
                val preview = withContext(Dispatchers.Default) {
                    importer.parseAndValidate(inputToParse, existingWords)
                }
                _uiState.update {
                    it.copy(
                        preview = preview,
                        isLoading = false,
                        errorMessage = preview.globalError
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Fehler bei der Validierung: ${e.localizedMessage ?: "Unbekannter Fehler"}"
                    )
                }
            }
        }
    }

    fun loadFromFile(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val fileContent = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            reader.readText()
                        }
                    } ?: throw IllegalStateException("Datei konnte nicht geöffnet werden.")
                }
                _uiState.update { it.copy(rawJsonInput = fileContent) }
                validateJson(fileContent)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Dateizugriffsfehler: ${e.localizedMessage ?: "Datei nicht lesbar"}"
                    )
                }
            }
        }
    }

    fun toggleEntrySelection(entryId: String) {
        _uiState.update { state ->
            val prev = state.preview ?: return@update state
            val updatedEntries = prev.entries.map { entry ->
                if (entry.id == entryId && entry.word != null) {
                    entry.copy(isSelectedForImport = !entry.isSelectedForImport)
                } else {
                    entry
                }
            }
            state.copy(preview = prev.copy(entries = updatedEntries))
        }
    }

    fun selectAllValid() {
        _uiState.update { state ->
            val prev = state.preview ?: return@update state
            val updated = prev.entries.map { entry ->
                if (entry.status == ImportEntryStatus.VALID || entry.status == ImportEntryStatus.DIFFERENT_MEANING) {
                    entry.copy(isSelectedForImport = true)
                } else {
                    entry.copy(isSelectedForImport = false)
                }
            }
            state.copy(preview = prev.copy(entries = updated))
        }
    }

    fun skipAllDuplicates() {
        _uiState.update { state ->
            val prev = state.preview ?: return@update state
            val updated = prev.entries.map { entry ->
                if (entry.status == ImportEntryStatus.DUPLICATE_EXACT) {
                    entry.copy(isSelectedForImport = false)
                } else {
                    entry
                }
            }
            state.copy(preview = prev.copy(entries = updated))
        }
    }

    fun includeAllDistinctMeanings() {
        _uiState.update { state ->
            val prev = state.preview ?: return@update state
            val updated = prev.entries.map { entry ->
                if (entry.status == ImportEntryStatus.DIFFERENT_MEANING && entry.word != null) {
                    entry.copy(isSelectedForImport = true)
                } else {
                    entry
                }
            }
            state.copy(preview = prev.copy(entries = updated))
        }
    }

    fun deselectAll() {
        _uiState.update { state ->
            val prev = state.preview ?: return@update state
            val updated = prev.entries.map { it.copy(isSelectedForImport = false) }
            state.copy(preview = prev.copy(entries = updated))
        }
    }

    fun commitImport() {
        val currentPreview = _uiState.value.preview ?: return
        val selectedWords = currentPreview.entries
            .filter { it.isSelectedForImport && it.word != null }
            .mapNotNull { it.word }

        if (selectedWords.isEmpty()) {
            _uiState.update {
                it.copy(
                    showConfirmDialog = false,
                    errorMessage = "Keine Einträge zum Importieren ausgewählt."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showConfirmDialog = false) }
            try {
                val insertedIds = repository.batchImport(selectedWords)
                val skippedDuplicates = currentPreview.entries.count {
                    it.status == ImportEntryStatus.DUPLICATE_EXACT && !it.isSelectedForImport
                }
                val failedCount = currentPreview.invalidCount

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        preview = null,
                        rawJsonInput = "",
                        commitResult = ImportCommitResult(
                            importedCount = insertedIds.size,
                            skippedDuplicatesCount = skippedDuplicates,
                            failedCount = failedCount,
                            message = "Erfolgreich ${insertedIds.size} Wörter importiert und für die Wiederholung vorbereitet!"
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Fehler beim Speichern in der Datenbank: ${e.localizedMessage ?: "Transaktionsfehler"}"
                    )
                }
            }
        }
    }

    fun loadSampleAiJson() {
        val sample = """
[
  {
    "german": "entscheiden",
    "wordType": "VERB",
    "article": null,
    "plural": null,
    "praeteritum": "entschied",
    "perfekt": "hat entschieden",
    "isSeparable": false,
    "isReflexive": true,
    "rektion": "über + Akk",
    "arabic": "يقرر / يحسم",
    "exampleSentence": "Ich muss mich bald entscheiden.",
    "exampleArabic": "يجب علي أن أقرر قريباً.",
    "topic": "Alltag"
  },
  {
    "german": "Entscheidung",
    "wordType": "NOUN",
    "article": "die",
    "plural": "die Entscheidungen",
    "praeteritum": null,
    "perfekt": null,
    "isSeparable": false,
    "isReflexive": false,
    "rektion": null,
    "arabic": "قرار",
    "exampleSentence": "Das war eine schwierige Entscheidung.",
    "exampleArabic": "كان ذلك قراراً صعباً.",
    "topic": "Alltag"
  },
  {
    "german": "zuverlässig",
    "wordType": "ADJECTIVE",
    "article": null,
    "plural": null,
    "praeteritum": null,
    "perfekt": null,
    "isSeparable": false,
    "isReflexive": false,
    "rektion": null,
    "arabic": "موثوق / يعتمد عليه",
    "exampleSentence": "Mein Kollege ist sehr zuverlässig.",
    "exampleArabic": "زميلي موثوق جداً.",
    "topic": "Arbeit"
  },
  {
    "german": "abfahren",
    "wordType": "VERB",
    "article": null,
    "plural": null,
    "praeteritum": "fuhr ab",
    "perfekt": "ist abgefahren",
    "isSeparable": true,
    "isReflexive": false,
    "rektion": null,
    "arabic": "ينطلق / يغادر",
    "exampleSentence": "Der Zug fährt um neun Uhr ab.",
    "exampleArabic": "ينطلق القطار في الساعة التاسعة.",
    "topic": "Reisen"
  },
  {
    "german": "Buch",
    "wordType": "NOUN",
    "article": "das",
    "plural": "die Bücher",
    "praeteritum": null,
    "perfekt": null,
    "isSeparable": false,
    "isReflexive": false,
    "rektion": null,
    "arabic": "كتاب",
    "exampleSentence": "Ich lese ein interessantes Buch.",
    "exampleArabic": "أنا أقرأ كتاباً ممتعاً.",
    "topic": "Bildung"
  }
]
""".trimIndent()
        onJsonInputChanged(sample)
        validateJson(sample)
    }

    class Factory(private val repository: VocabularyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ImportViewModel(repository) as T
        }
    }
}
