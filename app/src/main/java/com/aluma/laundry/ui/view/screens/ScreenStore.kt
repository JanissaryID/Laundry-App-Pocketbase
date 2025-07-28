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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.store.local.StoreLocalViewModel
import com.aluma.laundry.ui.view.components.itemscard.ItemInfoCard
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenStore(
    storeLocalViewModel: StoreLocalViewModel = koinInject(),
    onBack: () -> Unit
) {
    val selectedStore by storeLocalViewModel.selectedStore.collectAsState()

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
                selectedStore?.let {
                    Text(
                        text = it.storeName.orEmpty(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
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
                    selectedStore?.let {
                        Text(
                            text = "${it.address.orEmpty()}, ${it.city.orEmpty()}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
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
                            text = "Rp. 1.250.000",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Grafik pendapatan
//            item {
//                Text("Grafik Pendapatan", style = MaterialTheme.typography.titleMedium)
//                LineChart(
//                    modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
//                    data = remember {
//                        listOf(
//                            Line(
//                                label = "Total Pendapatan",
//                                values = listOf(28.0, 41.0, 5.0, 10.0, 35.0),
//                                color = SolidColor(Color(0xFF23af92)),
//                                firstGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .5f),
//                                secondGradientFillColor = Color.Transparent,
//                                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
//                                gradientAnimationDelay = 1000,
//                                drawStyle = DrawStyle.Stroke(width = 2.dp),
//                            )
//                        )
//                    },
//                    animationMode = AnimationMode.Together(delayBuilder = {
//                        it * 500L
//                    }),
//                )
//            }

            // Informasi Orders
            item {
                ItemInfoCard(title = "Orders Hari Ini", count = 45, icon = Icons.Default.ShoppingCart)
            }

            // Informasi Service
            item {
                ItemInfoCard(title = "Service", count = 12, icon = Icons.Default.Build)
            }

            // Informasi Mesin
            item {
                ItemInfoCard(title = "Mesin", count = 6, icon = Icons.Default.Memory)
            }
        }
    }
}
