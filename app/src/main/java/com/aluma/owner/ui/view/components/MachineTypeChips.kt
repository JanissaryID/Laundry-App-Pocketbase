package com.aluma.owner.ui.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MachineTypeChips(
    modifier: Modifier = Modifier,
    wash: Boolean,
    dry: Boolean,
    service: Boolean
) {
    // Definisi data untuk setiap tipe
    val typeItems = listOf(
        Triple("Cuci", Icons.Default.WaterDrop, Color(0xFF2196F3)),
        Triple("Kering", Icons.Default.LocalFireDepartment, Color(0xFFFF9800)),
        Triple("Servis", Icons.Default.CheckCircle, Color(0xFF4CAF50))
    )

    // Filter tipe mana yang aktif
    val activeTypes = buildList {
        if (wash) add(typeItems[0])
        if (dry) add(typeItems[1])
        if (service) add(typeItems[2])
    }

    // Menggunakan Row biasa karena item sangat sedikit (max 3)
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        activeTypes.forEach { (label, icon, color) ->
            Surface(
                color = color.copy(alpha = 0.12f), // Background soft transparan
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.wrapContentSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(12.dp) // Ukuran ikon diperkecil agar proporsional
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    )
                }
            }
        }
    }
}