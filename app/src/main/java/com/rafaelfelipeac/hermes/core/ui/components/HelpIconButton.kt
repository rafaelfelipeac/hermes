package com.rafaelfelipeac.hermes.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderHairline
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.HelpIconGlyphSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.HelpIconSize

@Composable
fun HelpIconButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = CircleShape,
        color = colorScheme.surfaceContainerLow,
        contentColor = colorScheme.onSurfaceVariant,
        border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
        tonalElevation = ElevationSm,
        shadowElevation = ElevationSm,
        modifier = modifier.size(HelpIconSize),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .fillMaxSize()
                    .clickable(onClick = onClick),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = contentDescription,
                modifier = Modifier.size(HelpIconGlyphSize),
            )
        }
    }
}
