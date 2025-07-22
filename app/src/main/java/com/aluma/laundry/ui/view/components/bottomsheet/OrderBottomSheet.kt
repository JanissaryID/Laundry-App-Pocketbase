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
import com.aluma.laundry.data.api.order.model.Order
import com.aluma.laundry.data.api.order.model.TypePayment
import com.aluma.laundry.data.api.service.Service
import com.aluma.laundry.data.api.service.ServiceViewModel
import com.aluma.laundry.ui.view.components.dropdown.ServiceDropdown
import com.aluma.laundry.utils.formatRupiah
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderBottomSheet(
    onDismissRequest: () -> Unit,
    serviceViewModel: ServiceViewModel = koinInject(),
    onSubmit: (order: Order) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var customerName by remember { mutableStateOf("") }
    var selectedService by remember { mutableStateOf<Service?>(null) }
    var selectedMethod by remember { mutableStateOf(TypePayment.TUNAI) }

    val services by serviceViewModel.service.collectAsState()
    val idUser by serviceViewModel.idUser.collectAsState()
    val idStore by serviceViewModel.idStore.collectAsState()

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
                services = services,
                selectedService = selectedService,
                onServiceSelected = {
                    selectedService = it
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
                    if(selectedService != null){
                        Text(
                            text = selectedService?.let {
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
                        val service = selectedService
                        if (service != null) {

                            val order = Order(
                                customerName = customerName,
                                serviceName = service.nameService,
                                sizeMachine = service.sizeMachine,
                                stepMachine = 0,
                                price = service.priceService,
                                typePayment = selectedMethod.name,
                                user = idUser,
                                store = idStore
                            )

                            onSubmit(order)
                            onDismissRequest()
                        }
                    },
                    enabled = !isSubmitting && customerName.isNotBlank() && selectedService != null,
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
