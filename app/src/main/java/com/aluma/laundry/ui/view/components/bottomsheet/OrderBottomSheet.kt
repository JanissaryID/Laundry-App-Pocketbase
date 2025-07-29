package com.aluma.laundry.ui.view.components.bottomsheet

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.order.model.OrderLocal
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
    onSubmit: (order: OrderLocal) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var customerName by remember { mutableStateOf("") }
    var selectedServiceLocal by remember { mutableStateOf<ServiceLocal?>(null) }
    var selectedMethod by remember { mutableStateOf(TypePayment.TUNAI) }

    val services by serviceLocalViewModel.services.collectAsState()
//    val idUser by serviceRemoteViewModel.idUser.collectAsState()
//    val idStore by serviceRemoteViewModel.idStore.collectAsState()

    var isSubmitting by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Buat Order", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = customerName,
                onValueChange = { input ->
                    customerName = input
                        .split(" ")
                        .joinToString(" ") { word ->
                            word.lowercase().replaceFirstChar { it.uppercase() }
                        }
                },
                label = { Text("Nama Pelanggan") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            ServiceDropdown(
                serviceLocal = services,
                selectedServiceLocal = selectedServiceLocal,
                onServiceSelected = {
                    selectedServiceLocal = it
                }
            )

            Text("Metode Pembayaran", style = MaterialTheme.typography.titleSmall)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TypePayment.entries.forEach { method ->
                    val isSelected = selectedMethod == method

                    Card(
                        onClick = { selectedMethod = method },
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = when (method) {
                                    TypePayment.TUNAI -> Icons.Default.AttachMoney
                                    TypePayment.QRIS -> Icons.Default.QrCode
                                },
                                contentDescription = method.label,
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = method.label,
                                style = if (isSelected)
                                    MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                else
                                    MaterialTheme.typography.bodyMedium,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if(selectedServiceLocal != null){
                        Text(
                            text = selectedServiceLocal?.let {
                                "${it.nameService} - ${formatRupiah(it.priceService)}"
                            } ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selectedMethod.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(
                    onClick = {
                        isSubmitting = true
                        val service = selectedServiceLocal
                        if (service != null) {

                            val step = if(service.typeMachine == 2){
                                0
                            }
                            else if(service.typeMachine > 2){
                                4
                            }
                            else{
                                service.typeMachine
                            }

                            val nowDate = Instant.now()
                            val formatted = DateTimeFormatter
                                .ofPattern("yyyy-MM-dd HH:mm:ss.SSSX")
                                .withZone(ZoneOffset.UTC)
                                .format(nowDate)

//                            val order = OrderLocal(
//                                customerName = customerName,
//                                serviceName = service.nameService,
//                                sizeMachine = service.sizeMachine,
//                                stepMachine = step,
//                                price = service.priceService,
//                                typePayment = selectedMethod.name,
//                                user = idUser,
//                                store = idStore,
//                                typeMachineService = service.typeMachine,
//                                numberMachine = 0,
//                                date = formatted,
//                                syncStatus = SyncStatus.PENDING
//                            )
//
//                            onSubmit(order)
                            onDismissRequest()
                        }
                    },
                    enabled = !isSubmitting && customerName.isNotBlank() && selectedServiceLocal != null,
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text("Lanjutkan")
                    }
                }
            }
        }
    }
}
