package com.aluma.laundry.ui.view.components.bottomsheet

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.aluma.laundry.data.machine.local.MachineLocalViewModel
import com.aluma.laundry.data.machine.model.MachineLocal
import com.aluma.laundry.data.order.model.OrderLocal
import com.aluma.laundry.data.order.utils.TypePayment
import com.aluma.laundry.ui.view.components.dropdown.MachineDropdown
import com.aluma.laundry.utils.formatRupiah
import org.koin.compose.koinInject

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
    val bluetoothAddress by storePreferenceViewModel.bluetoothAddress.collectAsState()

    var isSubmitting by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
            // --- HEADER ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(id = R.string.machine_activation),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // --- RINGKASAN ORDER (Visual Card) ---
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OrderInfoRow(label = stringResource(id = R.string.customer), value = order.customerName ?: "-")
                    OrderInfoRow(
                        label = stringResource(id = R.string.service),
                        value = order.serviceName ?: "-",
                        isHighlighted = true
                    )
                    OrderInfoRow(
                        label = stringResource(id = R.string.capacity),
                        value = if (order.sizeMachine) stringResource(id = R.string.capacity_large) else stringResource(id = R.string.capacity_small)
                    )
                    OrderInfoRow(
                        label = stringResource(id = R.string.total),
                        value = buildString {
                            append(formatRupiah(order.price ?: "0"))
                            order.typePayment?.let { type ->
                                val resId = try {
                                    TypePayment.valueOf(type).labelRes
                                } catch (e: Exception) {
                                    null
                                }
                                val label = resId?.let { stringResource(it) } ?: type
                                append(" ($label)")
                            }
                        }
                    )
                }
            }

            // --- PEMILIHAN MESIN ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.select_machine_unit),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                MachineDropdown(
                    availableMachines = availableMachines,
                    selectedMachine = selectedMachine,
                    onMachineSelected = { selectedMachine = it },
                    enabled = availableMachines.isNotEmpty()
                )

                if (availableMachines.isEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.no_machines_available_warning),
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- ACTION BUTTONS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tombol Print Nota
                OutlinedButton(
                    onClick = {
                        val printer = BluetoothPrinter()
                        val success = printer.printLaundryReceipt(
                            context = context,
                            address = bluetoothAddress,
                            storeName = nameStore.orEmpty(),
                            storeAddress = addressStore.orEmpty(),
                            storeCity = cityStore.orEmpty(),
                            services = Pair(order.serviceName.orEmpty(), order.price.orEmpty()),
                            customerName = order.customerName.orEmpty(),
                            paymentMethod = order.typePayment.orEmpty()
                        )
                        if (!success) {
                            Toast.makeText(context, context.getString(R.string.print_failed_toast), Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(0.4f).height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(20.dp))
                }

                // Tombol Mulai (Bluetooth Command)
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
                    modifier = Modifier.weight(0.6f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSubmitting && selectedMachine != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMachine != null) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        Text(stringResource(id = R.string.activate_machine), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderInfoRow(label: String, value: String, isHighlighted: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
    }
}