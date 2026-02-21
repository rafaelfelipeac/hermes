package com.rafaelfelipeac.hermes.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs

@Composable
fun TitleChip(
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = shapes.small,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = typography.labelSmall,
            modifier =
                Modifier.padding(
                    horizontal = SpacingMd,
                    vertical = SpacingXs,
                ),
        )
    }
}
