package com.aluma.owner.ui.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
    val typeLabels = listOf("Cuci", "Pengering", "Layanan")

    val typeIcons = listOf(
        Icons.Default.WaterDrop,
        Icons.Default.LocalFireDepartment,
        Icons.Default.CheckCircle
    )

    val typeColors = listOf(
        Pair(Color(0xFFE3F2FD), Color(0xFF1976D2)),  // Cuci
        Pair(Color(0xFFFFF3E0), Color(0xFFEF6C00)),  // Pengering
        Pair(Color(0xFFF3E5F5), Color(0xFF6A1B9A))   // Layanan
    )

    val types = buildList {
        if (wash) add(0)
        if (dry) add(1)
        if (service) add(2)
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(types) { index ->
            val (bgColor, textColor) = typeColors[index]
            Box(
                modifier = Modifier
                    .background(bgColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = typeIcons[index],
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = typeLabels[index],
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                    )
                }
            }
        }
    }
}