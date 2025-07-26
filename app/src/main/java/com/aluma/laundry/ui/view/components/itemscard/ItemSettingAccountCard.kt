package com.aluma.laundry.ui.view.components.itemscard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ItemSettingAccountCard(
    email: String?,
    onLogout: () -> Unit
) {
    ListItem(
        headlineContent = { Text("Keluar", color = MaterialTheme.colorScheme.error) },
        supportingContent = {
            Text(email.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        leadingContent = {
            Icon(
                Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLogout() }
    )
}
