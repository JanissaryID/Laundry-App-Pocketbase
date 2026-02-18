package com.aluma.owner.ui.view.components.bottomsheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aluma.owner.data.service.model.ServiceRemote
import com.aluma.owner.data.service.remote.ServiceRemoteViewModel
import com.aluma.owner.data.store.local.StoreLocalViewModel
import com.aluma.owner.utils.toTitleCase
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

    LaunchedEffect(service) {
        name = service?.nameService.orEmpty()
        priceRaw = service?.priceService ?: ""
        selectedCuci = service?.wash == "yes"
        selectedPengering = service?.dry == "yes"
        selectedNonMesin = service?.service == "yes"
        isLarge = service?.sizeMachine ?: false
    }

    val isFormValid = name.isNotBlank() && priceRaw.isNotBlank() &&
            (selectedCuci || selectedPengering || selectedNonMesin) &&
            selectedStore != null

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp), // Beri ruang di bawah agar tidak mepet navigasi HP
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Text(
                text = if (service == null) "Tambah Layanan Baru" else "Edit Detail Layanan",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Input Nama & Harga
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.toTitleCase() },
                    label = { Text("Nama Layanan (Contoh: Cuci Kering)") },
                    placeholder = { Text("Masukkan nama layanan") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = priceRaw,
                    onValueChange = { if (it.all { char -> char.isDigit() }) priceRaw = it },
                    label = { Text("Harga") },
                    prefix = { Text("Rp ", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Tipe Mesin
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle(title = "Tipe Layanan", icon = Icons.Default.Settings)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomFilterChip(selected = selectedCuci, label = "Cuci") { selectedCuci = !selectedCuci }
                    CustomFilterChip(selected = selectedPengering, label = "Pengering") { selectedPengering = !selectedPengering }
                    CustomFilterChip(selected = selectedNonMesin, label = "Tanpa Mesin") { selectedNonMesin = !selectedNonMesin }
                }
            }

            // Ukuran Mesin
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle(title = "Kapasitas Mesin", icon = Icons.Default.Straighten)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ChoiceCard(
                        label = "Standar",
                        isSelected = !isLarge,
                        modifier = Modifier.weight(1f),
                        onClick = { isLarge = false }
                    )
                    ChoiceCard(
                        label = "Besar",
                        isSelected = isLarge,
                        modifier = Modifier.weight(1f),
                        onClick = { isLarge = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Batal")
                }

                Button(
                    onClick = {
                        isSubmitting = true
                        onSubmit(
                            ServiceRemote(
                                nameService = name,
                                priceService = priceRaw,
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
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text(if (service == null) "Tambah" else "Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Komponen Pendukung agar UI Konsisten
@Composable
fun SectionTitle(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = Color.Gray)
    }
}

@Composable
fun CustomFilterChip(selected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = RoundedCornerShape(8.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun ChoiceCard(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
            Text(label, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray, fontWeight = FontWeight.Medium)
        }
    }
}