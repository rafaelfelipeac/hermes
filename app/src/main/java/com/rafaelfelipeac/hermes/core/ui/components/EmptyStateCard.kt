package com.rafaelfelipeac.hermes.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.EmptyStateCardMaxWidth
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.EmptyStateCardPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.EmptyStateIconContainerSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.EmptyStateIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    containerColor: Color = colorScheme.surfaceContainerLow,
    iconContainerColor: Color = colorScheme.primaryContainer,
    iconContentColor: Color = colorScheme.onPrimaryContainer,
    actionContent: (@Composable () -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth().widthIn(max = EmptyStateCardMaxWidth),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.padding(EmptyStateCardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingMd),
        ) {
            Surface(
                shape = shapes.small,
                color = iconContainerColor,
            ) {
                Box(
                    modifier = Modifier.size(EmptyStateIconContainerSize),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconContentColor,
                        modifier = Modifier.size(EmptyStateIconSize),
                    )
                }
            }

            Text(
                text = title,
                style = typography.titleMedium,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = body,
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            actionContent?.let { action ->
                Box(modifier = Modifier.padding(top = SpacingSm)) {
                    action()
                }
            }
        }
    }
}
