package com.rafaelfelipeac.hermes.features.trophies.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs

private const val TROPHY_PREVIEW_TEXT_ALPHA = 0.72f

@Composable
internal fun TrophiesContent(
    state: TrophyPageState,
    selectedFamilyName: String?,
    selectedTrophyId: String?,
    onFamilySelected: (TrophyFamilyUi) -> Unit,
    onBackFromFamily: () -> Unit,
    onTrophySelected: (TrophyCardUi) -> Unit,
    overviewFirstVisibleItemIndex: Int,
    overviewFirstVisibleItemScrollOffset: Int,
    onOverviewScrollChanged: (Int, Int) -> Unit,
    familyFirstVisibleItemIndex: MutableMap<String, Int>,
    familyFirstVisibleItemScrollOffset: MutableMap<String, Int>,
    modifier: Modifier = Modifier,
) {
    if (state.families.isEmpty() || state.families.all { it.sections.isEmpty() }) {
        TrophyEmptyState(modifier = modifier)
        return
    }

    val selectedFamily = state.families.firstOrNull { it.family.name == selectedFamilyName }

    androidx.activity.compose.BackHandler(enabled = selectedFamily != null) {
        onBackFromFamily()
    }

    if (selectedFamily == null) {
        TrophyOverviewContent(
            families = state.families,
            onOpenFamily = onFamilySelected,
            onTrophySelected = onTrophySelected,
            firstVisibleItemIndex = overviewFirstVisibleItemIndex,
            firstVisibleItemScrollOffset = overviewFirstVisibleItemScrollOffset,
            onScrollChanged = onOverviewScrollChanged,
            modifier = modifier,
        )
    } else {
        TrophyFamilyDetailContent(
            familySection = selectedFamily,
            onTrophySelected = onTrophySelected,
            requestedTrophyStableId = selectedTrophyId,
            firstVisibleItemIndex = familyFirstVisibleItemIndex[selectedFamily.family.name] ?: 0,
            firstVisibleItemScrollOffset = familyFirstVisibleItemScrollOffset[selectedFamily.family.name] ?: 0,
            onScrollChanged = { index, offset ->
                familyFirstVisibleItemIndex[selectedFamily.family.name] = index
                familyFirstVisibleItemScrollOffset[selectedFamily.family.name] = offset
            },
            modifier = modifier,
        )
    }
}

@Composable
internal fun TrophiesHeader(
    familySection: TrophyFamilySectionUi?,
    onBack: () -> Unit,
    onBrowseBack: (() -> Unit)?,
    onOpenActivities: () -> Unit,
) {
    if (familySection == null) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = SpacingSm,
                        end = SpacingXl,
                        top = SpacingSm,
                        bottom = SpacingSm,
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBrowseBack != null) {
                IconButton(onClick = onBrowseBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.trophies_back),
                    )
                }
            }

            Text(
                text = stringResource(R.string.trophies_title),
                style = typography.titleLarge,
                color = colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )

            ActivitiesButton(onClick = onOpenActivities)
        }
    } else {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = SpacingSm,
                        end = SpacingXl,
                        top = SpacingSm,
                        bottom = SpacingSm,
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.trophies_back),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SpacingXs),
            ) {
                Text(
                    text = stringResource(familyTitleRes(familySection.family)),
                    style = typography.titleLarge,
                    color = colorScheme.onSurface,
                )
                Text(
                    text =
                        stringResource(
                            R.string.trophies_unlocked_count,
                            familySection.unlockedCount,
                            familySection.totalCount,
                        ),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TrophyEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .testTag(TROPHIES_EMPTY_STATE_TAG),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        Text(
            text = stringResource(R.string.trophies_empty_title),
            style = typography.titleMedium,
            color = colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.trophies_empty_body),
            style = typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )
        TrophyPreviewCard(
            title = stringResource(R.string.trophies_name_full_time),
            description =
                stringResource(
                    R.string.trophies_desc_complete_weeks_locked,
                    TROPHIES_SAMPLE_FULL_TIME_TARGET,
                ),
        )
        TrophyPreviewCard(
            title = stringResource(R.string.trophies_name_comeback_week),
            description = stringResource(R.string.trophies_desc_comeback_weeks_locked, TROPHIES_SAMPLE_COMEBACK_TARGET),
        )
        TrophyPreviewCard(
            title = stringResource(R.string.trophies_name_podium_place),
            description =
                stringResource(
                    R.string.trophies_desc_category_completions_locked,
                    TROPHIES_SAMPLE_PODIUM_TARGET,
                ),
        )
    }
}

@Composable
private fun TrophyPreviewCard(
    title: String,
    description: String,
) {
    androidx.compose.material3.Card(
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(SpacingMd),
            verticalArrangement = Arrangement.spacedBy(SpacingXs),
        ) {
            Text(
                text = title,
                style = typography.titleMedium,
                color = colorScheme.onSurface.copy(alpha = TROPHY_PREVIEW_TEXT_ALPHA),
            )
            Text(
                text = description,
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = TROPHY_PREVIEW_TEXT_ALPHA),
            )
        }
    }
}

@Composable
private fun ActivitiesButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.primary),
        modifier = modifier,
    ) {
        Text(text = stringResource(R.string.trophies_activities_action))
    }
}

@Composable
internal fun rememberPreservedLazyListState(
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
    onScrollChanged: (Int, Int) -> Unit,
): LazyListState {
    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex = firstVisibleItemIndex,
            initialFirstVisibleItemScrollOffset = firstVisibleItemScrollOffset,
        )

    LaunchedEffect(listState, onScrollChanged) {
        androidx.compose.runtime.snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }
            .collect { (index, offset) ->
                onScrollChanged(index, offset)
            }
    }

    return listState
}
