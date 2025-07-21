package com.aluma.laundry.ui.view.components.itemscard

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
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
import com.aluma.laundry.data.api.order.Order

@Composable
fun ItemOrderCard(
    order: Order,
    onSelectToggle: () -> Unit = {}
) {
    val backgroundColor = Color(0xFFFDFDFD)
    val shape = RoundedCornerShape(16.dp)
    val interactionSource = remember { MutableInteractionSource() }

    val machineType = if (order.sizeMachine) "Besar" else "Kecil"
    val stepMachine = order.stepMachine
    val machineNumber = order.numberMachine ?: "-"
    val isComplete = stepMachine == 2

    val statusMessage = when (stepMachine) {
        0 -> "Belum memilih dan menyalakan mesin cuci"
        1 -> "Belum memilih dan menyalakan mesin pengering"
        2 -> "Sedang mencuci di mesin nomor $machineNumber"
        3 -> "Sedang mengeringkan di mesin nomor $machineNumber"
        else -> "Status tidak diketahui"
    }

    val statusColor = when (stepMachine) {
        0 -> Color.Red.copy(alpha = 0.1f)
        1 -> Color.Yellow.copy(alpha = 0.1f)
        2 -> Color.Blue.copy(alpha = 0.1f)
        3 -> Color.Green.copy(alpha = 0.1f)
        else -> Color.LightGray.copy(alpha = 0.1f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.Transparent, shape = shape),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .clip(shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onSelectToggle
                )
                .padding(16.dp)
        ) {
            Text(
                text = order.customerName ?: "Tanpa Nama",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Tipe Mesin",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = machineType,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column {
                    Text(
                        text = "Layanan",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = order.serviceName ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = statusColor, shape = RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (stepMachine == 2) Icons.Default.Water else Icons.Default.Warning,
                    contentDescription = null,
                    tint = when (stepMachine) {
                        0 -> Color.Red
                        1 -> Color(0xFFFFA000)
                        2 -> Color(0xFF4CAF50)
                        3 -> Color(0xFF4CAF50)
                        else -> Color.Gray
                    },
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }
    }
}
