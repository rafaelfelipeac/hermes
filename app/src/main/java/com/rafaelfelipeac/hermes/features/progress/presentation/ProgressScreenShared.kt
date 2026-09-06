package com.rafaelfelipeac.hermes.features.progress.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.relativeDaysUntilText
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressSupportCardMinHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm

@Composable
internal fun ProgressSection(
    title: String,
    trailingContent: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = typography.titleMedium,
                color = colorScheme.onSurface,
            )
            trailingContent?.invoke()
        }

        Card(
            shape = shapes.medium,
            colors =
                CardDefaults.cardColors(
                    containerColor = colorScheme.surfaceContainerLow,
                ),
        ) {
            Column(
                modifier = Modifier.padding(SpacingLg),
                verticalArrangement = Arrangement.spacedBy(SpacingMd),
                content = { content() },
            )
        }
    }
}

@Composable
internal fun ProgressSupportBlock(
    modifier: Modifier = Modifier,
    content: ProgressSupportCardContent,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = ProgressSupportCardMinHeight)
                .clip(shapes.small)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                )
                .background(colorScheme.surfaceVariant)
                .padding(SpacingMd),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingSm),
        ) {
            Text(
                text = stringResource(content.labelRes),
                style = typography.labelMedium,
                color = colorScheme.onSurfaceVariant,
            )
            Text(
                text = content.title,
                style = typography.bodyMedium,
                color = colorScheme.onSurface,
            )
            content.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
            content.detail?.let { detail ->
                Text(
                    text = detail,
                    style = typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun daysUntilText(daysUntil: Int): String {
    return relativeDaysUntilText(
        daysUntil = daysUntil,
        todayLabel = stringResource(R.string.activity_today),
        tomorrowLabel = stringResource(R.string.activity_tomorrow),
        yesterdayLabel = stringResource(R.string.activity_yesterday),
        fallbackText = stringResource(R.string.progress_days_until, daysUntil),
    )
}

internal data class ProgressSupportCardContent(
    @StringRes val labelRes: Int,
    val title: String,
    val subtitle: String? = null,
    val detail: String? = null,
)
