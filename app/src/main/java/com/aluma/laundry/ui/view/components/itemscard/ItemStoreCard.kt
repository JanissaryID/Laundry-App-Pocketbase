package com.aluma.laundry.ui.view.components.itemscard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aluma.laundry.R
import com.aluma.laundry.data.store.model.StoreRemote

@Composable
fun ItemStoreCard(
    storeRemote: StoreRemote,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    // Warna untuk Admin: Hijau memberikan kesan "Ready/Go"
    val targetBorderColor = if (isSelected) Color(0xFF4CAF50) else Color.Transparent
    val targetBackgroundColor = if (isSelected) Color(0xFFF1F8E9) else Color.White
    val targetIconColor = if (isSelected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary

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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp), // Padding horizontal dikelola oleh LazyColumn contentPadding
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(2.dp, animatedBorderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 0.dp else 2.dp // Flat saat terpilih agar fokus ke warna
        ),
        colors = CardDefaults.cardColors(containerColor = animatedBackgroundColor),
        onClick = onClick // Pindahkan clickable ke Card agar seluruh area merespon ripple
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Store Icon dengan Background yang berubah saat terpilih
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(targetIconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront, // Ikon lebih "toko"
                    contentDescription = null,
                    tint = targetIconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Teks informasi toko
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = storeRemote.storeName.orEmpty().ifBlank { stringResource(R.string.default_store_name_placeholder) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2D3142)
                )

                Spacer(modifier = Modifier.height(2.dp))

                if (!storeRemote.city.isNullOrBlank() || !storeRemote.address.isNullOrBlank()) {
                    Text(
                        text = buildString {
                            if (!storeRemote.city.isNullOrBlank()) append(storeRemote.city)
                            if (!storeRemote.address.isNullOrBlank()) append(", ${storeRemote.address}")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Indicator Terpilih (Kanan)
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.selected),
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}