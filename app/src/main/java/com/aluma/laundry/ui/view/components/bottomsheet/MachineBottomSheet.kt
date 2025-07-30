package com.aluma.laundry.ui.view.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.machine.remote.MachineRemoteViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineBottomSheet(
    onDismissRequest: () -> Unit,
    machineRemoteViewModel: MachineRemoteViewModel = koinInject(),
) {
    val machine by machineRemoteViewModel.selectedMachineRemote.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var timerMachine by remember { mutableStateOf(machine?.timer?.toString() ?: "") }
    var isSubmitting by remember { mutableStateOf(false) }

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
            Text(
                text = "Mesin ${if (machine?.typeMachine == true) "Pengering" else "Cuci"} #${machine?.numberMachine}",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = timerMachine,
                onValueChange = { input ->
                    timerMachine = input.filter { it.isDigit() }
                },
                label = { Text("Timer Mesin (Menit)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )

            Button(
                onClick = {
                    val timer = timerMachine.toIntOrNull()
                    val currentMachine = machine

                    if (currentMachine != null && timer != null) {
                        isSubmitting = true

                        currentMachine.id?.let {
                            machineRemoteViewModel.updateTimer(
                                machineId = it,
                                newTimer = timer
                            ) { success ->
                                isSubmitting = false
                                if (success) {
                                    onDismissRequest()
                                }
                            }
                        }
                    }
                },
                enabled = !isSubmitting && timerMachine.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text("Simpan")
                }
            }
        }
    }
}