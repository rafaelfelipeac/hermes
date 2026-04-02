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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderThin
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyBadgeLockBadgeSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyBadgeLockIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyDetailArtworkSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyDetailIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyShelfIconSize

private const val BADGE_PATCH_SCALE = 0.72f
private const val BADGE_MEDAL_SCALE = 0.82f
private const val BADGE_CREST_SCALE = 0.96f
private const val BADGE_PATCH_ICON_SCALE = 0.96f
private const val BADGE_MEDAL_ICON_SCALE = 1.0f
private const val BADGE_CREST_ICON_SCALE = 1.08f

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
    val baseIconSize = if (size == TrophyDetailArtworkSize) TrophyDetailIconSize else TrophyShelfIconSize
    val iconTint =
        if (trophy.isUnlocked) {
            accent.copy(alpha = tierStyle.iconAlpha)
        } else {
            colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
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
            colorScheme.outline.copy(alpha = 0.78f)
        }
    val outerRingColor =
        if (trophy.isUnlocked) {
            accent.copy(alpha = tierStyle.outerRingAlpha)
        } else {
            colorScheme.outline.copy(alpha = 0.18f)
        }

    Box(
        modifier =
            modifier
                .size(size)
                .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        if (tierStyle.outerRingAlpha > 0f) {
            Surface(
                modifier = Modifier.size(size * tierStyle.outerRingScale),
                shape = CircleShape,
                color = outerRingColor,
            ) {}
        }

        Surface(
            modifier = Modifier.size(size * tierStyle.badgeScale),
            shape = CircleShape,
            color = badgeColor,
            border = BorderStroke(BorderThin, borderColor),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
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
                        .align(Alignment.BottomEnd)
                        .padding(SpacingXs),
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
        border = BorderStroke(BorderThin, colorScheme.outline.copy(alpha = 0.88f)),
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
    val outerRingScale: Float,
    val fillAlpha: Float,
    val borderAlpha: Float,
    val iconAlpha: Float,
    val outerRingAlpha: Float,
    val iconScale: Float,
)

private fun Int.toBadgeTierStyle(): TrophyBadgeTierStyle {
    return when {
        this >= 3 ->
            TrophyBadgeTierStyle(
                badgeScale = BADGE_CREST_SCALE,
                outerRingScale = 1f,
                fillAlpha = 0.28f,
                borderAlpha = 0.54f,
                iconAlpha = 1f,
                outerRingAlpha = 0.12f,
                iconScale = BADGE_CREST_ICON_SCALE,
            )

        this == 2 ->
            TrophyBadgeTierStyle(
                badgeScale = BADGE_MEDAL_SCALE,
                outerRingScale = 0.9f,
                fillAlpha = 0.22f,
                borderAlpha = 0.46f,
                iconAlpha = 0.94f,
                outerRingAlpha = 0.08f,
                iconScale = BADGE_MEDAL_ICON_SCALE,
            )

        else ->
            TrophyBadgeTierStyle(
                badgeScale = BADGE_PATCH_SCALE,
                outerRingScale = 0f,
                fillAlpha = 0.16f,
                borderAlpha = 0.36f,
                iconAlpha = 0.88f,
                outerRingAlpha = 0f,
                iconScale = BADGE_PATCH_ICON_SCALE,
            )
    }
}
