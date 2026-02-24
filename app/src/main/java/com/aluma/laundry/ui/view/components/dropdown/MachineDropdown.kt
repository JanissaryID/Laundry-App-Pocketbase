package com.aluma.laundry.ui.view.components.dropdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.laundry.R
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
                stringResource(R.string.machine_unit_format, it.numberMachine) + " (${if (it.sizeMachine) "12kg" else "7kg"})"
            } ?: stringResource(R.string.select_machine_unit_placeholder),
            onValueChange = {},
            label = { Text(stringResource(R.string.select_machine)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            leadingIcon = {
                Icon(
                    imageVector = if (selectedMachine?.sizeMachine == true) Icons.Default.Kitchen else Icons.Default.LocalLaundryService,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray
                )
            },
            modifier = Modifier
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = enabled
                )
                .fillMaxWidth(),
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            if (availableMachines.isEmpty()) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.no_machines_available)) },
                    onClick = {},
                    enabled = false
                )
            } else {
                // --- KELOMPOK MESIN KECIL ---
                if (smallMachines.isNotEmpty()) {
                    DropdownHeader(label = stringResource(R.string.small_machine_header))
                    smallMachines.forEach { machine ->
                        MachineItem(
                            number = machine.numberMachine.toString(),
                            onClick = {
                                onMachineSelected(machine)
                                expanded = false
                            }
                        )
                    }
                }

                // Divider antar kelompok jika keduanya ada
                if (smallMachines.isNotEmpty() && bigMachines.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }

                // --- KELOMPOK MESIN BESAR ---
                if (bigMachines.isNotEmpty()) {
                    DropdownHeader(label = stringResource(R.string.big_machine_header))
                    bigMachines.forEach { machine ->
                        MachineItem(
                            number = machine.numberMachine.toString(),
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

@Composable
fun DropdownHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun MachineItem(number: String, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.SettingsRemote,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.machine_unit_item_format, number), style = MaterialTheme.typography.bodyLarge)
            }
        },
        onClick = onClick
    )
}