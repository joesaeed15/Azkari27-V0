package com.azkari2027.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azkari2027.app.data.AppSettings
import com.azkari2027.app.data.RamadanDatabaseHelper
import com.azkari2027.app.data.RamadanTaskItem
import com.azkari2027.app.ui.components.IslamicGeometricBackground
import com.azkari2027.app.ui.theme.EmeraldAccent

@Composable
fun RamadanScreen(
    ramadanDb: RamadanDatabaseHelper,
    settings: AppSettings
) {
    val days = remember { ramadanDb.getDays() }
    val types = remember { ramadanDb.getTaskTypes() }
    var selectedDayId by remember { mutableIntStateOf(1) }
    var selectedTypeId by remember { mutableStateOf<Int?>(null) }
    var refreshTick by remember { mutableIntStateOf(0) }

    val tasks = remember(selectedDayId, selectedTypeId, refreshTick) {
        ramadanDb.getTasksForDay(selectedDayId, selectedTypeId)
    }
    val (earned, maxScore) = remember(selectedDayId, refreshTick) {
        ramadanDb.getDayScoreSummary(selectedDayId)
    }
    val pct = (earned / maxScore).toFloat().coerceIn(0f, 1f)
    val primary = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        IslamicGeometricBackground(goldColor = primary)

        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (settings.isArabic) "مهام رمضان" else "Ramadan Tasks",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        val dayObj = days.firstOrNull { it.id == selectedDayId }
                        Text(
                            text = if (settings.isArabic) dayObj?.nameAr.orEmpty() else dayObj?.nameEn.orEmpty(),
                            style = MaterialTheme.typography.labelLarge,
                            color = primary
                        )
                    }
                    Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { pct },
                            modifier = Modifier.fillMaxSize(),
                            color = EmeraldAccent,
                            trackColor = primary.copy(alpha = 0.2f),
                            strokeWidth = 5.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = "${(pct * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = primary
                        )
                    }
                }
            }

            // 30-day horizontal selector
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(days, key = { it.id }) { day ->
                        val active = day.id == selectedDayId
                        Surface(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { selectedDayId = day.id },
                            shape = CircleShape,
                            color = if (active) primary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, primary.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = if (settings.isArabic) "اليوم ${day.id}" else "Day ${day.id}",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // 5 TaskTypes filter chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedTypeId == null,
                            onClick = { selectedTypeId = null },
                            label = { Text(if (settings.isArabic) "الكل (99)" else "All (99)") }
                        )
                    }
                    items(types, key = { it.id }) { t ->
                        FilterChip(
                            selected = selectedTypeId == t.id,
                            onClick = { selectedTypeId = t.id },
                            label = { Text(if (settings.isArabic) t.textAr else t.textEn) }
                        )
                    }
                }
            }

            items(tasks, key = { "${selectedDayId}_${it.taskId}" }) { task ->
                RamadanTaskRow(
                    task = task,
                    isArabic = settings.isArabic,
                    onUpdate = { answerIndex ->
                        ramadanDb.updateTaskProgress(selectedDayId, task, answerIndex)
                        refreshTick++
                    }
                )
            }
        }
    }
}

@Composable
private fun RamadanTaskRow(
    task: RamadanTaskItem,
    isArabic: Boolean,
    onUpdate: (Int) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val label = if (isArabic) task.textAr else task.textEn

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, if (task.isChecked) EmeraldAccent.copy(alpha = 0.45f) else primary.copy(alpha = 0.16f))
    ) {
        if (!task.isMultiOption) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUpdate(if (task.isChecked) 0 else 1) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isChecked,
                    onCheckedChange = { checked -> onUpdate(if (checked) 1 else 0) }
                )
                Spacer(Modifier.width(8.dp))
                Text(text = label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "+${task.weight}",
                    style = MaterialTheme.typography.labelSmall,
                    color = primary,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            val options = if (isArabic) task.optionsAr else task.optionsEn
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.DarkMode, null, tint = primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(text = label, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "${task.calculatedWeight.toInt()} / ${task.weight}",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(options.indices.toList()) { idx ->
                        val selected = task.weightAnswer == idx
                        FilterChip(
                            selected = selected,
                            onClick = { onUpdate(idx) },
                            label = { Text(options[idx], style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    }
}
