package com.example.data.importer

import com.example.data.model.VocabularyWord
import java.util.UUID

enum class ImportEntryStatus {
    VALID,
    DUPLICATE_EXACT,
    DIFFERENT_MEANING,
    INVALID
}

data class ImportEntryPreview(
    val id: String = UUID.randomUUID().toString(),
    val index: Int,
    val status: ImportEntryStatus,
    val word: VocabularyWord?,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val isSelectedForImport: Boolean = status == ImportEntryStatus.VALID || status == ImportEntryStatus.DIFFERENT_MEANING,
    val rawPreview: String = ""
)

data class ImportPreview(
    val totalCount: Int = 0,
    val validCount: Int = 0,
    val duplicateCount: Int = 0,
    val invalidCount: Int = 0,
    val entries: List<ImportEntryPreview> = emptyList(),
    val globalError: String? = null
) {
    val selectedCount: Int
        get() = entries.count { it.isSelectedForImport && it.word != null }

    val hasEntries: Boolean
        get() = entries.isNotEmpty()
}

data class ImportCommitResult(
    val importedCount: Int,
    val skippedDuplicatesCount: Int,
    val failedCount: Int,
    val message: String
)
