package com.rafaelfelipeac.hermes.features.trophies.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderThin
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyBadgeLockBadgeSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyBadgeLockIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyDetailArtworkSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyDetailIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyShelfIconSize

private const val BADGE_PATCH_SCALE = 0.72f
private const val BADGE_MEDAL_SCALE = 0.82f
private const val BADGE_CREST_SCALE = 0.94f
private const val BADGE_PATCH_ICON_SCALE = 0.96f
private const val BADGE_MEDAL_ICON_SCALE = 1.0f
private const val BADGE_CREST_ICON_SCALE = 1.08f
private const val BADGE_FILL_ALPHA = 0.24f
private const val BADGE_BORDER_ALPHA = 0.46f
private const val BADGE_ICON_ALPHA = 0.92f
private const val BADGE_LOCKED_ICON_ALPHA = 0.72f
private const val BADGE_LOCKED_BORDER_ALPHA = 0.78f
private const val BADGE_LOCKED_MIDDLE_RING_ALPHA = 0.34f
private const val BADGE_LOCKED_INNER_RING_ALPHA = 0.26f
private const val BADGE_LOCK_OVERLAY_BORDER_ALPHA = 0.88f
private const val BADGE_NO_RING_ALPHA = 0f
private const val BADGE_LOCK_SHELF_ANCHOR_X_FRACTION = 0.82f
private const val BADGE_LOCK_SHELF_ANCHOR_Y_FRACTION = 0.86f
private const val BADGE_LOCK_DETAIL_ANCHOR_X_FRACTION = 0.82f
private const val BADGE_LOCK_DETAIL_ANCHOR_Y_FRACTION = 0.82f
private const val BADGE_MEDAL_RING_STEP = 0.08f
private const val BADGE_CREST_RING_STEP = 0.08f

@Composable
internal fun TrophyBadge(
    trophy: TrophyCardUi,
    icon: ImageVector,
    contentDescription: String,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val accent = trophyAccentColor(trophy)
    val tierStyle = trophy.badgeRank.toBadgeTierStyle()
    val isDetailBadge = size == TrophyDetailArtworkSize
    val baseIconSize = if (isDetailBadge) TrophyDetailIconSize else TrophyShelfIconSize
    val lockAnchorXFraction = if (isDetailBadge) BADGE_LOCK_DETAIL_ANCHOR_X_FRACTION else BADGE_LOCK_SHELF_ANCHOR_X_FRACTION
    val lockAnchorYFraction = if (isDetailBadge) BADGE_LOCK_DETAIL_ANCHOR_Y_FRACTION else BADGE_LOCK_SHELF_ANCHOR_Y_FRACTION
    val iconTint =
        if (trophy.isUnlocked) {
            accent.copy(alpha = tierStyle.iconAlpha)
        } else {
            colorScheme.onSurfaceVariant.copy(alpha = BADGE_LOCKED_ICON_ALPHA)
        }
    val badgeColor =
        if (trophy.isUnlocked) {
            accent.copy(alpha = tierStyle.fillAlpha)
        } else {
            colorScheme.surfaceContainerHigh
        }
    val borderColor =
        if (trophy.isUnlocked) {
            accent.copy(alpha = tierStyle.borderAlpha)
        } else {
            colorScheme.outline.copy(alpha = BADGE_LOCKED_BORDER_ALPHA)
        }
    val middleRingColor =
        if (trophy.isUnlocked) {
            accent.copy(alpha = tierStyle.middleRingAlpha)
        } else {
            colorScheme.outline.copy(alpha = BADGE_LOCKED_MIDDLE_RING_ALPHA)
        }
    val innerRingColor =
        if (trophy.isUnlocked) {
            accent.copy(alpha = tierStyle.innerRingAlpha)
        } else {
            colorScheme.outline.copy(alpha = BADGE_LOCKED_INNER_RING_ALPHA)
        }
    val badgeDiameter = size * tierStyle.badgeScale
    val badgeOffset = (size - badgeDiameter) / 2
    Box(
        modifier =
            modifier
                .size(size)
                .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.size(badgeDiameter),
            shape = CircleShape,
            color = badgeColor,
            border = BorderStroke(BorderThin, borderColor),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (tierStyle.middleRingAlpha > 0f) {
                    Surface(
                        modifier = Modifier.size(badgeDiameter * tierStyle.middleRingScale),
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = BorderStroke(BorderThin, middleRingColor),
                    ) {}
                }
                if (tierStyle.innerRingAlpha > 0f) {
                    Surface(
                        modifier = Modifier.size(badgeDiameter * tierStyle.innerRingScale),
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = BorderStroke(BorderThin, innerRingColor),
                    ) {}
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(baseIconSize * tierStyle.iconScale),
                )
            }
        }

        if (!trophy.isUnlocked) {
            LockedOverlayBadge(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(
                            start = badgeOffset + (badgeDiameter * lockAnchorXFraction) - (TrophyBadgeLockBadgeSize / 2),
                            top = badgeOffset + (badgeDiameter * lockAnchorYFraction) - (TrophyBadgeLockBadgeSize / 2),
                        ),
            )
        }
    }
}

