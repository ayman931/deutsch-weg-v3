package com.example.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SettingsRepository
import com.example.data.repository.VocabularyRepository
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.FeedbackCorrect
import com.example.ui.theme.OnPrimaryWarm
import com.example.ui.theme.PrimaryEspresso
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmBackground
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    vocabularyRepository: VocabularyRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val maxNewCards by settingsRepository.maxNewCardsPerDay.collectAsState()

    var restoreJsonText by remember { mutableStateOf("") }
    var isRestoring by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(WarmBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "الإعدادات والنسخ الاحتياطي",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Box(modifier = Modifier.size(42.dp))
            }

            // Section 1: Daily New Cards Quota (N)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(26.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "حد الكلمات الجديدة يومياً (N)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "عدد البطاقات الجديدة التي تظهر في جلسات اليوم",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainer)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$maxNewCards",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryEspresso
                            )
                        }
                    }

                    // Stepper Buttons and Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { settingsRepository.setMaxNewCardsPerDay(maxNewCards - 5) },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerHigh)
                                .testTag("decrease_new_cards_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = TextPrimary
                            )
                        }

                        Slider(
                            value = maxNewCards.toFloat(),
                            onValueChange = { settingsRepository.setMaxNewCardsPerDay(it.toInt()) },
                            valueRange = 5f..100f,
                            steps = 18,
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryEspresso,
                                activeTrackColor = PrimaryEspresso,
                                inactiveTrackColor = SurfaceContainerHigh
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                                .testTag("new_cards_slider")
                        )

                        IconButton(
                            onClick = { settingsRepository.setMaxNewCardsPerDay(maxNewCards + 5) },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerHigh)
                                .testTag("increase_new_cards_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = TextPrimary
                            )
                        }
                    }
                }
            }

            // Section 2: JSON Backup & Export (Share Sheet)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(26.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = PrimaryEspresso,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "تصدير نسخة احتياطية (JSON)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "تصدير جميع الكلمات، حالات المراجعة، وسجلات FSRS في ملف JSON يمكن مشاركته أو حفظه.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                val json = vocabularyRepository.exportBackupJson()
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, json)
                                    putExtra(Intent.EXTRA_SUBJECT, "Deutsch_Weg_Backup.json")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "تصدير النسخة الاحتياطية")
                                context.startActivity(shareIntent)
                                Toast.makeText(context, "تم تجهيز النسخة الاحتياطية", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceContainerHigh,
                            contentColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("export_backup_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مشاركة / تصدير ملف JSON",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Section 3: JSON Backup Restore
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(26.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = PrimaryEspresso,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "استعادة نسخة احتياطية (JSON)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "قم بلصق محتوى النسخة الاحتياطية JSON لاستعادة جميع كلماتك وسجلات تقدمك بدقة تامة.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = restoreJsonText,
                        onValueChange = { restoreJsonText = it },
                        placeholder = { Text("الصق كود JSON للنسخة الاحتياطية هنا...", fontSize = 13.sp) },
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryEspresso,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = SurfaceContainerLow,
                            unfocusedContainerColor = SurfaceContainerLow,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("restore_json_input")
                    )

                    Button(
                        onClick = {
                            if (restoreJsonText.isBlank()) {
                                Toast.makeText(context, "يرجى لصق كود JSON أولاً", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            scope.launch {
                                isRestoring = true
                                val result = vocabularyRepository.restoreBackupJson(restoreJsonText)
                                isRestoring = false
                                if (result.isSuccess) {
                                    val count = result.getOrNull() ?: 0
                                    statusMessage = "تمت استعادة $count كلمة وسجل بنجاح!"
                                    Toast.makeText(context, "تمت الاستعادة بنجاح ($count كلمة)", Toast.LENGTH_LONG).show()
                                    restoreJsonText = ""
                                } else {
                                    val err = result.exceptionOrNull()?.message ?: "خطأ غير معروف"
                                    statusMessage = "فشل الاستعادة: $err"
                                    Toast.makeText(context, "فشل الاستعادة: $err", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !isRestoring && restoreJsonText.isNotBlank(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryEspresso,
                            contentColor = OnPrimaryWarm
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("restore_backup_button")
                    ) {
                        Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRestoring) "جارِ الاستعادة..." else "استعادة النسخة الاحتياطية الآن",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (statusMessage != null) {
                        Text(
                            text = statusMessage ?: "",
                            fontSize = 12.sp,
                            color = FeedbackCorrect,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
