package com.rafaelfelipeac.hermes.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.rafaelfelipeac.hermes.R

enum class AppDestinations(
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    WEEKLY_TRAINING(R.string.weekly_training_nav_label, Icons.Default.Home),
    ACTIVITY(R.string.activity_nav_label, Icons.Default.History),
    SETTINGS(R.string.settings_nav_label, Icons.Default.Settings),
}
