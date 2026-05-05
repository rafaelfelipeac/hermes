@file:Suppress("CyclomaticComplexMethod", "LongMethod", "TooManyFunctions")

package com.rafaelfelipeac.hermes.features.trophies.presentation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.BuildConfig
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.AppConstants.NEW_LINE
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderThin
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyBackButtonSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyBackButtonTouchTargetSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyCardArtworkTopPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyCardCategoryBlockHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyCardTitleBlockHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyDetailArtworkSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyDetailCardMinHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyGridArtworkSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyOverviewCardMinHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyProgressHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyShelfArtworkSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyShelfCardMinWidth
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyStateLineBlockHeight
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyId
import java.text.DateFormat
import java.util.Date
import java.util.Locale

internal const val TROPHIES_EMPTY_STATE_TAG = "trophies_empty_state"
internal const val TROPHIES_DETAIL_DIALOG_TAG = "trophies_detail_dialog"
internal const val TROPHIES_OVERVIEW_LIST_TAG = "trophies_overview_list"
internal const val TROPHIES_FAMILY_LIST_TAG_PREFIX = "trophies_family_list_"
internal const val TROPHIES_VIEW_ALL_TAG_PREFIX = "trophies_view_all_"
internal const val TROPHIES_FAMILY_DETAIL_TAG_PREFIX = "trophies_family_detail_"
private const val TROPHIES_SHARE_INTENT_TYPE = "text/plain"
private const val TROPHIES_DEBUG_PACKAGE_SUFFIX = ".dev"
private const val TROPHIES_PREVIEW_COUNT = 6
private const val TROPHIES_GRID_COLUMNS = 2
private const val TROPHIES_SAMPLE_FULL_TIME_TARGET = 4
private const val TROPHIES_SAMPLE_COMEBACK_TARGET = 4
private const val TROPHIES_SAMPLE_PODIUM_TARGET = 10
private const val TROPHY_PREVIEW_TITLE_ALPHA = 0.72f
private const val TROPHY_PREVIEW_DESCRIPTION_ALPHA = 0.72f
private const val TROPHY_UNLOCKED_CARD_BORDER_ALPHA = 0.42f
private const val TROPHY_UNLOCKED_CARD_FILL_ALPHA = 0.16f
private const val TROPHY_PROGRESS_FILL_ALPHA = 0.32f
private const val TROPHIES_QUOTE = "\""

