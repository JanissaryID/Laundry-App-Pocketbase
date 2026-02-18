package com.aluma.owner.ui.view.components.itemscard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ChevronRight
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
import com.aluma.owner.data.machine.model.MachineRemote

@Composable
fun ItemMachineCard(
    machine: MachineRemote,
    onClick: () -> Unit = {}
) {
    val machineTypeLabel = if (machine.typeMachine) "Pengering" else "Pencuci"
    val machineSizeLabel = if (machine.sizeMachine) "Kapasitas Besar" else "Kapasitas Standar"

    // Warna berdasarkan tipe (Pencuci = Biru, Pengering = Oranye/Merah Muda)
    val typeColor = if (machine.typeMachine) Color(0xFFFF9800) else Color(0xFF2196F3)
    val typeBg = typeColor.copy(alpha = 0.1f)

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp), // Konsisten dengan card lain yang kita buat
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
            // Sisi Kiri: Ikon Mesin dalam Lingkaran
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(typeBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (machine.typeMachine) Icons.Default.LocalFireDepartment else Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Tengah: Informasi Mesin
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mesin $machineTypeLabel",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Text(
                    text = "Nomor Seri #${machine.numberMachine}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3142)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Kapasitas & Durasi dalam Row kecil
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.LightGray
                    )
                    Text(
                        text = " ${machine.timer} mnt",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "•  $machineSizeLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (machine.sizeMachine) Color(0xFF6A1B9A) else Color.Gray,
                        fontWeight = if (machine.sizeMachine) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // Sisi Kanan: Indikator Panah (UX Hint)
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}