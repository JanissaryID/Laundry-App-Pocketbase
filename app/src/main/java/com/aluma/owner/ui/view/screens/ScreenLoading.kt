package com.aluma.owner.ui.view.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aluma.owner.R
import androidx.compose.ui.unit.sp

@Composable
fun ScreenLoading(
    message: String? = null,
    isFullScreen: Boolean = true,
    modifier: Modifier = Modifier
) {
    val displayMessage = message ?: stringResource(R.string.loading_default_message)
    // Animasi Infinite untuk transisi yang halus
    val transition = rememberInfiniteTransition(label = "loading_anim")

    // Animasi Alpha (Transparansi)
    val alphaAnim by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // Animasi Skala (Biar detak jantung/pulsing)
    val scaleAnim by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = if (isFullScreen) modifier.fillMaxSize().background(MaterialTheme.colorScheme.background) else modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .graphicsLayer(scaleX = scaleAnim, scaleY = scaleAnim) // Efek pulsing halus
                .padding(32.dp)
                .then(
                    if (!isFullScreen) {
                        Modifier
                            .shadow(8.dp, RoundedCornerShape(24.dp))
                            .background(Color.White, RoundedCornerShape(24.dp))
                            .padding(32.dp)
                    } else Modifier
                )
        ) {
            // Indikator utama dengan warna brand
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary.copy(alpha = alphaAnim),
                strokeWidth = 5.dp,
                modifier = Modifier.size(56.dp),
                strokeCap = StrokeCap.Round // Membuat ujung loading melengkung (lebih modern)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = displayMessage,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            // Tambahan opsional: Teks kecil di bawahnya
            Text(
                text = stringResource(R.string.loading_connection_check),
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}