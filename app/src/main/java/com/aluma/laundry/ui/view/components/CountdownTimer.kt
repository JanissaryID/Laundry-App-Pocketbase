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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aluma.laundry.R
import kotlinx.coroutines.delay

@Composable
fun CountdownTimer(
    startTimeMillis: Long,
    endTimeMillis: Long,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF388E3C),
    warningThresholdSeconds: Long = 60,
    showProgressBar: Boolean = true,
) {
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val totalDuration = (endTimeMillis - startTimeMillis).coerceAtLeast(1L)
    val remainingTime = (endTimeMillis - currentTimeMillis).coerceAtLeast(0L)

    val progress by animateFloatAsState(
        targetValue = if (totalDuration > 0) remainingTime / totalDuration.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
        label = "progress"
    )

    val timerColor by animateColorAsState(
        targetValue = when {
            remainingTime == 0L -> Color(0xFF9E9E9E)
            remainingTime <= warningThresholdSeconds * 1000 -> Color(0xFFE53935)
            remainingTime <= totalDuration / 2 -> Color(0xFFFB8C00)
            else -> accentColor
        },
        animationSpec = tween(800),
        label = "color"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (remainingTime in 1..(warningThresholdSeconds * 1000)) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
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

    val isWarning = remainingTime in 1..(warningThresholdSeconds * 1000)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Time display
        Text(
            text = if (remainingTime > 0) formattedTime else stringResource(id = R.string.finished),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = if (remainingTime > 0) Color(0xFF212121) else timerColor,
            modifier = if (isWarning) Modifier.scale(pulseScale) else Modifier
        )

        if (remainingTime > 0) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(id = R.string.process_remaining),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF9E9E9E)
            )
        }

        if (remainingTime == 0L) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(id = R.string.machine_ready_empty),
                style = MaterialTheme.typography.labelSmall,
                color = timerColor,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (showProgressBar) {
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = timerColor,
                trackColor = timerColor.copy(alpha = 0.12f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}