package com.aluma.laundry.ui.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.api.machine.Machine
import com.aluma.laundry.data.api.machine.MachineViewModel
import com.aluma.laundry.data.api.order.OrderViewModel
import com.aluma.laundry.data.api.order.model.Order
import com.aluma.laundry.data.api.store.StoreViewModel
import com.aluma.laundry.ui.view.components.EmptyState
import com.aluma.laundry.ui.view.components.bottomsheet.OrderBottomSheet
import com.aluma.laundry.ui.view.components.bottomsheet.OrderBottomSheetInformation
import com.aluma.laundry.ui.view.components.bottomsheet.OrderBottomSheetInformationTime
import com.aluma.laundry.ui.view.components.fab.FabWithSubmenu
import com.aluma.laundry.ui.view.components.itemscard.ItemOrderCard
import org.koin.compose.koinInject
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenHome(
    orderViewModel: OrderViewModel = koinInject(),
    storeViewModel: StoreViewModel = koinInject(),
    machineViewModel: MachineViewModel = koinInject(),
    onNavigate: (String) -> Unit
) {
    val nameStore by storeViewModel.nameStore.collectAsState()
    val orders by orderViewModel.ordersFilter.collectAsState()
    val machines by machineViewModel.machine.collectAsState()
    val selectedOrder by orderViewModel.selectedOrder.collectAsState()

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
                    message = "Admin akan membuatkan order di sistem,\nkemudian akan muncul di sini secara otomatis."
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
                                orderViewModel.setSelectedOrder(order)
                                if (order.stepMachine < 2) {
                                    showOrderSheetMachine = true
                                    machineViewModel.filterMachine(
                                        type = order.typeMachineService,
                                        size = order.sizeMachine,
                                        stepMachine = order.stepMachine
                                    )
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
            onSubmit = { orderViewModel.createOrder(order = it) }
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
                        numberMachine = machine.numberMachine
                    )
                    orderViewModel.patchOrder(id = selectedOrder!!.id, order = updatedOrder)

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

                    machineViewModel.patchMachine(id = machine.id, machine = updatedMachine)
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

