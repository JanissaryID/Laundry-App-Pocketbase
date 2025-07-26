package com.aluma.laundry.ui.view.components.itemscard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ItemSettingCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(title, color = textColor, style = MaterialTheme.typography.bodyLarge)
        },
        supportingContent = {
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
        },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = iconTint)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    )
}

