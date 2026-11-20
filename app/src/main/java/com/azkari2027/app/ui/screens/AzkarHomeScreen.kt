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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azkari2027.app.data.AppSettings
import com.azkari2027.app.data.Category
import com.azkari2027.app.ui.components.IslamicGeometricBackground
import com.azkari2027.app.ui.components.categoryIconFor
import com.azkari2027.app.ui.theme.MedinaGold
import java.util.Calendar

@Composable
fun AzkarHomeScreen(
    categories: List<Category>,
    settings: AppSettings,
    onCategoryClick: (Category) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) categories
        else categories.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }

    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = if (settings.isArabic) {
        when (hour) {
            in 4..11 -> "صباح الخير • أذكار الصباح"
            in 12..16 -> "طاب يومك بذكر الله"
            in 17..20 -> "مساء الخير • أذكار المساء"
            else -> "تصبح على خير • أذكار النوم"
        }
    } else {
        when (hour) {
            in 4..11 -> "Good Morning • Morning Azkar"
            in 12..16 -> "Peace Be Upon You"
            in 17..20 -> "Good Evening • Evening Azkar"
            else -> "Good Night • Sleep Azkar"
        }
    }

    // Featured quick-access IDs (Morning, Evening, Sleep, Wake Up, After Prayer, Ruqya)
    val featuredIds = if (settings.isArabic) listOf(1, 2, 5, 6, 4, 11) else listOf(47, 48, 51, 52, 50, 57)
    val featured = remember(categories, featuredIds) {
        featuredIds.mapNotNull { id -> categories.firstOrNull { it.id == id } }
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
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.labelLarge,
                        color = primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (settings.isArabic) "أذكاري" else "Azkari 2027",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    placeholder = {
                        Text(if (settings.isArabic) "ابحث في 46 باباً من الأذكار..." else "Search 46 Azkar categories...")
                    },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = primary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = null)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primary,
                        unfocusedBorderColor = primary.copy(alpha = 0.25f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    )
                )
            }

            // Quick-access Mihrab cards when not searching
            if (searchQuery.isBlank() && featured.isNotEmpty()) {
                item {
                    Text(
                        text = if (settings.isArabic) "الأكثر قراءة يومياً" else "Daily Essentials",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(featured, key = { "feat_${it.id}" }) { cat ->
                            Surface(
                                modifier = Modifier
                                    .width(164.dp)
                                    .height(132.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable { onCategoryClick(cat) },
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, primary.copy(alpha = 0.32f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                listOf(primary.copy(alpha = 0.16f), Color.Transparent)
                                            )
                                        )
                                        .padding(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(primary.copy(alpha = 0.18f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(categoryIconFor(cat.icon), null, tint = primary, modifier = Modifier.size(22.dp))
                                        }
                                        Column {
                                            Text(
                                                text = cat.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = if (settings.isArabic) "${cat.prayerCount} ذكر" else "${cat.prayerCount} prayers",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = if (settings.isArabic) "جميع أبواب الأذكار (${filtered.size})" else "All Categories (${filtered.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            items(filtered, key = { it.id }) { cat ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { onCategoryClick(cat) },
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, primary.copy(alpha = 0.18f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(primary.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(categoryIconFor(cat.icon), null, tint = primary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = if (settings.isArabic)
                                    "${cat.prayerCount} ذكر • ${cat.totalRepetitions} تكرار"
                                else
                                    "${cat.prayerCount} prayers • ${cat.totalRepetitions} reps",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (cat.count > 0) {
                            Surface(
                                shape = CircleShape,
                                color = MedinaGold.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = "${cat.count}×",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
