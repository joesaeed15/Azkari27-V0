package com.azkari2027.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azkari2027.app.data.AppSettings
import com.azkari2027.app.data.Category
import com.azkari2027.app.ui.components.IslamicGeometricBackground
import com.azkari2027.app.ui.components.formatDuration
import com.azkari2027.app.ui.theme.EmeraldAccent

@Composable
fun LogScreen(
    allCategories: List<Category>,
    settings: AppSettings,
    onResetLogs: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf("ALL") }

    val totalAzkar = remember(allCategories) { allCategories.filter { it.type == "A" }.sumOf { it.count } }
    val totalTasbeeh = remember(allCategories) { allCategories.filter { it.type == "T" }.sumOf { it.count } }
    val totalSecs = remember(allCategories) { allCategories.sumOf { it.timeSeconds } }

    val itemsToShow = remember(allCategories, filterType) {
        allCategories.filter { (filterType == "ALL" || it.type == filterType) && (it.count > 0 || it.timeSeconds > 0L) }
    }
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (settings.isArabic) "السجل والإحصائيات" else "Activity Log",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    IconButton(onClick = { showConfirm = true }) {
                        Icon(Icons.Rounded.DeleteSweep, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Summary KPI Cards
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        title = if (settings.isArabic) "الأذكار" else "Azkar",
                        value = totalAzkar.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = if (settings.isArabic) "التسبيح" else "Tasbeeh",
                        value = totalTasbeeh.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = if (settings.isArabic) "الوقت" else "Time",
                        value = formatDuration(totalSecs),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = filterType == "ALL", onClick = { filterType = "ALL" },
                        label = { Text(if (settings.isArabic) "الكل" else "All") })
                    FilterChip(selected = filterType == "A", onClick = { filterType = "A" },
                        label = { Text(if (settings.isArabic) "الأذكار" else "Azkar") })
                    FilterChip(selected = filterType == "T", onClick = { filterType = "T" },
                        label = { Text(if (settings.isArabic) "التسابيح" else "Tasbeeh") })
                }
            }

            if (itemsToShow.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = if (settings.isArabic) "ابدأ بقراءة الأذكار أو التسبيح ليظهر سجلك هنا" else "Start reading Azkar or Tasbeeh to track your progress here",
                            modifier = Modifier.padding(24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(itemsToShow, key = { it.id }) { cat ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, primary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cat.name, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "⏱ ${formatDuration(cat.timeSeconds)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${cat.count}×",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldAccent
                            )
                        }
                    }
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text(if (settings.isArabic) "تصفير السجل" else "Clear Log") },
            text = { Text(if (settings.isArabic) "هل أنت متأكد من تصفير جميع العدادات والأوقات؟" else "Are you sure you want to reset all counters?") },
            confirmButton = {
                Button(onClick = { onResetLogs(); showConfirm = false }) {
                    Text(if (settings.isArabic) "نعم، تصفير" else "Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text(if (settings.isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, primary.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = primary)
            Spacer(Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
