package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.IndicatorSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.WeeklyTrainingSummaryCollapsedMinHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.Zero
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WeeklyHeaderSummaryUi

internal const val WEEKLY_SUMMARY_BLOCK_TAG = "weekly-summary-block"
internal const val WEEKLY_SUMMARY_METRICS_ROW_TAG = "weekly-summary-metrics-row"
internal const val WEEKLY_SUMMARY_WORKOUTS_STAT_TAG = "weekly-summary-workouts-stat"
internal const val WEEKLY_SUMMARY_EVENTS_STAT_TAG = "weekly-summary-events-stat"
internal const val WEEKLY_SUMMARY_PROGRESS_TAG = "weekly-summary-progress"
internal const val WEEKLY_SUMMARY_SECONDARY_ROW_TAG = "weekly-summary-secondary-row"
internal const val WEEKLY_SUMMARY_TOGGLE_TAG = "weekly-summary-toggle"

@Composable
fun WeeklyHeaderSummary(
    summary: WeeklyHeaderSummaryUi,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val primarySummary =
        stringResource(
            id = R.string.weekly_training_summary_line_primary,
            summary.plannedItems,
            summary.completedItems,
        )
    val secondarySummary =
        buildList {
            if (summary.plannedRestEvents > 0) {
                add(
                    pluralStringResource(
                        id = R.plurals.weekly_training_summary_item_rest,
                        count = summary.plannedRestEvents,
                        summary.plannedRestEvents,
                    ),
                )
            }
            if (summary.plannedBusyEvents > 0) {
                add(
                    pluralStringResource(
                        id = R.plurals.weekly_training_summary_item_busy,
                        count = summary.plannedBusyEvents,
                        summary.plannedBusyEvents,
                    ),
                )
            }
            if (summary.plannedSickEvents > 0) {
                add(
                    pluralStringResource(
                        id = R.plurals.weekly_training_summary_item_sick,
                        count = summary.plannedSickEvents,
                        summary.plannedSickEvents,
                    ),
                )
            }
        }.takeIf { it.isNotEmpty() }?.joinToString(
            separator = stringResource(R.string.weekly_training_summary_separator),
        )
    val progressDescription =
        pluralStringResource(
            id = R.plurals.weekly_training_summary_progress_content_description,
            count = summary.plannedItems,
            summary.completedItems,
            summary.plannedItems,
        )
    val toggleDescription =
        stringResource(
            id =
                if (isExpanded) {
                    R.string.weekly_training_summary_collapse_details
                } else {
                    R.string.weekly_training_summary_expand_details
                },
        )
    val contentVerticalPadding = if (isExpanded) SpacingSm else SpacingXs
    val progressTopPadding = if (isExpanded) SpacingSm else Zero

    Surface(
        tonalElevation = ElevationSm,
        shape = shapes.medium,
        modifier =
            modifier
                .fillMaxWidth()
                .testTag(WEEKLY_SUMMARY_BLOCK_TAG),
    ) {
        if (isExpanded) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = SpacingLg,
                            vertical = contentVerticalPadding,
                        ),
                verticalArrangement = Arrangement.Top,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = primarySummary,
                        modifier = Modifier.weight(1f),
                        style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                            Modifier
                                .size(IndicatorSize)
                                .padding(start = SpacingMd)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { isExpanded = !isExpanded },
                                )
                                .testTag(WEEKLY_SUMMARY_TOGGLE_TAG)
                                .semantics {
                                    contentDescription = toggleDescription
                                },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowUp,
                            contentDescription = null,
                            modifier = Modifier.size(IndicatorSize),
                        )
                    }
                }

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = SpacingXxs)
                            .testTag(WEEKLY_SUMMARY_METRICS_ROW_TAG),
                    verticalArrangement = Arrangement.spacedBy(SpacingXs),
                ) {
                    WeeklyHeaderSummaryMetric(
                        label = stringResource(R.string.weekly_training_summary_metric_workouts),
                        value =
                            stringResource(
                                R.string.weekly_training_summary_metric_value,
                                summary.completedWorkouts,
                                summary.plannedWorkouts,
                            ),
                        modifier = Modifier.testTag(WEEKLY_SUMMARY_WORKOUTS_STAT_TAG),
                    )

                    WeeklyHeaderSummaryMetric(
                        label = stringResource(R.string.weekly_training_summary_metric_events),
                        value =
                            stringResource(
                                R.string.weekly_training_summary_metric_value,
                                summary.completedRaceEvents,
                                summary.plannedRaceEvents,
                            ),
                        modifier = Modifier.testTag(WEEKLY_SUMMARY_EVENTS_STAT_TAG),
                    )
                }

                secondarySummary?.let { summaryText ->
                    Text(
                        text = summaryText,
                        modifier =
                            Modifier
                                .padding(top = SpacingXxs)
                                .testTag(WEEKLY_SUMMARY_SECONDARY_ROW_TAG),
                        style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                top = progressTopPadding,
                                bottom = SpacingXs,
                            ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LinearProgressIndicator(
                        progress = { summary.progress },
                        modifier =
                            Modifier
                                .weight(1f)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { isExpanded = !isExpanded },
                                )
                                .testTag(WEEKLY_SUMMARY_PROGRESS_TAG)
                                .semantics {
                                    contentDescription = progressDescription
                                },
                        strokeCap = Butt,
                        drawStopIndicator = {},
                    )
                }
            }
        } else {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = WeeklyTrainingSummaryCollapsedMinHeight)
                        .padding(
                            horizontal = SpacingLg,
                            vertical = contentVerticalPadding,
                        ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LinearProgressIndicator(
                    progress = { summary.progress },
                    modifier =
                        Modifier
                            .weight(1f)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { isExpanded = !isExpanded },
                            )
                            .testTag(WEEKLY_SUMMARY_PROGRESS_TAG)
                            .semantics {
                                contentDescription = progressDescription
                            },
                    strokeCap = Butt,
                    drawStopIndicator = {},
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier
                            .size(IndicatorSize)
                            .padding(start = SpacingMd)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { isExpanded = !isExpanded },
                            )
                            .testTag(WEEKLY_SUMMARY_TOGGLE_TAG)
                            .semantics {
                                contentDescription = toggleDescription
                            },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(IndicatorSize),
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyHeaderSummaryMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingXs),
    ) {
        Text(
            text = label,
            style = typography.labelMedium,
            color = colorScheme.onSurfaceVariant,
        )

        Text(
            text = value,
            style = typography.titleMedium,
            color = colorScheme.onSurface,
        )
    }
}
