package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.importing.ImportViewModel
import com.example.ui.importing.JsonImportScreen
import com.example.ui.review.FlashcardReviewScreen
import com.example.ui.review.FlashcardReviewViewModel
import com.example.ui.vocabulary.VocabularyLibraryScreen
import com.example.ui.vocabulary.VocabularyViewModel

enum class AppDestination(val label: String) {
    LIBRARY("Wortschatz"),
    REVIEW("Wiederholen"),
    IMPORT("JSON Import")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeutschWegApp(
    importViewModel: ImportViewModel,
    vocabularyViewModel: VocabularyViewModel,
    reviewViewModel: FlashcardReviewViewModel
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.IMPORT) }

    // When on secondary destinations, back button takes user back to LIBRARY (or previous screen)
    if (currentDestination != AppDestination.LIBRARY) {
        BackHandler {
            currentDestination = AppDestination.LIBRARY
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Deutsch Weg",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF16A34A))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Offline",
                                    color = Color(0xFF15803D),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = currentDestination == AppDestination.IMPORT,
                    onClick = { currentDestination = AppDestination.IMPORT },
                    icon = { Icon(Icons.Default.UploadFile, contentDescription = "JSON Import") },
                    label = { Text("Import") },
                    modifier = Modifier.testTag("nav_tab_import")
                )
                NavigationBarItem(
                    selected = currentDestination == AppDestination.LIBRARY,
                    onClick = { currentDestination = AppDestination.LIBRARY },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Wortschatz") },
                    label = { Text("Wortschatz") },
                    modifier = Modifier.testTag("nav_tab_library")
                )
                NavigationBarItem(
                    selected = currentDestination == AppDestination.REVIEW,
                    onClick = { currentDestination = AppDestination.REVIEW },
                    icon = { Icon(Icons.Default.School, contentDescription = "Wiederholen") },
                    label = { Text("Lernen") },
                    modifier = Modifier.testTag("nav_tab_review")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AppDestination.IMPORT -> {
                    JsonImportScreen(
                        viewModel = importViewModel,
                        onNavigateToLibrary = { currentDestination = AppDestination.LIBRARY }
                    )
                }
                AppDestination.LIBRARY -> {
                    VocabularyLibraryScreen(
                        viewModel = vocabularyViewModel,
                        onNavigateToImport = { currentDestination = AppDestination.IMPORT }
                    )
                }
                AppDestination.REVIEW -> {
                    FlashcardReviewScreen(
                        viewModel = reviewViewModel,
                        onNavigateToImport = { currentDestination = AppDestination.IMPORT }
                    )
                }
            }
        }
    }
}
