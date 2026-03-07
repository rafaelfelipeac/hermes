package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WeeklyHeaderSummaryUi

internal const val WEEKLY_SUMMARY_BLOCK_TAG = "weekly-summary-block"
internal const val WEEKLY_SUMMARY_PROGRESS_TAG = "weekly-summary-progress"

@Composable
fun WeeklyHeaderSummary(
    summary: WeeklyHeaderSummaryUi,
    modifier: Modifier = Modifier,
) {
    val primarySummary =
        stringResource(
            id = R.string.weekly_training_summary_line_primary,
            summary.plannedWorkouts,
            summary.completedWorkouts,
        )
    val secondarySummary =
        buildList {
            if (summary.plannedRestEvents > 0) {
                add(
                    stringResource(
                        id = R.string.weekly_training_summary_item_rest,
                        summary.plannedRestEvents,
                    ),
                )
            }
            if (summary.plannedBusyEvents > 0) {
                add(
                    stringResource(
                        id = R.string.weekly_training_summary_item_busy,
                        summary.plannedBusyEvents,
                    ),
                )
            }
            if (summary.plannedSickEvents > 0) {
                add(
                    stringResource(
                        id = R.string.weekly_training_summary_item_sick,
                        summary.plannedSickEvents,
                    ),
                )
            }
        }.takeIf { it.isNotEmpty() }?.joinToString(
            separator = stringResource(R.string.weekly_training_summary_separator),
        )
    val progressDescription =
        stringResource(
            id = R.string.weekly_training_summary_progress_content_description,
            summary.completedWorkouts,
            summary.plannedWorkouts,
        )

    Column(
        modifier = modifier.testTag(WEEKLY_SUMMARY_BLOCK_TAG),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(
            text = primarySummary,
            style = typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )

        secondarySummary?.let { summaryText ->
            Text(
                text = summaryText,
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
        }

        LinearProgressIndicator(
            progress = { summary.progress },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag(WEEKLY_SUMMARY_PROGRESS_TAG)
                    .semantics {
                        contentDescription = progressDescription
                    },
            strokeCap = Butt,
            drawStopIndicator = {},
        )
    }
}
