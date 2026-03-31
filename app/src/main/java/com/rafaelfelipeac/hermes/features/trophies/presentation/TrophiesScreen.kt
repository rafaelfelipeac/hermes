@file:Suppress("LongMethod")

package com.rafaelfelipeac.hermes.features.trophies.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderThin
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyBackButtonSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyArtworkSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyCardFooterMinHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyDetailArtworkSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyDetailCardMinHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyDetailIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyOverviewCardMinHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyProgressHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyShelfIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyShelfArtworkSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyShelfCardMinWidth
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyId
import java.text.DateFormat
import java.util.Date
import java.util.Locale

internal const val TROPHIES_EMPTY_STATE_TAG = "trophies_empty_state"
internal const val TROPHIES_DETAIL_DIALOG_TAG = "trophies_detail_dialog"
internal const val TROPHIES_VIEW_ALL_TAG_PREFIX = "trophies_view_all_"
internal const val TROPHIES_FAMILY_DETAIL_TAG_PREFIX = "trophies_family_detail_"
private const val TROPHIES_PREVIEW_COUNT = 6
private const val TROPHIES_GRID_COLUMNS = 2

@Composable
fun TrophiesScreen(
    modifier: Modifier = Modifier,
    viewModel: TrophyViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var selectedFamilyName by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTrophyId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedFamily = state.families.firstOrNull { it.family.name == selectedFamilyName }
    val selectedTrophy =
        state.families
            .flatMap { it.sections }
            .flatMap { it.trophies }
            .firstOrNull { it.stableId == selectedTrophyId }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(SpacingXl),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        if (selectedFamily == null) {
            Text(
                text = stringResource(R.string.trophies_title),
                style = typography.titleLarge,
                color = colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.trophies_subtitle),
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
        } else {
            TrophyFamilyHeader(
                familySection = selectedFamily,
                onBack = { selectedFamilyName = null },
            )
        }

        TrophiesContent(
            state = state,
            selectedFamilyName = selectedFamilyName,
            onFamilySelected = { family -> selectedFamilyName = family.name },
            onBackFromFamily = { selectedFamilyName = null },
            onTrophySelected = { selectedTrophyId = it.stableId },
            modifier = Modifier.fillMaxSize(),
        )
    }

    selectedTrophy?.let { trophy ->
        TrophyDetailDialog(
            trophy = trophy,
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
            modifier = modifier,
        )
    } else {
        TrophyFamilyDetailContent(
            familySection = selectedFamily,
            onTrophySelected = onTrophySelected,
            modifier = modifier,
        )
    }
}

@Composable
private fun TrophyOverviewContent(
    families: List<TrophyFamilySectionUi>,
    onOpenFamily: (TrophyFamilyUi) -> Unit,
    onTrophySelected: (TrophyCardUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier.testTag(
                TROPHIES_FAMILY_DETAIL_TAG_PREFIX + familySection.family.name.lowercase(Locale.ROOT),
            ),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        LazyColumn(
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
                    text = stringResource(
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
private fun TrophyEmptyState(
    modifier: Modifier = Modifier,
) {
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
            description = stringResource(R.string.trophies_desc_full_time_locked),
        )
        TrophyPreviewCard(
            title = stringResource(R.string.trophies_name_comeback_week),
            description = stringResource(R.string.trophies_desc_comeback_week_locked),
        )
        TrophyPreviewCard(
            title = stringResource(R.string.trophies_name_podium_place),
            description = stringResource(R.string.trophies_desc_podium_place_locked),
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
            Text(text = title, style = typography.titleMedium, color = colorScheme.onSurface.copy(alpha = 0.72f))
            Text(
                text = description,
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
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

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cardWidth = (maxWidth - (SpacingMd * (TROPHIES_GRID_COLUMNS - 1))) / TROPHIES_GRID_COLUMNS

            FlowRow(
                maxItemsInEachRow = TROPHIES_GRID_COLUMNS,
                horizontalArrangement = Arrangement.spacedBy(SpacingMd),
                verticalArrangement = Arrangement.spacedBy(SpacingMd),
            ) {
                section.trophies.forEach { trophy ->
                    TrophyShelfCard(
                        trophy = trophy,
                        showExpandedMeta = true,
                        onClick = { onTrophySelected(trophy) },
                        modifier = Modifier.width(cardWidth),
                    )
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
            accent.copy(alpha = 0.28f)
        } else {
            colorScheme.outlineVariant
        }
    val cardColor =
        if (trophy.isUnlocked) {
            accent.copy(alpha = 0.10f)
        } else {
            colorScheme.surfaceContainerLow
        }
    val semanticsLabel =
        listOfNotNull(
            trophyName(trophy),
            trophy.categoryName,
            trophyStatusLabel(trophy),
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
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingSm),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TrophyArtwork(
                        trophy = trophy,
                        size = TrophyShelfArtworkSize,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                Text(
                    text = trophyName(trophy),
                    style = typography.titleSmall,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                )
                trophy.categoryName?.takeIf { showExpandedMeta }?.let { categoryName ->
                    Text(
                        text = categoryName,
                        style = typography.labelSmall,
                        color = accent,
                        textAlign = TextAlign.Center,
                    )
                }
                if (showExpandedMeta) {
                    TrophyCardFooter(
                        trophy = trophy,
                        modifier = Modifier.padding(horizontal = SpacingXs),
                    )
                }
            }
        }
    }
}

@Composable
private fun TrophyArtwork(
    trophy: TrophyCardUi,
    size: androidx.compose.ui.unit.Dp = TrophyArtworkSize,
    modifier: Modifier = Modifier,
) {
    val accent = trophyAccentColor(trophy)
    val artworkTint =
        if (trophy.isUnlocked) {
            accent
        } else {
            colorScheme.onSurfaceVariant
        }
    val artworkBackground =
        if (trophy.isUnlocked) {
            accent.copy(alpha = 0.16f)
        } else {
            colorScheme.surfaceContainerHigh
        }
    val artworkBorder =
        if (trophy.isUnlocked) {
            accent.copy(alpha = 0.28f)
        } else {
            colorScheme.outline
        }
    val artworkDescription = trophyName(trophy)

    Surface(
        modifier =
            modifier
                .size(size)
                .semantics { contentDescription = artworkDescription },
        shape = shapes.large,
        color = artworkBackground,
        border = BorderStroke(BorderThin, artworkBorder),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = trophyArtworkIcon(trophy),
                contentDescription = null,
                tint = artworkTint,
                modifier = Modifier.size(if (size == TrophyDetailArtworkSize) TrophyDetailIconSize else TrophyShelfIconSize),
            )
        }
    }
}

@Composable
internal fun TrophyDetailDialog(
    trophy: TrophyCardUi,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(TROPHIES_DETAIL_DIALOG_TAG),
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.trophies_detail_close))
            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingMd),
            ) {
                TrophyArtwork(
                    trophy = trophy,
                    size = TrophyDetailArtworkSize,
                )
                Text(text = trophyName(trophy))
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
private fun TrophyCardFooter(
    trophy: TrophyCardUi,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(TrophyCardFooterMinHeight),
        contentAlignment = Alignment.Center,
    ) {
        if (trophy.isUnlocked) {
            Text(
                text = trophy.unlockedAt?.let { unlockedAt -> unlockedDateLabel(unlockedAt) }.orEmpty(),
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 2,
                textAlign = TextAlign.Center,
            )
        } else {
            TrophyProgressIndicator(trophy = trophy)
        }
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
                            .background(trophyAccentColor(trophy).copy(alpha = 0.32f)),
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
private fun TrophyFamilyHeader(
    familySection: TrophyFamilySectionUi,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Surface(
            modifier =
                Modifier
                    .size(TrophyBackButtonSize)
                    .clickable(onClick = onBack),
            color = Color.Transparent,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.trophies_back),
                    tint = colorScheme.onSurface,
                )
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
                text = stringResource(R.string.trophies_unlocked_count, familySection.unlockedCount, familySection.totalCount),
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.size(TrophyBackButtonSize))
    }
}

