package com.azkari2027.app.ui.screens

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azkari2027.app.audio.OfflineAudioController
import com.azkari2027.app.data.AppSettings
import com.azkari2027.app.data.Category
import com.azkari2027.app.data.Prayer
import com.azkari2027.app.ui.components.GoldenSparkleBurst
import com.azkari2027.app.ui.components.HapticsHelper
import com.azkari2027.app.ui.components.IslamicGeometricBackground
import com.azkari2027.app.ui.components.MiniAudioEqualizer
import com.azkari2027.app.ui.theme.EmeraldAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerReaderScreen(
    category: Category,
    prayers: List<Prayer>,
    settings: AppSettings,
    audioController: OfflineAudioController,
    onSessionFinished: (completedCount: Int, secondsSpent: Long) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val startTimeMs = remember { System.currentTimeMillis() }
    val safePrayers = remember(prayers) { prayers }
    val pagerState = rememberPagerState(pageCount = { safePrayers.size.coerceAtLeast(1) })
    val remainingCounts = remember(safePrayers) {
        mutableStateMapOf<Int, Int>().apply {
            safePrayers.forEach { put(it.id, it.repetition) }
        }
    }
    var showVirtueText by remember { mutableStateOf<String?>(null) }
    var burstTrigger by remember { mutableIntStateOf(0) }
    var completedAllLogged by remember { mutableStateOf(false) }
    val audioState by audioController.state.collectAsState()
    val primary = MaterialTheme.colorScheme.primary

    // Poll audio progress smoothly when playing
    LaunchedEffect(audioState.isPlaying) {
        while (audioState.isPlaying) {
            audioController.syncProgress()
            delay(200)
        }
    }

    // Stop previous audio when swiping to a new zikr
    LaunchedEffect(pagerState.currentPage) {
        audioController.stop()
    }

    DisposableEffect(Unit) {
        onDispose {
            audioController.stop()
            val secs = ((System.currentTimeMillis() - startTimeMs) / 1000L).coerceAtLeast(1L)
            val allDone = safePrayers.isNotEmpty() && safePrayers.all { (remainingCounts[it.id] ?: 1) == 0 }
            val delta = if (allDone && !completedAllLogged) 1 else 0
            onSessionFinished(delta, secs)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        IslamicGeometricBackground(goldColor = primary)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = primary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${pagerState.currentPage + 1} / ${safePrayers.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = primary
                    )
                }
                IconButton(onClick = {
                    val cur = safePrayers.getOrNull(pagerState.currentPage) ?: return@IconButton
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, buildString {
                            append(cur.text)
                            if (cur.virtue.isNotBlank()) append("

").append(cur.virtue)
                        })
                    }
                    context.startActivity(Intent.createChooser(shareIntent, category.name))
                }) {
                    Icon(Icons.Rounded.Share, null, tint = primary)
                }
                IconButton(onClick = {
                    val cur = safePrayers.getOrNull(pagerState.currentPage) ?: return@IconButton
                    remainingCounts[cur.id] = cur.repetition
                }) {
                    Icon(Icons.Rounded.Refresh, null, tint = primary)
                }
            }

            LinearProgressIndicator(
                progress = { (pagerState.currentPage + 1f) / safePrayers.size.coerceAtLeast(1) },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = primary,
                trackColor = primary.copy(alpha = 0.18f),
                strokeCap = StrokeCap.Round
            )

            Spacer(Modifier.height(14.dp))

            if (safePrayers.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { pageIndex ->
                    val prayer = safePrayers[pageIndex]
                    val remaining = remainingCounts[prayer.id] ?: prayer.repetition
                    val doneCount = (prayer.repetition - remaining).coerceAtLeast(0)
                    val targetFraction = doneCount.toFloat() / prayer.repetition.coerceAtLeast(1)
                    val animatedProgress by animateFloatAsState(
                        targetValue = targetFraction,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "zikr_prog"
                    )
                    val isThisAudio = audioState.currentAudioName == prayer.audioName
                    val isPlayingThis = isThisAudio && audioState.isPlaying

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Main Azkar Text Card (exact original text & diacritics preserved 100%)
                        Surface(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.93f),
                            border = BorderStroke(1.dp, primary.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(22.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = prayer.text,
                                    fontSize = settings.fontSizeSp.sp,
                                    lineHeight = (settings.fontSizeSp * 1.78f).sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (prayer.virtue.isNotBlank()) {
                                    Spacer(Modifier.height(18.dp))
                                    AssistChip(
                                        onClick = { showVirtueText = prayer.virtue },
                                        label = { Text(if (settings.isArabic) "الفضل والدليل" else "Virtue & Reference") },
                                        leadingIcon = { Icon(Icons.Rounded.AutoAwesome, null, tint = primary) },
                                        border = BorderStroke(1.dp, primary.copy(alpha = 0.4f))
                                    )
                                }
                            }
                        }

                        // Offline Audio Player Bar (when audio exists for this prayer)
                        if (prayer.audioName.isNotBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, primary.copy(alpha = 0.18f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FilledTonalIconButton(
                                        onClick = { audioController.playOrToggle(prayer.audioName) },
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = primary.copy(alpha = 0.18f),
                                            contentColor = primary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingThis) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                            contentDescription = null
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    MiniAudioEqualizer(isPlaying = isPlayingThis, color = primary)
                                    Spacer(Modifier.width(10.dp))
                                    val dur = if (isThisAudio && audioState.durationMs > 0L) audioState.durationMs.toFloat() else 1f
                                    val pos = if (isThisAudio) audioState.positionMs.toFloat().coerceIn(0f, dur) else 0f
                                    Slider(
                                        value = pos,
                                        onValueChange = { if (isThisAudio) audioController.seekTo(it.toLong()) },
                                        valueRange = 0f..dur,
                                        modifier = Modifier.weight(1f),
                                        colors = SliderDefaults.colors(thumbColor = primary, activeTrackColor = primary)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Circular Liquid Gold Counter
                        Box(
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .size(106.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable {
                                    if (remaining > 0) {
                                        val nextVal = remaining - 1
                                        remainingCounts[prayer.id] = nextVal
                                        audioController.playTick(settings.soundEnabled)
                                        if (nextVal == 0) {
                                            HapticsHelper.heavyComplete(context, settings.vibrationEnabled)
                                            burstTrigger++
                                            val allNowDone = safePrayers.all { (remainingCounts[it.id] ?: 1) == 0 }
                                            if (allNowDone && !completedAllLogged) {
                                                completedAllLogged = true
                                                val secs = ((System.currentTimeMillis() - startTimeMs) / 1000L).coerceAtLeast(1L)
                                                onSessionFinished(1, secs)
                                            }
                                            if (settings.autoAdvanceOnComplete && pageIndex < safePrayers.lastIndex) {
                                                scope.launch {
                                                    delay(380)
                                                    audioController.playPageFlip(settings.soundEnabled)
                                                    pagerState.animateScrollToPage(pageIndex + 1)
                                                }
                                            }
                                        } else {
                                            HapticsHelper.lightTick(context, settings.vibrationEnabled)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.fillMaxSize(),
                                color = if (remaining == 0) EmeraldAccent else primary,
                                trackColor = primary.copy(alpha = 0.2f),
                                strokeWidth = 6.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (remaining == 0) "✓" else remaining.toString(),
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (remaining == 0) EmeraldAccent else primary
                                )
                                Text(
                                    text = "$doneCount / ${prayer.repetition}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        if (burstTrigger > 0) {
            var animVal by remember(burstTrigger) { mutableFloatStateOf(0f) }
            LaunchedEffect(burstTrigger) {
                animate(0f, 1f, animationSpec = tween(550)) { v, _ -> animVal = v }
            }
            GoldenSparkleBurst(progress = animVal, goldColor = primary)
        }
    }

    showVirtueText?.let { virtue ->
        ModalBottomSheet(onDismissRequest = { showVirtueText = null }) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding()) {
                Text(
                    text = if (settings.isArabic) "الفضل والدليل" else "Virtue & Reference",
                    style = MaterialTheme.typography.titleMedium,
                    color = primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Text(text = virtue, fontSize = 18.sp, lineHeight = 32.sp)
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
