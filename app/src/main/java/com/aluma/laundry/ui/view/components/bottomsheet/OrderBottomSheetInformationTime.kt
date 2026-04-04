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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.laundry.R
import com.aluma.laundry.bluetooth.BluetoothPrinter
import com.aluma.laundry.data.datastore.StorePreferenceViewModel
import com.aluma.laundry.data.machine.model.MachineLocal
import com.aluma.laundry.data.order.model.OrderLocal
import com.aluma.laundry.data.order.utils.Quad
import com.aluma.laundry.ui.view.components.CountdownTimer
import com.aluma.laundry.utils.formatRupiah
import org.koin.compose.koinInject
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

sealed class VerificationState {
    object Idle : VerificationState()
    object Verifying : VerificationState()
    object Completed : VerificationState()
    object PowerOutage : VerificationState()
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderBottomSheetInformationTime(
    order: OrderLocal,
    machine: MachineLocal,
    stepMachine: Int,
    machineNumber: Int?,
    storePreferenceViewModel: StorePreferenceViewModel = koinInject(),
    onDismissRequest: () -> Unit,
    onVerifyAndComplete: (onResult: (canProceed: Boolean) -> Unit) -> Unit,
    onRerun: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val bluetoothAddress by storePreferenceViewModel.bluetoothAddress.collectAsState()
    val nameStore by storePreferenceViewModel.nameStore.collectAsState()
    val addressStore by storePreferenceViewModel.addressStore.collectAsState()
    val cityStore by storePreferenceViewModel.cityStore.collectAsState()

    val context = LocalContext.current

    // Kalkulasi Waktu (Mulai & Estimasi Selesai)
    val (startTimeStr, endTimeStr, startTimeMillis, endTimeMillis) = remember(machine.timeOn, machine.timer) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSX").withZone(ZoneOffset.UTC)
        val timeDisplayFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

        try {
            val startInstant = formatter.parse(machine.timeOn, Instant::from)
            val endInstant = startInstant.plus(Duration.ofMinutes(machine.timer.toLong()))

            Quad(
                timeDisplayFormatter.format(startInstant),
                timeDisplayFormatter.format(endInstant),
                startInstant.toEpochMilli(),
                endInstant.toEpochMilli()
            )
        } catch (e: Exception) {
            val now = System.currentTimeMillis()
            Quad("--:--", "--:--", now, now)
        }
    }

    // Identifikasi Status Berdasarkan Step
    val (statusTitle, statusColor, statusIcon) = when (stepMachine) {
        2 -> Triple(stringResource(id = R.string.status_washing), Color(0xFF2196F3), Icons.Default.LocalLaundryService)
        3 -> Triple(stringResource(id = R.string.status_drying), Color(0xFF4CAF50), Icons.Default.DryCleaning)
        else -> Triple(stringResource(id = R.string.status_active), Color.Gray, Icons.Default.Info)
    }

    var verificationState by remember { 
        mutableStateOf<VerificationState>(
            if (machine.needsVerification || System.currentTimeMillis() >= endTimeMillis) VerificationState.Verifying else VerificationState.Idle
        ) 
    }

    androidx.compose.runtime.LaunchedEffect(verificationState) {
        if (verificationState == VerificationState.Verifying) {
            onVerifyAndComplete { canProceed ->
                verificationState = if (canProceed) {
                    VerificationState.Completed
                } else {
                    VerificationState.PowerOutage
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- HEADER STATUS (Modern) ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = statusTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E1E1E)
                        )
                        Text(
                            text = stringResource(id = R.string.machine_unit_number, machineNumber ?: 0),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            // --- COUNTDOWN & ESTIMASI (Premium) ---
            Surface(
                color = statusColor.copy(alpha = 0.05f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.time_left),
                        style = MaterialTheme.typography.labelLarge,
                        color = statusColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold
                    )

                    CountdownTimer(
                        startTimeMillis = startTimeMillis,
                        endTimeMillis = endTimeMillis,
                        onFinish = {
                            if (verificationState == VerificationState.Idle) {
                                verificationState = VerificationState.Verifying
                            }
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Text(
                            text = stringResource(id = R.string.estimated_finish_at, endTimeStr),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            // --- DETAIL PELANGGAN (Grid Layout Simple) ---
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = stringResource(id = R.string.order_info_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OrderInfoCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(id = R.string.customer),
                        value = order.customerName ?: "-"
                    )
                    OrderInfoCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(id = R.string.service),
                        value = order.serviceName ?: "-"
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OrderInfoCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(id = R.string.capacity),
                        value = if (order.sizeMachine) "Large" else "Small"
                    )
                    OrderInfoCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(id = R.string.total_payment),
                        value = formatRupiah(order.price ?: "0"),
                        isValueHighlighted = true
                    )
                }
            }

            // --- BUTTONS ---
            when (verificationState) {
                is VerificationState.Idle -> {
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
                                if (!stat) Toast.makeText(context, context.getString(R.string.print_failed_toast), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Icon(Icons.Default.Print, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(id = R.string.print_receipt), fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = onDismissRequest,
                            modifier = Modifier.weight(1f).height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = statusColor)
                        ) {
                            Text(stringResource(id = R.string.back), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                is VerificationState.Verifying -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = stringResource(id = R.string.status_checking), color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }
                is VerificationState.Completed -> {
                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text(stringResource(id = R.string.status_completed_next), fontWeight = FontWeight.Bold)
                    }
                }
                is VerificationState.PowerOutage -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Text(
                                    stringResource(id = R.string.machine_error_warning),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    lineHeight = androidx.compose.ui.unit.TextUnit(18f, androidx.compose.ui.unit.TextUnitType.Sp)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismissRequest,
                                modifier = Modifier.weight(1f).height(54.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Text(stringResource(id = R.string.btn_proceed), fontWeight = FontWeight.Bold, color = Color.Gray)
                            }
                            Button(
                                onClick = onRerun,
                                modifier = Modifier.weight(1f).height(54.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(stringResource(id = R.string.btn_retry), fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderInfoCard(modifier: Modifier = Modifier, label: String, value: String, isValueHighlighted: Boolean = false) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isValueHighlighted) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (isValueHighlighted) MaterialTheme.colorScheme.primary else Color(0xFF1E1E1E),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}