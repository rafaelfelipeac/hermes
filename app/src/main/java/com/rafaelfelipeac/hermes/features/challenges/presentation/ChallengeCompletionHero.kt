package com.rafaelfelipeac.hermes.features.challenges.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.currentLocale
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ChallengeCompletionIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeCalculationResult
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuantity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeStatus

@Composable
internal fun ChallengeCompletionHero(calculation: ChallengeCalculationResult) {
    val isExceeded = calculation.status == ChallengeStatus.EXCEEDED
    val currentLocale = currentLocale()
    Card(
        modifier = Modifier.fillMaxWidth().testTag(CHALLENGES_TAG_COMPLETION_CELEBRATION),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(SpacingMd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingMd),
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(ChallengeCompletionIconSize),
                )
                Column(verticalArrangement = Arrangement.spacedBy(SpacingXs)) {
                    Text(
                        text =
                            stringResource(
                                if (isExceeded) {
                                    R.string.challenges_completion_exceeded_title
                                } else {
                                    R.string.challenges_completion_title
                                },
                            ),
                        style = typography.titleMedium,
                        color = colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text =
                            stringResource(
                                R.string.challenges_completion_summary,
                                ChallengeQuantity.format(calculation.completedTotal, currentLocale),
                                ChallengeQuantity.format(calculation.plannedTotal, currentLocale),
                            ),
                        style = typography.bodyMedium,
                        color = colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}
