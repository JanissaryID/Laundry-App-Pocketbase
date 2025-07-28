package com.aluma.laundry.ui.view.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aluma.laundry.bluetooth.BluetoothHelper
import com.aluma.laundry.bluetooth.BluetoothPrinter
import com.aluma.laundry.data.datastore.StorePreferenceViewModel
import com.aluma.laundry.data.store.remote.StoreRemoteViewModel
import com.aluma.laundry.data.user.remote.UserRemoteViewModel
import com.aluma.laundry.ui.view.components.ConfirmDialog
import com.aluma.laundry.ui.view.components.PrinterListDialog
import com.aluma.laundry.ui.view.components.itemscard.ItemSettingCard
import com.aluma.laundry.utils.getAppInfo
import org.koin.compose.koinInject

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenSettings(
    storeRemoteViewModel: StoreRemoteViewModel = koinInject(),
    userRemoteViewModel: UserRemoteViewModel = koinInject(),
    storePreferenceViewModel: StorePreferenceViewModel = koinInject(),
    bluetoothHelper: BluetoothHelper,
    onBack: () -> Unit,
    onChangeStore: () -> Unit,
    onLogout: () -> Unit,
) {
    val nameStore by storeRemoteViewModel.nameStore.collectAsState()
    val cityStore by storeRemoteViewModel.cityStore.collectAsState()
    val streetStore by storeRemoteViewModel.streetStore.collectAsState()
    val email by userRemoteViewModel.email.collectAsState()
    val bluetoothName by storePreferenceViewModel.bluetoothName.collectAsState()
    val bluetoothAddress by storePreferenceViewModel.bluetoothAddress.collectAsState()

    var showBluetoothDevice by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    var titleDialog by remember { mutableStateOf("") }
    var messageDialog by remember { mutableStateOf("") }
    var typeDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        bottomBar = {
            val (appName, appVersion) = remember { context.getAppInfo() }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text(appName, style = MaterialTheme.typography.labelSmall)
                Text("Versi $appVersion", style = MaterialTheme.typography.bodySmall)
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Info toko - DI LUAR LazyColumn
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = "Toko",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = nameStore.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${streetStore.orEmpty()}, ${cityStore.orEmpty()}",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = {
                    typeDialog = false
                    showDialog = true

                    titleDialog = "Ganti Toko?"
                    messageDialog = "Apakah anda yakin ingin Mengganti toko?\n\nData di toko ini akan hilang!"
                }) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Ganti Toko")
                }
            }

            Spacer(Modifier.height(24.dp))

            // LazyColumn untuk isi item setting
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    ItemSettingCard(
                        title = if (!bluetoothName.isNullOrBlank()) bluetoothName.orEmpty() else "Belum memilih printer",
                        subtitle = if (!bluetoothAddress.isNullOrBlank()) bluetoothAddress else "Pilih perangkat printer Bluetooth",
                        icon = Icons.Default.Bluetooth,
                        onClick = {
                            bluetoothHelper.requestBluetooth {
                                showBluetoothDevice = true
                            }
                        }
                    )
                }

                item {
                    ItemSettingCard(
                        title = "Keluar",
                        subtitle = email,
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        iconTint = MaterialTheme.colorScheme.error,
                        textColor = MaterialTheme.colorScheme.error,
                        onClick = {
                            typeDialog = true
                            showDialog = true

                            titleDialog = "Keluar?"
                            messageDialog = "Apakah anda yakin ingin Keluar?\n\nData di akun ini akan hilang!"
                        }
                    )
                }
            }
        }
    }

    if (showBluetoothDevice) {
        PrinterListDialog(
            bluetoothHelper = bluetoothHelper,
            onPrinterSelected = { device ->
                storePreferenceViewModel.saveBluetooth(
                    bluetoothName = device.name,
                    bluetoothAddress = device.address
                )
                showBluetoothDevice = false

                if (!bluetoothAddress.isNullOrEmpty()){
                    val printer = BluetoothPrinter()
                    printer.testPrinter(context = context, address = bluetoothAddress)
                }
            },
            onDismiss = {
                showBluetoothDevice = false
            }
        )
    }

    if (showDialog) {
        ConfirmDialog(
            title = titleDialog,
            message = messageDialog,
            onDismiss = { showDialog = false },
            onConfirm = {
                showDialog = false
                if(typeDialog) onLogout() else onChangeStore()
            }
        )
    }
}

