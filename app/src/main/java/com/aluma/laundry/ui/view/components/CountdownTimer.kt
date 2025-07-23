package com.aluma.laundry.ui.view.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.room.order.OrderRoomViewModel
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@Composable
fun CountdownTimer(
    startTimeMillis: Long,
    endTimeMillis: Long,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    warningThresholdSeconds: Long = 10,
    showProgressBar: Boolean = true,
) {
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val totalDuration = endTimeMillis - startTimeMillis
    val remainingTime = (endTimeMillis - currentTimeMillis).coerceAtLeast(0L)

    val rawProgress = if (totalDuration > 0) remainingTime / totalDuration.toFloat() else 0f

    val progress by animateFloatAsState(
        targetValue = rawProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    val animatedColor by animateColorAsState(
        targetValue = when {
            remainingTime <= warningThresholdSeconds * 1000 -> Color.Red
            remainingTime <= totalDuration / 2 -> Color(0xFFFFA000) // Amber
            else -> Color(0xFF4CAF50) // Green
        },
        animationSpec = tween(500),
        label = "color"
    )

    // Pulse animation jika di bawah threshold
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (remainingTime <= warningThresholdSeconds * 1000) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

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
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Waktu
        Text(
            text = if (remainingTime > 0) formattedTime else "00:00",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
            color = animatedColor,
            modifier = Modifier.scale(pulseScale)
        )

        if (showProgressBar) {
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = animatedColor,
                trackColor = Color.LightGray.copy(alpha = 0.2f),
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
        }

        if (remainingTime == 0L) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "⏱ Waktu Mesin Telah Habis",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}