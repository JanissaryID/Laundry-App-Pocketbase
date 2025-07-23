package com.aluma.laundry.ui.view.components.itemscard

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.aluma.laundry.data.order.model.OrderLocal
import com.aluma.laundry.data.order.utils.Quad
import com.aluma.laundry.ui.view.components.InfoLabelValue

@Composable
fun ItemOrderCard(
    order: OrderLocal,
    onSelect: () -> Unit = {}
) {
    val backgroundColor = Color(0xFFFDFDFD)
    val shape = RoundedCornerShape(16.dp)
    val interactionSource = remember { MutableInteractionSource() }

    val machineType = if (order.sizeMachine) "Besar" else "Kecil"
    val stepMachine = order.stepMachine
    val machineNumber = order.numberMachine

    val (statusMessage, statusColor, statusIcon, iconTint) = when (stepMachine) {
        0 -> Quad(
            "Belum memilih dan menyalakan mesin cuci",
            Color.Red.copy(alpha = 0.1f),
            Icons.Default.ErrorOutline,
            Color.Red
        )
        1 -> Quad(
            "Belum memilih dan menyalakan mesin pengering",
            Color(0xFFFFF3CD),
            Icons.Default.WarningAmber,
            Color(0xFFFFA000)
        )
        2 -> Quad(
            "Sedang mencuci di mesin nomor $machineNumber",
            Color(0xFFE3F2FD),
            Icons.Default.LocalLaundryService,
            Color(0xFF2196F3)
        )
        3 -> Quad(
            "Sedang mengeringkan di mesin nomor $machineNumber",
            Color(0xFFE8F5E9),
            Icons.Default.DryCleaning,
            Color(0xFF4CAF50)
        )
        else -> Quad(
            "Status tidak diketahui",
            Color.LightGray.copy(alpha = 0.1f),
            Icons.AutoMirrored.Filled.HelpOutline,
            Color.Gray
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.Transparent, shape),
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
                    onClick = onSelect
                )
                .padding(16.dp)
        ) {
            Text(
                text = order.customerName ?: "Tanpa Nama",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoLabelValue(label = "Layanan", value = order.serviceName ?: "-", highlight = true)
                InfoLabelValue(label = "Tipe Mesin", value = machineType)
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
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                )
            }
        }
    }
}