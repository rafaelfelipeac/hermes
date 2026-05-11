package com.rafaelfelipeac.hermes.features.progress.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.EmptyStateCard
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressActivityDotSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressCategoryColorDotSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressReadoutBarHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressScreenBottomPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressSupportCardMinHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressTrainingMixBarHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressTrendAxisWidth
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressTrendBarHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressTrendCountMinHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.core.strings.relativeDaysUntilText
import com.rafaelfelipeac.hermes.features.trophies.presentation.FeaturedTrophyMode
import com.rafaelfelipeac.hermes.features.trophies.presentation.trophyNameRes
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    onOpenActivity: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val configuration = LocalConfiguration.current
    val locale = configuration.locales.get(0) ?: Locale.getDefault()

    ProgressContent(
        state = state,
        locale = locale,
        onOpenActivity = onOpenActivity,
        modifier = modifier,
    )
}

@Composable
internal fun ProgressContent(
    state: ProgressState,
    locale: Locale,
    onOpenActivity: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.emptyReason != null) {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(
                        start = SpacingXl,
                        top = SpacingXl,
                        end = SpacingXl,
                        bottom = ProgressScreenBottomPadding,
                    ),
            verticalArrangement = Arrangement.spacedBy(SpacingXl),
        ) {
            Text(
                text = stringResource(R.string.progress_title),
                style = typography.headlineSmall,
                color = colorScheme.onSurface,
            )

            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                EmptyStateCard(
                    icon = Icons.Outlined.QueryStats,
                    title = stringResource(R.string.progress_empty_title),
                    body = stringResource(R.string.progress_empty),
                )
            }
        }

        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding =
            PaddingValues(
                start = SpacingXl,
                top = SpacingXl,
                end = SpacingXl,
                bottom = ProgressScreenBottomPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(SpacingXl),
    ) {
        item {
            Text(
                text = stringResource(R.string.progress_title),
                style = typography.headlineSmall,
                color = colorScheme.onSurface,
            )
        }

        items(
            items = state.sections,
            key = { it.key },
        ) { section ->
            when (section) {
                is ProgressSectionUi.WeeklyReadout -> {
                    ProgressWeeklyReadout(readout = section.readout)
                }
                is ProgressSectionUi.WeeklyTrend -> {
                    ProgressSection(
                        title = stringResource(R.string.progress_section_weekly_trend),
                    ) {
                        ProgressWeeklyTrend(
                            weeks = section.weeks,
                            insight = section.insight,
                            locale = locale,
                        )
                    }
                }
                is ProgressSectionUi.TrainingMix -> {
                    ProgressSection(
                        title = stringResource(R.string.progress_section_training_mix),
                    ) {
                        ProgressCategoryDistribution(
                            items = section.items,
                            insight = section.insight,
                        )
                    }
                }
                is ProgressSectionUi.SupportingProgress -> {
                    ProgressSection(
                        title = stringResource(R.string.progress_section_supporting_progress),
                    ) {
                        ProgressSupportingProgress(
                            nextFocus = section.nextFocus,
                            upcomingEvent = section.upcomingEvent,
                            trophyHighlight = section.trophyHighlight,
                        )
                    }
                }
                is ProgressSectionUi.RecentActivity -> {
                    ProgressSection(
                        title = stringResource(R.string.progress_section_recent_activity),
                        trailingContent = {
                            TextButton(onClick = onOpenActivity) {
                                Text(stringResource(R.string.progress_view_all_activity))
                            }
                        },
                    ) {
                        ProgressRecentActivity(items = section.items)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressWeeklyReadout(readout: ProgressWeeklyReadoutUi) {
    val remainingWorkouts = (readout.plannedWorkouts - readout.completedWorkouts).coerceAtLeast(0)

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
            if (remainingWorkouts > 0) {
                Text(
                    text =
                        pluralStringResource(
                            R.plurals.progress_readout_workouts_left,
                            remainingWorkouts,
                            remainingWorkouts,
                        ),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
            readout.nextFocus?.let { nextFocus ->
                HorizontalDivider(color = colorScheme.outlineVariant)
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

@Composable
private fun ProgressSection(
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

        Card(shape = shapes.medium) {
            Column(
                modifier = Modifier.padding(SpacingLg),
                verticalArrangement = Arrangement.spacedBy(SpacingMd),
                content = { content() },
            )
        }
    }
}

@Composable
private fun ProgressWeeklyTrend(
    weeks: List<ProgressWeekBarUi>,
    insight: ProgressWeeklyTrendInsightUi?,
    locale: Locale,
) {
    val formatter = DateTimeFormatter.ofPattern(WEEK_LABEL_FORMAT_PATTERN, locale)
    val currentWeekIndex = weeks.indexOfFirst { it.isCurrentWeek }
    val maxPlannedWorkouts = weeks.maxOfOrNull { it.plannedWorkouts } ?: 0

    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingSm),
            verticalAlignment = Alignment.Bottom,
        ) {
            Spacer(modifier = Modifier.width(ProgressTrendAxisWidth))
            weeks.forEach { week ->
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = ProgressTrendCountMinHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(
                            R.string.progress_weekly_chart_count,
                            week.completedWorkouts,
                            week.plannedWorkouts
                        ),
                        style = typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

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
                val plannedFraction = week.plannedFraction(maxPlannedWorkouts)
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(ProgressTrendBarHeight)
                            .clip(shapes.small)
                            .background(colorScheme.surfaceVariant),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(plannedFraction)
                                .background(
                                    colorScheme.surfaceVariant,
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
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingSm),
        ) {
            Spacer(modifier = Modifier.width(ProgressTrendAxisWidth))
            weeks.forEachIndexed { index, week ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = week.chartLabel(index = index, currentWeekIndex = currentWeekIndex, formatter = formatter),
                        style = typography.labelSmall,
                        color = if (week.isCurrentWeek) colorScheme.onSurface else colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
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
private fun ProgressCategoryDistribution(
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
                    text = item.countShareText(),
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
private fun ProgressSupportingProgress(
    nextFocus: ProgressNextFocusUi?,
    upcomingEvent: ProgressUpcomingEventUi?,
    trophyHighlight: com.rafaelfelipeac.hermes.features.trophies.presentation.FeaturedTrophyUi?,
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
                    )
                    ProgressSupportBlock(
                        content =
                            ProgressSupportCardContent(
                                labelRes = R.string.progress_support_upcoming_event,
                                title = upcomingEvent.title,
                                subtitle = daysUntilText(upcomingEvent.daysUntil),
                            ),
                        modifier = Modifier.weight(1f),
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
            )
        }
    }
}

@Composable
private fun ProgressSupportBlock(
    content: ProgressSupportCardContent,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = ProgressSupportCardMinHeight)
                .clip(shapes.small)
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
private fun ProgressRecentActivity(items: List<com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi>) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        Text(
            text = stringResource(R.string.progress_recent_activity_caption),
            style = typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
        )

        items.take(RECENT_ACTIVITY_TIMELINE_LIMIT).forEachIndexed { index, item ->
            if (index > 0) {
                HorizontalDivider(color = colorScheme.outlineVariant)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = item.time,
                    style = typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier =
                        Modifier
                            .size(ProgressActivityDotSize)
                            .clip(CircleShape)
                            .background(colorScheme.primary),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(SpacingXs),
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

@Composable
private fun ProgressWeekBarUi.chartLabel(
    index: Int,
    currentWeekIndex: Int,
    formatter: DateTimeFormatter,
): String {
    return when (index) {
        currentWeekIndex -> stringResource(R.string.progress_weekly_chart_current_label)
        currentWeekIndex - 1 -> stringResource(R.string.progress_weekly_chart_previous_label)
        else -> weekStartDate.format(formatter)
    }
}

@Composable
private fun daysUntilText(daysUntil: Int): String {
    return relativeDaysUntilText(
        daysUntil = daysUntil,
        todayLabel = stringResource(R.string.activity_today),
        tomorrowLabel = stringResource(R.string.activity_tomorrow),
        yesterdayLabel = stringResource(R.string.activity_yesterday),
        fallbackText = stringResource(R.string.progress_days_until, daysUntil),
    )
}

private fun ProgressCategoryShareUi.countShareText(): String {
    return buildString {
        append(count)
        append(COUNT_SHARE_SEPARATOR)
        append(sharePercent)
        append(PERCENT_SUFFIX)
    }
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

private data class ProgressSupportCardContent(
    @StringRes val labelRes: Int,
    val title: String,
    val subtitle: String? = null,
    val detail: String? = null,
)

private const val WEEK_LABEL_FORMAT_PATTERN = "MMM d"
private const val AXIS_LABEL_EMPTY = "0%"
private const val COUNT_SHARE_SEPARATOR = " / "
private const val PERCENT_SUFFIX = "%"
private const val PERCENT_MAX = 100
private const val PERCENT_MIN_FRACTION = 0f
private const val PERCENT_MAX_FRACTION = 1f
private const val RECENT_ACTIVITY_TIMELINE_LIMIT = 5
