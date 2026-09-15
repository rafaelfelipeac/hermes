package com.rafaelfelipeac.hermes.features.challenges.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.EmptyStateCard
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.FloatingActionContentBottomPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeUiState

@Composable
internal fun ChallengesListRoute(
    modifier: Modifier,
    selectedTab: ChallengeListTab,
    pagerState: PagerState,
    state: ChallengeUiState,
    onSelectedTabChange: (ChallengeListTab) -> Unit,
    onChallengeClick: (Challenge) -> Unit,
) {
    val activeChallenges =
        remember(state.activeChallenges) {
            state.activeChallenges.sortedByDescending { it.updatedAt }
        }
    val archivedChallenges =
        remember(state.archivedChallenges) {
            state.archivedChallenges.sortedByDescending { it.updatedAt }
        }
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
                Tab(
                    selected = selectedTab == ChallengeListTab.ACTIVE,
                    onClick = { onSelectedTabChange(ChallengeListTab.ACTIVE) },
                    selectedContentColor = colorScheme.primary,
                    unselectedContentColor = colorScheme.onSurfaceVariant,
                    text = { Text(text = stringResource(R.string.challenges_active_title)) },
                )
                Tab(
                    selected = selectedTab == ChallengeListTab.ARCHIVED,
                    onClick = { onSelectedTabChange(ChallengeListTab.ARCHIVED) },
                    selectedContentColor = colorScheme.primary,
                    unselectedContentColor = colorScheme.onSurfaceVariant,
                    text = { Text(text = stringResource(R.string.challenges_archived_title)) },
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) { page ->
                when (ChallengeListTab.entries[page]) {
                    ChallengeListTab.ACTIVE -> {
                        if (activeChallenges.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().testTag(CHALLENGES_TAG_ACTIVE_LIST),
                                contentPadding =
                                    PaddingValues(
                                        start = SpacingXl,
                                        top = SpacingMd,
                                        end = SpacingXl,
                                        bottom = FloatingActionContentBottomPadding,
                                    ),
                                verticalArrangement = Arrangement.spacedBy(SpacingMd),
                            ) {
                                items(activeChallenges, key = { it.id }) { challenge ->
                                    ChallengeCard(
                                        challenge = challenge,
                                        category = state.categories.firstOrNull { it.id == challenge.categoryId },
                                        calculation = state.challengeCalculations[challenge.id],
                                        onClick = { onChallengeClick(challenge) },
                                    )
                                }
                            }
                        }
                    }

                    ChallengeListTab.ARCHIVED -> {
                        if (archivedChallenges.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().testTag(CHALLENGES_TAG_ARCHIVED_LIST),
                                contentPadding =
                                    PaddingValues(
                                        start = SpacingXl,
                                        top = SpacingMd,
                                        end = SpacingXl,
                                        bottom = FloatingActionContentBottomPadding,
                                    ),
                                verticalArrangement = Arrangement.spacedBy(SpacingMd),
                            ) {
                                items(archivedChallenges, key = { it.id }) { challenge ->
                                    ChallengeCard(
                                        challenge = challenge,
                                        category = state.categories.firstOrNull { it.id == challenge.categoryId },
                                        calculation = state.challengeCalculations[challenge.id],
                                        onClick = { onChallengeClick(challenge) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedTab == ChallengeListTab.ACTIVE && activeChallenges.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Outlined.TrackChanges,
                title = stringResource(R.string.challenges_empty_active_title),
                body = stringResource(R.string.challenges_empty_active_body),
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = SpacingXl)
                        .testTag(CHALLENGES_TAG_ACTIVE_EMPTY_STATE),
                actionContent = null,
            )
        }

        if (selectedTab == ChallengeListTab.ARCHIVED && archivedChallenges.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Outlined.Archive,
                title = stringResource(R.string.challenges_empty_archived_title),
                body = stringResource(R.string.challenges_empty_archived_body),
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = SpacingXl)
                        .testTag(CHALLENGES_TAG_ARCHIVED_EMPTY_STATE),
            )
        }
    }
}
