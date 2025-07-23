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
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.room.machine.MachineRoom
import com.aluma.laundry.data.room.machine.MachineRoomViewModel
import com.aluma.laundry.data.room.order.OrderRoom
import com.aluma.laundry.ui.view.components.OrderInfo
import com.aluma.laundry.ui.view.components.dropdown.MachineDropdown
import com.aluma.laundry.utils.formatRupiah
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderBottomSheetInformation(
    machineRoomViewModel: MachineRoomViewModel = koinInject(),
    order: OrderRoom,
    onDismissRequest: () -> Unit,
    onSubmit: (MachineRoom?, onDone: () -> Unit) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val machines by machineRoomViewModel.machineFilter.collectAsState()
    var selectedMachine by remember { mutableStateOf<MachineRoom?>(null) }

    val availableMachines = machines.filter { !it.inUse }

    var isSubmitting by remember { mutableStateOf(false) }

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
            // Header
            Text(
                text = "Detail Pesanan",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            // Informasi Pesanan
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

            // Dropdown mesin
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Pilih Mesin yang Tersedia",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                MachineDropdown(
                    availableMachines = availableMachines,
                    selectedMachine = selectedMachine,
                    onMachineSelected = { selectedMachine = it },
                    enabled = availableMachines.isNotEmpty()
                )

                if (availableMachines.isEmpty()) {
                    Text(
                        text = "Semua mesin sedang digunakan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
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
                    onClick = {
                        isSubmitting = true
                        onSubmit(selectedMachine) {
                            isSubmitting = false
                            onDismissRequest() // ⬅️ Tutup setelah semua selesai
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = selectedMachine != null && !isSubmitting
                ) {
                    if (isSubmitting) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Memproses...")
                        }
                    } else {
                        Text("Mulai")
                    }
                }
            }
        }
    }
}