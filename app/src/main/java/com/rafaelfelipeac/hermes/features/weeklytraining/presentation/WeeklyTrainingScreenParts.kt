package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.BuildConfig
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.AddActionPillHorizontalPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.AddActionPillMinWidth
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.AddMenuBottomPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import java.time.LocalDate

private const val ADD_MENU_SCRIM_ALPHA = 0.30f
private const val WEEKLY_FILTER_ALL_CHIP_KEY = "weekly-filter-all-chip"

@Composable
internal fun WeeklyTrainingAddMenu(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onAddWorkout: () -> Unit,
    onAddRaceEvent: () -> Unit,
    onAddRest: () -> Unit,
    onAddBusy: () -> Unit,
    onAddSick: () -> Unit,
    onCopyLastWeek: () -> Unit,
    onAddMockWorkout: (() -> Unit)? = null,
) {
    if (!isVisible) return

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        colorScheme.scrim.copy(alpha = ADD_MENU_SCRIM_ALPHA),
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) {
                        onDismiss()
                    },
        )

        Column(
            modifier =
                Modifier
                    .width(IntrinsicSize.Max)
                    .align(Alignment.BottomEnd)
                    .padding(end = SpacingXl, bottom = AddMenuBottomPadding),
            verticalArrangement = Arrangement.spacedBy(SpacingLg),
            horizontalAlignment = Alignment.End,
        ) {
            AddActionPill(
                icon = Icons.Default.Add,
                label = stringResource(R.string.add_workout),
                onClick = onAddWorkout,
            )

            AddActionPill(
                icon = Icons.Outlined.Flag,
                label = stringResource(R.string.weekly_training_add_race_event),
                onClick = onAddRaceEvent,
            )

            AddActionPill(
                icon = Icons.Outlined.Bedtime,
                label = stringResource(R.string.weekly_training_add_rest_day),
                onClick = onAddRest,
            )

            AddActionPill(
                icon = Icons.Outlined.EventBusy,
                label = stringResource(R.string.weekly_training_add_busy),
                onClick = onAddBusy,
            )

            AddActionPill(
                icon = Icons.Outlined.MedicalServices,
                label = stringResource(R.string.weekly_training_add_sick),
                onClick = onAddSick,
            )

            AddActionPill(
                icon = Icons.Default.History,
                label = stringResource(R.string.weekly_training_copy_last_week),
                onClick = onCopyLastWeek,
            )

            if (BuildConfig.DEBUG) {
                AddActionPill(
                    icon = Icons.Default.Settings,
                    label = stringResource(R.string.weekly_training_add_mock_workout),
                    onClick = onAddMockWorkout ?: {},
                )
            }
        }
    }
}

@Composable
internal fun WeeklyPlannerCategoryFilters(
    categories: List<CategoryUi>,
    focusedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val clearFiltersLabel = stringResource(R.string.filters_clear)
    val selectedIndex =
        if (focusedCategoryId == null) {
            0
        } else {
            categories.indexOfFirst { it.id == focusedCategoryId } + 1
        }

    LaunchedEffect(selectedIndex, categories, focusedCategoryId) {
        centerWeeklySelectedChip(listState, selectedIndex)
    }

    Box(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            item(key = WEEKLY_FILTER_ALL_CHIP_KEY) {
                FilterChip(
                    selected = focusedCategoryId == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text(text = stringResource(R.string.activity_filter_all)) },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorScheme.primaryContainer,
                            selectedLabelColor = colorScheme.onPrimaryContainer,
                        ),
                )
            }

            itemsIndexed(categories, key = { _, item -> item.id }) { _, category ->
                FilterChip(
                    selected = focusedCategoryId == category.id,
                    onClick = { onCategorySelected(category.id) },
                    label = { Text(text = category.name) },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorScheme.secondaryContainer,
                            selectedLabelColor = colorScheme.onSecondaryContainer,
                        ),
                )
            }
        }

        if (focusedCategoryId != null) {
            FilterChip(
                selected = false,
                onClick = onClearFilters,
                modifier = Modifier.align(Alignment.CenterEnd),
                label = {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = clearFiltersLabel,
                    )
                },
            )
        }
    }
}

internal fun WorkoutUi.workoutDateOrNull(): LocalDate? {
    val day = dayOfWeek ?: return null
    return weekStartDate.plusDays((day.value - 1).toLong())
}

private suspend fun centerWeeklySelectedChip(
    listState: LazyListState,
    selectedIndex: Int,
) {
    if (selectedIndex < 0) return

    var itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == selectedIndex }

    if (itemInfo == null) {
        listState.animateScrollToItem(selectedIndex)
        itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == selectedIndex } ?: return
    }

    val viewportCenter =
        (listState.layoutInfo.viewportStartOffset + listState.layoutInfo.viewportEndOffset) / 2
    val itemCenter = itemInfo.offset + itemInfo.size / 2
    val delta = (itemCenter - viewportCenter).toFloat()

    if (delta != 0f) {
        listState.animateScrollBy(delta)
    }
}

@Composable
private fun AddActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = shapes.extraLarge,
        tonalElevation = ElevationMd,
        shadowElevation = ElevationMd,
        modifier =
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minWidth = AddActionPillMinWidth),
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = AddActionPillHorizontalPadding,
                    vertical = SpacingLg,
                ),
            horizontalArrangement = Arrangement.spacedBy(SpacingLg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(text = label, style = typography.titleSmall)
        }
    }
}
