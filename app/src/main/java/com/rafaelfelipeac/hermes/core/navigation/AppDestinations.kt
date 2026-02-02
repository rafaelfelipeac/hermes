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
    WEEKLY_TRAINING(R.string.nav_weekly_training, Icons.Default.Home),
    ACTIVITY(R.string.nav_activity, Icons.Default.History),
    SETTINGS(R.string.nav_settings, Icons.Default.Settings),
}
