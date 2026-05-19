package com.rafaelfelipeac.hermes.features.activity.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.relativeDateText
import com.rafaelfelipeac.hermes.core.ui.components.EmptyStateCard
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityFiltersUi
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityPrimaryFilter
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivitySectionUi
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale

@Composable
fun ActivityScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    requestedActivityId: Long? = null,
    onRequestedActivityConsumed: () -> Unit = {},
    viewModel: ActivityViewModel = hiltViewModel(),
) {
    val configuration = LocalConfiguration.current
    val currentLocale = configuration.locales.get(0) ?: Locale.getDefault()

    BackHandler(onBack = onBack)

    LaunchedEffect(currentLocale) {
        viewModel.updateLocale(currentLocale)
    }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(requestedActivityId) {
        if (requestedActivityId != null) {
            onRequestedActivityConsumed()
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        ActivityHeader(onBack = onBack)

        ActivityContent(
            sections = state.sections,
            currentLocale = currentLocale,
            requestedActivityId = requestedActivityId,
            filters = state.filters,
            emptyTitle =
                stringResource(
                    if (state.filters.isAnyFilterActive) {
                        R.string.activity_filtered_empty_title
                    } else {
                        R.string.activity_empty_title
                    },
                ),
            emptyMessage =
                stringResource(
                    if (state.filters.isAnyFilterActive) {
                        R.string.activity_filtered_empty
                    } else {
                        R.string.activity_empty
                    },
                ),
            onPrimaryFilterSelected = viewModel::selectPrimaryFilter,
            onCategorySelected = viewModel::selectCategoryFilter,
            onWeekSelected = viewModel::selectWeekFilter,
            onClearFilters = viewModel::clearFilters,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = SpacingXl),
        )
    }
}

