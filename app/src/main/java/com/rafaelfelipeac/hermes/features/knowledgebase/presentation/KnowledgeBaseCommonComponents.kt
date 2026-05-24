package com.rafaelfelipeac.hermes.features.knowledgebase.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd

@Composable
internal fun KnowledgeBaseHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = colorScheme.primary)
        Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
            Text(text = title, style = typography.titleLarge)
            Text(text = subtitle, style = typography.bodyMedium, color = colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun KnowledgeNoteCard(
    title: String,
    preview: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(SpacingLg), verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
            Text(text = title, style = typography.titleMedium, color = colorScheme.onSurfaceVariant)
            Text(text = preview, style = typography.bodyMedium, color = colorScheme.onSurfaceVariant)
            subtitle?.let {
                Text(text = it, style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
            }
        }
    }
}
