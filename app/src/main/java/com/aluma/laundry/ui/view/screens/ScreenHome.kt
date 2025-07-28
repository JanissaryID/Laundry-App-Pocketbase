package com.aluma.laundry.ui.view.screens

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aluma.laundry.bluetooth.BluetoothHelper
import com.aluma.laundry.bluetooth.BluetoothSender
import com.aluma.laundry.data.logmachine.local.LogMachineLocalViewModel
import com.aluma.laundry.data.logmachine.model.LogMachineLocal
import com.aluma.laundry.data.machine.local.MachineLocalViewModel
import com.aluma.laundry.data.machine.remote.MachineRemoteViewModel
import com.aluma.laundry.data.order.local.OrderLocalViewModel
import com.aluma.laundry.data.order.utils.SyncStatus
import com.aluma.laundry.data.service.remote.ServiceRemoteViewModel
import com.aluma.laundry.data.store.remote.StoreRemoteViewModel
import com.aluma.laundry.ui.view.components.EmptyState
import com.aluma.laundry.ui.view.components.bottomsheet.OrderBottomSheet
import com.aluma.laundry.ui.view.components.bottomsheet.OrderBottomSheetInformation
import com.aluma.laundry.ui.view.components.bottomsheet.OrderBottomSheetInformationTime
import com.aluma.laundry.ui.view.components.fab.FabWithSubmenu
import com.aluma.laundry.ui.view.components.itemscard.ItemOrderCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNUSED_VARIABLE")
@Composable
fun ScreenHome(
    storeRemoteViewModel: StoreRemoteViewModel = koinInject(),
    machineLocalViewModel: MachineLocalViewModel = koinInject(),
    orderLocalViewModel: OrderLocalViewModel = koinInject(),
    machineRemoteViewModel: MachineRemoteViewModel = koinInject(),
    serviceRemoteViewModel: ServiceRemoteViewModel = koinInject(),
    logMachineLocalViewModel: LogMachineLocalViewModel = koinInject(),
    bluetoothHelper: BluetoothHelper,
    onNavigateMachine: () -> Unit,
    onNavigateOrder: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val nameStore by storeRemoteViewModel.nameStore.collectAsState()
    val orders by orderLocalViewModel.ordersFilter.collectAsState()
    val machines by machineLocalViewModel.machines.collectAsState()
    val selectedOrder by orderLocalViewModel.selectedOrder.collectAsState()

    var isFabExpanded by remember { mutableStateOf(false) }

    var showOrderSheet by remember { mutableStateOf(false) }
    var showOrderSheetMachine by remember { mutableStateOf(false) }
    var showOrderSheetMachineRunning by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { nameStore?.let { Text(it) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = { onNavigateSettings() }) {
                        Icon(Icons.Default.Settings, contentDescription = "Pengaturan")
                    }
                }
            )
        },
        floatingActionButton = {
            FabWithSubmenu(
                isFabExpanded = isFabExpanded,
                onFabToggle = {
                    machineRemoteViewModel.fetchMachine()
                    serviceRemoteViewModel.fetchServices()
                    isFabExpanded = !isFabExpanded
                },
                onDismissRequest = { isFabExpanded = false },
                listMachine = onNavigateMachine,
                listOrder = onNavigateOrder,
                addOrder = { showOrderSheet = true }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (orders.isEmpty()) {
                EmptyState(
                    title = "Belum ada pesanan",
                    message = "Buat pesanan di sistem,\nkemudian akan muncul di sini secara otomatis."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(orders) { order ->
                        ItemOrderCard(
                            order = order,
                            onSelect = {
                                orderLocalViewModel.setSelectedOrder(order)
                                if (order.stepMachine < 2) {
                                    machineLocalViewModel.filterMachine(
                                        type = order.typeMachineService,
                                        size = order.sizeMachine,
                                        stepMachine = order.stepMachine
                                    )
                                    showOrderSheetMachine = true
                                } else {
                                    showOrderSheetMachineRunning = true
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

    if (showOrderSheet) {
        OrderBottomSheet(
            onDismissRequest = { showOrderSheet = false },
            onSubmit = { orderLocalViewModel.addOrder(orderLocal = it) }
        )
    }

    if (showOrderSheetMachine && selectedOrder != null) {
        OrderBottomSheetInformation(
            order = selectedOrder!!,
            onDismissRequest = { showOrderSheetMachine = false },
            onSubmit = { machine, onDone ->
                if (machine != null) {
                    bluetoothHelper.requestBluetooth {
                        val sender = BluetoothSender()

                        // Jalankan di coroutine
                        CoroutineScope(Dispatchers.IO).launch {
                            val success = sender.sendToESP(
                                context = context,
                                address = machine.bluetoothAddress.orEmpty(),
                                message = "m:${machine.numberMachine},t:${machine.timer},s:true"
                            )

                            withContext(Dispatchers.Main) {
                                if (success) {
                                    Log.w("BluetoothSender", "Ok Kirim ke ${machine.numberMachine}")

                                    val updatedOrder = selectedOrder!!.copy(
                                        stepMachine = selectedOrder!!.stepMachine + 2,
                                        numberMachine = machine.numberMachine,
                                    )
                                    orderLocalViewModel.updateOrder(orderLocal = updatedOrder)

                                    val later = Instant.now()
                                    val formatted = DateTimeFormatter
                                        .ofPattern("yyyy-MM-dd HH:mm:ss.SSSX")
                                        .withZone(ZoneOffset.UTC)
                                        .format(later)

                                    val updatedMachine = machine.copy(
                                        inUse = true,
                                        order = selectedOrder!!.id,
                                        timeOn = formatted
                                    )
                                    machineLocalViewModel.updateMachine(updatedMachine)

                                    val logMachine = LogMachineLocal(
                                        numberMachine = machine.numberMachine,
                                        sizeMachine = machine.sizeMachine,
                                        typeMachine = machine.typeMachine,
                                        user = machine.user,
                                        store = machine.store,
                                        date = formatted,
                                        syncStatus = SyncStatus.PENDING
                                    )
                                    logMachineLocalViewModel.addLogMachine(logMachine)

                                    Toast.makeText(context, "Terkirim ke ESP", Toast.LENGTH_SHORT).show()
                                } else {
                                    Log.w("BluetoothSender", "Gagal Kirim")
                                    Toast.makeText(context, "Gagal kirim", Toast.LENGTH_SHORT).show()
                                }

                                onDone()
                            }
                        }
                    }
                }
            }
        )
    }

    if (showOrderSheetMachineRunning && selectedOrder != null) {
        val selectedMachine = machines.find { it.numberMachine == selectedOrder!!.numberMachine }
        if (selectedMachine != null) {
            OrderBottomSheetInformationTime(
                order = selectedOrder!!,
                machine = selectedMachine,
                stepMachine = selectedOrder!!.stepMachine,
                onDismissRequest = { showOrderSheetMachineRunning = false },
                machineNumber = selectedOrder!!.numberMachine
            )
        }
    }
}