@Composable
fun TrophiesScreen(
    modifier: Modifier = Modifier,
    requestedTrophyStableId: String? = null,
    onRequestedTrophyConsumed: () -> Unit = {},
    onOpenActivities: () -> Unit = {},
    viewModel: TrophyViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var selectedFamilyName by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTrophyId by rememberSaveable { mutableStateOf<String?>(null) }
    var overviewFirstVisibleItemIndex by rememberSaveable { mutableIntStateOf(0) }
    var overviewFirstVisibleItemScrollOffset by rememberSaveable { mutableIntStateOf(0) }
    val familyFirstVisibleItemIndex = remember { mutableStateMapOf<String, Int>() }
    val familyFirstVisibleItemScrollOffset = remember { mutableStateMapOf<String, Int>() }
    val selectedFamily = state.families.firstOrNull { it.family.name == selectedFamilyName }
    val selectedTrophy =
        state.families
            .flatMap { it.sections }
            .flatMap { it.trophies }
            .firstOrNull { it.stableId == selectedTrophyId }
    val requestedTrophy =
        requestedTrophyStableId?.let { requestedId ->
            state.families
                .flatMap { it.sections }
                .flatMap { it.trophies }
                .firstOrNull { it.stableId == requestedId }
        }

    LaunchedEffect(requestedTrophy?.stableId) {
        if (requestedTrophy != null) {
            selectedFamilyName = null
            selectedTrophyId = requestedTrophy.stableId
            onRequestedTrophyConsumed()
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(SpacingXl),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        TrophiesHeader(
            familySection = selectedFamily,
            onBack = { selectedFamilyName = null },
            onOpenActivities = onOpenActivities,
        )

        TrophiesContent(
            state = state,
            selectedFamilyName = selectedFamilyName,
            onFamilySelected = { family -> selectedFamilyName = family.name },
            onBackFromFamily = { selectedFamilyName = null },
            onTrophySelected = { selectedTrophyId = it.stableId },
            overviewFirstVisibleItemIndex = overviewFirstVisibleItemIndex,
            overviewFirstVisibleItemScrollOffset = overviewFirstVisibleItemScrollOffset,
            onOverviewScrollChanged = { index, offset ->
                overviewFirstVisibleItemIndex = index
                overviewFirstVisibleItemScrollOffset = offset
            },
            familyFirstVisibleItemIndex = familyFirstVisibleItemIndex,
            familyFirstVisibleItemScrollOffset = familyFirstVisibleItemScrollOffset,
            modifier = Modifier.fillMaxSize(),
        )
    }

    selectedTrophy?.let { trophy ->
        val selectedTrophyName = trophyName(trophy)
        TrophyDetailDialog(
            trophy = trophy,
            onShare = { viewModel.logShareTrophy(trophy, selectedTrophyName) },
            onDismiss = { selectedTrophyId = null },
        )
    }
}

@Composable
internal fun TrophiesContent(
    state: TrophyPageState,
    selectedFamilyName: String?,
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

    BackHandler(enabled = selectedFamily != null) {
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
    onOpenActivities: () -> Unit,
) {
    if (familySection == null) {
        Column(verticalArrangement = Arrangement.spacedBy(SpacingXs)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.trophies_title),
                    style = typography.titleLarge,
                    color = colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.TopStart),
                )
                ActivitiesButton(
                    onClick = onOpenActivities,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(y = -(SpacingSm + SpacingXs)),
                )
            }
            Text(
                text = stringResource(R.string.trophies_subtitle),
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier =
                    Modifier
                        .size(TrophyBackButtonTouchTargetSize)
                        .clickable(onClick = onBack),
                color = Color.Transparent,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier.size(TrophyBackButtonSize),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.trophies_back),
                            tint = colorScheme.onSurface,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SpacingXs),
            ) {
                Text(
                    text = familyTitle(familySection.family),
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
private fun TrophyOverviewContent(
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
private fun TrophyFamilyDetailContent(
    familySection: TrophyFamilySectionUi,
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
    Column(
        modifier =
            modifier.testTag(
                TROPHIES_FAMILY_DETAIL_TAG_PREFIX + familySection.family.name.lowercase(Locale.ROOT),
            ),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        LazyColumn(
            state = listState,
            modifier =
                Modifier.testTag(
                    TROPHIES_FAMILY_LIST_TAG_PREFIX + familySection.family.name.lowercase(Locale.ROOT),
                ),
            verticalArrangement = Arrangement.spacedBy(SpacingLg),
        ) {
            items(familySection.sections, key = { it.stableId }) { section ->
                TrophySection(
                    section = section,
                    onTrophySelected = onTrophySelected,
                )
            }
        }
    }
}

@Composable
private fun rememberPreservedLazyListState(
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
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                onScrollChanged(index, offset)
            }
    }

    return listState
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
                            TROPHIES_VIEW_ALL_TAG_PREFIX + familySection.family.name.lowercase(Locale.ROOT),
                        ),
            ) {
                Text(text = stringResource(R.string.trophies_view_all))
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(SpacingMd),
        ) {
            items(trophies.take(TROPHIES_PREVIEW_COUNT), key = { it.stableId }) { trophy ->
                TrophyShelfCard(
                    trophy = trophy,
                    showExpandedMeta = false,
                    onClick = { onTrophySelected(trophy) },
                    modifier = Modifier.width(TrophyShelfCardMinWidth),
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
    Card(colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow)) {
        Column(
            modifier = Modifier.padding(SpacingMd),
            verticalArrangement = Arrangement.spacedBy(SpacingXs),
        ) {
            Text(
                text = title,
                style = typography.titleMedium,
                color = colorScheme.onSurface.copy(alpha = TROPHY_PREVIEW_TITLE_ALPHA),
            )
            Text(
                text = description,
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = TROPHY_PREVIEW_DESCRIPTION_ALPHA),
            )
        }
    }
}

