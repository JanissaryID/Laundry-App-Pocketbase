package com.aluma.owner.ui.view.components.itemscard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aluma.owner.R
import com.aluma.owner.data.service.model.ServiceRemote
import com.aluma.owner.ui.view.components.MachineTypeChips
import com.aluma.owner.utils.formatToRupiah

@Composable
fun ItemServiceCard(
    service: ServiceRemote,
    isDeleting: Boolean = false,
    onDelete: () -> Unit = {}
) {
    val machineSizeLabel = if (service.sizeMachine) stringResource(R.string.item_machine_capacity_large) else stringResource(R.string.item_machine_capacity_standard)
    val sizeColor = if (service.sizeMachine) Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
    val sizeTextColor = if (service.sizeMachine) Color(0xFF1976D2) else Color(0xFF616161)

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp), // Lebih bulat = lebih modern
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Label Kapasitas (Tag Kecil di atas)
                Surface(
                    color = sizeColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = machineSizeLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = sizeTextColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Nama Layanan
                Text(
                    text = service.nameService.orEmpty().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2D3142),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Chips Fitur (Wash, Dry, dll)
                MachineTypeChips(
                    wash = service.wash == "yes",
                    dry = service.dry == "yes",
                    service = service.service == "yes"
                )
            }

            // Bagian Kanan: Harga & Aksi
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = service.priceService?.formatToRupiah() ?: "Rp -",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tombol Hapus yang lebih clean
                IconButton(
                    onClick = onDelete,
                    enabled = !isDeleting,
                    modifier = Modifier.size(32.dp)
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.Red
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.common_delete),
                            tint = Color.Red.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}