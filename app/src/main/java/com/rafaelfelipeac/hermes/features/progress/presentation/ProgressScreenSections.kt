package com.rafaelfelipeac.hermes.features.progress.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderHairline
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressActivityContentGap
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressCategoryColorDotSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressReadoutBarHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressRecentActivityTopPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressTrainingMixBarHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressTrendAxisWidth
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressTrendBarHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressTrendBarMinWidth
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressTrendChartRowSpacing
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressTrendChartVerticalPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressTrendCountMinHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressTrendSectionSpacing
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi
import com.rafaelfelipeac.hermes.features.trophies.presentation.FeaturedTrophyMode
import com.rafaelfelipeac.hermes.features.trophies.presentation.FeaturedTrophyUi
import com.rafaelfelipeac.hermes.features.trophies.presentation.trophyNameRes
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val CURRENT_WEEK_TREND_BAR_ALPHA = 0.24f
private const val TREND_LABEL_MAX_LINES = 2

@Composable
internal fun ProgressWeeklyReadout(
    readout: ProgressWeeklyReadoutUi,
    onOpenWorkout: (ProgressNextFocusUi) -> Unit,
) {
    val remainingItems = (readout.plannedWorkouts - readout.completedWorkouts).coerceAtLeast(0)

    ProgressSection(
        title = stringResource(R.string.progress_section_weekly_readout),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
            Text(
                text = stringResource(R.string.progress_readout_on_track),
                style = typography.labelLarge,
                color = colorScheme.onSurfaceVariant,
            )
            Text(
                text =
                    stringResource(
                        R.string.progress_readout_completed,
                        readout.completedWorkouts,
                        readout.plannedWorkouts,
                    ),
                style = typography.titleMedium,
                color = colorScheme.onSurface,
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(ProgressReadoutBarHeight)
                        .clip(shapes.small)
                        .background(colorScheme.surfaceVariant),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(readout.completionPercent.percentFraction())
                            .background(colorScheme.onSurfaceVariant),
                )
            }
            if (remainingItems > 0) {
                Text(
                    text =
                        pluralStringResource(
                            R.plurals.progress_readout_items_left,
                            remainingItems,
                            remainingItems,
                        ),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
            readout.nextFocus?.let { nextFocus ->
                HorizontalDivider(color = colorScheme.outlineVariant)
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(shapes.small)
                            .background(colorScheme.surfaceVariant)
                            .clickable(onClick = { onOpenWorkout(nextFocus) })
                            .padding(SpacingMd),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(SpacingXs)) {
                        Text(
                            text = stringResource(R.string.progress_support_next_focus),
                            style = typography.labelMedium,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = nextFocus.title,
                            style = typography.bodyMedium,
                            color = colorScheme.onSurface,
                        )
                        Text(
                            text = daysUntilText(nextFocus.daysUntil),
                            style = typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ProgressWeeklyTrend(
    weeks: List<ProgressWeekBarUi>,
    insight: ProgressWeeklyTrendInsightUi?,
    locale: Locale,
) {
    val maxPlannedWorkouts = weeks.maxOfOrNull { it.plannedWorkouts } ?: 0

    Column(verticalArrangement = Arrangement.spacedBy(ProgressTrendSectionSpacing)) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.padding(vertical = ProgressTrendChartVerticalPadding),
                verticalArrangement = Arrangement.spacedBy(ProgressTrendChartRowSpacing),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column(
                        modifier =
                            Modifier
                                .width(ProgressTrendAxisWidth)
                                .height(ProgressTrendBarHeight),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = maxPlannedWorkouts.toString(),
                            style = typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = (maxPlannedWorkouts / 2).toString(),
                            style = typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = AXIS_LABEL_EMPTY,
                            style = typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }

                    weeks.forEach { week ->
                        val isCurrentWeek = week.isCurrentWeek
                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .defaultMinSize(minWidth = ProgressTrendBarMinWidth)
                                    .height(
                                        if (maxPlannedWorkouts > 0) {
                                            (ProgressTrendBarHeight * week.plannedFraction(maxPlannedWorkouts))
                                                .coerceAtLeast(ProgressTrendCountMinHeight)
                                        } else {
                                            ProgressTrendCountMinHeight
                                        },
                                    )
                                    .clip(shapes.small)
                                    .background(colorScheme.surfaceVariant)
                                    .border(
                                        width = BorderHairline,
                                        color =
                                            if (isCurrentWeek) {
                                                colorScheme.primary.copy(alpha = CURRENT_WEEK_TREND_BAR_ALPHA)
                                            } else {
                                                colorScheme.outlineVariant
                                            },
                                        shape = shapes.small,
                                    ),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(week.completedFraction().coerceAtMost(1f))
                                        .background(
                                            if (week.isCurrentWeek) {
                                                colorScheme.primary
                                            } else {
                                                colorScheme.secondary
                                            },
                                        ),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(ProgressTrendAxisWidth))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                ) {
                    Spacer(modifier = Modifier.width(ProgressTrendAxisWidth))
                    weeks.forEach { week ->
                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .defaultMinSize(
                                        minWidth = ProgressTrendBarMinWidth,
                                        minHeight = ProgressTrendCountMinHeight,
                                    ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = week.chartLabel(locale = locale),
                                style = typography.labelSmall,
                                color = if (week.isCurrentWeek) colorScheme.onSurface else colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                maxLines = TREND_LABEL_MAX_LINES,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(ProgressTrendAxisWidth))
                }
            }
        }

        Text(
            text = stringResource(R.string.progress_weekly_chart_caption),
            style = typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
        )

        if (insight?.kind == ProgressWeeklyTrendInsightKind.CURRENT_WEEK_HEAVIEST) {
            Text(
                text = stringResource(R.string.progress_weekly_insight_heaviest),
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun ProgressCategoryDistribution(
    items: List<ProgressCategoryShareUi>,
    insight: ProgressTrainingMixInsightUi?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(
            text = stringResource(R.string.progress_training_mix_caption),
            style = typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(ProgressTrainingMixBarHeight)
                    .clip(shapes.small),
        ) {
            items.forEach { item ->
                Box(
                    modifier =
                        Modifier
                            .weight(item.count.toFloat())
                            .fillMaxHeight()
                            .background(categoryAccentColor(item.colorId)),
                )
            }
        }

        items.forEach { item ->
            val accent = categoryAccentColor(item.colorId)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingMd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(ProgressCategoryColorDotSize)
                            .clip(CircleShape)
                            .background(accent),
                )
                Text(
                    text = item.name,
                    style = typography.bodyMedium,
                    color = colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.progress_weekly_chart_count, item.count, item.sharePercent),
                    style = typography.labelMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }

        insight?.let { trainingMixInsight ->
            Text(
                text = trainingMixInsight.trainingMixInsightText(),
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun ProgressSupportingProgress(
    nextFocus: ProgressNextFocusUi?,
    upcomingEvent: ProgressUpcomingEventUi?,
    trophyHighlight: FeaturedTrophyUi?,
    onOpenWorkout: (ProgressNextFocusUi) -> Unit,
    onOpenEvent: (ProgressUpcomingEventUi) -> Unit,
    onOpenTrophy: (FeaturedTrophyUi) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        val hasTopRow = nextFocus != null || upcomingEvent != null

        when {
            nextFocus != null && upcomingEvent != null -> {
                Row(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
                    ProgressSupportBlock(
                        content =
                            ProgressSupportCardContent(
                                labelRes = R.string.progress_support_next_focus,
                                title = nextFocus.title,
                                subtitle = daysUntilText(nextFocus.daysUntil),
                            ),
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenWorkout(nextFocus) },
                    )
                    ProgressSupportBlock(
                        content =
                            ProgressSupportCardContent(
                                labelRes = R.string.progress_support_upcoming_event,
                                title = upcomingEvent.title,
                                subtitle = daysUntilText(upcomingEvent.daysUntil),
                            ),
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenEvent(upcomingEvent) },
                    )
                }
            }

            nextFocus != null -> {
                ProgressSupportBlock(
                    content =
                        ProgressSupportCardContent(
                            labelRes = R.string.progress_support_next_focus,
                            title = nextFocus.title,
                            subtitle = daysUntilText(nextFocus.daysUntil),
                        ),
                    onClick = { onOpenWorkout(nextFocus) },
                )
            }

            upcomingEvent != null -> {
                ProgressSupportBlock(
                    content =
                        ProgressSupportCardContent(
                            labelRes = R.string.progress_support_upcoming_event,
                            title = upcomingEvent.title,
                            subtitle = daysUntilText(upcomingEvent.daysUntil),
                        ),
                    onClick = { onOpenEvent(upcomingEvent) },
                )
            }
        }

        trophyHighlight?.let { highlight ->
            if (hasTopRow) {
                HorizontalDivider(color = colorScheme.outlineVariant)
            }

            val trophy = highlight.trophy
            ProgressSupportBlock(
                content =
                    ProgressSupportCardContent(
                        labelRes = R.string.progress_support_trophy,
                        title = stringResource(trophyNameRes(trophy.trophyId)),
                        subtitle =
                            if (highlight.mode == FeaturedTrophyMode.RECENT_UNLOCK) {
                                stringResource(R.string.progress_trophy_recent_unlock)
                            } else {
                                stringResource(R.string.progress_trophy_nearest)
                            },
                        detail =
                            stringResource(
                                R.string.progress_weekly_chart_count,
                                trophy.currentValue,
                                trophy.target,
                            ),
                    ),
                onClick = { onOpenTrophy(highlight) },
            )
        }
    }
}

@Composable
internal fun ProgressRecentActivity(
    items: List<ActivityItemUi>,
    onOpenActivityItem: (ActivityItemUi) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        Text(
            text = stringResource(R.string.progress_recent_activity_caption),
            style = typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(ProgressRecentActivityTopPadding))

        items.take(RECENT_ACTIVITY_TIMELINE_LIMIT).forEachIndexed { index, item ->
            if (index > 0) {
                HorizontalDivider(color = colorScheme.outlineVariant)
            }
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(onClick = { onOpenActivityItem(item) }),
                horizontalArrangement = Arrangement.spacedBy(ProgressActivityContentGap),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = item.time,
                    style = typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(SpacingXs),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = item.title,
                        style = typography.bodyMedium,
                        color = colorScheme.onSurface,
                    )
                    item.subtitle?.takeIf { it.isNotBlank() }?.let { subtitle ->
                        Text(
                            text = subtitle,
                            style = typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressTrainingMixInsightUi.trainingMixInsightText(): String {
    return when (kind) {
        ProgressTrainingMixInsightKind.BALANCED -> stringResource(R.string.progress_training_mix_balanced)
        ProgressTrainingMixInsightKind.DOMINANT_CATEGORY ->
            stringResource(R.string.progress_training_mix_dominant, categoryName.orEmpty())
    }
}

internal fun ProgressWeekBarUi.chartLabel(locale: Locale): String {
    return weekStartDate.format(DateTimeFormatter.ofPattern("MMM d", locale))
}

private fun Int.percentFraction(): Float {
    return (this / PERCENT_MAX.toFloat()).coerceIn(PERCENT_MIN_FRACTION, PERCENT_MAX_FRACTION)
}

internal fun ProgressWeekBarUi.plannedFraction(maxPlannedWorkouts: Int): Float {
    if (maxPlannedWorkouts <= 0) return 0f
    return (plannedWorkouts / maxPlannedWorkouts.toFloat()).coerceIn(PERCENT_MIN_FRACTION, PERCENT_MAX_FRACTION)
}

internal fun ProgressWeekBarUi.completedFraction(): Float {
    if (plannedWorkouts <= 0) return 0f
    return (completedWorkouts / plannedWorkouts.toFloat()).coerceIn(PERCENT_MIN_FRACTION, PERCENT_MAX_FRACTION)
}

private const val AXIS_LABEL_EMPTY = "0"
private const val RECENT_ACTIVITY_TIMELINE_LIMIT = 5
private const val PERCENT_MAX = 100
private const val PERCENT_MIN_FRACTION = 0f
private const val PERCENT_MAX_FRACTION = 1f
