package com.aluma.laundry.ui.view.components.dropdown

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.aluma.laundry.data.machine.model.MachineLocal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineDropdown(
    availableMachines: List<MachineLocal>,
    selectedMachine: MachineLocal?,
    onMachineSelected: (MachineLocal) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    val smallMachines = availableMachines.filter { !it.sizeMachine }
    val bigMachines = availableMachines.filter { it.sizeMachine }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
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
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(
                    type = MenuAnchorType.PrimaryNotEditable,
                    enabled = enabled
                )
                .fillMaxWidth(),
            enabled = enabled
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
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
                                onMachineSelected(machine)
                                expanded = false
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
                                onMachineSelected(machine)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

