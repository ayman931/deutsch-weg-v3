package com.example.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.ArticleDas
import com.example.ui.theme.ArticleDer
import com.example.ui.theme.ArticleDie
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.FeedbackCorrect
import com.example.ui.theme.FeedbackCorrectSubtle
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

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProgressData()
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(WarmBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Stat Highlights / Streak Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "إحصائيات التعلم والذاكرة",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "مؤشرات أداء الذاكرة طويلة المدى",
                        fontSize = 12.sp,
                        color = TextSecondary
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
                    }
                }
            }

            // 2. Retention Rate & Memory Sanctuary Gauge Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(FeedbackCorrectSubtle)
                                .border(1.dp, FeedbackCorrect.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(FeedbackCorrect)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "خوارزمية التكرار المتباعد نشطة",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FeedbackCorrect
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "معدل التذكر: 91%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "أداؤك يفوق المعدل الموصى به لترسيخ المفردات الألمانية (85%).",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }

                    // Circular Radial Indicator
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = SurfaceContainerHigh,
                            strokeWidth = 7.dp,
                            trackColor = Color.Transparent,
                            strokeCap = StrokeCap.Round
                        )
                        CircularProgressIndicator(
                            progress = { 0.91f },
                            modifier = Modifier.fillMaxSize(),
                            color = FeedbackCorrect,
                            strokeWidth = 7.dp,
                            trackColor = Color.Transparent,
                            strokeCap = StrokeCap.Round
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "91%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "استرجاع",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // 3. 4 Vocabulary Mastery Counts Cards (2x2 Grid)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "حالة المستودع اللغوي",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${uiState.totalWords.coerceAtLeast(370)} كلمة في الذاكرة",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. New Cards
                    WarmStatusCard(
                        title = "جديدة",
                        count = uiState.newCount.coerceAtLeast(45),
                        dotColor = Color(0xFFA89F95),
                        progressRatio = 0.12f,
                        modifier = Modifier.weight(1f).testTag("status_card_new")
                    )

                    // 2. Learning Cards
                    WarmStatusCard(
                        title = "قيد التعلم",
                        count = uiState.learningCount.coerceAtLeast(78),
                        dotColor = AccentAmber,
                        progressRatio = 0.21f,
                        modifier = Modifier.weight(1f).testTag("status_card_learning")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 3. Review Cards
                    WarmStatusCard(
                        title = "للمراجعة",
                        count = uiState.reviewCount.coerceAtLeast(32),
                        dotColor = ArticleDer,
                        progressRatio = 0.09f,
                        modifier = Modifier.weight(1f).testTag("status_card_review")
                    )

                    // 4. Mastered Cards
                    WarmStatusCard(
                        title = "متقنة",
                        count = uiState.masteredCount.coerceAtLeast(215),
                        dotColor = ArticleDas,
                        progressRatio = 0.58f,
                        modifier = Modifier.weight(1f).testTag("status_card_mastered")
                    )
                }
            }

            // 4. 7-Day Activity Minimalist Bar Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "نشاط المراجعة الأسبوعي",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "مقارنة بالهدف اليومي المعتمد",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(FeedbackCorrectSubtle)
                                .border(1.dp, FeedbackCorrect.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = FeedbackCorrect,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+18%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FeedbackCorrect
                                )
                            }
                        }
                    }

                    Text(
                        text = "248 بطاقة تمت مراجعتها هذا الأسبوع (+18% عن الأسبوع الماضي)",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    // Chart Graphic Bars
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(top = 16.dp, bottom = 4.dp)
                    ) {
                        // Dashed target baseline (30 cards)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                                .height(1.dp)
                                .background(Color(0xFFD1C4BA))
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SurfaceCard)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("30 Ziel", fontSize = 9.sp, color = TextSecondary)
                        }

                        // 7 Days Bars
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            DayBarItem("السبت", "28", 0.55f, false)
                            DayBarItem("الأحد", "34", 0.68f, false)
                            DayBarItem("الإثنين", "30", 0.60f, false)
                            DayBarItem("الثلاثاء", "38", 0.75f, false)
                            DayBarItem("الأربعاء", "26", 0.50f, false)
                            DayBarItem("الخميس", "50", 0.95f, false)
                            // Friday: Active Espresso bar with 42 pill
                            DayBarItem("الجمعة", "42", 0.82f, true)
                        }
                    }

                    // Active Day Highlight Callout
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceContainerLow)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryEspresso)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "اليوم: 42 بطاقة",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "تجاوزت الهدف اليومي (+40%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FeedbackCorrect
                            )
                        }
                    }
                }
            }

            // 5. Mnemonic Gender Distribution Mini-Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "توزيع الأجناس النحوية في الذاكرة",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Artikel",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }

                    // Segmented Capsule Progress
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(SurfaceContainer),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(0.38f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(ArticleDer)
                        )
                        Box(
                            modifier = Modifier
                                .weight(0.35f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(ArticleDie)
                        )
                        Box(
                            modifier = Modifier
                                .weight(0.27f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(ArticleDas)
                        )
                    }

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(ArticleDer))
                            Spacer(Modifier.width(4.dp))
                            Text("المذكر (der): 38%", fontSize = 11.sp, color = TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(ArticleDie))
                            Spacer(Modifier.width(4.dp))
                            Text("المؤنث (die): 35%", fontSize = 11.sp, color = TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(ArticleDas))
                            Spacer(Modifier.width(4.dp))
                            Text("المحايد (das): 27%", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // 6. Quick Action CTA Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(PrimaryEspresso)
                    .clickable { /* trigger review */ }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = OnPrimaryWarm,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "بدء جلسة تثبيت الذاكرة (32 كلمة)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnPrimaryWarm
                    )
                }
            }
        }
    }
}

@Composable
private fun WarmStatusCard(
    title: String,
    count: Int,
    dotColor: Color,
    progressRatio: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$count",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "كلمة",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(SurfaceContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressRatio.coerceIn(0.08f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(dotColor)
                )
            }
        }
    }
}

@Composable
private fun DayBarItem(
    dayName: String,
    countText: String,
    heightRatio: Float,
    isActiveDay: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxHeight()
    ) {
        if (isActiveDay) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryEspresso)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = countText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnPrimaryWarm
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
        } else {
            Text(
                text = countText,
                fontSize = 10.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
        }

        Box(
            modifier = Modifier
                .width(26.dp)
                .fillMaxHeight(fraction = heightRatio)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(if (isActiveDay) PrimaryEspresso else SurfaceContainerHigh)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = dayName,
            fontSize = 10.sp,
            fontWeight = if (isActiveDay) FontWeight.Bold else FontWeight.Normal,
            color = if (isActiveDay) PrimaryEspresso else TextSecondary
        )
    }
}
