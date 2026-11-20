package com.azkari2027.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.azkari2027.app.ui.theme.MedinaGold
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IslamicGeometricBackground(
    goldColor: Color = MedinaGold,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_glow")
    val slowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 120_000, easing = LinearEasing)),
        label = "star_rot"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val step = 112.dp.toPx()
        val r = 27.dp.toPx()
        var y = 0f
        while (y < size.height + step) {
            var x = 0f
            while (x < size.width + step) {
                val center = Offset(x, y)
                for (angle in listOf(0f, 45f)) {
                    rotate(degrees = angle, pivot = center) {
                        val path = Path().apply {
                            moveTo(center.x - r, center.y - r)
                            lineTo(center.x + r, center.y - r)
                            lineTo(center.x + r, center.y + r)
                            lineTo(center.x - r, center.y + r)
                            close()
                        }
                        drawPath(path, color = goldColor.copy(alpha = 0.045f), style = Stroke(width = 1.dp.toPx()))
                    }
                }
                x += step
            }
            y += step
        }
        val glowCenter = Offset(size.width / 2f, size.height * 0.14f)
        rotate(degrees = slowRotation, pivot = glowCenter) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(goldColor.copy(alpha = 0.11f), Color.Transparent),
                    center = glowCenter,
                    radius = size.width * 0.65f
                ),
                center = glowCenter,
                radius = size.width * 0.65f
            )
        }
    }
}

@Composable
fun AnimatedBeadArc(
    tapCount: Int,
    goldColor: Color = MedinaGold,
    beadDark: Color = Color(0xFF132A23),
    modifier: Modifier = Modifier
) {
    val animatedOffset by animateFloatAsState(
        targetValue = tapCount.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bead_arc"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(112.dp)) {
        val w = size.width
        val h = size.height
        val cordPath = Path().apply {
            moveTo(0f, h * 0.80f)
            quadraticTo(w * 0.5f, h * 0.10f, w, h * 0.80f)
        }
        drawPath(cordPath, color = goldColor.copy(alpha = 0.45f), style = Stroke(width = 2.dp.toPx()))

        val visibleBeads = 9
        val frac = animatedOffset - animatedOffset.toInt()
        for (i in 0..visibleBeads) {
            val t = ((i + frac) / visibleBeads.toFloat()).coerceIn(0f, 1f)
            val inv = 1f - t
            val bx = (inv * inv * 0f) + (2f * inv * t * (w * 0.5f)) + (t * t * w)
            val by = (inv * inv * (h * 0.80f)) + (2f * inv * t * (h * 0.10f)) + (t * t * (h * 0.80f))
            val isCenter = abs(t - 0.5f) < 0.085f
            val radius = if (isCenter) 18.dp.toPx() else 12.dp.toPx()

            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isCenter) {
                        listOf(Color(0xFFFFF4C8), goldColor, Color(0xFF8C6616))
                    } else {
                        listOf(Color(0xFF28594A), beadDark, Color(0xFF060E0C))
                    },
                    center = Offset(bx - radius * 0.3f, by - radius * 0.3f),
                    radius = radius * 1.3f
                ),
                radius = radius,
                center = Offset(bx, by)
            )
        }
    }
}

@Composable
fun GoldenSparkleBurst(
    progress: Float,
    goldColor: Color = MedinaGold,
    modifier: Modifier = Modifier
) {
    if (progress <= 0f || progress >= 1f) return
    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxR = size.minDimension * 0.46f
        val alpha = (1f - progress).coerceIn(0f, 1f)
        for (i in 0 until 16) {
            val angle = (i * 22.5) * PI / 180.0
            val dist = maxR * progress
            val px = cx + (cos(angle) * dist).toFloat()
            val py = cy + (sin(angle) * dist).toFloat()
            drawCircle(
                color = goldColor.copy(alpha = alpha),
                radius = 5.dp.toPx() * (1f - progress * 0.55f),
                center = Offset(px, py)
            )
        }
    }
}

@Composable
fun MiniAudioEqualizer(
    isPlaying: Boolean,
    color: Color = MedinaGold,
    modifier: Modifier = Modifier.width(26.dp).height(18.dp)
) {
    val transition = rememberInfiniteTransition(label = "eq")
    val b1 by transition.animateFloat(0.25f, 1f, infiniteRepeatable(tween(420), RepeatMode.Reverse), label = "b1")
    val b2 by transition.animateFloat(0.95f, 0.2f, infiniteRepeatable(tween(340), RepeatMode.Reverse), label = "b2")
    val b3 by transition.animateFloat(0.35f, 0.9f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "b3")

    Canvas(modifier = modifier) {
        val heights = if (isPlaying) listOf(b1, b2, b3) else listOf(0.25f, 0.45f, 0.25f)
        val barW = size.width / 5f
        heights.forEachIndexed { idx, frac ->
            val h = size.height * frac
            val left = idx * barW * 1.8f
            drawRoundRect(
                color = color,
                topLeft = Offset(left, size.height - h),
                size = Size(barW, h),
                cornerRadius = CornerRadius(barW / 2f, barW / 2f)
            )
        }
    }
}

fun categoryIconFor(iconKey: String): ImageVector = when {
    iconKey.contains("morning") -> Icons.Rounded.WbSunny
    iconKey.contains("evening") -> Icons.Rounded.NightsStay
    iconKey.contains("sleep") || iconKey.contains("takalab") || iconKey.contains("helem") -> Icons.Rounded.Bedtime
    iconKey.contains("wake") -> Icons.Rounded.WbTwilight
    iconKey.contains("salah") || iconKey.contains("estefta") || iconKey.contains("qunoot") || iconKey.contains("estekharah") -> Icons.Rounded.Mosque
    iconKey.contains("ruqya") || iconKey.contains("mreed") || iconKey.contains("wjaaa") -> Icons.Rounded.HealthAndSafety
    iconKey.contains("glorification") || iconKey.contains("zanb") -> Icons.Rounded.AutoAwesome
    iconKey.contains("travel") || iconKey.contains("rukoob") || iconKey.contains("sfar") -> Icons.Rounded.FlightTakeoff
    iconKey.contains("manzel") || iconKey.contains("dukhol") || iconKey.contains("khuruj") -> Icons.Rounded.Home
    iconKey.contains("wadoo") || iconKey.contains("mtar") -> Icons.Rounded.WaterDrop
    iconKey.contains("azan") -> Icons.Rounded.NotificationsActive
    iconKey.contains("helal") || iconKey.contains("iftar") -> Icons.Rounded.DarkMode
    iconKey.contains("huzon") || iconKey.contains("karb") || iconKey.contains("gadab") -> Icons.Rounded.Favorite
    iconKey.contains("kbr") || iconKey.contains("taazye") || iconKey.contains("mkbra") -> Icons.Rounded.VolunteerActivism
    else -> Icons.Rounded.MenuBook
}

fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format("%02d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
}
