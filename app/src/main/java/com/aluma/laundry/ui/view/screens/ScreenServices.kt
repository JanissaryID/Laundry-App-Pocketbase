package com.aluma.laundry.ui.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.service.remote.ServiceRemoteViewModel
import com.aluma.laundry.ui.view.components.EmptyState
import com.aluma.laundry.ui.view.components.bottomsheet.ServiceBottomSheet
import com.aluma.laundry.ui.view.components.itemscard.ItemServiceCard
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenServices(
    serviceRemoteViewModel: ServiceRemoteViewModel = koinInject(),
//    storePreferenceViewModel: StorePreferenceViewModel = koinInject(),
    onBack: () -> Unit,
) {
    val services by serviceRemoteViewModel.serviceRemote.collectAsState()
    val servicesSelected by serviceRemoteViewModel.selectedServiceRemote.collectAsState()
    val storeID by serviceRemoteViewModel.storeId.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    var editOrAdd by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Layanan") },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    serviceRemoteViewModel.setSelectedService(null)
                    editOrAdd = true // kosongkan yang dipilih
                    showBottomSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Layanan")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (services.isEmpty()) {
                EmptyState(
                    title = "Belum ada Layanan",
                    message = "Hubungi pengembang,\nuntuk menambahkan mesin"
                )
            } else {
                var deletingId by remember { mutableStateOf<String?>(null) }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(services) { service ->
                        ItemServiceCard(
                            service = service,
                            onClick = {
                                serviceRemoteViewModel.setSelectedService(service)
                                editOrAdd = false
                                showBottomSheet = true
                            },
                            onDelete = {
                                if (deletingId == null) {
                                    deletingId = service.id
                                    serviceRemoteViewModel.deleteService(
                                        serviceId = service.id.orEmpty()
                                    ) { success ->
                                        deletingId = null // reset
                                        serviceRemoteViewModel.fetchServices(storeID = storeID.orEmpty())
                                    }
                                }
                            },
                            isDeleting = deletingId == service.id
                        )
                    }
                }
            }
        }
    }

    // ====================
    // BOTTOM SHEET
    // ====================

    if (showBottomSheet) {
        ServiceBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            onSubmit = { service ->
                showBottomSheet = false
                if (editOrAdd) {
                    serviceRemoteViewModel.addService(
                        service = service,
                    ) { success ->
                        serviceRemoteViewModel.fetchServices(storeID = storeID.orEmpty())
                    }
                } else {
                    serviceRemoteViewModel.editService(
                        serviceId = servicesSelected!!.id!!,
                        service = service,
                    ) { success ->
                        serviceRemoteViewModel.fetchServices(storeID = storeID.orEmpty())
                    }
                }
            },
        )
    }
}