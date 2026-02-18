package com.aluma.laundry.ui.view.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.laundry.bluetooth.BluetoothHelper
import com.aluma.laundry.bluetooth.BluetoothSender
import com.aluma.laundry.data.logmachine.local.LogMachineLocalViewModel
import com.aluma.laundry.data.logmachine.model.LogMachineLocal
import com.aluma.laundry.data.machine.local.MachineLocalViewModel
import com.aluma.laundry.data.machine.model.MachineLocal
import com.aluma.laundry.data.machine.remote.MachineRemoteViewModel
import com.aluma.laundry.data.order.local.OrderLocalViewModel
import com.aluma.laundry.data.order.model.OrderLocal
import com.aluma.laundry.data.order.utils.SyncStatus
import com.aluma.laundry.data.service.remote.ServiceRemoteViewModel
import com.aluma.laundry.data.store.StoreRemoteViewModel
import com.aluma.laundry.ui.view.components.EmptyState
import com.aluma.laundry.ui.view.components.bottomsheet.OrderBottomSheet
import com.aluma.laundry.ui.view.components.bottomsheet.OrderBottomSheetInformation
import com.aluma.laundry.ui.view.components.bottomsheet.OrderBottomSheetInformationTime
import com.aluma.laundry.ui.view.components.fab.FabWithSubmenu
import com.aluma.laundry.ui.view.components.itemscard.ItemOrderCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
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
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = nameStore ?: "Aluma Laundry",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "• Mode Admin •",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
        // floatingActionButton sengaja dikosongkan agar kita bisa mengontrol posisi custom FAB secara bebas
    ) { innerPadding ->

        // --- CONTAINER UTAMA ---
        // Box ini penting agar background FAB bisa Full Screen murni
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. KONTEN DATA (List & Ringkasan)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Ringkasan Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatusMiniCard(
                        label = "Pesanan",
                        value = orders.size.toString(),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatusMiniCard(
                        label = "Mesin Jalan",
                        value = machines.count { it.inUse }.toString(),
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF4CAF50)
                    )
                }

                // Daftar Pesanan
                if (orders.isEmpty()) {
                    EmptyState(
                        title = "Belum Ada Pesanan",
                        message = "Klik tombol + untuk membuat pesanan baru\ndan memulai pencucian.",
                        icon = Icons.AutoMirrored.Filled.ReceiptLong
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Antrean Aktif",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        items(orders, key = { it.id ?: 0 }) { order ->
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
                        // Ruang kosong agar item terakhir tidak tertimpa FAB
                        item { Spacer(modifier = Modifier.height(88.dp)) }
                    }
                }
            }

            // 2. CUSTOM FAB & OVERLAY (Di luar innerPadding agar full screen)
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
    }

    // --- BOTTOM SHEETS LOGIC ---
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
                        scope.launch(Dispatchers.IO) {
                            val sender = BluetoothSender()
                            val success = sender.sendToESP(
                                context = context,
                                address = machine.bluetoothAddress.orEmpty(),
                                message = "m:${machine.numberMachine},t:${machine.timer},s:true"
                            )

                            withContext(Dispatchers.Main) {
                                if (success) {
                                    processMachineActivation(
                                        order = selectedOrder!!,
                                        machine = machine,
                                        orderLocalViewModel = orderLocalViewModel,
                                        machineLocalViewModel = machineLocalViewModel,
                                        logMachineLocalViewModel = logMachineLocalViewModel
                                    )
                                    Toast.makeText(context, "Mesin ${machine.numberMachine} Aktif", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Bluetooth Gagal Terhubung", Toast.LENGTH_SHORT).show()
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
        machines.find { it.numberMachine == selectedOrder!!.numberMachine }?.let { machine ->
            OrderBottomSheetInformationTime(
                order = selectedOrder!!,
                machine = machine,
                stepMachine = selectedOrder!!.stepMachine,
                onDismissRequest = { showOrderSheetMachineRunning = false },
                machineNumber = selectedOrder!!.numberMachine
            )
        }
    }
}

// --- HELPER COMPONENT ---
@Composable
fun StatusMiniCard(label: String, value: String, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

// --- HELPER FUNCTION (Logic) ---
private fun processMachineActivation(
    order: OrderLocal,
    machine: MachineLocal,
    orderLocalViewModel: OrderLocalViewModel,
    machineLocalViewModel: MachineLocalViewModel,
    logMachineLocalViewModel: LogMachineLocalViewModel
) {
    val now = Instant.now()
    val formattedDate = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss.SSSX")
        .withZone(ZoneOffset.UTC)
        .format(now)

    // 1. Update Order
    orderLocalViewModel.updateOrder(order.copy(
        stepMachine = order.stepMachine + 2,
        numberMachine = machine.numberMachine
    ))

    // 2. Update Machine
    machineLocalViewModel.updateMachine(machine.copy(
        inUse = true,
        order = order.id,
        timeOn = formattedDate
    ))

    // 3. Add Log
    logMachineLocalViewModel.addLogMachine(LogMachineLocal(
        numberMachine = machine.numberMachine,
        sizeMachine = machine.sizeMachine,
        typeMachine = machine.typeMachine,
        user = machine.user,
        store = machine.store,
        date = formattedDate,
        syncStatus = SyncStatus.PENDING
    ))
}