package com.aluma.owner.ui.view.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.aluma.owner.data.income.remote.IncomeRemoteViewModel
import com.aluma.owner.data.logmachine.remote.LogMachineRemoteViewModel
import com.aluma.owner.data.machine.remote.MachineRemoteViewModel
import com.aluma.owner.data.order.remote.OrderRemoteViewModel
import com.aluma.owner.data.service.remote.ServiceRemoteViewModel
import com.aluma.owner.data.store.local.StoreLocalViewModel
import com.aluma.owner.data.store.remote.StoreRemoteViewModel
import com.aluma.owner.ui.view.components.ChartIncome
import com.aluma.owner.ui.view.components.ConfirmDialog
import com.aluma.owner.ui.view.components.itemscard.ItemStatBox
import com.aluma.owner.ui.view.components.itemscard.ItemStoreCardOwner
import com.aluma.owner.ui.view.components.itemscard.MenuCard
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
    val currentMonthName = LocalDate.now().month.getDisplayName(TextStyle.FULL, locale)

    var showDialog by remember { mutableStateOf(false) }

    val machineByStore by machineRemoteViewModel.machineRemote.collectAsState()
    val serviceByStore by serviceRemoteViewModel.serviceRemote.collectAsState()
    val orderByStore by orderRemoteViewModel.orderRemoteStore.collectAsState()
    val incomeStore by incomeRemoteViewModel.incomeRemoteStore.collectAsState()

    // Sync data saat store berubah
    LaunchedEffect(selectedStore?.id) {
        selectedStore?.let { store ->
            logMachineRemoteViewModel.apply {
                setStoreName(store.storeName)
                setStoreAddress(store.address)
            }
            machineRemoteViewModel.apply {
                fetchMachine(store.id)
                setStoreId(store.id)
            }
            serviceRemoteViewModel.apply {
                fetchServices(store.id)
                setStoreId(store.id)
            }
            val today = LocalDate.now()
            orderRemoteViewModel.apply {
                fetchOrdersByDate(date = today, storeID = store.id)
                setStoreId(store.id)
            }
            incomeRemoteViewModel.apply {
                fetchIncome(date = today.toString())
                incomeStore()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Ringkasan Bisnis", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                        Text("Halo, Owner!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // --- SECTION 1: PEMILIHAN STORE ---
            Text(
                "Cabang Laundry Anda",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(storeList) { store ->
                    ItemStoreCardOwner(
                        store = store,
                        isSelected = selectedStoreIndex == storeList.indexOf(store),
                        onClick = {
                            val idx = storeList.indexOf(store)
                            storeLocalViewModel.setSelectedStoreIndex(idx)
                            storeLocalViewModel.setSelectedStore(store)
                        },
                        todayIncome = incomeStore
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (selectedStore != null) {
                // --- SECTION 2: STATS RINGKAS ---
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ItemStatBox(
                        title = "Order Hari Ini",
                        value = "${orderByStore.size}",
                        containerColor = Color(0xFFE8F0FE),
                        contentColor = Color(0xFF1967D2),
                        modifier = Modifier.weight(1f)
                    )
                    ItemStatBox(
                        title = "Total Mesin",
                        value = "${machineByStore.size}",
                        containerColor = Color(0xFFE6F4EA),
                        contentColor = Color(0xFF137333),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- SECTION 3: GRAFIK ---
                Text(
                    "Grafik Pendapatan $currentMonthName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(220.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        ChartIncome(
                            storeId = selectedStore.id,
                            incomeList = incomeStore
                        )
                    }
                }

                // --- SECTION 4: MENU MANAJEMEN ---
                Text(
                    "Kelola Operasional",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MenuCard(
                        title = "Daftar Antrean Order",
                        subtitle = "Pantau status cucian pelanggan",
                        icon = Icons.AutoMirrored.Filled.List,
                        color = Color(0xFF673AB7),
                        onClick = onListOrder
                    )
                    MenuCard(
                        title = "Layanan & Harga",
                        subtitle = "Atur paket laundry anda",
                        icon = Icons.Default.DryCleaning,
                        color = Color(0xFFFF9800),
                        onClick = onListService
                    )
                    MenuCard(
                        title = "Monitoring Mesin",
                        subtitle = "Cek kondisi mesin aktif",
                        icon = Icons.Default.LocalLaundryService,
                        color = Color(0xFF00BCD4),
                        onClick = onListMachine
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

            } else {
                // Tampilan jika store kosong
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Belum ada store terpilih", color = Color.Gray)
                }
            }
        }
    }

    if (showDialog) {
        ConfirmDialog(
            title = "Keluar Aplikasi",
            message = "Apakah anda yakin ingin keluar? Anda perlu login kembali untuk mengakses data.",
            onDismiss = { showDialog = false },
            onConfirm = { onLogout() }
        )
    }
}