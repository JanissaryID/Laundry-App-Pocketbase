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
import com.aluma.laundry.data.income.remote.IncomeRemoteViewModel
import com.aluma.laundry.data.machine.remote.MachineRemoteViewModel
import com.aluma.laundry.data.order.remote.OrderRemoteViewModel
import com.aluma.laundry.data.service.remote.ServiceRemoteViewModel
import com.aluma.laundry.data.store.local.StoreLocalViewModel
import com.aluma.laundry.data.store.remote.StoreRemoteViewModel
import com.aluma.laundry.ui.view.components.ConfirmDialog
import com.aluma.laundry.ui.view.components.itemscard.ItemInfoCard
import com.aluma.laundry.ui.view.components.itemscard.ItemStoreCardOwner
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import org.koin.compose.koinInject
import java.time.LocalDate
import java.time.YearMonth
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
                        ChartPendapatan(
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

@Composable
fun ChartPendapatan(
    storeId: String, // id store yang sedang ditampilkan
    incomeList: List<Triple<String, String, String>>
) {
    val today = LocalDate.now()
    val currentMonth = today.month
    val currentYear = today.year
    val daysInMonth = YearMonth.of(currentYear, currentMonth).lengthOfMonth()

    // 🧠 Map tanggal ke total (default 0)
    val dailyIncomeMap = remember(storeId, incomeList) {
        val incomePerDay = MutableList(daysInMonth) { index -> (index + 1) to 0L }

        incomeList
            .filter { it.first == storeId }
            .forEach { (_, dateStr, totalStr) ->
                val parsedDate = try {
                    LocalDate.parse(dateStr.substring(0, 10)) // YYYY-MM-DD
                } catch (e: Exception) {
                    null
                }

                if (parsedDate != null && parsedDate.month == currentMonth && parsedDate.year == currentYear) {
                    val dayIndex = parsedDate.dayOfMonth - 1
                    val total = totalStr.toLongOrNull() ?: 0L
                    incomePerDay[dayIndex] = parsedDate.dayOfMonth to total
                }
            }

        incomePerDay
    }

    val startAxis = rememberStartAxis(
        valueFormatter = { value, _ -> "Rp ${value.toInt() / 1000}K" }
    )

    val bottomAxis = rememberBottomAxis(
        valueFormatter = { value, _ -> value.toInt().toString() }
    )

    Chart(
        chart = lineChart(),
        model = entryModelOf(*dailyIncomeMap.toTypedArray()),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 16.dp),
        startAxis = startAxis,
        bottomAxis = bottomAxis
    )
}


