package com.aluma.laundry.ui.view.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aluma.laundry.R
import com.aluma.laundry.bluetooth.BluetoothHelper
import com.aluma.laundry.bluetooth.BluetoothPrinter
import com.aluma.laundry.data.datastore.StorePreferenceViewModel
import com.aluma.laundry.data.store.StoreRemoteViewModel
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
    var isLogoutAction by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            val (appName, appVersion) = remember { context.getAppInfo() }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
                Text(
                    text = stringResource(id = R.string.app_version, appVersion),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // --- SECTION: INFO TOKO ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(90.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(45.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = nameStore.orEmpty().ifBlank { stringResource(id = R.string.default_store_name) },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${streetStore.orEmpty()}, ${cityStore.orEmpty()}",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            isLogoutAction = false
                            titleDialog = context.getString(R.string.change_store_title)
                            messageDialog = context.getString(R.string.change_store_message)
                            showDialog = true
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.SyncAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(id = R.string.change_branch_button))
                    }
                }
            }

            // --- SECTION: PERANGKAT & PRINTER ---
            item { SettingSectionHeader(title = stringResource(id = R.string.digital_devices)) }
            item {
                ItemSettingCard(
                    title = if (!bluetoothName.isNullOrBlank()) bluetoothName.orEmpty() else stringResource(id = R.string.printer_not_selected),
                    subtitle = if (!bluetoothAddress.isNullOrBlank()) bluetoothAddress.orEmpty() else stringResource(id = R.string.printer_search_hint),
                    icon = Icons.Default.Bluetooth,
                    onClick = {
                        bluetoothHelper.requestBluetooth {
                            showBluetoothDevice = true
                        }
                    }
                )
            }

            // --- SECTION: AKUN ---
            item { Spacer(Modifier.height(16.dp)) }
            item { SettingSectionHeader(title = stringResource(id = R.string.admin_account)) }
            item {
                ItemSettingCard(
                    title = stringResource(id = R.string.logout_button),
                    subtitle = email.orEmpty().ifBlank { "admin@laundry.com" },
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    iconTint = MaterialTheme.colorScheme.error,
                    textColor = MaterialTheme.colorScheme.error,
                    onClick = {
                        isLogoutAction = true
                        titleDialog = context.getString(R.string.logout_title)
                        messageDialog = context.getString(R.string.logout_message)
                        showDialog = true
                    }
                )
            }
        }
    }

    // --- DIALOGS ---
    if (showBluetoothDevice) {
        PrinterListDialog(
            bluetoothHelper = bluetoothHelper,
            onPrinterSelected = { device ->
                storePreferenceViewModel.saveBluetooth(
                    bluetoothName = device.name,
                    bluetoothAddress = device.address
                )
                showBluetoothDevice = false
                // Langsung tes printer untuk feedback Admin
                BluetoothPrinter().testPrinter(context = context, address = device.address)
            },
            onDismiss = { showBluetoothDevice = false }
        )
    }

    if (showDialog) {
        ConfirmDialog(
            title = titleDialog,
            message = messageDialog,
            onDismiss = { showDialog = false },
            onConfirm = {
                showDialog = false
                if(isLogoutAction) onLogout() else onChangeStore()
            }
        )
    }
}

@Composable
fun SettingSectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}