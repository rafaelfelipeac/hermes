package com.rafaelfelipeac.hermes.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    TRAINING_WEEK("Training Week", Icons.Default.Home),
    SETTINGS("Settings", Icons.Default.Settings),
}
