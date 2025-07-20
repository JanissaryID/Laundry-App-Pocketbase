package com.aluma.laundry.view.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import com.aluma.laundry.store.StoreViewModel
import com.aluma.laundry.view.components.ItemStoreCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenChoseStore(
    modifier: Modifier = Modifier,
    storeViewModel: StoreViewModel = koinInject(),
    nextScreen: () -> Unit
) {
    val storeList by storeViewModel.store.collectAsState()
    val selectedStore by storeViewModel.selectedStore.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Store") },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            if (selectedStore != null) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Button(
                        onClick = {
                            storeViewModel.saveStoreID()
                            nextScreen()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Masuk")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp), // beri space buat bottomBar
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(storeList) { store ->
                    ItemStoreCard(
                        store = store,
                        isSelected = store.id == selectedStore?.id,
                        onClick = {
                            if (store.id == selectedStore?.id) {
                                storeViewModel.selectStore(null)
                            } else {
                                storeViewModel.selectStore(store)
                            }
                        }
                    )
                }
            }
        }
    }
}