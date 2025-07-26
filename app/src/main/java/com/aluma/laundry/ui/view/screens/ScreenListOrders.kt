package com.aluma.laundry.ui.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.order.local.OrderLocalViewModel
import com.aluma.laundry.ui.view.components.EmptyState
import com.aluma.laundry.ui.view.components.bottomsheet.OrderBottomSheetReadOnly
import com.aluma.laundry.ui.view.components.itemscard.ItemOrderCard
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenListOrders(
    orderLocalViewModel: OrderLocalViewModel = koinInject(),
    onBack: () -> Unit,
) {
    val orders by orderLocalViewModel.orders.collectAsState()
    val selectedOrder by orderLocalViewModel.selectedOrder.collectAsState()

    var showOrderInformation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orders") },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
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
            if (orders.isEmpty()) {
                EmptyState(
                    title = "Belum ada Order",
                    message = "tambahkan order,\ndi screen Home"
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
                                showOrderInformation = true
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

    if(showOrderInformation){
        selectedOrder?.let {
            OrderBottomSheetReadOnly(
                order = selectedOrder!!,
                onDismissRequest = { showOrderInformation = false },
            )
        }
    }
}