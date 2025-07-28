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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.laundry.bluetooth.BluetoothPrinter
import com.aluma.laundry.data.datastore.StorePreferenceViewModel
import com.aluma.laundry.data.machine.local.MachineLocalViewModel
import com.aluma.laundry.data.machine.model.MachineLocal
import com.aluma.laundry.data.order.model.OrderLocal
import com.aluma.laundry.ui.view.components.OrderInfo
import com.aluma.laundry.ui.view.components.dropdown.MachineDropdown
import com.aluma.laundry.utils.formatRupiah
import org.koin.compose.koinInject

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderBottomSheetInformation(
    machineLocalViewModel: MachineLocalViewModel = koinInject(),
    storePreferenceViewModel: StorePreferenceViewModel = koinInject(),
    order: OrderLocal,
    onDismissRequest: () -> Unit,
    onSubmit: (MachineLocal?, onDone: () -> Unit) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val machines by machineLocalViewModel.machineFilter.collectAsState()
    var selectedMachine by remember { mutableStateOf<MachineLocal?>(null) }

    val availableMachines = machines.filter { !it.inUse }

    val nameStore by storePreferenceViewModel.nameStore.collectAsState()
    val addressStore by storePreferenceViewModel.addressStore.collectAsState()
    val cityStore by storePreferenceViewModel.cityStore.collectAsState()

    var isSubmitting by remember { mutableStateOf(false) }

    val bluetoothAddress by storePreferenceViewModel.bluetoothAddress.collectAsState()
    val context = LocalContext.current

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
                        if (!stat){
                            Toast.makeText(context, "Gagal Mencetak", Toast.LENGTH_SHORT).show()
                        }
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
                    Text("Nota")
                }

                Button(
                    onClick = {
                        if (!isSubmitting) {
                            isSubmitting = true
                            onSubmit(selectedMachine) {
                                isSubmitting = false
                                onDismissRequest()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = !isSubmitting && selectedMachine != null, // ⬅️ Perubahan di sini
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