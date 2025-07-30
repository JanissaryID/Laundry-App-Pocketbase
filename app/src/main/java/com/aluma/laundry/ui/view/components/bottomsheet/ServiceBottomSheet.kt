package com.aluma.laundry.ui.view.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.service.model.ServiceRemote
import com.aluma.laundry.data.service.remote.ServiceRemoteViewModel
import com.aluma.laundry.data.store.local.StoreLocalViewModel
import com.aluma.laundry.utils.formatWithSeparator
import com.aluma.laundry.utils.toTitleCase
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceBottomSheet(
    storeLocalViewModel: StoreLocalViewModel = koinInject(),
    serviceRemoteViewModel: ServiceRemoteViewModel = koinInject(),
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onSubmit: (ServiceRemote) -> Unit,
) {
    val storeList by storeLocalViewModel.stores.collectAsState()
    val selectedStoreIndex by storeLocalViewModel.selectedStoreIndex.collectAsState()
    val service by serviceRemoteViewModel.selectedServiceRemote.collectAsState()
    val userId by serviceRemoteViewModel.userId.collectAsState()
    val selectedStore = storeList.getOrNull(selectedStoreIndex)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var priceRaw by remember { mutableStateOf("") }
    var selectedCuci by remember { mutableStateOf(false) }
    var selectedPengering by remember { mutableStateOf(false) }
    var selectedNonMesin by remember { mutableStateOf(false) }
    var isLarge by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Reset state saat service berubah (add/edit)
    LaunchedEffect(service) {
        name = service?.nameService.orEmpty()
        priceRaw = service?.priceService ?: ""
        selectedCuci = service?.wash == "yes"
        selectedPengering = service?.dry == "yes"
        selectedNonMesin = service?.service == "yes"
        isLarge = service?.sizeMachine ?: false
    }

    val formattedPrice by remember(priceRaw) {
        derivedStateOf {
            priceRaw.filter { it.isDigit() }.toLongOrNull()?.formatWithSeparator() ?: ""
        }
    }

    val isFormValid by remember(
        name, priceRaw, selectedCuci, selectedPengering, selectedNonMesin, selectedStore
    ) {
        derivedStateOf {
            name.isNotBlank() &&
                    priceRaw.isNotBlank() &&
                    (selectedCuci || selectedPengering || selectedNonMesin) &&
                    selectedStore != null
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (service == null) "Tambah Layanan" else "Edit Layanan",
                style = MaterialTheme.typography.titleMedium,
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it.toTitleCase() },
                label = { Text("Nama Layanan") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formattedPrice,
                onValueChange = {
                    priceRaw = it.filter { ch -> ch.isDigit() }
                },
                label = { Text("Harga (dalam angka)") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Tipe Mesin:")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCuci,
                    onClick = { selectedCuci = !selectedCuci },
                    label = { Text("Cuci") }
                )
                FilterChip(
                    selected = selectedPengering,
                    onClick = { selectedPengering = !selectedPengering },
                    label = { Text("Pengering") }
                )
                FilterChip(
                    selected = selectedNonMesin,
                    onClick = { selectedNonMesin = !selectedNonMesin },
                    label = { Text("Layanan Non Mesin") }
                )
            }

            Text("Ukuran Mesin:")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !isLarge,
                    onClick = { isLarge = false },
                    label = { Text("Kecil") }
                )
                FilterChip(
                    selected = isLarge,
                    onClick = { isLarge = true },
                    label = { Text("Besar") }
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Kembali")
                }

                Button(
                    onClick = {
                        isSubmitting = true
                        onSubmit(
                            ServiceRemote(
                                nameService = name,
                                priceService = priceRaw.filter { it.isDigit() },
                                wash = if (selectedCuci) "yes" else "no",
                                dry = if (selectedPengering) "yes" else "no",
                                service = if (selectedNonMesin) "yes" else "no",
                                sizeMachine = isLarge,
                                store = selectedStore!!.id,
                                user = userId,
                            )
                        )
                    },
                    enabled = isFormValid && !isSubmitting,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(if (service == null) "Tambah" else "Simpan")
                    }
                }
            }
        }
    }
}
