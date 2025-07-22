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
import com.aluma.laundry.data.api.machine.Machine
import com.aluma.laundry.data.api.machine.MachineViewModel
import com.aluma.laundry.data.api.order.OrderViewModel
import com.aluma.laundry.data.api.order.model.Order
import com.aluma.laundry.data.api.store.StoreViewModel
import com.aluma.laundry.ui.view.components.bottomsheet.OrderBottomSheet
import com.aluma.laundry.ui.view.components.bottomsheet.OrderBottomSheetInformation
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
    val orders by orderViewModel.orders.collectAsState()
    val selectedOrders by orderViewModel.selectedOrder.collectAsState()
    var isFabExpanded by remember { mutableStateOf(false) }

    var showOrderSheet by remember { mutableStateOf(false) }
    var showOrderSheetMachine by remember { mutableStateOf(false) }


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
                listMachine = {
                    // isi nanti jika perlu
                },
                addOrder = {
                    showOrderSheet = true
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
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
                            if(order.stepMachine < 2){
                                showOrderSheetMachine = true
                                orderViewModel.setSelectedOrder(order = order)
                                machineViewModel.filterMachine(type = order.typeMachineService, size = order.sizeMachine, stepMachine = order.stepMachine)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showOrderSheet) {
        OrderBottomSheet(
            onDismissRequest = { showOrderSheet = false },
            onSubmit = {
                orderViewModel.createOrder(order = it)
            }
        )
    }

    if (showOrderSheetMachine) {
        OrderBottomSheetInformation(
            order = orders[0],
            onDismissRequest = { showOrderSheetMachine = false },
            onSubmit = { machine, onDone ->
                // Patch Order (update step dan machine)
                val order = Order(
                    stepMachine = selectedOrders!!.stepMachine + 2,
                    numberMachine = machine!!.numberMachine,
                    store = selectedOrders!!.store,
                    user = selectedOrders!!.user,
                    sizeMachine = selectedOrders!!.sizeMachine,
                    price = selectedOrders!!.price,
                    typePayment = selectedOrders!!.typePayment,
                    serviceName = selectedOrders!!.serviceName,
                    customerName = selectedOrders!!.customerName,
                    typeMachineService = selectedOrders!!.typeMachineService,
                )
                orderViewModel.patchOrder(id = selectedOrders?.id.orEmpty(), order = order)

                val later = Instant.now().plus(Duration.ofMinutes(45))
                val formatted = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm:ss.SSSX")
                    .withZone(ZoneOffset.UTC)
                    .format(later)

                val updatedMachine = Machine(
                    inUse = true,
                    order = selectedOrders!!.id,
                    timeOn = formatted,
                    numberMachine = machine.numberMachine,
                    sizeMachine = machine.sizeMachine,
                    user = machine.user,
                    store = machine.store,
                    typeMachine = machine.typeMachine,
                    timer = machine.timer,
                    bluetoothAddress = machine.bluetoothAddress
                )
                machineViewModel.patchMachine(id = machine.id, machine = updatedMachine)

                onDone() // ✅ Tutup sheet setelah proses selesai
            },
        )
    }
}
