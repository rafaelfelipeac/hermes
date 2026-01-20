package com.rafaelfelipeac.hermes.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.rafaelfelipeac.hermes.R

enum class AppDestinations(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    TRAINING_WEEK(R.string.nav_training_week, Icons.Default.Home),
    SETTINGS(R.string.nav_settings, Icons.Default.Settings),
}
