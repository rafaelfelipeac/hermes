package com.rafaelfelipeac.hermes.features.trophies.presentation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.rafaelfelipeac.hermes.BuildConfig
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.currentLocale
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderThin
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyDetailArtworkSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophyProgressHeight
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyId

private const val TROPHY_DETAIL_ACCENT_ALPHA = 0.32f

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
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(SpacingXs))
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
                TrophyProgressIndicator(
                    trophy = trophy,
                    modifier = Modifier.fillMaxWidth(),
                )
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
private fun TrophyProgressIndicator(
    trophy: TrophyCardUi,
    modifier: Modifier = Modifier,
) {
    val progress = trophyProgressFraction(trophy.currentValue, trophy.target)

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
                            .background(trophyAccentColor(trophy).copy(alpha = TROPHY_DETAIL_ACCENT_ALPHA)),
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
private fun trophyName(trophy: TrophyCardUi): String = stringResource(trophyNameRes(trophy.trophyId))

@Composable
private fun trophyDescription(trophy: TrophyCardUi): String =
    stringResource(
        trophyDescriptionRes(trophy.trophyId, trophy.isUnlocked),
        trophy.target,
    )

@Composable
private fun trophyConditionLabel(trophy: TrophyCardUi): String {
    return stringResource(
        R.string.trophies_unlock_target,
        trophy.currentValue,
        trophy.target,
    )
}

@Composable
private fun unlockedDateLabel(unlockedAt: Long): String {
    val currentLocale = currentLocale()
    return stringResource(
        R.string.trophies_unlocked_on,
        formatUnlockedDateLabel(unlockedAt, currentLocale),
    )
}

@Composable
private fun trophyShareMessage(trophy: TrophyCardUi): String {
    val unlockedDateText =
        trophy.unlockedAt?.let { unlockedAt ->
            unlockedDateLabel(unlockedAt)
        }
    val appPackageName = resolveAppPackageName(BuildConfig.APPLICATION_ID, BuildConfig.DEBUG)
    val playStoreUrl = stringResource(R.string.settings_play_store_web_url, appPackageName)
    return buildTrophyShareMessage(
        shareTitle = stringResource(R.string.trophies_share_title, quotedShareValue(trophyName(trophy))),
        shareDescription = trophyShareDescription(trophy),
        unlockedDateText = unlockedDateText,
        shareCta = stringResource(R.string.trophies_share_cta, playStoreUrl),
    )
}

@Composable
private fun trophyShareDescription(trophy: TrophyCardUi): String {
    val categoryName = trophy.categoryName.orEmpty()
    return when (trophy.trophyId) {
        TrophyId.CHALLENGE_ACCEPTED,
        TrophyId.CHALLENGE_GOAL_SETTER,
        TrophyId.CHALLENGE_GOAL_ARCHITECT,
        -> stringResource(R.string.trophies_share_desc_challenge_creations, trophy.target)
        TrophyId.FIRST_CHALLENGE_WIN,
        TrophyId.CHALLENGE_MOMENTUM,
        TrophyId.CHALLENGE_VETERAN,
        -> stringResource(R.string.trophies_share_desc_challenge_completions, trophy.target)
        TrophyId.BACK_ON_TRACK,
        TrophyId.COMEBACK_MOMENTUM,
        TrophyId.NEVER_OUT,
        -> stringResource(R.string.trophies_share_desc_challenge_recoveries, trophy.target)
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
