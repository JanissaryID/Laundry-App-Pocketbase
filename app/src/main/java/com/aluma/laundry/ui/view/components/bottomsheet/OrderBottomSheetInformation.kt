package com.aluma.laundry.ui.view.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.api.machine.Machine
import com.aluma.laundry.data.api.machine.MachineViewModel
import com.aluma.laundry.data.api.order.model.Order
import com.aluma.laundry.ui.view.components.dropdown.MachineDropdown
import com.aluma.laundry.utils.formatRupiah
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderBottomSheetInformation(
    machineViewModel: MachineViewModel = koinInject(),
    order: Order,
    onDismissRequest: () -> Unit,
    onSubmit: (Machine?, onDone: () -> Unit) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val machines by machineViewModel.machineFilter.collectAsState()
    var selectedMachine by remember { mutableStateOf<Machine?>(null) }

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

            Button(
                onClick = {
                    isSubmitting = true
                    onSubmit(selectedMachine) {
                        isSubmitting = false
                        onDismissRequest() // ⬅️ Tutup setelah semua selesai
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
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
                    Text("Konfirmasi & Mulai")
                }
            }
        }
    }
}

@Composable
fun OrderInfo(label: String, value: String, isHighlight: Boolean = false) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isHighlight) {
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        )
    }
}


