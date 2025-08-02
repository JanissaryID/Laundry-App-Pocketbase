package com.aluma.owner.ui.view.components.itemscard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aluma.owner.data.order.model.OrderRemote
import com.aluma.owner.utils.formatToRupiah

@Composable
fun ItemOrderCard(
    order: OrderRemote,
) {
    val shape = RoundedCornerShape(16.dp)
    val backgroundColor = Color(0xFFFDFDFD)

    val machineType = if (order.sizeMachine) "Besar" else "Kecil"
    val formattedPrice = remember(order.price) {
        order.price?.formatToRupiah() ?: "-"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .clip(shape)
        ) {
            // Judul Layanan
            Text(
                text = order.serviceName ?: "-",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Container Info bawah
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mesin
                Column(
                    modifier = Modifier.padding(start = 16.dp, bottom = 16.dp, top = 8.dp)
                ) {
                    Text(
                        text = "Tipe Mesin",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = machineType,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Harga
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 16.dp, bottom = 16.dp, top = 8.dp)
                ) {
                    Text(
                        text = "Harga",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = formattedPrice,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4CAF50)
                        )
                    )
                }
            }
        }
    }
}