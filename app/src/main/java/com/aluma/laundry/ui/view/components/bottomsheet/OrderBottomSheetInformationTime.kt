package com.aluma.laundry.ui.view.components.bottomsheet

import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.laundry.bluetooth.BluetoothPrinter
import com.aluma.laundry.data.datastore.StorePreferenceViewModel
import com.aluma.laundry.data.machine.model.MachineLocal
import com.aluma.laundry.data.order.model.OrderLocal
import com.aluma.laundry.data.order.utils.Quad
import com.aluma.laundry.ui.view.components.CountdownTimer
import com.aluma.laundry.utils.formatRupiah
import org.koin.compose.koinInject
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderBottomSheetInformationTime(
    order: OrderLocal,
    machine: MachineLocal,
    stepMachine: Int,
    machineNumber: Int?,
    storePreferenceViewModel: StorePreferenceViewModel = koinInject(),
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val bluetoothAddress by storePreferenceViewModel.bluetoothAddress.collectAsState()
    val nameStore by storePreferenceViewModel.nameStore.collectAsState()
    val addressStore by storePreferenceViewModel.addressStore.collectAsState()
    val cityStore by storePreferenceViewModel.cityStore.collectAsState()

    val context = LocalContext.current

    // Kalkulasi Waktu (Mulai & Estimasi Selesai)
    val (startTimeStr, endTimeStr, startTimeMillis, endTimeMillis) = remember(machine.timeOn, machine.timer) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSX").withZone(ZoneOffset.UTC)
        val timeDisplayFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

        try {
            val startInstant = formatter.parse(machine.timeOn, Instant::from)
            val endInstant = startInstant.plus(Duration.ofMinutes(machine.timer.toLong()))

            Quad(
                timeDisplayFormatter.format(startInstant),
                timeDisplayFormatter.format(endInstant),
                startInstant.toEpochMilli(),
                endInstant.toEpochMilli()
            )
        } catch (e: Exception) {
            val now = System.currentTimeMillis()
            Quad("--:--", "--:--", now, now)
        }
    }

    // Identifikasi Status Berdasarkan Step
    val (statusTitle, statusColor, statusIcon) = when (stepMachine) {
        2 -> Triple("Proses Pencucian", Color(0xFF2196F3), Icons.Default.LocalLaundryService)
        3 -> Triple("Proses Pengeringan", Color(0xFF4CAF50), Icons.Default.DryCleaning)
        else -> Triple("Status Aktif", Color.Gray, Icons.Default.Info)
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- HEADER STATUS ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = statusTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Unit Mesin Nomor $machineNumber",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // --- DETAIL PELANGGAN ---
            Surface(
                color = Color(0xFFF8F9FA),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OrderInfoRow(label = "Pelanggan", value = order.customerName ?: "-")
                    OrderInfoRow(label = "Layanan", value = order.serviceName ?: "-")
                    OrderInfoRow(label = "Kapasitas", value = if (order.sizeMachine) "Mesin Besar" else "Mesin Kecil")
                    OrderInfoRow(label = "Total Bayar", value = formatRupiah(order.price ?: "0"), isHighlighted = true)
                }
            }

            // --- COUNTDOWN & ESTIMASI ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sisa Waktu",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )

                CountdownTimer(
                    startTimeMillis = startTimeMillis,
                    endTimeMillis = endTimeMillis,
                    onFinish = { /* Notifikasi Selesai */ },
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Text(
                        text = "Estimasi selesai pukul $endTimeStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // --- BUTTONS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cetak Nota Ulang (Jika perlu)
                OutlinedButton(
                    onClick = {
                        val printer = BluetoothPrinter()
                        val stat = printer.printLaundryReceipt(
                            context = context,
                            address = bluetoothAddress,
                            storeName = nameStore.orEmpty(),
                            storeAddress = addressStore.orEmpty(),
                            storeCity = cityStore.orEmpty(),
                            services = Pair(order.serviceName.orEmpty(), order.price.orEmpty()),
                            customerName = order.customerName.orEmpty(),
                            paymentMethod = order.typePayment.orEmpty()
                        )
                        if (!stat) Toast.makeText(context, "Printer Gagal", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.Print, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Nota")
                }

                // Tutup
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = statusColor)
                ) {
                    Text("Kembali", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}