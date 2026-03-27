package com.aluma.owner.ui.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.owner.R
import com.aluma.owner.data.machine.remote.MachineRemoteViewModel
import com.aluma.owner.data.realtime.RealtimeViewModel
import com.aluma.owner.ui.view.components.EmptyState
import com.aluma.owner.ui.view.components.bottomsheet.MachineBottomSheet
import com.aluma.owner.ui.view.components.itemscard.ItemMachineCard
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenMachine(
    machineRemoteViewModel: MachineRemoteViewModel = koinInject(),
    realtimeViewModel: RealtimeViewModel = koinInject(),
    onBack: () -> Unit,
) {
    val machines by machineRemoteViewModel.machineRemote.collectAsState()
    val storeId by machineRemoteViewModel.storeId.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }

    // Realtime SSE: re-fetch machines when server-side events arrive
    LaunchedEffect(Unit) {
        realtimeViewModel.realtimeEvent.collect { collectionName ->
            if (collectionName == "LaundryMachine") {
                machineRemoteViewModel.fetchMachine(storeId.orEmpty())
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.machine_list_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.machine_list_count, machines.size), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA)) // Background soft gray konsisten
        ) {
                if (machines.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = stringResource(R.string.machine_list_empty_title),
                        message = stringResource(R.string.machine_list_empty_message)
                    )
                }
            } else {
                // List Mesin
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp), // Jarak antar kartu lebih lega
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(machines) { machine ->
                        ItemMachineCard(
                            machine = machine,
                            onClick = {
                                machineRemoteViewModel.setSelectedMachine(machine)
                                showBottomSheet = true
                            }
                        )
                    }
                    // Spacer bawah agar nyaman discroll
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    if (showBottomSheet) {
        MachineBottomSheet(
            onDismissRequest = { showBottomSheet = false }
        )
    }
}