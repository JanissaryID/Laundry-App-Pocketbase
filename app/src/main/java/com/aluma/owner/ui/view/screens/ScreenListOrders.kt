package com.aluma.owner.ui.view.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.owner.data.logmachine.remote.LogMachineRemoteViewModel
import com.aluma.owner.data.order.remote.OrderRemoteViewModel
import com.aluma.owner.ui.view.components.EmptyState
import com.aluma.owner.ui.view.components.itemscard.ItemOrderCard
import com.aluma.owner.utils.ExcelPOIViewModel
import com.aluma.owner.utils.formatToRupiah
import org.koin.compose.koinInject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenListOrders(
    orderRemoteViewModel: OrderRemoteViewModel = koinInject(),
    excelPOIViewModel: ExcelPOIViewModel = koinInject(),
    logMachineRemoteViewModel: LogMachineRemoteViewModel = koinInject(),
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val orders by orderRemoteViewModel.orderRemoteStore.collectAsState()
    val logMachine by logMachineRemoteViewModel.logMachineRemote.collectAsState()
    val storeID by orderRemoteViewModel.storeId.collectAsState()
    val storeName by logMachineRemoteViewModel.storeName.collectAsState()
    val storeAddress by logMachineRemoteViewModel.storeAddress.collectAsState()

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    val selectedDate = datePickerState.selectedDateMillis?.let { millis ->
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    } ?: LocalDate.now()

    val formattedDateTitle = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id", "ID")))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Riwayat Order", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(formattedDateTitle, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    Surface(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.CalendarToday, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(4.dp))
                            Text("Pilih", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            if (orders.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        excelPOIViewModel.createExcelReport(
                            orderList = orders,
                            logList = logMachine,
                            storeAddress = storeAddress.orEmpty(),
                            storeName = storeName.orEmpty(),
                            date = formattedDateTitle
                        ) { success ->
                            Toast.makeText(context, if (success) "Laporan Excel berhasil diunduh" else "Gagal ekspor", Toast.LENGTH_SHORT).show()
                        }
                    },
                    icon = { Icon(Icons.Filled.FileDownload, null) },
                    text = { Text("Ekspor Excel") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA)) // Background soft gray
        ) {
            // --- SECTION SUMMARY (Baru) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryMiniCard(
                    label = "Total Order",
                    value = "${orders.size}",
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.List
                )
                SummaryMiniCard(
                    label = "Pendapatan",
                    value = orders.sumOf { it.price?.toInt() ?: 0 }.formatToRupiah(),
                    modifier = Modifier.weight(1.3f),
                    icon = null,
                    isSuccess = true
                )
            }

            // --- SECTION LIST ---
            if (orders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = "Tidak ada order",
                        message = "Belum ada pesanan pada tanggal ini."
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp), // Extra padding for FAB
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(orders) { order ->
                        ItemOrderCard(order = order)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        orderRemoteViewModel.fetchOrdersByDate(date, storeID.orEmpty())
                        logMachineRemoteViewModel.fetchLogMachinesByDate(date, storeID.orEmpty())
                    }
                }) { Text("Konfirmasi", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun SummaryMiniCard(label: String, value: String, modifier: Modifier, icon: ImageVector?, isSuccess: Boolean = false) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSuccess) Color(0xFF2E7D32) else Color.Black
            )
        }
    }
}