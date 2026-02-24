package com.aluma.laundry.ui.view.components.itemscard

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
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.laundry.R
import com.aluma.laundry.data.order.model.OrderLocal
import com.aluma.laundry.data.order.utils.Quad

@Composable
fun ItemOrderCard(
    order: OrderLocal,
    onSelect: () -> Unit = {}
) {
    val shape = RoundedCornerShape(16.dp)

    // Logika Status (Tetap menggunakan logika stepMachine kamu)
    val machineNumber = order.numberMachine
    val (statusMessage, statusColor, statusIcon, iconTint) = when (order.stepMachine) {
        0 -> Quad(
            stringResource(R.string.needs_activation_washer),
            Color(0xFFFFEBEE), // Merah sangat muda
            Icons.Default.ErrorOutline,
            Color(0xFFD32F2F)
        )
        1 -> Quad(
            stringResource(R.string.needs_activation_dryer),
            Color(0xFFFFF8E1), // Kuning sangat muda
            Icons.Default.WarningAmber,
            Color(0xFFFFA000)
        )
        2 -> Quad(
            stringResource(R.string.washing_at_number, machineNumber ?: 0),
            Color(0xFFE3F2FD),
            Icons.Default.LocalLaundryService,
            Color(0xFF2196F3)
        )
        3 -> Quad(
            stringResource(R.string.drying_at_number, machineNumber ?: 0),
            Color(0xFFF3E5F5),
            Icons.Default.DryCleaning,
            Color(0xFF9C27B0)
        )
        else -> Quad(
            stringResource(R.string.done),
            Color(0xFFE8F5E9),
            Icons.Default.CheckCircleOutline,
            Color(0xFF4CAF50)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onSelect // Gunakan onClick bawaan Card agar Ripple Effect rapi mengikuti Shape
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Baris Atas: Nama & Waktu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.customerName.orEmpty().ifBlank { stringResource(R.string.default_customer_name) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2D3142)
                )

                // Format tanggal/waktu (Misal diambil dari string order.date)
                Text(
                    text = order.date?.take(10) ?: "", // Menampilkan YYYY-MM-DD
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Baris Tengah: Layanan & Harga
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Label Layanan
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.serviceName.orEmpty(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Info Tipe Mesin
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Straighten, // Ikon ukuran
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (order.sizeMachine) stringResource(R.string.capacity_large_machine) else stringResource(R.string.capacity_small_machine),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Baris Bawah: Status Progress (Indikator Utama Admin)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = statusColor,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = iconTint
                    )

                    if (order.stepMachine < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.BluetoothConnected,
                            contentDescription = stringResource(R.string.ready_to_sync),
                            tint = iconTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}