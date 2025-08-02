package com.aluma.owner.ui.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.owner.data.income.remote.IncomeRemoteViewModel
import com.aluma.owner.data.logmachine.remote.LogMachineRemoteViewModel
import com.aluma.owner.data.machine.remote.MachineRemoteViewModel
import com.aluma.owner.data.order.remote.OrderRemoteViewModel
import com.aluma.owner.data.service.remote.ServiceRemoteViewModel
import com.aluma.owner.data.store.local.StoreLocalViewModel
import com.aluma.owner.data.store.remote.StoreRemoteViewModel
import com.aluma.owner.ui.view.components.ChartIncome
import com.aluma.owner.ui.view.components.ConfirmDialog
import com.aluma.owner.ui.view.components.itemscard.ItemInfoCard
import com.aluma.owner.ui.view.components.itemscard.ItemStoreCardOwner
import org.koin.compose.koinInject
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenHomeOwner(
    storeRemoteViewModel: StoreRemoteViewModel = koinInject(),
    storeLocalViewModel: StoreLocalViewModel = koinInject(),
    machineRemoteViewModel: MachineRemoteViewModel = koinInject(),
    serviceRemoteViewModel: ServiceRemoteViewModel = koinInject(),
    orderRemoteViewModel: OrderRemoteViewModel = koinInject(),
    incomeRemoteViewModel: IncomeRemoteViewModel = koinInject(),
    logMachineRemoteViewModel: LogMachineRemoteViewModel = koinInject(),
    onListOrder: () -> Unit,
    onListService: () -> Unit,
    onListMachine: () -> Unit,
    onLogout: () -> Unit
) {
    val storeList by storeLocalViewModel.stores.collectAsState()
    val selectedStoreIndex by storeLocalViewModel.selectedStoreIndex.collectAsState()
    val selectedStore = storeList.getOrNull(selectedStoreIndex)
    val locale = Locale.Builder().setLanguage("id").setRegion("ID").build()
    val currentMonthName = LocalDate.now()
        .month
        .getDisplayName(TextStyle.FULL, locale)

    var showDialog by remember { mutableStateOf(false) }

    val machineByStore by machineRemoteViewModel.machineRemote.collectAsState()
    val serviceByStore by serviceRemoteViewModel.serviceRemote.collectAsState()
    val orderByStore by orderRemoteViewModel.orderRemoteStore.collectAsState()
    val incomeStore by incomeRemoteViewModel.incomeRemoteStore.collectAsState()

    LaunchedEffect(selectedStore?.id) {
        selectedStore?.let { store ->
            logMachineRemoteViewModel.setStoreName(store.storeName)
            logMachineRemoteViewModel.setStoreAddress(store.address)

            machineRemoteViewModel.fetchMachine(store.id)
            machineRemoteViewModel.setStoreId(store.id)

            serviceRemoteViewModel.fetchServices(store.id)
            serviceRemoteViewModel.setStoreId(store.id)

            val today = LocalDate.now()
            orderRemoteViewModel.fetchOrdersByDate(date = today, storeID = store.id)
            orderRemoteViewModel.setStoreId(store.id)

            incomeRemoteViewModel.fetchIncome(date = today.toString())
            incomeRemoteViewModel.incomeStore() // ✅ Ini dipanggil ulang tiap ganti store
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Owner", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        showDialog = true
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(0.dp)
                .fillMaxSize()
        ) {
            Text(
                "Daftar Store Laundry",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(storeList) { store ->
                    ItemStoreCardOwner(
                        store = store,
                        isSelected = selectedStoreIndex == storeList.indexOf(store),
                        onClick = {
                            val today = LocalDate.now()

                            storeLocalViewModel.setSelectedStoreIndex(storeList.indexOf(store))
                            storeLocalViewModel.setSelectedStore(store)

                            machineRemoteViewModel.fetchMachine(store.id)
                            machineRemoteViewModel.setStoreId(store.id)
                            serviceRemoteViewModel.fetchServices(store.id)
                            orderRemoteViewModel.fetchOrdersByDate(date = today, storeID = store.id)
                            orderRemoteViewModel.setStoreId(store.id)
                            logMachineRemoteViewModel.setStoreName(store.storeName)
                            logMachineRemoteViewModel.setStoreAddress(store.address)
                        },
                        todayIncome = incomeStore
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (selectedStore != null) {
                Text(
                    "Grafik Pendapatan ${selectedStore.storeName.orEmpty()} Bulan $currentMonthName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(200.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        ChartIncome(
                            storeId = selectedStore.id,
                            incomeList = incomeStore
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                ItemInfoCard(
                    title = "Daftar Order Hari Ini",
                    count = orderByStore.size,
                    icon = Icons.AutoMirrored.Filled.List,
                    onClick = onListOrder
                )

                Spacer(modifier = Modifier.height(16.dp))
                ItemInfoCard(
                    title = "Daftar Service Laundry",
                    count = serviceByStore.size,
                    icon = Icons.Default.DryCleaning,
                    onClick = onListService
                )

                Spacer(modifier = Modifier.height(16.dp))
                ItemInfoCard(
                    title = "Daftar Mesin Laundry",
                    count = machineByStore.size,
                    icon = Icons.Default.LocalLaundryService,
                    onClick = onListMachine
                )
            } else {
                Text(
                    "Belum ada store yang tersedia.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    if (showDialog) {
        ConfirmDialog(
            title = "Keluar?",
            message = "Apakah anda yakin ingin Keluar?\n\nData di akun ini akan hilang!",
            onDismiss = { showDialog = false },
            onConfirm = {
                onLogout()
            }
        )
    }
}


