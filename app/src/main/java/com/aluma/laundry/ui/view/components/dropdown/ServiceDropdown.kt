package com.aluma.laundry.ui.view.components.dropdown

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.aluma.laundry.data.service.model.ServiceRemote
import com.aluma.laundry.utils.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDropdown(
    serviceRemotes: List<ServiceRemote>,
    selectedServiceRemote: ServiceRemote?,
    onServiceSelected: (ServiceRemote) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedServiceRemote?.let {
                "${it.nameService.orEmpty()} (${if (it.sizeMachine) "Besar" else "Kecil"})"
            } ?: "",
            onValueChange = {},
            label = { Text("Layanan") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (serviceRemotes.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Tidak ada layanan tersedia") },
                    onClick = {},
                    enabled = false
                )
            } else {
                serviceRemotes.forEach { service ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${service.nameService.orEmpty()} (${if (service.sizeMachine) "Besar" else "Kecil"})")
                                Text(formatRupiah(service.priceService.orEmpty()))
                            }
                        },
                        onClick = {
                            onServiceSelected(service)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
