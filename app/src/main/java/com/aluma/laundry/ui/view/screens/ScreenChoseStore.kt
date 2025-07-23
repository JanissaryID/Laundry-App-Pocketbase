package com.aluma.laundry.ui.view.screens

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import com.aluma.laundry.data.store.StoreRemoteViewModel
import com.aluma.laundry.ui.view.components.EmptyState
import com.aluma.laundry.ui.view.components.bottombar.StoreBottomBar
import com.aluma.laundry.ui.view.components.itemscard.ItemStoreCard
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenChoseStore(
    storeRemoteViewModel: StoreRemoteViewModel = koinInject(),
    nextScreen: () -> Unit
) {
    val storeList by storeRemoteViewModel.storeRemote.collectAsState()
    val selectedStore by storeRemoteViewModel.selectedStoreRemote.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pilih Toko") },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            StoreBottomBar(
                selectedStoreRemote = selectedStore,
                onSaveAndNext = {
                    storeRemoteViewModel.saveStoreID()
                    nextScreen()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (storeList.isEmpty()) {
                EmptyState(
                    title = "Belum ada Toko",
                    message = "Hubungi Pengembang untuk menambahkan Toko,\nkemudian akan muncul di sini secara otomatis."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(storeList) { store ->
                        ItemStoreCard(
                            storeRemote = store,
                            isSelected = store.id == selectedStore?.id,
                            onClick = {
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
}