@Composable
private fun trophyName(trophy: TrophyCardUi): String = stringResource(trophyNameRes(trophy.trophyId))

@Composable
private fun trophyDescription(trophy: TrophyCardUi): String = stringResource(trophyDescriptionRes(trophy.trophyId, trophy.isUnlocked))

@Composable
private fun familyTitle(family: TrophyFamilyUi): String = stringResource(familyTitleRes(family))

@Composable
private fun trophyStatusLabel(trophy: TrophyCardUi): String {
    return stringResource(
        if (trophy.isUnlocked) {
            R.string.trophies_status_unlocked
        } else {
            R.string.trophies_status_locked
        },
    )
}

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

private fun trophyAccentColor(trophy: TrophyCardUi): Color {
    return trophy.categoryColorId?.let(::categoryAccentColor) ?: trophyFamilyAccentColor(trophy.family)
}

private fun trophyFamilyAccentColor(family: TrophyFamilyUi): Color {
    return when (family) {
        TrophyFamilyUi.FOLLOW_THROUGH -> Color(0xFF4277B8)
        TrophyFamilyUi.CONSISTENCY -> Color(0xFF2B917D)
        TrophyFamilyUi.ADAPTABILITY -> Color(0xFFB97B38)
        TrophyFamilyUi.MOMENTUM -> Color(0xFF7B63C8)
        TrophyFamilyUi.BUILDER -> Color(0xFF6F7E4A)
        TrophyFamilyUi.CATEGORIES -> Color(0xFF8A5E3A)
    }
}

private fun trophyArtworkIcon(trophy: TrophyCardUi): ImageVector {
    if (!trophy.isUnlocked) return Icons.Outlined.Lock

    return trophyIcon(trophy.trophyId)
}

private fun trophyIcon(trophyId: TrophyId): ImageVector {
    return when (trophyId) {
        TrophyId.FULL_TIME -> Icons.Outlined.Flag
        TrophyId.MATCH_FITNESS -> Icons.Outlined.FitnessCenter
        TrophyId.IN_FORM -> Icons.Outlined.Repeat
        TrophyId.COMEBACK_WEEK -> Icons.Outlined.Explore
        TrophyId.GAME_PLAN -> Icons.Outlined.Build
        TrophyId.BACK_IN_FORMATION -> Icons.Outlined.ContentCopy
        TrophyId.HOLD_THE_LINE -> Icons.Outlined.CheckCircle
        TrophyId.TEAM_SHEET -> Icons.Outlined.GridView
        TrophyId.KIT_BAG -> Icons.Outlined.Archive
        TrophyId.PODIUM_PLACE -> Icons.Default.EmojiEvents
        TrophyId.HOME_GROUND -> Icons.Outlined.Home
        TrophyId.TRAINING_BLOCK -> Icons.Outlined.Inventory2
    }
}
