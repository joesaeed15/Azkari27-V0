package com.azkari2027.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azkari2027.app.ui.theme.MedinaGold
import com.azkari2027.app.ui.theme.ObsidianBg
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isArabic: Boolean,
    onFinished: () -> Unit
) {
    val scale = remember { Animatable(0.65f) }
    val alpha = remember { Animatable(0f) }
    val rot = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(700))
        rot.animateTo(45f, tween(900, easing = FastOutSlowInEasing))
        delay(550)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0A2018), ObsidianBg, Color(0xFF040807)))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha.value).scale(scale.value)
        ) {
            Canvas(modifier = Modifier.size(140.dp)) {
                val c = Offset(size.width / 2f, size.height / 2f)
                val r = 44.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(MedinaGold.copy(alpha = 0.28f), Color.Transparent),
                        center = c, radius = size.minDimension * 0.55f
                    ),
                    radius = size.minDimension * 0.55f, center = c
                )
                for (base in listOf(0f, 45f)) {
                    rotate(degrees = base + rot.value, pivot = c) {
                        val p = Path().apply {
                            moveTo(c.x - r, c.y - r)
                            lineTo(c.x + r, c.y - r)
                            lineTo(c.x + r, c.y + r)
                            lineTo(c.x - r, c.y + r)
                            close()
                        }
                        drawPath(p, color = MedinaGold, style = Stroke(width = 2.5.dp.toPx()))
                    }
                }
                drawCircle(color = MedinaGold, radius = 12.dp.toPx(), center = c)
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = if (isArabic) "أذكاري" else "Azkari",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MedinaGold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (isArabic) "حصنك اليومي • يعمل بدون إنترنت" else "Your Daily Fortress • 100% Offline",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9FB8AE)
            )
        }
    }
}
