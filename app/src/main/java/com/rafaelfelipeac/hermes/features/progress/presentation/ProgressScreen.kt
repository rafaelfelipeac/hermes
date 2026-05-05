package com.rafaelfelipeac.hermes.features.progress.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressCategoryColorDotSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressScreenBottomPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressSummaryCardMinHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressTrendBarHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressTrendBarMinWidth
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
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

        if (state.emptyReason != null) {
            item {
                Text(
                    text = stringResource(R.string.progress_empty),
                    style = typography.bodyLarge,
                    color = colorScheme.onSurfaceVariant,
                )
            }
            return@LazyColumn
        }

        item {
            ProgressSummaryCards(cards = state.summaryCards)
        }

        item {
            ProgressSection(
                title = stringResource(R.string.progress_section_this_week),
            ) {
                ProgressThisWeek(snapshot = state.thisWeek)
            }
        }

        item {
            ProgressSection(
                title = stringResource(R.string.progress_section_weekly_trend),
            ) {
                ProgressWeeklyTrend(weeks = state.weeklyTrend, locale = locale)
            }
        }

        if (state.categoryDistribution.isNotEmpty()) {
            item {
                ProgressSection(
                    title = stringResource(R.string.progress_section_categories),
                ) {
                    ProgressCategoryDistribution(items = state.categoryDistribution)
                }
            }
        }

        state.trophyHighlight?.let { highlight ->
            item {
                ProgressSection(
                    title = stringResource(R.string.progress_section_trophy),
                ) {
                    ProgressTrophyHighlight(highlight = highlight)
                }
            }
        }

        state.upcomingEvent?.let { event ->
            item {
                ProgressSection(
                    title = stringResource(R.string.progress_section_upcoming),
                ) {
                    ProgressUpcomingEvent(event = event)
                }
            }
        }

        if (state.recentActivities.isNotEmpty()) {
            item {
                ProgressSection(
                    title = stringResource(R.string.progress_section_recent_activity),
                    trailingContent = {
                        TextButton(onClick = onOpenActivity) {
                            Text(stringResource(R.string.progress_view_all_activity))
                        }
                    },
                ) {
                    ProgressRecentActivity(items = state.recentActivities)
                }
            }
        }
    }
}

