package com.aluma.laundry.ui.view.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.delay

@Composable
fun CountdownTimer(
    startTimeMillis: Long,
    endTimeMillis: Long,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    warningThresholdSeconds: Long = 60, // Admin butuh warning lebih awal (1 menit)
    showProgressBar: Boolean = true,
) {
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val totalDuration = (endTimeMillis - startTimeMillis).coerceAtLeast(1L)
    val remainingTime = (endTimeMillis - currentTimeMillis).coerceAtLeast(0L)

    // Animasi progress yang mulus
    val progress by animateFloatAsState(
        targetValue = if (totalDuration > 0) remainingTime / totalDuration.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
        label = "progress"
    )

    // Perubahan warna berdasarkan sisa waktu
    val animatedColor by animateColorAsState(
        targetValue = when {
            remainingTime == 0L -> Color.Gray
            remainingTime <= warningThresholdSeconds * 1000 -> Color(0xFFD32F2F) // Red mendesak
            remainingTime <= totalDuration / 2 -> Color(0xFFF57C00) // Orange/Amber
            else -> Color(0xFF388E3C) // Green aman
        },
        animationSpec = tween(800),
        label = "color"
    )

    // Efek detak jantung halus saat waktu kritis
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (remainingTime in 1..(warningThresholdSeconds * 1000)) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Update timer setiap detik
    LaunchedEffect(endTimeMillis) {
        while (remainingTime > 0) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
        onFinish()
    }

    val seconds = (remainingTime / 1000) % 60
    val minutes = (remainingTime / 1000) / 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- DISPLAY WAKTU (Premium Chip Style) ---
        Surface(
            color = animatedColor.copy(alpha = 0.08f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, animatedColor.copy(alpha = 0.15f))
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 16.dp)
            ) {
                Text(
                    text = if (remainingTime > 0) formattedTime else "SELESAI",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = animatedColor,
                    modifier = Modifier.scale(pulseScale)
                )
            }
        }

        if (showProgressBar) {
            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                // Info Persentase untuk Admin (Diletakkan di atas progress bar agar lebih clean)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (remainingTime > 0) "Sisa proses..." else "Proses Selesai",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = animatedColor
                    )
                }

                // Progress Bar dengan desain modern & tebal
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape),
                    color = animatedColor,
                    trackColor = animatedColor.copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round
                )
            }
        }

        if (remainingTime == 0L) {
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                color = Color.Red.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
            ) {
                Text(
                    text = "MESIN SIAP DIKOSONGKAN",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Red,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}