package com.aluma.laundry.ui.view.components.itemscard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aluma.laundry.R
import com.aluma.laundry.data.machine.model.MachineLocal
import com.aluma.laundry.data.order.utils.Quad

@Composable
fun ItemMachineCard(
    machine: MachineLocal,
    onClick: () -> Unit = {}
) {
    val corner = 16.dp

    // Logika Warna & Ikon berdasarkan Tipe (Cuci vs Pengering)
    val (machineLabel, typeIcon, typeColor) = if (machine.typeMachine) {
        Triple(stringResource(R.string.machine_dryer), Icons.Default.LocalFireDepartment, Color(0xFFEF6C00))
    } else {
        Triple(stringResource(R.string.machine_washer), Icons.Default.WaterDrop, Color(0xFF2196F3))
    }

    // Logika Status Penggunaan
    val (statusLabel, statusColor, statusTextColor, statusIcon) = if (machine.inUse) {
        Quad(stringResource(R.string.in_use), Color(0xFFFFEBEE), Color(0xFFD32F2F), Icons.Default.HourglassTop)
    } else {
        Quad(stringResource(R.string.available), Color(0xFFE8F5E9), Color(0xFF388E3C), Icons.Default.CheckCircle)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(corner),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- HEADER: Nomor & Tipe ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = typeColor.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = null,
                            tint = typeColor,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.machine_unit_format, machine.numberMachine),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = stringResource(R.string.machine_type_format, machineLabel),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                // Badge Ukuran (Kecil/Besar)
                Surface(
                    color = if (machine.sizeMachine) Color(0xFFF3E5F5) else Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (machine.sizeMachine) stringResource(R.string.capacity_12kg) else stringResource(R.string.capacity_7kg),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (machine.sizeMachine) Color(0xFF7B1FA2) else Color(0xFF616161)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- STATUS BAR ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = statusColor,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusLabel.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusTextColor,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}