package com.azkari2027.app.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azkari2027.app.audio.OfflineAudioController
import com.azkari2027.app.data.AppSettings
import com.azkari2027.app.data.Category
import com.azkari2027.app.ui.components.AnimatedBeadArc
import com.azkari2027.app.ui.components.HapticsHelper
import com.azkari2027.app.ui.components.IslamicGeometricBackground
import com.azkari2027.app.ui.components.formatDuration
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbeehScreen(
    tasabeeh: List<Category>,
    settings: AppSettings,
    audioController: OfflineAudioController,
    onIncrement: (catId: Int, countDelta: Int, secondsDelta: Long) -> Unit,
    onAddTasbeeh: (String) -> Unit,
    onDeleteTasbeeh: (Int) -> Unit
) {
    val context = LocalContext.current
    var selectedId by remember(tasabeeh) { mutableIntStateOf(tasabeeh.firstOrNull()?.id ?: -1) }
    val current = tasabeeh.firstOrNull { it.id == selectedId } ?: tasabeeh.firstOrNull()

    var sessionCount by remember(selectedId) { mutableIntStateOf(0) }
    var sessionSeconds by remember(selectedId) { mutableLongStateOf(0L) }
    var isTimerRunning by remember(selectedId) { mutableStateOf(false) }
    var pulseToggle by remember { mutableStateOf(false) }
    var showPickerSheet by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newTasbeehText by remember { mutableStateOf("") }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000L)
            sessionSeconds++
        }
    }

    val orbScale by animateFloatAsState(
        targetValue = if (pulseToggle) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "orb_scale"
    )
    LaunchedEffect(pulseToggle) {
        if (pulseToggle) {
            delay(90)
            pulseToggle = false
        }
    }

    val primary = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        IslamicGeometricBackground(goldColor = primary)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 104.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (settings.isArabic) "المسبحة الإلكترونية" else "Digital Masbaha",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Row {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Rounded.AddCircleOutline, null, tint = primary)
                    }
                    IconButton(onClick = { showPickerSheet = true }) {
                        Icon(Icons.Rounded.List, null, tint = primary)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Active Tasbeeha Selector Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { showPickerSheet = true },
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, primary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = current?.name ?: if (settings.isArabic) "سبحان الله" else "Subhan Allah",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = if (settings.isArabic) "الإجمالي: ${current?.count ?: 0}" else "Total: ${current?.count ?: 0}",
                            style = MaterialTheme.typography.labelLarge,
                            color = primary
                        )
                        Text(
                            text = "⏱ ${formatDuration(sessionSeconds)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 3D Bezier Animated Prayer Beads
            AnimatedBeadArc(tapCount = sessionCount, goldColor = primary)

            Spacer(Modifier.weight(1f))

            // Main Interactive Glowing Tap Orb
            Box(
                modifier = Modifier
                    .size(196.dp)
                    .scale(orbScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(primary.copy(alpha = 0.35f), MaterialTheme.colorScheme.surface)
                        )
                    )
                    .clickable {
                        val cat = current ?: return@clickable
                        if (!isTimerRunning) isTimerRunning = true
                        sessionCount++
                        pulseToggle = true
                        audioController.playTick(settings.soundEnabled)
                        if (sessionCount % 33 == 0) {
                            HapticsHelper.heavyComplete(context, settings.vibrationEnabled)
                        } else {
                            HapticsHelper.lightTick(context, settings.vibrationEnabled)
                        }
                        onIncrement(cat.id, 1, 1L)
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(164.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(2.5.dp, primary)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = sessionCount.toString(),
                            fontSize = 52.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = primary
                        )
                        Text(
                            text = if (settings.isArabic) "اضغط للتسبيح" else "Tap to Count",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Reset session button
            OutlinedButton(
                onClick = {
                    sessionCount = 0
                    sessionSeconds = 0L
                    isTimerRunning = false
                },
                shape = CircleShape,
                border = BorderStroke(1.dp, primary.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (settings.isArabic) "تصفير عداد الجلسة" else "Reset Session")
            }
        }
    }

    if (showPickerSheet) {
        ModalBottomSheet(onDismissRequest = { showPickerSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).navigationBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (settings.isArabic) "اختر التسبيحة (${tasabeeh.size})" else "Choose Tasbeeha (${tasabeeh.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = primary
                    )
                    TextButton(onClick = { showPickerSheet = false; showAddDialog = true }) {
                        Text(if (settings.isArabic) "+ إضافة تسبيحة" else "+ Add New")
                    }
                }
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(tasabeeh, key = { it.id }) { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    selectedId = item.id
                                    showPickerSheet = false
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = if (item.id == selectedId) primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (item.id == selectedId) BorderStroke(1.dp, primary) else null
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = if (settings.isArabic) "المجموع: ${item.count}" else "Total: ${item.count}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = primary
                                    )
                                }
                                if (item.isUserDefined) {
                                    IconButton(onClick = { onDeleteTasbeeh(item.id) }) {
                                        Icon(Icons.Rounded.DeleteOutline, null, tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (settings.isArabic) "إضافة تسبيحة جديدة" else "Add New Tasbeeha") },
            text = {
                OutlinedTextField(
                    value = newTasbeehText,
                    onValueChange = { newTasbeehText = it },
                    placeholder = { Text(if (settings.isArabic) "اكتب نص التسبيحة..." else "Enter tasbeeha text...") },
                    singleLine = false,
                    maxLines = 3
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newTasbeehText.isNotBlank()) {
                        onAddTasbeeh(newTasbeehText.trim())
                        newTasbeehText = ""
                        showAddDialog = false
                    }
                }) {
                    Text(if (settings.isArabic) "حفظ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(if (settings.isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }
}
