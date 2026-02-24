package com.aluma.owner.ui.view.components.bottomsheet

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aluma.owner.R
import com.aluma.owner.data.machine.remote.MachineRemoteViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineBottomSheet(
    onDismissRequest: () -> Unit,
    machineRemoteViewModel: MachineRemoteViewModel = koinInject(),
) {
    val machine by machineRemoteViewModel.selectedMachineRemote.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Inisialisasi timer dari data mesin
    var timerMachine by remember { mutableStateOf(machine?.timer?.toString() ?: "0") }
    var isSubmitting by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp), // Extra padding bawah
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- HEADER DENGAN IKON ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = if (machine?.typeMachine == true) Color(0xFFFFF3E0) else Color(0xFFE3F2FD),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (machine?.typeMachine == true) Icons.Default.LocalFireDepartment else Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = if (machine?.typeMachine == true) Color(0xFFEF6C00) else Color(0xFF1976D2),
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        stringResource(R.string.machine_bs_timer_settings),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(
                            R.string.machine_bs_machine_title,
                            if (machine?.typeMachine == true) stringResource(R.string.machine_bs_type_dryer) else stringResource(R.string.machine_bs_type_washer),
                            machine?.numberMachine ?: ""
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
            }

            // --- INPUT TIMER ---
            OutlinedTextField(
                value = timerMachine,
                onValueChange = { input ->
                    // Hanya angka dan maksimal 3 digit
                    if (input.all { it.isDigit() } && input.length <= 3) {
                        timerMachine = input
                    }
                },
                label = { Text(stringResource(R.string.machine_bs_label_duration)) },
                placeholder = { Text(stringResource(R.string.machine_bs_placeholder_duration)) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Timer, null, tint = Color.Gray) },
                suffix = { Text(stringResource(R.string.machine_bs_suffix_minutes), color = Color.Gray) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // --- QUICK ACTION BUTTONS (Fitur Tambahan) ---
            Text(
                stringResource(R.string.machine_bs_quick_add),
                style = MaterialTheme.typography.labelLarge,
                color = Color.DarkGray
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val increments = listOf(5, 10, 15, 20)
                increments.forEach { min ->
                    SuggestionChip(
                        onClick = {
                            val current = timerMachine.toIntOrNull() ?: 0
                            timerMachine = (current + min).toString()
                        },
                        label = { Text(stringResource(R.string.machine_bs_quick_add_val, min)) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // --- TOMBOL SIMPAN ---
            Button(
                onClick = {
                    val timer = timerMachine.toIntOrNull()
                    if (machine != null && timer != null) {
                        isSubmitting = true
                        machine?.id?.let { id ->
                            machineRemoteViewModel.updateTimer(id, timer) { success ->
                                isSubmitting = false
                                if (success) onDismissRequest()
                            }
                        }
                    }
                },
                enabled = !isSubmitting && timerMachine.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(stringResource(R.string.machine_bs_button_update), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}