@Composable
private fun TrophySection(
    section: TrophySectionUi,
    onTrophySelected: (TrophyCardUi) -> Unit,
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
    modifier: Modifier = Modifier,
) {
    val accent = trophyAccentColor(trophy)
    val borderColor =
        if (trophy.isUnlocked) {
            accent.copy(alpha = TROPHY_UNLOCKED_CARD_BORDER_ALPHA)
        } else {
            colorScheme.outlineVariant
        }
    val cardColor =
        if (trophy.isUnlocked) {
            accent.copy(alpha = TROPHY_UNLOCKED_CARD_FILL_ALPHA)
        } else {
            colorScheme.surfaceContainerLow
        }
    val semanticsLabel =
        listOfNotNull(
            trophyName(trophy),
            trophy.categoryName,
            trophyConditionLabel(trophy),
        ).joinToString(separator = ". ")

    Card(
        modifier =
            modifier
                .height(if (showExpandedMeta) TrophyDetailCardMinHeight else TrophyOverviewCardMinHeight)
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
                        maxLines = 2,
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
}

@Composable
internal fun TrophyDetailDialog(
    trophy: TrophyCardUi,
    onShare: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val shareMessage = trophyShareMessage(trophy)
    val shareChooserTitle = stringResource(R.string.trophies_share_chooser)
    val shareUnavailableMessage = stringResource(R.string.settings_share_unavailable)
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(TROPHIES_DETAIL_DIALOG_TAG),
        confirmButton = {
            if (trophy.isUnlocked) {
                Button(
                    onClick = {
                        onShare()
                        val shareIntent =
                            Intent(Intent.ACTION_SEND).apply {
                                type = TROPHIES_SHARE_INTENT_TYPE
                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                            }

                        try {
                            context.startActivity(Intent.createChooser(shareIntent, shareChooserTitle))
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                shareUnavailableMessage,
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.size(SpacingXs))
                    Text(text = stringResource(R.string.trophies_detail_share))
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.trophies_detail_close))
                }
            }
        },
        dismissButton = {
            if (trophy.isUnlocked) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.trophies_detail_close))
                }
            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingMd),
            ) {
                TrophyBadge(
                    trophy = trophy,
                    icon = trophyIcon(trophy.trophyId),
                    contentDescription = trophyName(trophy),
                    size = TrophyDetailArtworkSize,
                )
                Text(
                    text = trophyName(trophy),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                trophy.categoryName?.let {
                    Text(
                        text = it,
                        style = typography.labelMedium,
                        color = trophyAccentColor(trophy),
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingMd),
            ) {
                Text(
                    text = trophyDescription(trophy),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                if (!trophy.isUnlocked) {
                    TrophyProgressIndicator(
                        trophy = trophy,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                trophy.unlockedAt?.let { unlockedAt ->
                    TrophyDetailMeta(
                        label = unlockedDateLabel(unlockedAt),
                    )
                }
            }
        },
    )
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
                if (trophy.isUnlocked) {
                    trophy.unlockedAt?.let { unlockedAt -> unlockedDateCompactLabel(unlockedAt) }.orEmpty()
                } else {
                    trophyConditionLabel(trophy)
                },
            style = typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun TrophyProgressIndicator(
    trophy: TrophyCardUi,
    modifier: Modifier = Modifier,
) {
    val progress = trophy.currentValue.coerceAtMost(trophy.target).toFloat() / trophy.target.toFloat()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(TrophyProgressHeight + (SpacingXs * 5)),
            shape = shapes.small,
            color = colorScheme.surfaceVariant,
            border = BorderStroke(BorderThin, colorScheme.outlineVariant),
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(
                    modifier =
                        Modifier
                            .width(maxWidth * progress)
                            .fillMaxSize()
                            .background(trophyAccentColor(trophy).copy(alpha = TROPHY_PROGRESS_FILL_ALPHA)),
                )
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = trophyConditionLabel(trophy),
                        style = typography.labelSmall,
                        color = colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun TrophyDetailMeta(
    label: String,
    value: String? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingXs),
    ) {
        Text(
            text = label,
            style = typography.bodyMedium,
            color = colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        value?.let {
            Text(
                text = it,
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
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
private fun trophyName(trophy: TrophyCardUi): String = stringResource(trophyNameRes(trophy.trophyId))

@Composable
private fun trophyDescription(trophy: TrophyCardUi): String =
    stringResource(
        trophyDescriptionRes(trophy.trophyId, trophy.isUnlocked),
        trophy.target,
    )

@Composable
private fun familyTitle(family: TrophyFamilyUi): String = stringResource(familyTitleRes(family))

@Composable
private fun trophyConditionLabel(trophy: TrophyCardUi): String {
    return stringResource(
        R.string.trophies_unlock_target,
        trophy.currentValue.coerceAtMost(trophy.target),
        trophy.target,
    )
}

@Composable
private fun unlockedDateLabel(unlockedAt: Long): String {
    val currentLocale = LocalConfiguration.current.locales.get(0) ?: Locale.getDefault()
    return stringResource(
        R.string.trophies_unlocked_on,
        DateFormat.getDateInstance(DateFormat.MEDIUM, currentLocale).format(Date(unlockedAt)),
    )
}

@Composable
private fun unlockedDateCompactLabel(unlockedAt: Long): String {
    val currentLocale = LocalConfiguration.current.locales.get(0) ?: Locale.getDefault()
    return DateFormat.getDateInstance(DateFormat.SHORT, currentLocale).format(Date(unlockedAt))
}

@Composable
private fun trophyShareMessage(trophy: TrophyCardUi): String {
    val unlockedDateText =
        trophy.unlockedAt?.let { unlockedAt ->
            unlockedDateLabel(unlockedAt)
        }.orEmpty()
    val appPackageName =
        if (BuildConfig.DEBUG && BuildConfig.APPLICATION_ID.endsWith(TROPHIES_DEBUG_PACKAGE_SUFFIX)) {
            BuildConfig.APPLICATION_ID.removeSuffix(TROPHIES_DEBUG_PACKAGE_SUFFIX)
        } else {
            BuildConfig.APPLICATION_ID
        }
    val playStoreUrl = stringResource(R.string.settings_play_store_web_url, appPackageName)
    return listOfNotNull(
        stringResource(R.string.trophies_share_title, quotedShareValue(trophyName(trophy))),
        trophyShareDescription(trophy),
        unlockedDateText,
        EMPTY,
        stringResource(R.string.trophies_share_cta, playStoreUrl),
    ).joinToString(separator = NEW_LINE)
}

private fun quotedShareValue(value: String): String = TROPHIES_QUOTE + value + TROPHIES_QUOTE

@Composable
private fun trophyShareDescription(trophy: TrophyCardUi): String {
    val categoryName = trophy.categoryName.orEmpty()
    return when (trophy.trophyId) {
        TrophyId.PODIUM_PLACE,
        TrophyId.IN_ROTATION,
        TrophyId.MAINSTAY,
        -> stringResource(R.string.trophies_share_desc_category_completions, trophy.target, categoryName)
        TrophyId.HOME_GROUND,
        TrophyId.LOCAL_FAVORITE,
        TrophyId.TERRITORY,
        -> stringResource(R.string.trophies_share_desc_category_presence, trophy.target, categoryName)
        TrophyId.TRAINING_BLOCK ->
            stringResource(R.string.trophies_share_desc_category_planning, trophy.target, categoryName)
        else -> stringResource(trophyShareDescriptionRes(trophy.trophyId), trophy.target)
    }
}

internal fun trophyAccentColor(trophy: TrophyCardUi): Color {
    return trophy.categoryColorId?.let(::categoryAccentColor) ?: trophyFamilyAccentColor(trophy.family)
}

private fun trophyFamilyAccentColor(family: TrophyFamilyUi): Color {
    return when (family) {
        TrophyFamilyUi.FOLLOW_THROUGH -> Color(0xFF4277B8)
        TrophyFamilyUi.CONSISTENCY -> Color(0xFF2B917D)
        TrophyFamilyUi.ADAPTABILITY -> Color(0xFFB97B38)
        TrophyFamilyUi.MOMENTUM -> Color(0xFF7B63C8)
        TrophyFamilyUi.BUILDER -> Color(0xFF6F7E4A)
        TrophyFamilyUi.RACE_EVENTS -> Color(0xFFB44F3E)
        TrophyFamilyUi.CATEGORIES -> Color(0xFF8A5E3A)
    }
}

internal fun trophyIcon(trophyId: TrophyId): androidx.compose.ui.graphics.vector.ImageVector {
    return when (trophyId) {
        TrophyId.FULL_TIME,
        TrophyId.SEASON_BUILDER,
        TrophyId.SEASON_ANCHOR,
        -> Icons.Outlined.FitnessCenter
        TrophyId.MATCH_FITNESS,
        TrophyId.ENGINE_ROOM,
        TrophyId.WORKHORSE,
        -> Icons.Outlined.FitnessCenter
        TrophyId.IN_FORM,
        TrophyId.LOCKED_IN,
        TrophyId.STEADY_RHYTHM,
        -> Icons.Outlined.Repeat
        TrophyId.COMEBACK_WEEK -> Icons.Outlined.Explore
        TrophyId.GAME_PLAN,
        TrophyId.TACTICAL_BOARD,
        TrophyId.FIELD_MARSHAL,
        -> Icons.Outlined.Build
        TrophyId.BACK_IN_FORMATION -> Icons.Outlined.ContentCopy
        TrophyId.HOLD_THE_LINE -> Icons.Outlined.CheckCircle
        TrophyId.TEAM_SHEET -> Icons.Outlined.GridView
        TrophyId.KIT_BAG -> Icons.Outlined.Archive
        TrophyId.KICKOFF,
        TrophyId.SET_PIECE,
        TrophyId.PROGRAM_BUILDER,
        -> Icons.Filled.Add
        TrophyId.PROTECTED_TIME -> Icons.Outlined.EventBusy
        TrophyId.EVENT_PLANNER,
        TrophyId.EVENT_CALENDAR,
        TrophyId.EVENT_SEASON,
        -> Icons.Outlined.Flag
        TrophyId.RACE_READY,
        TrophyId.RACE_SHARP,
        TrophyId.RACE_FINISH,
        -> Icons.Outlined.CheckCircle
        TrophyId.PODIUM_PLACE,
        TrophyId.IN_ROTATION,
        TrophyId.MAINSTAY,
        -> Icons.Default.EmojiEvents
        TrophyId.HOME_GROUND,
        TrophyId.LOCAL_FAVORITE,
        TrophyId.TERRITORY,
        -> Icons.Outlined.Home
        TrophyId.TRAINING_BLOCK -> Icons.Outlined.Inventory2
    }
}
