package com.aluma.laundry.ui.view.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.api.machine.Machine
import com.aluma.laundry.data.api.machine.MachineViewModel
import com.aluma.laundry.data.api.service.Service
import com.aluma.laundry.data.api.service.ServiceViewModel
import com.aluma.laundry.utils.formatRupiah
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderBottomSheet(
    onDismissRequest: () -> Unit,
    serviceViewModel: ServiceViewModel = koinInject(),
    machineViewModel: MachineViewModel = koinInject(),
    onSubmit: (customerName: String, service: Service, machine: Machine) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var customerName by remember { mutableStateOf("") }
    var selectedService by remember { mutableStateOf<Service?>(null) }
    var selectedMachine by remember { mutableStateOf<Machine?>(null) }

    var serviceDropdownExpanded by remember { mutableStateOf(false) }
    var machineDropdownExpanded by remember { mutableStateOf(false) }

    val services by serviceViewModel.service.collectAsState()
    val machines by machineViewModel.machine.collectAsState()
    val availableMachines = machines.filter { !it.inUse }

    val smallMachines = availableMachines.filter { !it.sizeMachine }
    val bigMachines = availableMachines.filter { it.sizeMachine }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Buat Order", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("Nama Pelanggan") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Pilih Layanan", style = MaterialTheme.typography.labelMedium)

            ExposedDropdownMenuBox(
                expanded = serviceDropdownExpanded,
                onExpandedChange = { serviceDropdownExpanded = !serviceDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = selectedService?.nameService ?: "",
                    onValueChange = {},
                    label = { Text("Layanan") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceDropdownExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = serviceDropdownExpanded,
                    onDismissRequest = { serviceDropdownExpanded = false }
                ) {
                    if (services.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Tidak ada layanan tersedia") },
                            onClick = {},
                            enabled = false
                        )
                    } else {
                        services.forEach { service ->
                            DropdownMenuItem(

                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        service.nameService?.let { Text(it) }
                                        Text(formatRupiah(service.priceService.orEmpty()))
                                    }
                               },
                                onClick = {
                                    selectedService = service
                                    serviceDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Text("Pilih Mesin", style = MaterialTheme.typography.labelMedium)

            ExposedDropdownMenuBox(
                expanded = machineDropdownExpanded,
                onExpandedChange = { machineDropdownExpanded = !machineDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = selectedMachine?.let {
                        "Mesin ${it.numberMachine} - ${if (it.sizeMachine) "Besar (12kg)" else "Kecil (7kg)"}"
                    } ?: "",
                    onValueChange = {},
                    label = { Text("Mesin Tersedia") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = machineDropdownExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = machineDropdownExpanded,
                    onDismissRequest = { machineDropdownExpanded = false }
                ) {
                    if (availableMachines.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Tidak ada mesin tersedia") },
                            onClick = {},
                            enabled = false
                        )
                    } else {
                        if (smallMachines.isNotEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Mesin Kecil (7kg)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = {},
                                enabled = false
                            )
                            smallMachines.forEach { machine ->
                                DropdownMenuItem(
                                    text = { Text("Mesin ${machine.numberMachine}") },
                                    onClick = {
                                        selectedMachine = machine
                                        machineDropdownExpanded = false
                                    }
                                )
                            }
                        }

                        if (bigMachines.isNotEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Mesin Besar (12kg)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = {},
                                enabled = false
                            )
                            bigMachines.forEach { machine ->
                                DropdownMenuItem(
                                    text = { Text("Mesin ${machine.numberMachine}") },
                                    onClick = {
                                        selectedMachine = machine
                                        machineDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val service = selectedService
                    val machine = selectedMachine
                    if (service != null && machine != null) {
                        onSubmit(customerName, service, machine)
                        onDismissRequest()
                    }
                },
                enabled = customerName.isNotBlank() && selectedService != null && selectedMachine != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lanjutkan")
            }
        }
    }
}
