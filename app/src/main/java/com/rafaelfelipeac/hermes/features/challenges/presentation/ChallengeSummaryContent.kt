@file:Suppress("ArgumentListWrapping", "ImportOrdering", "MaximumLineLength", "MaxLineLength")

package com.rafaelfelipeac.hermes.features.challenges.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.currentLocale
import com.rafaelfelipeac.hermes.core.ui.components.TitleChip
import com.rafaelfelipeac.hermes.core.ui.components.formatWorkoutDate
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.core.ui.theme.contentColorForBackground
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeCalculationResult

@Composable
internal fun ChallengeSummaryContent(
    modifier: Modifier = Modifier,
    challenge: Challenge,
    category: Category?,
    calculation: ChallengeCalculationResult?,
    showProgressBar: Boolean,
) {
    val categoryAccent = category?.let { categoryAccentColor(it.colorId) }
    val currentLocale = currentLocale()
    Text(
        text = challenge.title,
        style = typography.titleMedium,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(SpacingXs),
        verticalArrangement = Arrangement.spacedBy(SpacingXs),
    ) {
        TitleChip(
            label = challengeTargetTypeLabel(challenge.targetType),
            containerColor = colorScheme.surfaceVariant,
            contentColor = colorScheme.onSurfaceVariant,
        )
        category?.let {
            TitleChip(
                label = it.name,
                containerColor = categoryAccent ?: colorScheme.surfaceVariant,
                contentColor = categoryAccent?.let { accent -> contentColorForBackground(accent) } ?: colorScheme.onSurfaceVariant,
            )
        }
        calculation?.let { result ->
            TitleChip(
                label = challengeStatusLabel(result.status),
                containerColor = challengeProgressContainerColor(result.status),
                contentColor = challengeProgressColor(result.status),
            )
        }
    }
    challenge.description?.takeIf { it.isNotBlank() }?.let { description ->
        Text(text = description, color = colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
    Text(
        text = stringResource(R.string.challenges_date_range, formatWorkoutDate(challenge.startDate, currentLocale), formatWorkoutDate(challenge.endDate, currentLocale)),
        style = typography.bodySmall,
        color = colorScheme.onSurfaceVariant,
    )
    calculation?.let { result ->
        if (showProgressBar) {
            ChallengeProgressBar(result.visualProgress, challengeProgressColor(result.status), modifier)
        }
        Text(
            text = challengeProgressLabel(result),
            modifier = Modifier.fillMaxWidth(),
            style = typography.labelLarge,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
        )
    }
}
