package com.aluma.owner.ui.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.owner.data.service.remote.ServiceRemoteViewModel
import com.aluma.owner.ui.view.components.EmptyState
import com.aluma.owner.ui.view.components.bottomsheet.ServiceBottomSheet
import com.aluma.owner.ui.view.components.itemscard.ItemServiceCard
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenServices(
    serviceRemoteViewModel: ServiceRemoteViewModel = koinInject(),
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
                title = {
                    Column {
                        Text("Layanan Laundry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Atur daftar harga dan paket", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    serviceRemoteViewModel.setSelectedService(null)
                    editOrAdd = true
                    showBottomSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Tambah Baru") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA))
        ) {
            // Kita bisa menambahkan Search bar di sini nanti

            if (services.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = "Layanan Kosong",
                        message = "Anda belum memiliki daftar harga.\nKetuk tombol + untuk menambah."
                    )
                }
            } else {
                var deletingId by remember { mutableStateOf<String?>(null) }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
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
                                        deletingId = null
                                        serviceRemoteViewModel.fetchServices(storeID = storeID.orEmpty())
                                    }
                                }
                            },
                            isDeleting = deletingId == service.id
                        )
                    }
                    // Spacer bawah agar tidak tertutup FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showBottomSheet) {
        ServiceBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            onSubmit = { service ->
                showBottomSheet = false
                if (editOrAdd) {
                    serviceRemoteViewModel.addService(service) {
                        serviceRemoteViewModel.fetchServices(storeID = service.store.orEmpty())
                    }
                } else {
                    serviceRemoteViewModel.editService(
                        serviceId = servicesSelected!!.id!!,
                        service = service,
                    ) {
                        serviceRemoteViewModel.fetchServices(storeID = service.store.orEmpty())
                    }
                }
            },
        )
    }
}