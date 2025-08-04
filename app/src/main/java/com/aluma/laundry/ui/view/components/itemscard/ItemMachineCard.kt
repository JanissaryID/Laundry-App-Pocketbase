package com.aluma.laundry.ui.view.components.itemscard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.machine.model.MachineLocal

@Composable
fun ItemMachineCard(
    machine: MachineLocal,
    onClick: () -> Unit = {}
) {
    val shape = 16

    val machineTypeLabel = if (machine.typeMachine) "Pengering" else "Cuci"
    val machineSizeLabel = if (machine.sizeMachine) "BESAR" else "KECIL"

    val typeTextColor = if (machine.typeMachine) Color(0xFFEF6C00) else Color(0xFF1976D2)

    val sizeColor = if (machine.sizeMachine) Color(0xFFE1BEE7) else Color(0xFFECEFF1)
    val sizeTextColor = if (machine.sizeMachine) Color(0xFF6A1B9A) else Color(0xFF455A64)

    val inUseStatus = if (machine.inUse) "Digunakan" else "Tersedia"
    val statusColor = if (machine.inUse) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
    val statusTextColor = if (machine.inUse) Color(0xFFD32F2F) else Color(0xFF388E3C)
    val statusIcon = if (machine.inUse) Icons.Default.SyncDisabled else Icons.Default.CheckCircle

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(shape.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Top: Title dan Ukuran
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (machine.typeMachine) Icons.Default.LocalFireDepartment else Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = typeTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mesin $machineTypeLabel #${machine.numberMachine}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }

                // Badge: Ukuran Mesin
                Box(
                    modifier = Modifier
                        .background(color = sizeColor, shape = RoundedCornerShape((shape/2).dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = machineSizeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = sizeTextColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Penggunaan
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(statusColor, shape = RoundedCornerShape((shape-4).dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusTextColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = inUseStatus,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = statusTextColor
                    )
                )
            }
        }
    }
}