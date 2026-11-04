package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.database.AppDatabase
import com.example.data.repository.SettingsRepository
import com.example.data.repository.VocabularyRepositoryImpl
import com.example.ui.importing.ImportViewModel
import com.example.ui.navigation.DeutschWegApp
import com.example.ui.progress.ProgressViewModel
import com.example.ui.review.FlashcardReviewViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.vocabulary.VocabularyViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = VocabularyRepositoryImpl(
            vocabularyDao = database.vocabularyDao(),
            reviewStateDao = database.reviewStateDao()
        )
        val settingsRepository = SettingsRepository(applicationContext)

        val importViewModel by viewModels<ImportViewModel> {
            ImportViewModel.Factory(repository)
        }
        val vocabularyViewModel by viewModels<VocabularyViewModel> {
            VocabularyViewModel.Factory(repository)
        }
        val reviewViewModel by viewModels<FlashcardReviewViewModel> {
            FlashcardReviewViewModel.Factory(repository, settingsRepository)
        }
        val progressViewModel by viewModels<ProgressViewModel> {
            ProgressViewModel.Factory(repository)
        }

        setContent {
            MyApplicationTheme {
                DeutschWegApp(
                    importViewModel = importViewModel,
                    vocabularyViewModel = vocabularyViewModel,
                    reviewViewModel = reviewViewModel,
                    progressViewModel = progressViewModel,
                    settingsRepository = settingsRepository,
                    vocabularyRepository = repository
                )
            }
        }
    }
}
