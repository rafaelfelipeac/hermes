package com.rafaelfelipeac.hermes.features.challenges.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.formatWorkoutDate
import com.rafaelfelipeac.hermes.core.ui.currentLocale
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderHairline
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuantity

@Composable
internal fun ChallengeProgressHistoryCard(
    group: ChallengeProgressDateGroup,
    isEditable: Boolean,
    onRequestEditProgress: (ChallengeProgressEntry) -> Unit,
    onDeleteProgress: (Long) -> Unit,
) {
    val currentLocale = currentLocale()

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag(challengeHistoryGroupTag(group.date)),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(SpacingMd),
            verticalArrangement = Arrangement.spacedBy(SpacingXs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatWorkoutDate(group.date, currentLocale),
                    style = typography.titleSmall,
                    color = colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text =
                        stringResource(
                            R.string.challenges_history_day_completed,
                            ChallengeQuantity.format(group.completedQuantity, currentLocale),
                        ),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
            group.entries.forEachIndexed { index, entry ->
                if (index > 0) {
                    HorizontalDivider()
                }
                ChallengeProgressEntryRow(
                    entry = entry,
                    isEditable = isEditable,
                    onEdit = { onRequestEditProgress(entry) },
                    onDelete = { onDeleteProgress(entry.id) },
                )
            }
        }
    }
}
