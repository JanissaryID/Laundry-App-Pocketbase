package com.aluma.laundry.ui.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.machine.local.MachineLocalViewModel
import com.aluma.laundry.data.machine.remote.MachineRemoteViewModel
import com.aluma.laundry.data.order.local.OrderLocalViewModel
import com.aluma.laundry.data.store.StoreRemoteViewModel
import com.aluma.laundry.ui.view.components.EmptyState
import com.aluma.laundry.ui.view.components.bottomsheet.OrderBottomSheet
import com.aluma.laundry.ui.view.components.bottomsheet.OrderBottomSheetInformation
import com.aluma.laundry.ui.view.components.bottomsheet.OrderBottomSheetInformationTime
import com.aluma.laundry.ui.view.components.fab.FabWithSubmenu
import com.aluma.laundry.ui.view.components.itemscard.ItemOrderCard
import org.koin.compose.koinInject
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenHome(
    storeRemoteViewModel: StoreRemoteViewModel = koinInject(),
    machineRemoteViewModel: MachineRemoteViewModel = koinInject(),
    machineLocalViewModel: MachineLocalViewModel = koinInject(),
    orderLocalViewModel: OrderLocalViewModel = koinInject(),
    onNavigate: (String) -> Unit
) {
    val nameStore by storeRemoteViewModel.nameStore.collectAsState()
    val orders by orderLocalViewModel.ordersFilter.collectAsState()
    val machines by machineLocalViewModel.machines.collectAsState()
    val selectedOrder by orderLocalViewModel.selectedOrder.collectAsState()

    var isFabExpanded by remember { mutableStateOf(false) }

    var showOrderSheet by remember { mutableStateOf(false) }
    var showOrderSheetMachine by remember { mutableStateOf(false) }
    var showOrderSheetMachineRunning by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { nameStore?.let { Text(it) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FabWithSubmenu(
                isFabExpanded = isFabExpanded,
                onFabToggle = { isFabExpanded = !isFabExpanded },
                onDismissRequest = { isFabExpanded = false },
                listMachine = { /* opsional */ },
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

                    machineLocalViewModel.updateMachine(machineLocal = updatedMachine)
                    onDone()
                }
            },
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