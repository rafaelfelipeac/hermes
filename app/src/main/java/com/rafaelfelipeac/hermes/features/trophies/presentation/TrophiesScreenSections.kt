package com.rafaelfelipeac.hermes.features.trophies.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderThin
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyCardArtworkTopPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyCardCategoryBlockHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyCardTitleBlockHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyDetailCardMinHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyGridArtworkSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyOverviewCardMinHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyShelfArtworkSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyShelfCardMinWidth
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyStateLineBlockHeight
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor

private const val UNLOCKED_TROPHY_BORDER_ALPHA = 0.42f
private const val UNLOCKED_TROPHY_CARD_ALPHA = 0.16f
private const val TROPHY_TITLE_MAX_LINES = 2

@Composable
internal fun TrophyOverviewContent(
    families: List<TrophyFamilySectionUi>,
    onOpenFamily: (TrophyFamilyUi) -> Unit,
    onTrophySelected: (TrophyCardUi) -> Unit,
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
    onScrollChanged: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState =
        rememberPreservedLazyListState(
            firstVisibleItemIndex = firstVisibleItemIndex,
            firstVisibleItemScrollOffset = firstVisibleItemScrollOffset,
            onScrollChanged = onScrollChanged,
        )
    LazyColumn(
        state = listState,
        modifier = modifier.testTag(TROPHIES_OVERVIEW_LIST_TAG),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        items(families, key = { it.family.name }) { familySection ->
            TrophyOverviewSection(
                familySection = familySection,
                onOpenFamily = { onOpenFamily(familySection.family) },
                onTrophySelected = onTrophySelected,
            )
        }
    }
}

@Composable
internal fun TrophyFamilyDetailContent(
    familySection: TrophyFamilySectionUi,
    onTrophySelected: (TrophyCardUi) -> Unit,
    requestedTrophyStableId: String?,
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
    onScrollChanged: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState =
        rememberPreservedLazyListState(
            firstVisibleItemIndex = firstVisibleItemIndex,
            firstVisibleItemScrollOffset = firstVisibleItemScrollOffset,
            onScrollChanged = onScrollChanged,
        )
    Column(
        modifier =
            modifier.testTag(
                TROPHIES_FAMILY_DETAIL_TAG_PREFIX + familySection.family.name.lowercase(java.util.Locale.ROOT),
            ),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        LazyColumn(
            state = listState,
            modifier =
                Modifier.testTag(
                    TROPHIES_FAMILY_LIST_TAG_PREFIX + familySection.family.name.lowercase(java.util.Locale.ROOT),
                ),
            verticalArrangement = Arrangement.spacedBy(SpacingLg),
        ) {
            items(familySection.sections, key = { it.stableId }) { section ->
                TrophySection(
                    section = section,
                    onTrophySelected = onTrophySelected,
                    requestedTrophyStableId = requestedTrophyStableId,
                )
            }
        }
    }
}

@Composable
private fun TrophyOverviewSection(
    familySection: TrophyFamilySectionUi,
    onOpenFamily: () -> Unit,
    onTrophySelected: (TrophyCardUi) -> Unit,
) {
    val trophies = familySection.sections.flatMap { it.trophies }
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingXs)) {
                Text(
                    text = familyTitle(familySection.family),
                    style = typography.titleMedium,
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
            TextButton(
                onClick = onOpenFamily,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .testTag(
                            TROPHIES_VIEW_ALL_TAG_PREFIX + familySection.family.name.lowercase(java.util.Locale.ROOT),
                        ),
            ) {
                Text(text = stringResource(R.string.trophies_view_all))
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(SpacingMd),
        ) {
            items(previewTrophies(trophies), key = { it.stableId }) { trophy ->
                TrophyShelfCard(
                    trophy = trophy,
                    showExpandedMeta = false,
                    onClick = { onTrophySelected(trophy) },
                    focusRequested = false,
                    modifier = Modifier.width(TrophyShelfCardMinWidth),
                )
            }
        }
    }
}

