package com.aluma.laundry.ui.view.components.itemscard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.store.model.StoreRemote

@Composable
fun ItemStoreCard(
    storeRemote: StoreRemote,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val targetBorderColor = if (isSelected) Color(0xFF4CAF50) else Color.Transparent
    val targetBackgroundColor = if (isSelected) Color(0xFFE8F5E9) else Color(0xFFFDFDFD)

    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 300),
        label = "borderColor"
    )

    val animatedBackgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )

    val shape = RoundedCornerShape(16.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = shape,
        border = BorderStroke(2.dp, animatedBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = animatedBackgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Store Icon di kiri
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = "Store Icon",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Teks informasi toko
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = storeRemote.storeName.orEmpty().ifBlank { "Nama Toko Tidak Diketahui" },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!storeRemote.city.isNullOrBlank() || !storeRemote.address.isNullOrBlank()) {
                    val locationText = buildAnnotatedString {
                        if (!storeRemote.city.isNullOrBlank()) {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(storeRemote.city)
                            }
                        }

                        if (!storeRemote.city.isNullOrBlank() && !storeRemote.address.isNullOrBlank()) {
                            append(" • ")
                        }

                        if (!storeRemote.address.isNullOrBlank()) {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                                append(storeRemote.address)
                            }
                        }
                    }

                    Text(
                        text = locationText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}