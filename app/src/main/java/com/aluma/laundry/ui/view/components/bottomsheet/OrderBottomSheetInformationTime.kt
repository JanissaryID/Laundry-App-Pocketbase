package com.aluma.laundry.ui.view.components.bottomsheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.api.order.model.Quad
import com.aluma.laundry.data.room.machine.MachineRoom
import com.aluma.laundry.data.room.order.OrderRoom
import com.aluma.laundry.ui.view.components.CountdownTimer
import com.aluma.laundry.ui.view.components.OrderInfo
import com.aluma.laundry.utils.formatRupiah
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderBottomSheetInformationTime(
    order: OrderRoom,
    machine: MachineRoom,
    stepMachine: Int,
    machineNumber: Int?,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSubmitting by remember { mutableStateOf(false) }

    val (startTimeMillis, endTimeMillis) = remember(machine.timeOn, machine.timer) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSX")
            .withZone(ZoneOffset.UTC)

        try {
            val startInstant = formatter.parse(machine.timeOn, Instant::from)
            val endInstant = startInstant.plus(Duration.ofMinutes(machine.timer.toLong()))
            startInstant.toEpochMilli() to endInstant.toEpochMilli()
        } catch (e: Exception) {
            val now = System.currentTimeMillis()
            now to now
        }
    }

    val currentTimeMillis = System.currentTimeMillis()
    val isTimeUp = currentTimeMillis >= endTimeMillis

    // 🌟 Header Status Mesin Berdasarkan stepMachine
    val (statusMessage, statusColor, statusIcon, iconTint) = when (stepMachine) {
        2 -> Quad(
            "Sedang mencuci di mesin nomor ${machineNumber ?: "-"}",
            Color(0xFFE3F2FD),
            Icons.Default.LocalLaundryService,
            Color(0xFF2196F3)
        )
        3 -> Quad(
            "Sedang mengeringkan di mesin nomor ${machineNumber ?: "-"}",
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

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 🧾 Header Dinamis
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = iconTint
            )

            // Detail Pesanan
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OrderInfo(label = "Nama Pelanggan", value = order.customerName ?: "-")
                OrderInfo(
                    label = "Layanan",
                    value = "${order.serviceName ?: "-"} (${if (order.sizeMachine) "Mesin Besar" else "Mesin Kecil"})"
                )
                OrderInfo(
                    label = "Harga & Pembayaran",
                    value = "${formatRupiah(order.price ?: "0")} • ${order.typePayment ?: "-"}"
                )
            }

            HorizontalDivider()

            // ⏳ Countdown Mesin
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Sisa Waktu Mesin",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                CountdownTimer(
                    startTimeMillis = startTimeMillis,
                    endTimeMillis = endTimeMillis,
                    onFinish = { println("Waktu mesin habis!") },
                    modifier = Modifier.padding(16.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        // TODO: Handle Print
                        println("Print clicked")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp), // penting untuk bentuk bulat proporsional
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Print")
                }

                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Tutup")
                }
            }
        }
    }
}
