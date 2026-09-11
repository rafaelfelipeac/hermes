package com.rafaelfelipeac.hermes.features.progress.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.EmptyStateCard
import com.rafaelfelipeac.hermes.core.ui.currentLocale
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ProgressScreenBottomPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi
import com.rafaelfelipeac.hermes.features.trophies.presentation.FeaturedTrophyUi
import java.util.Locale

@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    onOpenActivity: () -> Unit,
    onOpenActivityItem: (ActivityItemUi) -> Unit,
    onOpenWorkout: (ProgressNextFocusUi) -> Unit,
    onOpenEvent: (ProgressUpcomingEventUi) -> Unit,
    onOpenTrophy: (FeaturedTrophyUi) -> Unit,
    viewModel: ProgressViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val locale = currentLocale()

    ProgressContent(
        state = state,
        locale = locale,
        onOpenActivity = onOpenActivity,
        onOpenActivityItem = onOpenActivityItem,
        onOpenWorkout = onOpenWorkout,
        onOpenEvent = onOpenEvent,
        onOpenTrophy = onOpenTrophy,
        modifier = modifier,
    )
}

@Composable
internal fun ProgressContent(
    state: ProgressState,
    locale: Locale,
    onOpenActivity: () -> Unit,
    onOpenActivityItem: (ActivityItemUi) -> Unit,
    onOpenWorkout: (ProgressNextFocusUi) -> Unit,
    onOpenEvent: (ProgressUpcomingEventUi) -> Unit,
    onOpenTrophy: (FeaturedTrophyUi) -> Unit,
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
                    ProgressWeeklyReadout(
                        readout = section.readout,
                        onOpenWorkout = onOpenWorkout,
                    )
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
                            onOpenWorkout = onOpenWorkout,
                            onOpenEvent = onOpenEvent,
                            onOpenTrophy = onOpenTrophy,
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
                        ProgressRecentActivity(
                            items = section.items,
                            onOpenActivityItem = onOpenActivityItem,
                        )
                    }
                }
            }
        }
    }
}
