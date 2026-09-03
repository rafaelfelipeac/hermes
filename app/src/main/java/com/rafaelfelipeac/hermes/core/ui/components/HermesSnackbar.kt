package com.rafaelfelipeac.hermes.core.ui.components

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal fun hermesSnackbarPalette(colors: ColorScheme): Triple<Color, Color, Color> =
    Triple(
        first = colors.surfaceVariant,
        second = colors.onSurfaceVariant,
        third = colors.primary,
    )

@Composable
internal fun HermesSnackbar(snackbarData: SnackbarData) {
    val palette = hermesSnackbarPalette(colorScheme)

    Snackbar(
        snackbarData = snackbarData,
        containerColor = palette.first,
        contentColor = palette.second,
        actionColor = palette.third,
    )
}
