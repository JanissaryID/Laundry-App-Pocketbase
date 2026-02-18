package com.aluma.laundry.ui.view.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.machine.local.MachineLocalViewModel
import com.aluma.laundry.ui.view.components.EmptyState
import com.aluma.laundry.ui.view.components.bottomsheet.MachineBottomSheetInformationTime
import com.aluma.laundry.ui.view.components.itemscard.ItemMachineCard
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenMachine(
    machineLocalViewModel: MachineLocalViewModel = koinInject(),
    onBack: () -> Unit,
) {
    val machines by machineLocalViewModel.machines.collectAsState()
    val selectedMachine by machineLocalViewModel.selectedMachine.collectAsState()

    var showSheetMachineRunning by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF8F9FA), // Latar belakang abu-abu muda agar card menonjol
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Status Mesin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${machines.size} Mesin Terdaftar", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (machines.isEmpty()) {
                EmptyState(
                    title = "Belum Ada Mesin",
                    message = "Hubungi pengembang untuk mendaftarkan mesin laundry di cabang ini.",
                    icon = Icons.Default.Dns // Ikon yang merepresentasikan server/hardware
                )
            } else {
                // Statistik Singkat
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val inUseCount = machines.count { it.inUse }
                    val availableCount = machines.size - inUseCount

                    MachineStatusChip(label = "Tersedia", count = availableCount, color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
                    MachineStatusChip(label = "Berjalan", count = inUseCount, color = Color(0xFF2196F3), modifier = Modifier.weight(1f))
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(machines, key = { it.id ?: 0 }) { machine ->
                        ItemMachineCard(
                            machine = machine,
                            onClick = {
                                machineLocalViewModel.setSelectMachine(machine)
                                if (machine.inUse) {
                                    showSheetMachineRunning = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // ====================
    // BOTTOM SHEETS
    // ====================

    if (showSheetMachineRunning) {
        selectedMachine?.let {
            MachineBottomSheetInformationTime(
                machine = it,
                onDismissRequest = {
                    showSheetMachineRunning = false
                }
            )
        }
    }
}

@Composable
fun MachineStatusChip(label: String, count: Int, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$count $label",
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}