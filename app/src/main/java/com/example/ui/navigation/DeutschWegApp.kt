package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import com.example.data.repository.SettingsRepository
import com.example.data.repository.VocabularyRepository
import com.example.ui.home.HomeScreen
import com.example.ui.importing.ImportViewModel
import com.example.ui.importing.JsonImportScreen
import com.example.ui.progress.ProgressScreen
import com.example.ui.progress.ProgressViewModel
import com.example.ui.review.FlashcardReviewScreen
import com.example.ui.review.FlashcardReviewViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.OnPrimaryWarm
import com.example.ui.theme.PrimaryEspresso
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmBackground
import com.example.ui.vocabulary.VocabularyLibraryScreen
import com.example.ui.vocabulary.VocabularyViewModel

enum class AppDestination(val labelAr: String, val titleEn: String) {
    HOME("الرئيسية", "Home"),
    LIBRARY("المكتبة", "Library"),
    PROGRESS("التقدم", "Progress"),
    REVIEW("جلسة المراجعة", "Review"),
    SETTINGS("الإعدادات", "Settings"),
    IMPORT("استيراد كلمات", "Import")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeutschWegApp(
    importViewModel: ImportViewModel,
    vocabularyViewModel: VocabularyViewModel,
    reviewViewModel: FlashcardReviewViewModel,
    progressViewModel: ProgressViewModel,
    settingsRepository: SettingsRepository,
    vocabularyRepository: VocabularyRepository
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.HOME) }

    // When on secondary destinations, back button takes user back to HOME
    if (currentDestination != AppDestination.HOME) {
        BackHandler {
            currentDestination = AppDestination.HOME
        }
    }

    Scaffold(
        containerColor = WarmBackground,
        topBar = {
            if (currentDestination != AppDestination.REVIEW) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceContainer)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = null,
                                    tint = PrimaryEspresso,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "طريق الألمانية",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SurfaceContainerHigh)
                                            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Deutsch Weg",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Text(
                                    text = currentDestination.titleEn,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { currentDestination = AppDestination.SETTINGS },
                            modifier = Modifier.testTag("appbar_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "الإعدادات",
                                tint = TextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PrimaryEspresso),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = OnPrimaryWarm,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = WarmBackground
                    )
                )
            }
        },
        bottomBar = {
            if (currentDestination != AppDestination.REVIEW && currentDestination != AppDestination.SETTINGS) {
                NavigationBar(
                    containerColor = WarmBackground,
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(width = 1.dp, color = BorderSubtle)
                ) {
                    // 1. Al Raeesia (Home)
                    NavigationBarItem(
                        selected = currentDestination == AppDestination.HOME,
                        onClick = { currentDestination = AppDestination.HOME },
                        icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                        label = { Text("الرئيسية", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OnPrimaryWarm,
                            selectedTextColor = PrimaryEspresso,
                            indicatorColor = PrimaryEspresso,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_home")
                    )

                    // 2. Al Maktaba (Library)
                    NavigationBarItem(
                        selected = currentDestination == AppDestination.LIBRARY,
                        onClick = { currentDestination = AppDestination.LIBRARY },
                        icon = { Icon(Icons.Default.LocalLibrary, contentDescription = "المكتبة") },
                        label = { Text("المكتبة", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OnPrimaryWarm,
                            selectedTextColor = PrimaryEspresso,
                            indicatorColor = PrimaryEspresso,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_library")
                    )

                    // 3. Al Taqaddum (Progress)
                    NavigationBarItem(
                        selected = currentDestination == AppDestination.PROGRESS,
                        onClick = { currentDestination = AppDestination.PROGRESS },
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = "التقدم") },
                        label = { Text("التقدم", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OnPrimaryWarm,
                            selectedTextColor = PrimaryEspresso,
                            indicatorColor = PrimaryEspresso,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_progress")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(WarmBackground)
        ) {
            when (currentDestination) {
                AppDestination.HOME -> {
                    HomeScreen(
                        vocabularyRepository = vocabularyRepository,
                        reviewViewModel = reviewViewModel,
                        settingsRepository = settingsRepository,
                        onStartReview = {
                            reviewViewModel.restartSession()
                            currentDestination = AppDestination.REVIEW
                        },
                        onOpenSettings = { currentDestination = AppDestination.SETTINGS }
                    )
                }
                AppDestination.LIBRARY -> {
                    VocabularyLibraryScreen(
                        viewModel = vocabularyViewModel,
                        onNavigateToImport = { currentDestination = AppDestination.IMPORT }
                    )
                }
                AppDestination.PROGRESS -> {
                    ProgressScreen(
                        viewModel = progressViewModel
                    )
                }
                AppDestination.REVIEW -> {
                    FlashcardReviewScreen(
                        viewModel = reviewViewModel,
                        onNavigateToImport = { currentDestination = AppDestination.IMPORT },
                        onBack = { currentDestination = AppDestination.HOME }
                    )
                }
                AppDestination.SETTINGS -> {
                    SettingsScreen(
                        settingsRepository = settingsRepository,
                        vocabularyRepository = vocabularyRepository,
                        onBack = { currentDestination = AppDestination.HOME }
                    )
                }
                AppDestination.IMPORT -> {
                    JsonImportScreen(
                        viewModel = importViewModel,
                        onNavigateToLibrary = { currentDestination = AppDestination.LIBRARY }
                    )
                }
            }
        }
    }
}