@Composable
private fun TrophySection(
    section: TrophySectionUi,
    onTrophySelected: (TrophyCardUi) -> Unit,
    requestedTrophyStableId: String?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        section.title?.let { title ->
            Text(
                text = title,
                style = typography.titleMedium,
                color = section.accentColorId?.let(::categoryAccentColor) ?: colorScheme.onSurface,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
            section.trophies.chunked(TROPHIES_GRID_COLUMNS).forEach { trophiesInRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpacingMd),
                ) {
                    trophiesInRow.forEach { trophy ->
                        TrophyShelfCard(
                            trophy = trophy,
                            showExpandedMeta = true,
                            onClick = { onTrophySelected(trophy) },
                            focusRequested = trophy.stableId == requestedTrophyStableId,
                            modifier =
                                if (trophiesInRow.size == 1) {
                                    Modifier.fillMaxWidth()
                                } else {
                                    Modifier.weight(1f)
                                },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrophyShelfCard(
    trophy: TrophyCardUi,
    showExpandedMeta: Boolean,
    onClick: () -> Unit,
    focusRequested: Boolean,
    modifier: Modifier = Modifier,
) {
    val accent = trophyAccentColor(trophy)
    val borderColor =
        if (trophy.isUnlocked) {
            accent.copy(alpha = UNLOCKED_TROPHY_BORDER_ALPHA)
        } else {
            colorScheme.outlineVariant
        }
    val cardColor =
        if (trophy.isUnlocked) {
            accent.copy(alpha = UNLOCKED_TROPHY_CARD_ALPHA)
        } else {
            colorScheme.surfaceContainerLow
        }
    val semanticsLabel =
        trophyCardSemanticsLabel(
            name = trophyName(trophy),
            categoryName = trophy.categoryName,
            conditionLabel = trophyConditionLabel(trophy),
        )
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    Card(
        modifier =
            modifier
                .height(if (showExpandedMeta) TrophyDetailCardMinHeight else TrophyOverviewCardMinHeight)
                .then(
                    if (focusRequested) {
                        Modifier.bringIntoViewRequester(bringIntoViewRequester)
                    } else {
                        Modifier
                    },
                )
                .semantics { contentDescription = semanticsLabel }
                .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(BorderThin, borderColor),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(SpacingMd),
        ) {
            Column(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (showExpandedMeta) SpacingXs else SpacingSm),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = TrophyCardArtworkTopPadding),
                ) {
                    TrophyBadge(
                        trophy = trophy,
                        icon = trophyIcon(trophy.trophyId),
                        contentDescription = trophyName(trophy),
                        size = if (showExpandedMeta) TrophyGridArtworkSize else TrophyShelfArtworkSize,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(TrophyCardTitleBlockHeight),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Text(
                        text = trophyName(trophy),
                        style = typography.titleSmall,
                        color = colorScheme.onSurface,
                        maxLines = TROPHY_TITLE_MAX_LINES,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(TrophyCardCategoryBlockHeight),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    trophy.categoryName
                        ?.takeIf { showExpandedMeta && trophy.family != TrophyFamilyUi.CATEGORIES }
                        ?.let { categoryName ->
                            Text(
                                text = categoryName,
                                style = typography.labelSmall,
                                color = accent,
                                textAlign = TextAlign.Center,
                            )
                        }
                }
            }
            if (showExpandedMeta) {
                TrophyStateLine(
                    trophy = trophy,
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = SpacingXs),
                )
            }
        }
    }

    if (focusRequested) {
        LaunchedEffect(bringIntoViewRequester) {
            bringIntoViewRequester.bringIntoView()
        }
    }
}

@Composable
private fun TrophyStateLine(
    trophy: TrophyCardUi,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(TrophyStateLineBlockHeight),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text =
                    trophyStateLineText(
                    isUnlocked = trophy.isUnlocked,
                    unlockedDateText =
                        trophy.unlockedAt?.let { unlockedAt ->
                            formatUnlockedDateCompactLabel(unlockedAt, java.util.Locale.getDefault())
                        },
                    conditionLabel = trophyConditionLabel(trophy),
                ),
            style = typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun trophyConditionLabel(trophy: TrophyCardUi): String {
    return stringResource(
        R.string.trophies_unlock_target,
        trophy.currentValue,
        trophy.target,
    )
}

@Composable
private fun trophyName(trophy: TrophyCardUi): String =
    stringResource(
        trophyNameRes(trophy.trophyId),
    )

@Composable
private fun familyTitle(family: TrophyFamilyUi): String =
    stringResource(
        familyTitleRes(family),
    )
