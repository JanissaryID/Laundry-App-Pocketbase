package com.aluma.laundry.ui.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.store.model.StoreRemote
import com.aluma.laundry.ui.view.components.itemscard.ItemInfoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenStore(
    store: StoreRemote,
    incomeToday: String,
    incomeChart: List<Float>,
    orderCount: Int,
    serviceCount: Int,
    machineCount: Int,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Informasi Toko") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Nama toko
            item {
                Text(
                    text = store.storeName.orEmpty(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Alamat + kota
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${store.address.orEmpty()}, ${store.city.orEmpty()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Pendapatan hari ini
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pendapatan Hari Ini", fontWeight = FontWeight.Medium)
                        Text(
                            text = incomeToday,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Grafik pendapatan
            item {
                Text("Grafik Pendapatan", style = MaterialTheme.typography.titleMedium)
//                LineChartView(data = incomeChart)
            }

            // Informasi Orders
            item {
                ItemInfoCard(title = "Orders Hari Ini", count = orderCount, icon = Icons.Default.ShoppingCart)
            }

            // Informasi Service
            item {
                ItemInfoCard(title = "Service", count = serviceCount, icon = Icons.Default.Build)
            }

            // Informasi Mesin
            item {
                ItemInfoCard(title = "Mesin", count = machineCount, icon = Icons.Default.Memory)
            }
        }
    }
}
