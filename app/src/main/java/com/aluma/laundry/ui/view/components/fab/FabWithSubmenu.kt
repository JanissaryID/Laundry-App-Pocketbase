package com.aluma.laundry.ui.view.components.fab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FabWithSubmenu(
    isFabExpanded: Boolean,
    onFabToggle: () -> Unit,
    onDismissRequest: () -> Unit,
    listMachine: () -> Unit,
    listOrder: () -> Unit,
    addOrder: () -> Unit,
) {
    // Kita gunakan Box fillMaxSize tanpa padding apa pun di layer ini
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. BACKGROUND / SCRIM (Full Screen murni)
        AnimatedVisibility(
            visible = isFabExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismissRequest() }
            )
        }

        // 2. KONTEN TOMBOL (FAB & Sub-menu)
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp) // Jarak standar FAB dari pojok layar
                .navigationBarsPadding() // Menangani padding navigasi Android (Bar/Gesture)
        ) {
            // Sub-menu Items
            AnimatedVisibility(
                visible = isFabExpanded,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    FabWithLabels("Daftar Riwayat", Icons.AutoMirrored.Filled.FormatListBulleted, onClick = listOrder)
                    FabWithLabels("Status Mesin", Icons.Default.LocalLaundryService, onClick = listMachine)
                    FabWithLabel(
                        label = "Order Baru",
                        icon = Icons.Default.Add,
                        containerColor = MaterialTheme.colorScheme.primary,
                        iconColor = Color.White,
                        onClick = addOrder
                    )
                }
            }

            // Tombol Utama (Toggle)
            FloatingActionButton(
                onClick = onFabToggle,
                containerColor = if (isFabExpanded) Color.White else MaterialTheme.colorScheme.primary,
                contentColor = if (isFabExpanded) Color.Black else Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(
                    imageVector = if (isFabExpanded) Icons.Default.Close else Icons.Default.Menu,
                    contentDescription = null,
                    modifier = Modifier.rotate(if (isFabExpanded) 90f else 0f)
                )
            }
        }
    }
}

@Composable
fun FabWithLabel(
    label: String,
    icon: ImageVector,
    containerColor: Color = Color.White,
    iconColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClick() }
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.8f)
        ) {
            Text(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp),
            containerColor = containerColor,
            contentColor = iconColor,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}