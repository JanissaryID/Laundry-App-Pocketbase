package com.aluma.laundry.ui.view.components.bottomsheet

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.laundry.bluetooth.BluetoothPrinter
import com.aluma.laundry.data.datastore.StorePreferenceViewModel
import com.aluma.laundry.data.order.model.OrderLocal
import com.aluma.laundry.data.order.utils.SyncStatus
import com.aluma.laundry.utils.formatRupiah
import org.koin.compose.koinInject

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderBottomSheetReadOnly(
    storePreferenceViewModel: StorePreferenceViewModel = koinInject(),
    order: OrderLocal,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val nameStore by storePreferenceViewModel.nameStore.collectAsState()
    val addressStore by storePreferenceViewModel.addressStore.collectAsState()
    val cityStore by storePreferenceViewModel.cityStore.collectAsState()
    val bluetoothAddress by storePreferenceViewModel.bluetoothAddress.collectAsState()

    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Detail Transaksi",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // Indikator Sinkronisasi (Opsional)
                val isSynced = order.syncStatus == SyncStatus.SYNCED
                Surface(
                    color = (if (isSynced) Color(0xFF4CAF50) else Color(0xFFFFA000)).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSynced) Icons.Default.CloudDone else Icons.Default.CloudSync,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isSynced) Color(0xFF4CAF50) else Color(0xFFFFA000)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSynced) "Terdata" else "Tertunda",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSynced) Color(0xFF4CAF50) else Color(0xFFFFA000)
                        )
                    }
                }
            }

            // --- INFO BOX (Gaya Struk) ---
            Surface(
                color = Color(0xFFFBFBFB),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OrderInfoRow(label = "ID Order", value = "#${order.id ?: "N/A"}")
                    OrderInfoRow(label = "Tanggal", value = order.date ?: "-")

                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFEEEEEE))

                    OrderInfoRow(label = "Pelanggan", value = order.customerName ?: "-")
                    OrderInfoRow(
                        label = "Layanan",
                        value = "${order.serviceName} (${if (order.sizeMachine) "Besar" else "Kecil"})",
                        isHighlighted = true
                    )
                    OrderInfoRow(label = "Pembayaran", value = order.typePayment ?: "-")

                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFEEEEEE))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "TOTAL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = formatRupiah(order.price ?: "0"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // --- ACTION BUTTONS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val printer = BluetoothPrinter()
                        val stat = printer.printLaundryReceipt(
                            context = context,
                            address = bluetoothAddress,
                            storeName = nameStore.orEmpty(),
                            storeAddress = addressStore.orEmpty(),
                            storeCity = cityStore.orEmpty(),
                            services = Pair(order.serviceName.orEmpty(), order.price.orEmpty()),
                            customerName = order.customerName.orEmpty(),
                            paymentMethod = order.typePayment.orEmpty()
                        )
                        if (!stat) Toast.makeText(context, "Printer Gagal", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Print, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Cetak Nota")
                }

                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Selesai", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}