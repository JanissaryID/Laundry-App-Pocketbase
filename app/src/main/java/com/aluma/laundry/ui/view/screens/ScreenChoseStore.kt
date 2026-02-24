package com.aluma.laundry.ui.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.laundry.R
import com.aluma.laundry.data.machine.local.MachineLocalViewModel
import com.aluma.laundry.data.order.local.OrderLocalViewModel
import com.aluma.laundry.data.service.local.ServiceLocalViewModel
import com.aluma.laundry.data.store.StoreRemoteViewModel
import com.aluma.laundry.ui.view.components.EmptyState
import com.aluma.laundry.ui.view.components.bottombar.StoreBottomBar
import com.aluma.laundry.ui.view.components.itemscard.ItemStoreCard
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenChoseStore(
    storeRemoteViewModel: StoreRemoteViewModel = koinInject(),
    orderLocalViewModel: OrderLocalViewModel = koinInject(),
    machineLocalViewModel: MachineLocalViewModel = koinInject(),
    serviceLocalViewModel: ServiceLocalViewModel = koinInject(),
    canGoBack: Boolean = false,
    onBack: () -> Unit = {},
    nextScreen: () -> Unit
) {
    val storeList by storeRemoteViewModel.storeRemote.collectAsState()
    val selectedStore by storeRemoteViewModel.selectedStoreRemote.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(id = R.string.work_location),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(id = R.string.chose_store_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(id = R.string.back))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            // Langsung panggil tanpa pembungkus Surface tambahan
            StoreBottomBar(
                selectedStoreRemote = selectedStore,
                onSaveAndNext = {
                    storeRemoteViewModel.saveStoreID()
                    // Membersihkan cache lokal karena berpindah toko
                    orderLocalViewModel.deleteAllOrders()
                    machineLocalViewModel.deleteAllMachines()
                    serviceLocalViewModel.deleteAllServices()
                    nextScreen()
                },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { innerPadding ->
        if (storeList.isEmpty()) {
            EmptyState(
                title = stringResource(id = R.string.store_not_found),
                message = stringResource(id = R.string.store_not_found_message),
                icon = Icons.Default.Storefront
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 80.dp, // Extra space untuk tombol
                    start = 16.dp,
                    end = 16.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = stringResource(id = R.string.available_branches, storeList.size),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )
                }

                items(storeList, key = { it.id ?: "" }) { store ->
                    ItemStoreCard(
                        storeRemote = store,
                        isSelected = store.id == selectedStore?.id,
                        onClick = {
                            // Logic toggle selection
                            if (store.id == selectedStore?.id) {
                                storeRemoteViewModel.selectStore(null)
                            } else {
                                storeRemoteViewModel.selectStore(store)
                            }
                        }
                    )
                }
            }
        }
    }
}