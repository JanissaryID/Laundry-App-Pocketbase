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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.laundry.R
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
        2 -> Triple(stringResource(id = R.string.status_washing), Color(0xFF2196F3), Icons.Default.LocalLaundryService)
        3 -> Triple(stringResource(id = R.string.status_drying), Color(0xFF4CAF50), Icons.Default.DryCleaning)
        else -> Triple(stringResource(id = R.string.status_active), Color.Gray, Icons.Default.Info)
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
                        text = stringResource(id = R.string.machine_unit_number, machineNumber ?: 0),
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
                    OrderInfoRow(label = stringResource(id = R.string.customer), value = order.customerName ?: "-")
                    OrderInfoRow(label = stringResource(id = R.string.service), value = order.serviceName ?: "-")
                    OrderInfoRow(label = stringResource(id = R.string.capacity), value = if (order.sizeMachine) stringResource(id = R.string.capacity_large) else stringResource(id = R.string.capacity_small))
                    OrderInfoRow(label = stringResource(id = R.string.total_payment), value = formatRupiah(order.price ?: "0"), isHighlighted = true)
                }
            }

            // --- COUNTDOWN & ESTIMASI ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.time_left),
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
                        text = stringResource(id = R.string.estimated_finish_at, endTimeStr),
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
                        if (!stat) Toast.makeText(context, context.getString(R.string.print_failed_toast), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.Print, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(id = R.string.print_receipt))
                }

                // Tutup
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = statusColor)
                ) {
                    Text(stringResource(id = R.string.back), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}