package com.aluma.laundry.ui.view.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.api.machine.Machine
import com.aluma.laundry.data.api.machine.MachineViewModel
import com.aluma.laundry.data.api.service.Service
import com.aluma.laundry.data.api.service.ServiceViewModel
import com.aluma.laundry.ui.view.components.dropdown.MachineDropdown
import com.aluma.laundry.ui.view.components.dropdown.ServiceDropdown
import com.aluma.laundry.utils.capitalizeEachWord
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

    val services by serviceViewModel.service.collectAsState()
    val machines by machineViewModel.machineFilter.collectAsState()
    val availableMachines = machines.filter { !it.inUse }

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
                onValueChange = { input ->
                    customerName = input
                        .split(" ")
                        .joinToString(" ") { word ->
                            word.lowercase().replaceFirstChar { it.uppercase() }
                        }
                },
                label = { Text("Nama Pelanggan") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            ServiceDropdown(
                services = services,
                selectedService = selectedService,
                onServiceSelected = {
                    selectedMachine = null
                    selectedService = it
                    machineViewModel.filterMachine(type = it.typeMachine, size = it.sizeMachine)
                }
            )

            MachineDropdown(
                availableMachines = availableMachines,
                selectedMachine = selectedMachine,
                onMachineSelected = { selectedMachine = it },
                enabled = selectedService != null && machines.isNotEmpty()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = selectedService?.let {
                            "${it.nameService} - ${formatRupiah(it.priceService)}"
                        } ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedMachine?.let {
                            "Mesin ${it.numberMachine} - ${if (it.sizeMachine) "Besar (12kg)" else "Kecil (7kg)"}"
                        } ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        val service = selectedService
                        val machine = selectedMachine
                        if (service != null && machine != null) {
                            onSubmit(customerName, service, machine)
                            onDismissRequest()
                        }
                    },
                    enabled = customerName.isNotBlank() && selectedService != null && if(selectedService!!.typeMachine < 3) selectedMachine != null else true,
                ) {
                    Text("Lanjutkan")
                }
            }
        }
    }
}
