package com.azkari2027.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azkari2027.app.data.*
import com.azkari2027.app.notifications.ReminderScheduler
import com.azkari2027.app.ui.components.IslamicGeometricBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    settingsStore: SettingsStore
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val primary = MaterialTheme.colorScheme.primary
    var editingReminder by remember { mutableStateOf<ReminderConfig?>(null) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        IslamicGeometricBackground(goldColor = primary)

        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = if (settings.isArabic) "الإعدادات والتنبيهات" else "Settings & Reminders",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Language & Theme Card
            item {
                SettingsCard(title = if (settings.isArabic) "المظهر واللغة" else "Appearance & Language") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = settings.isArabic,
                            onClick = { scope.launch { settingsStore.setLanguage(1) } },
                            label = { Text("العربية") }
                        )
                        FilterChip(
                            selected = !settings.isArabic,
                            onClick = { scope.launch { settingsStore.setLanguage(2) } },
                            label = { Text("English") }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = settings.themeMode == AppThemeMode.AMOLED_DARK,
                            onClick = { scope.launch { settingsStore.setThemeMode(AppThemeMode.AMOLED_DARK) } },
                            label = { Text(if (settings.isArabic) "ليلي AMOLED" else "AMOLED Dark") }
                        )
                        FilterChip(
                            selected = settings.themeMode == AppThemeMode.PARCHMENT_LIGHT,
                            onClick = { scope.launch { settingsStore.setThemeMode(AppThemeMode.PARCHMENT_LIGHT) } },
                            label = { Text(if (settings.isArabic) "نهاري دافئ" else "Parchment") }
                        )
                    }
                }
            }

            // Font Family & Size Card
            item {
                SettingsCard(title = if (settings.isArabic) "الخط وحجم القراءة" else "Typography & Font Size") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        AzkarFontFamily.entries.forEach { family ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { scope.launch { settingsStore.setFontFamily(family) } }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.fontFamily == family,
                                    onClick = { scope.launch { settingsStore.setFontFamily(family) } }
                                )
                                Text(if (settings.isArabic) family.displayNameAr else family.displayNameEn)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (settings.isArabic) "حجم الخط: ${settings.fontSizeSp.toInt()}sp" else "Font Size: ${settings.fontSizeSp.toInt()}sp",
                        style = MaterialTheme.typography.labelLarge,
                        color = primary
                    )
                    Slider(
                        value = settings.fontSizeSp,
                        onValueChange = { scope.launch { settingsStore.setFontSize(it) } },
                        valueRange = 16f..34f
                    )
                    Text(
                        text = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
                        fontSize = settings.fontSizeSp.sp,
                        lineHeight = (settings.fontSizeSp * 1.6f).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Audio, Haptics & Auto-Advance
            item {
                SettingsCard(title = if (settings.isArabic) "التفاعل والصوت" else "Audio & Haptics") {
                    ToggleRow(
                        label = if (settings.isArabic) "أصوات العد وتقليب الصفحات" else "Counter & Flip Sounds",
                        checked = settings.soundEnabled,
                        onCheckedChange = { scope.launch { settingsStore.setSoundEnabled(it) } }
                    )
                    ToggleRow(
                        label = if (settings.isArabic) "الاهتزاز الذكي (Haptic Feedback)" else "Haptic Feedback",
                        checked = settings.vibrationEnabled,
                        onCheckedChange = { scope.launch { settingsStore.setVibrationEnabled(it) } }
                    )
                    ToggleRow(
                        label = if (settings.isArabic) "الانتقال التلقائي عند إتمام الذكر" else "Auto-advance on completion",
                        checked = settings.autoAdvanceOnComplete,
                        onCheckedChange = { scope.launch { settingsStore.setAutoAdvance(it) } }
                    )
                }
            }

            // Daily 4 Offline Reminders
            item {
                SettingsCard(title = if (settings.isArabic) "التنبيهات اليومية (أوفلاين)" else "Daily Offline Reminders") {
                    settings.reminders.forEach { rem ->
                        val timeText = String.format("%02d:%02d", rem.hour, rem.minute)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { editingReminder = rem }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (settings.isArabic) rem.key.titleAr else rem.key.titleEn,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = timeText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = primary
                                )
                            }
                            Switch(
                                checked = rem.enabled,
                                onCheckedChange = { enable ->
                                    if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    scope.launch {
                                        settingsStore.updateReminder(rem.key, enable, rem.hour, rem.minute)
                                        ReminderScheduler.schedule(context, rem.copy(enabled = enable), settings.isArabic)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            item {
                SettingsCard(title = if (settings.isArabic) "عن التطبيق" else "About Azkari 2027") {
                    Text(
                        text = if (settings.isArabic)
                            "أذكاري 2027 • نسخة مطورة لنظام أندرويد 16 وشاشات 120Hz، تعمل أوفلاين 100% بدون إنترنت أو إعلانات، مع الحفاظ التام على جميع الأذكار والتسجيلات الصوتية والمهام الرمضانية الأصلية."
                        else
                            "Azkari 2027 • Modern Android 16 edition built for 120Hz AMOLED displays, 100% offline with zero ads while preserving all original Azkar texts, audio recitations, and Ramadan tasks.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    editingReminder?.let { rem ->
        val pickerState = rememberTimePickerState(initialHour = rem.hour, initialMinute = rem.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { editingReminder = null },
            title = { Text(if (settings.isArabic) rem.key.titleAr else rem.key.titleEn) },
            text = { TimePicker(state = pickerState) },
            confirmButton = {
                Button(onClick = {
                    val updated = rem.copy(enabled = true, hour = pickerState.hour, minute = pickerState.minute)
                    scope.launch {
                        settingsStore.updateReminder(rem.key, true, updated.hour, updated.minute)
                        ReminderScheduler.schedule(context, updated, settings.isArabic)
                    }
                    editingReminder = null
                }) {
                    Text(if (settings.isArabic) "حفظ الوقت" else "Save Time")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingReminder = null }) {
                    Text(if (settings.isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primary)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
