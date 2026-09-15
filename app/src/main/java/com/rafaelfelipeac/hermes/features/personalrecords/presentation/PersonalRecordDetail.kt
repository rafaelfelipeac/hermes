@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)
@file:Suppress("TooManyFunctions")

package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.EmptyStateCard
import com.rafaelfelipeac.hermes.core.ui.components.TitleChip
import com.rafaelfelipeac.hermes.core.ui.components.formatWorkoutDate
import com.rafaelfelipeac.hermes.core.ui.currentLocale
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderHairline
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.personalrecords.domain.PersonalRecordBestSelector
import com.rafaelfelipeac.hermes.features.personalrecords.domain.PersonalRecordHistoryOrderer
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.MANUAL
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import java.util.Locale

@Composable
internal fun PersonalRecordDetail(
    state: PersonalRecordsState,
    family: PersonalRecordFamily,
    currentLocale: Locale,
    onEditEntry: (PersonalRecordEntry) -> Unit,
    onSetManualCurrentEntry: (familyId: Long, entryId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val familyEntries = remember(state.entries, family.id) { state.entries.filter { it.familyId == family.id } }
    val currentBest = PersonalRecordBestSelector.selectBest(family, familyEntries)
    val orderedHistory = PersonalRecordHistoryOrderer.order(family, familyEntries)
    val category = state.categories.firstOrNull { it.id == family.categoryId }
    val currentBestLabel =
        currentBest?.let {
            formatPersonalRecordValue(
                value = it.value,
                unit = it.unit,
                locale = currentLocale,
                unitLabel = unitLabelFor(it.unit, it.customUnitLabel, it.value),
            )
        }
    val currentBestDateLabel =
        currentBest?.let { formatWorkoutDate(it.recordDate, currentLocale) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(SpacingXs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PersonalRecordCategoryChip(category = category)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(SpacingXs),
                verticalArrangement = Arrangement.spacedBy(SpacingXs),
            ) {
                TitleChip(
                    label = metricLabel(family.metricType),
                    containerColor = colorScheme.surfaceVariant,
                    contentColor = colorScheme.onSurfaceVariant,
                )
                TitleChip(
                    label = comparisonLabel(family.comparisonRule),
                    containerColor = colorScheme.surfaceVariant,
                    contentColor = colorScheme.onSurfaceVariant,
                )
            }
        }

        if (currentBest == null) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                val emptyStateColors = personalRecordRelatedColors(category)
                EmptyStateCard(
                    icon = Icons.Outlined.Add,
                    title = stringResource(R.string.personal_records_add_first_result_title),
                    body = stringResource(R.string.personal_records_add_first_result_body),
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = colorScheme.surfaceContainerHigh,
                    iconContainerColor = emptyStateColors.container,
                    iconContentColor = emptyStateColors.content,
                )
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
                border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
                shape = shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(SpacingLg),
                    verticalArrangement = Arrangement.spacedBy(SpacingSm),
                ) {
                    Text(text = stringResource(R.string.personal_records_current_best), style = typography.titleMedium)
                    Text(text = currentBestLabel.orEmpty(), style = typography.headlineMedium)
                    if (currentBestDateLabel != null) {
                        Text(
                            text = currentBestDateLabel,
                            style = typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
                Text(text = stringResource(R.string.personal_records_history_title), style = typography.titleMedium)

                if (orderedHistory.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        EmptyStateCard(
                            icon = Icons.Outlined.History,
                            title = stringResource(R.string.personal_records_history_empty_title),
                            body = stringResource(R.string.personal_records_history_empty_body),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    orderedHistory.forEach { entry ->
                        PersonalRecordHistoryRow(
                            entry = entry,
                            currentLocale = currentLocale,
                            onClick = { onEditEntry(entry) },
                            isManualSelection = family.comparisonRule == MANUAL,
                            isCurrent = currentBest.id == entry.id,
                            category = category,
                            onSetCurrent = { onSetManualCurrentEntry(family.id, entry.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonalRecordHistoryRow(
    entry: PersonalRecordEntry,
    currentLocale: Locale,
    onClick: () -> Unit,
    isManualSelection: Boolean,
    isCurrent: Boolean,
    category: Category?,
    onSetCurrent: () -> Unit,
) {
    val currentFlagColors = personalRecordRelatedColors(category)

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
        shape = shapes.medium,
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag(PERSONAL_RECORDS_ENTRY_CARD_TAG_PREFIX + entry.id),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(SpacingLg),
            horizontalArrangement = Arrangement.spacedBy(SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SpacingXs),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SpacingXs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text =
                            formatPersonalRecordValue(
                                value = entry.value,
                                unit = entry.unit,
                                locale = currentLocale,
                                unitLabel = unitLabelFor(entry.unit, entry.customUnitLabel, entry.value),
                            ),
                        style = typography.titleMedium,
                    )
                    if (isCurrent) {
                        TitleChip(
                            label = stringResource(R.string.personal_records_current_selected),
                            containerColor = currentFlagColors.container,
                            contentColor = currentFlagColors.content,
                        )
                    }
                }
                Text(
                    text = formatWorkoutDate(entry.recordDate, currentLocale),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
                if (!entry.note.isNullOrBlank()) {
                    Text(
                        text = entry.note.orEmpty(),
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (isManualSelection) {
                val selectionDescription =
                    stringResource(
                        if (isCurrent) {
                            R.string.personal_records_current_selected
                        } else {
                            R.string.personal_records_set_current
                        },
                    )
                RadioButton(
                    selected = isCurrent,
                    onClick = {
                        if (!isCurrent) onSetCurrent()
                    },
                    modifier =
                        Modifier
                            .testTag(PERSONAL_RECORDS_SET_CURRENT_TAG_PREFIX + entry.id)
                            .semantics { contentDescription = selectionDescription },
                )
            }
        }
    }
}
