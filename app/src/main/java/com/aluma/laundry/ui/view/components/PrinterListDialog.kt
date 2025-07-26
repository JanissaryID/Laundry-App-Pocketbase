package com.aluma.laundry.ui.view.components

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.laundry.bluetooth.BluetoothHelper

@SuppressLint("MissingPermission")
@Composable
fun PrinterListDialog(
    bluetoothHelper: BluetoothHelper,
    onPrinterSelected: (BluetoothDevice) -> Unit,
    onDismiss: () -> Unit
) {
    val devices = remember { bluetoothHelper.getPairedPrinterDevices().toList() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Printer") },
        text = {
            LazyColumn {
                items(devices) { device ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPrinterSelected(device)
                            }
                            .padding(8.dp)
                    ) {
                        Text(text = device.name ?: "Unknown", fontWeight = FontWeight.Bold)
                        Text(text = device.address, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}
