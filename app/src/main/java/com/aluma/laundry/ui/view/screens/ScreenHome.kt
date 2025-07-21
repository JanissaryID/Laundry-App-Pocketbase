package com.aluma.laundry.ui.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.api.store.StoreViewModel
import com.aluma.laundry.data.api.user.UserViewModel
import com.aluma.laundry.ui.view.components.bottomsheet.OrderBottomSheet
import com.aluma.laundry.ui.view.components.fab.FabWithSubmenu
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenHome(
    userViewModel: UserViewModel = koinInject(),
    storeViewModel: StoreViewModel = koinInject(),
    onNavigate: (String) -> Unit
) {
    val nameStore by storeViewModel.nameStore.collectAsState()
    var isFabExpanded by remember { mutableStateOf(false) }

    var showOrderSheet by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { nameStore?.let { Text(it) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FabWithSubmenu(
                isFabExpanded = isFabExpanded,
                onFabToggle = { isFabExpanded = !isFabExpanded },
                onDismissRequest = { isFabExpanded = false },
                listMachine = {
                    // isi nanti jika perlu
                },
                addOrder = {
                    showOrderSheet = true
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // List content di sini jika ada
            }
        }
    }

    if (showOrderSheet) {
        OrderBottomSheet(
            onDismissRequest = { showOrderSheet = false },
            onSubmit = { customerName, service, machine ->
                // 🔥 Proses order di sini
                println("Order: $customerName - $service - $machine")
            }
        )
    }
}
