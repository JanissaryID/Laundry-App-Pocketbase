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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
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
import com.aluma.laundry.data.machine.model.MachineRemote

@Composable
fun ItemMachineCard(
    machine: MachineRemote,
    onClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(12.dp)
    val machineTypeLabel = if (machine.typeMachine) "Pengering" else "Cuci"
    val machineSizeLabel = if (machine.sizeMachine) "BESAR" else "KECIL"

    val typeColor = if (machine.typeMachine) Color(0xFFFFF3E0) else Color(0xFFE3F2FD)
    val typeTextColor = if (machine.typeMachine) Color(0xFFEF6C00) else Color(0xFF1976D2)

    val sizeColor = if (machine.sizeMachine) Color(0xFFF3E5F5) else Color(0xFFF5F5F5)
    val sizeTextColor = if (machine.sizeMachine) Color(0xFF6A1B9A) else Color(0xFF607D8B)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header: Jenis & Ukuran Mesin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (machine.typeMachine) Icons.Default.LocalFireDepartment else Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = typeTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mesin $machineTypeLabel #${machine.numberMachine}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = typeTextColor
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .background(sizeColor, shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = machineSizeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = sizeTextColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timer
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${machine.timer} Menit",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                )
            }
        }
    }
}