@Composable
private fun ProgressSummaryCards(cards: List<ProgressSummaryCardUi>) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(
            text = stringResource(R.string.progress_section_at_a_glance),
            style = typography.titleMedium,
            color = colorScheme.onSurface,
        )

        cards.chunked(2).forEach { rowCards ->
            Row(horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
                rowCards.forEach { card ->
                    Card(
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(ProgressSummaryCardMinHeight),
                        shape = shapes.medium,
                    ) {
                        Column(
                            modifier = Modifier.padding(SpacingLg),
                            verticalArrangement = Arrangement.spacedBy(SpacingSm),
                        ) {
                            Text(
                                text = stringResource(cardTitleRes(card.kind)),
                                style = typography.labelLarge,
                                color = colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = card.value,
                                style = typography.headlineSmall,
                                color = colorScheme.onSurface,
                            )
                            val supportingText = summaryCardSupportingText(card)
                            if (supportingText != null) {
                                Text(
                                    text = supportingText,
                                    style = typography.bodySmall,
                                    color = colorScheme.primary,
                                )
                            }
                        }
                    }
                }

                if (rowCards.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
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
private fun ProgressThisWeek(snapshot: ProgressWeekSnapshotUi) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(
            text = stringResource(
                R.string.progress_completed_of_planned,
                snapshot.completedWorkouts,
                snapshot.plannedWorkouts,
            ),
            style = typography.bodyLarge,
            color = colorScheme.onSurface,
        )
        Text(
            text = snapshot.completionPercent.toString() + "%",
            style = typography.headlineSmall,
            color = colorScheme.primary,
        )
        Text(
            text =
                listOfNotNull(
                    snapshot.plannedRestEvents.takeIf { it > 0 }?.let {
                        pluralStringResource(R.plurals.weekly_training_summary_item_rest, it, it)
                    },
                    snapshot.plannedBusyEvents.takeIf { it > 0 }?.let {
                        pluralStringResource(R.plurals.weekly_training_summary_item_busy, it, it)
                    },
                    snapshot.plannedSickEvents.takeIf { it > 0 }?.let {
                        pluralStringResource(R.plurals.weekly_training_summary_item_sick, it, it)
                    },
                ).joinToString(stringResource(R.string.weekly_training_summary_separator)),
            style = typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProgressWeeklyTrend(
    weeks: List<ProgressWeekBarUi>,
    locale: Locale,
) {
    val formatter = DateTimeFormatter.ofPattern("EEE", locale)
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingSm),
            verticalAlignment = Alignment.Bottom,
        ) {
            weeks.forEach { week ->
                val fill = if (week.plannedWorkouts == 0) 0.1f else (week.completionPercent / 100f).coerceIn(0.1f, 1f)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(SpacingSm),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(ProgressTrendBarHeight)
                                .clip(RoundedCornerShape(SpacingSm))
                                .background(colorScheme.surfaceVariant),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(fill)
                                    .background(
                                        if (week.isCurrentWeek) {
                                            colorScheme.primary
                                        } else {
                                            colorScheme.secondary
                                        },
                                    ),
                        )
                    }
                    Text(
                        text = week.weekStartDate.format(formatter),
                        style = typography.labelSmall,
                        color = if (week.isCurrentWeek) colorScheme.primary else colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Text(
            text = stringResource(R.string.progress_last_8_weeks),
            style = typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProgressCategoryDistribution(items: List<ProgressCategoryShareUi>) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
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
                    text = "${item.count} • ${item.sharePercent}%",
                    style = typography.labelMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ProgressTrophyHighlight(highlight: com.rafaelfelipeac.hermes.features.trophies.presentation.FeaturedTrophyUi) {
    val trophy = highlight.trophy
    Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        Text(
            text =
                if (highlight.mode == com.rafaelfelipeac.hermes.features.trophies.presentation.FeaturedTrophyMode.RECENT_UNLOCK) {
                    stringResource(R.string.progress_trophy_recent_unlock)
                } else {
                    stringResource(R.string.progress_trophy_nearest)
                },
            style = typography.labelLarge,
            color = colorScheme.primary,
        )
        Text(
            text = stringResource(trophyNameRes(trophy.trophyId)),
            style = typography.titleMedium,
            color = colorScheme.onSurface,
        )
        Text(
            text = "${trophy.currentValue}/${trophy.target}",
            style = typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProgressUpcomingEvent(event: ProgressUpcomingEventUi) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        Text(
            text = event.title,
            style = typography.titleMedium,
            color = colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.progress_days_until, event.daysUntil),
            style = typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProgressRecentActivity(items: List<com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi>) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        items.forEach { item ->
            Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = item.title,
                        style = typography.bodyLarge,
                        color = colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = item.time,
                        style = typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
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

@Composable
private fun summaryCardSupportingText(card: ProgressSummaryCardUi): String? {
    return when (card.kind) {
        ProgressSummaryCardKind.THIS_WEEK -> card.supportingText
        ProgressSummaryCardKind.CONSISTENCY -> stringResource(R.string.progress_last_8_weeks)
        ProgressSummaryCardKind.TOP_CATEGORY -> card.supportingText
        ProgressSummaryCardKind.UPCOMING -> card.supportingText
    }
}

private fun cardTitleRes(kind: ProgressSummaryCardKind): Int {
    return when (kind) {
        ProgressSummaryCardKind.THIS_WEEK -> R.string.progress_card_this_week
        ProgressSummaryCardKind.CONSISTENCY -> R.string.progress_card_consistency
        ProgressSummaryCardKind.TOP_CATEGORY -> R.string.progress_card_top_category
        ProgressSummaryCardKind.UPCOMING -> R.string.progress_card_upcoming_days
    }
}