@Composable
internal fun ActivityHeader(onBack: () -> Unit) {
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

        Text(
            text = stringResource(R.string.activity_title),
            style = typography.titleLarge,
            color = colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun ActivityContent(
    sections: List<ActivitySectionUi>,
    currentLocale: Locale,
    requestedActivityId: Long? = null,
    filters: ActivityFiltersUi = ActivityFiltersUi(),
    emptyTitle: String = stringResource(R.string.activity_empty_title),
    emptyMessage: String = stringResource(R.string.activity_empty),
    onPrimaryFilterSelected: (ActivityPrimaryFilter) -> Unit = {},
    onCategorySelected: (Long) -> Unit = {},
    onWeekSelected: (LocalDate) -> Unit = {},
    onClearFilters: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val dayPattern = stringResource(R.string.activity_week_date_pattern)
    val dayFormatter =
        DateTimeFormatterBuilder()
            .appendPattern(dayPattern)
            .toFormatter(currentLocale)
    val today = LocalDate.now(ZoneId.systemDefault())
    val todayLabel = stringResource(R.string.activity_today)
    val tomorrowLabel = stringResource(R.string.activity_tomorrow)
    val yesterdayLabel = stringResource(R.string.activity_yesterday)

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        ActivityFilterBar(
            filters = filters,
            onPrimaryFilterSelected = onPrimaryFilterSelected,
            onCategorySelected = onCategorySelected,
            onWeekSelected = onWeekSelected,
            onClearFilters = onClearFilters,
        )

        if (sections.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                EmptyStateCard(
                    icon = Icons.Outlined.History,
                    title = emptyTitle,
                    body = emptyMessage,
                )
            }

            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    top = SpacingMd,
                    bottom = SpacingXl,
                ),
            verticalArrangement = Arrangement.spacedBy(SpacingLg),
        ) {
            sections.forEach { section ->
                val header =
                    relativeDateText(
                        date = section.date,
                        today = today,
                        todayLabel = todayLabel,
                        tomorrowLabel = tomorrowLabel,
                        yesterdayLabel = yesterdayLabel,
                        formatter = dayFormatter,
                    )

                item(key = HEADER_KEY_PREFIX + section.date) {
                    Text(
                        text = header,
                        style = typography.titleMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                }

                items(section.items, key = { it.id }) { item ->
                    ActivityRow(
                        item = item,
                        focusRequested = item.id == requestedActivityId,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityFilterBar(
    filters: ActivityFiltersUi,
    onPrimaryFilterSelected: (ActivityPrimaryFilter) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onWeekSelected: (LocalDate) -> Unit,
    onClearFilters: () -> Unit,
) {
    if (filters.primaryFilters.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        PrimaryFilterRow(
            filters = filters,
            onPrimaryFilterSelected = onPrimaryFilterSelected,
            onClearFilters = onClearFilters,
        )

        when (filters.selectedPrimaryFilter) {
            ActivityPrimaryFilter.CATEGORY ->
                SecondaryFilterRow(
                    labels =
                        filters.categoryFilters.map {
                            SecondaryFilterItem(
                                key = it.id.toString(),
                                label = it.name,
                                isSelected = it.isSelected,
                            )
                        },
                    onSelected = { key -> key.toLongOrNull()?.let(onCategorySelected) },
                )

            ActivityPrimaryFilter.WEEK ->
                SecondaryFilterRow(
                    labels =
                        filters.weekFilters.map {
                            SecondaryFilterItem(
                                key = it.weekStartDate.toString(),
                                label = it.label,
                                isSelected = it.isSelected,
                            )
                        },
                    onSelected = { label ->
                        filters.weekFilters
                            .firstOrNull { it.weekStartDate.toString() == label }
                            ?.weekStartDate
                            ?.let(onWeekSelected)
                    },
                )

            else -> Unit
        }
    }
}

@Composable
private fun SecondaryFilterRow(
    labels: List<SecondaryFilterItem>,
    onSelected: (String) -> Unit,
) {
    if (labels.isEmpty()) return

    val listState = rememberLazyListState()
    val selectedIndex = labels.indexOfFirst { it.isSelected }

    LaunchedEffect(selectedIndex, labels) {
        centerSelectedChip(listState, selectedIndex)
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        items(labels, key = { it.key }) { item ->
            FilterChip(
                selected = item.isSelected,
                onClick = { onSelected(item.key) },
                label = { Text(text = item.label) },
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colorScheme.secondaryContainer,
                        selectedLabelColor = colorScheme.onSecondaryContainer,
                    ),
            )
        }
    }
}

private data class SecondaryFilterItem(
    val key: String,
    val label: String,
    val isSelected: Boolean,
)

@Composable
private fun PrimaryFilterRow(
    filters: ActivityFiltersUi,
    onPrimaryFilterSelected: (ActivityPrimaryFilter) -> Unit,
    onClearFilters: () -> Unit,
) {
    val listState = rememberLazyListState()
    val selectedIndex = filters.primaryFilters.indexOfFirst { it.filter == filters.selectedPrimaryFilter }
    val clearFiltersLabel = stringResource(R.string.filters_clear)

    LaunchedEffect(selectedIndex, filters.primaryFilters, filters.isAnyFilterActive) {
        centerSelectedChip(listState, selectedIndex)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            itemsIndexed(filters.primaryFilters, key = { _, item -> item.filter.name }) { _, filter ->
                FilterChip(
                    selected = filters.selectedPrimaryFilter == filter.filter,
                    onClick = { onPrimaryFilterSelected(filter.filter) },
                    label = { Text(text = filter.label) },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorScheme.primaryContainer,
                            selectedLabelColor = colorScheme.onPrimaryContainer,
                        ),
                )
            }
        }

        if (filters.isAnyFilterActive) {
            FilterChip(
                selected = false,
                onClick = onClearFilters,
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

private suspend fun centerSelectedChip(
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
private fun ActivityRow(
    item: ActivityItemUi,
    focusRequested: Boolean,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(focusRequested) {
        if (focusRequested) {
            bringIntoViewRequester.bringIntoView()
        }
    }

    Surface(
        tonalElevation = ElevationSm,
        shape = shapes.medium,
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (focusRequested) {
                        Modifier.bringIntoViewRequester(bringIntoViewRequester)
                    } else {
                        Modifier
                    },
                ),
    ) {
        Column(
            modifier =
                Modifier.padding(
                    horizontal = SpacingXl,
                    vertical = SpacingLg,
                ),
        ) {
            Row {
                Text(
                    text = item.title,
                    style = typography.bodyLarge,
                    color = colorScheme.onSurface,
                    maxLines = TITLE_MAX_LINES,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(SpacingLg))

                Text(
                    text = item.time,
                    style = typography.labelMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            }
            item.subtitle?.let { subtitle ->
                HorizontalDivider(modifier = Modifier.padding(vertical = SpacingMd))

                Text(
                    text = subtitle,
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = SUBTITLE_MAX_LINES,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}

private const val HEADER_KEY_PREFIX = "header-"
private const val TITLE_MAX_LINES = 4
private const val SUBTITLE_MAX_LINES = 6
