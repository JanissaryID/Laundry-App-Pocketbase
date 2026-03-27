package com.aluma.laundry.ui.view.components.bottomsheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.aluma.laundry.R
import com.aluma.laundry.data.order.model.OrderLocal
import com.aluma.laundry.data.order.utils.SyncStatus
import com.aluma.laundry.data.order.utils.TypePayment
import com.aluma.laundry.data.service.local.ServiceLocalViewModel
import com.aluma.laundry.data.service.model.ServiceLocal
import com.aluma.laundry.data.service.remote.ServiceRemoteViewModel
import com.aluma.laundry.ui.view.components.dropdown.ServiceDropdown
import com.aluma.laundry.utils.formatRupiah
import org.koin.compose.koinInject
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderBottomSheet(
    onDismissRequest: () -> Unit,
    serviceRemoteViewModel: ServiceRemoteViewModel = koinInject(),
    serviceLocalViewModel: ServiceLocalViewModel = koinInject(),
    onSubmit: (order: OrderLocal) -> Unit,
    adminEmployeeId: String? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var customerName by remember { mutableStateOf("") }
    var selectedServiceLocal by remember { mutableStateOf<ServiceLocal?>(null) }
    var selectedMethod by remember { mutableStateOf(TypePayment.TUNAI) }

    val services by serviceLocalViewModel.services.collectAsState()
    val idUser by serviceRemoteViewModel.idUser.collectAsState()
    val idStore by serviceRemoteViewModel.idStore.collectAsState()

    var isSubmitting by remember { mutableStateOf(false) }

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
                .padding(bottom = 32.dp), // Beri ruang ekstra di bawah
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- HEADER ---
            Text(
                text = stringResource(id = R.string.input_new_order),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // --- INPUT NAMA ---
            OutlinedTextField(
                value = customerName,
                onValueChange = { input ->
                    customerName = input.split(" ").joinToString(" ") { word ->
                        word.lowercase().replaceFirstChar { it.uppercase() }
                    }
                },
                label = { Text(stringResource(id = R.string.customer_name)) },
                placeholder = { Text(stringResource(id = R.string.customer_name_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            // --- PILIH LAYANAN ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(id = R.string.laundry_service), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                ServiceDropdown(
                    serviceLocal = services,
                    selectedServiceLocal = selectedServiceLocal,
                    onServiceSelected = { selectedServiceLocal = it }
                )
            }

            // --- METODE PEMBAYARAN ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(id = R.string.payment_method), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TypePayment.entries.forEach { method ->
                        val isSelected = selectedMethod == method

                        // Card yang lebih interaktif
                        Surface(
                            onClick = { selectedMethod = method },
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF5F5F5),
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = when (method) {
                                        TypePayment.TUNAI -> Icons.Default.Payments
                                        TypePayment.QRIS -> Icons.Default.QrCodeScanner
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = stringResource(id = method.labelRes),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- FOOTER: RINGKASAN & TOMBOL ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(id = R.string.total_bill), style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = selectedServiceLocal?.let { formatRupiah(it.priceService) } ?: "Rp 0",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = {
                            isSubmitting = true
                            val service = selectedServiceLocal
                            if (service != null) {
                                // Logic step & type (Tetap sama dengan kodemu)
                                val typeMachine = when {
                                    service.wash == "yes" && service.dry == "yes" -> 2
                                    service.wash == "yes" && service.dry == "no" -> 0
                                    service.wash == "no" && service.dry == "yes" -> 1
                                    else -> 3
                                }

                                val step = when {
                                    typeMachine == 2 -> 0
                                    typeMachine > 2 -> 4
                                    else -> typeMachine
                                }

                                val nowDate = Instant.now()
                                val formatted = DateTimeFormatter
                                    .ofPattern("yyyy-MM-dd HH:mm:ss.SSSX")
                                    .withZone(ZoneOffset.UTC)
                                    .format(nowDate)

                                onSubmit(OrderLocal(
                                    customerName = customerName,
                                    serviceName = service.nameService,
                                    sizeMachine = service.sizeMachine,
                                    stepMachine = step,
                                    price = service.priceService,
                                    typePayment = selectedMethod.name,
                                    user = idUser,
                                    store = idStore,
                                    typeMachineService = typeMachine,
                                    date = formatted,
                                    admin = adminEmployeeId,
                                    syncStatus = SyncStatus.PENDING
                                ))
                                onDismissRequest()
                            }
                        },
                        modifier = Modifier.height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSubmitting && customerName.isNotBlank() && selectedServiceLocal != null,
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(id = R.string.create_order), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}