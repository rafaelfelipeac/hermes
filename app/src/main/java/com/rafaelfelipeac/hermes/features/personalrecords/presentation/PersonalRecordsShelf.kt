@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)
@file:Suppress("TooManyFunctions")

package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.EmptyStateCard
import com.rafaelfelipeac.hermes.core.ui.components.TitleChip
import com.rafaelfelipeac.hermes.core.ui.currentLocale
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderHairline
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SmallIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.personalrecords.domain.PersonalRecordBestSelector
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import java.util.Locale

@Composable
internal fun PersonalRecordsShelf(
    state: PersonalRecordsState,
    currentLocale: Locale,
    onFamilySelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val groups = remember(state.categories, state.families) { buildFamilyGroups(state.categories, state.families) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (state.families.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Outlined.Leaderboard,
                title = stringResource(R.string.personal_records_empty_title),
                body = stringResource(R.string.personal_records_empty_body),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
                groups.forEach { group ->
                    Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
                        Text(
                            text =
                                group.category?.name
                                    ?: stringResource(R.string.category_uncategorized),
                            style = typography.titleMedium,
                        )

                        group.families.forEach { family ->
                            val familyEntries = state.entries.filter { it.familyId == family.id }
                            val currentBest = PersonalRecordBestSelector.selectBest(family, familyEntries)
                            val entryCountText =
                                pluralStringResource(
                                    R.plurals.personal_records_family_entry_count,
                                    familyEntries.size,
                                    familyEntries.size,
                                )
                            val category = group.category
                            val accent = category?.colorId?.let(::categoryAccentColor) ?: colorScheme.primary

                            Card(
                                onClick = { onFamilySelected(family.id) },
                                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
                                border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
                                shape = shapes.medium,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .testTag(PERSONAL_RECORDS_FAMILY_CARD_TAG_PREFIX + family.id),
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxHeight()
                                                .width(SpacingXs)
                                                .background(accent),
                                    )

                                    Column(
                                        modifier = Modifier.weight(1f).padding(SpacingLg),
                                        verticalArrangement = Arrangement.spacedBy(SpacingMd),
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Outlined.Leaderboard,
                                                contentDescription = null,
                                                tint = accent,
                                                modifier = Modifier.size(SmallIconSize),
                                            )

                                            Spacer(modifier = Modifier.width(SpacingMd))

                                            Text(
                                                text = family.title,
                                                style = typography.titleMedium,
                                                modifier = Modifier.weight(1f),
                                            )
                                        }

                                        FlowRow(
                                            modifier =
                                                Modifier.padding(
                                                    start = SmallIconSize + SpacingMd,
                                                ),
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

                                        val currentBestLabel =
                                            currentBest?.let {
                                                formatPersonalRecordValue(
                                                    value = it.value,
                                                    unit = it.unit,
                                                    locale = currentLocale,
                                                    unitLabel = unitLabelFor(it.unit, it.customUnitLabel, it.value),
                                                )
                                            }

                                        if (currentBestLabel == null) {
                                            val actionColors = personalRecordRelatedColors(category)
                                            AddActionPill(
                                                icon = Icons.Outlined.Add,
                                                label = stringResource(R.string.personal_records_add_first_result),
                                                containerColor = actionColors.container,
                                                contentColor = actionColors.content,
                                                onClick = { onFamilySelected(family.id) },
                                            )
                                        } else {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Bottom,
                                            ) {
                                                Text(text = currentBestLabel, style = typography.titleMedium)
                                                Text(
                                                    text = entryCountText,
                                                    style = typography.bodySmall,
                                                    color = colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

internal data class PersonalRecordFamilyGroup(
    val category: Category?,
    val families: List<PersonalRecordFamily>,
)

internal fun buildFamilyGroups(
    categories: List<Category>,
    families: List<PersonalRecordFamily>,
): List<PersonalRecordFamilyGroup> {
    val grouped = families.groupBy { it.categoryId }
    val categoryGroups =
        categories
            .sortedBy { it.sortOrder }
            .mapNotNull { category ->
                val categoryFamilies = grouped[category.id].orEmpty().sortedWith(compareBy({ it.sortOrder }, { it.id }))
                if (categoryFamilies.isEmpty()) {
                    null
                } else {
                    PersonalRecordFamilyGroup(category = category, families = categoryFamilies)
                }
            }

    val uncategorizedFamilies = grouped[null].orEmpty().sortedWith(compareBy({ it.sortOrder }, { it.id }))
    val uncategorizedGroup =
        if (uncategorizedFamilies.isEmpty()) {
            emptyList()
        } else {
            listOf(
                PersonalRecordFamilyGroup(
                    category = null,
                    families = uncategorizedFamilies,
                ),
            )
        }

    return categoryGroups + uncategorizedGroup
}
