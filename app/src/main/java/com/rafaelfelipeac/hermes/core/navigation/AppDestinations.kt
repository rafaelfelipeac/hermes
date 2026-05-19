package com.rafaelfelipeac.hermes.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.ui.graphics.vector.ImageVector
import com.rafaelfelipeac.hermes.R

enum class AppDestinations(
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    HOME(R.string.weekly_training_nav_label, Icons.Default.Home),
    PROGRESS(R.string.progress_nav_label, Icons.Outlined.QueryStats),
    EVENTS(R.string.race_events_nav_label, Icons.Outlined.Flag),
    BROWSE(R.string.browse_nav_label, Icons.Outlined.GridView),
}