@Composable
private fun LockedOverlayBadge(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .size(TrophyBadgeLockBadgeSize),
        shape = CircleShape,
        color = colorScheme.surfaceContainerHighest,
        border = BorderStroke(BorderThin, colorScheme.outline.copy(alpha = BADGE_LOCK_OVERLAY_BORDER_ALPHA)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(TrophyBadgeLockIconSize),
            )
        }
    }
}

private data class TrophyBadgeTierStyle(
    val badgeScale: Float,
    val middleRingScale: Float,
    val innerRingScale: Float,
    val fillAlpha: Float,
    val borderAlpha: Float,
    val iconAlpha: Float,
    val middleRingAlpha: Float,
    val innerRingAlpha: Float,
    val iconScale: Float,
)

private fun Int.toBadgeTierStyle(): TrophyBadgeTierStyle {
    return when {
        this >= 3 ->
            TrophyBadgeTierStyle(
                badgeScale = BADGE_CREST_SCALE,
                middleRingScale = 1f - BADGE_CREST_RING_STEP,
                innerRingScale = 1f - (BADGE_CREST_RING_STEP * 2f),
                fillAlpha = BADGE_FILL_ALPHA,
                borderAlpha = BADGE_BORDER_ALPHA,
                iconAlpha = BADGE_ICON_ALPHA,
                middleRingAlpha = BADGE_BORDER_ALPHA,
                innerRingAlpha = BADGE_BORDER_ALPHA,
                iconScale = BADGE_CREST_ICON_SCALE,
            )

        this == 2 ->
            TrophyBadgeTierStyle(
                badgeScale = BADGE_MEDAL_SCALE,
                middleRingScale = 1f - BADGE_MEDAL_RING_STEP,
                innerRingScale = 0f,
                fillAlpha = BADGE_FILL_ALPHA,
                borderAlpha = BADGE_BORDER_ALPHA,
                iconAlpha = BADGE_ICON_ALPHA,
                middleRingAlpha = BADGE_BORDER_ALPHA,
                innerRingAlpha = BADGE_NO_RING_ALPHA,
                iconScale = BADGE_MEDAL_ICON_SCALE,
            )

        else ->
            TrophyBadgeTierStyle(
                badgeScale = BADGE_PATCH_SCALE,
                middleRingScale = 0f,
                innerRingScale = 0f,
                fillAlpha = BADGE_FILL_ALPHA,
                borderAlpha = BADGE_BORDER_ALPHA,
                iconAlpha = BADGE_ICON_ALPHA,
                middleRingAlpha = BADGE_NO_RING_ALPHA,
                innerRingAlpha = BADGE_NO_RING_ALPHA,
                iconScale = BADGE_PATCH_ICON_SCALE,
            )
    }
}
