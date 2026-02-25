package com.aluma.owner.utils

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController

/**
 * Safely pops the back stack to avoid blank screens caused by multiple rapid clicks.
 * Checks if the current destination is in the RESUMED state and if there's a previous entry.
 */
fun NavController.safePopBackStack() {
    val currentEntry = currentBackStackEntry
    val canPop = currentEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED
    
    if (canPop && previousBackStackEntry != null) {
        popBackStack()
    }
}
