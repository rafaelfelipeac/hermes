package com.rafaelfelipeac.hermes.features.challenges.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.rafaelfelipeac.hermes.core.ui.theme.CHALLENGE_FRAME_ALPHA
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderHairline
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ChallengeCategoryAccentWidth
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeCalculationResult

@Composable
internal fun ChallengeDetailSummaryCard(
    challenge: Challenge,
    category: Category?,
    calculation: ChallengeCalculationResult,
) {
    val frameColor = colorScheme.onSurface.copy(alpha = CHALLENGE_FRAME_ALPHA)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        border = BorderStroke(BorderHairline, frameColor),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clip(shapes.medium),
        ) {
            Surface(
                color = category?.let { categoryAccentColor(it.colorId) } ?: colorScheme.outlineVariant,
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(ChallengeCategoryAccentWidth),
            ) {}

            Column(
                modifier = Modifier.weight(1f).padding(SpacingXl),
                verticalArrangement = Arrangement.spacedBy(SpacingSm),
            ) {
                ChallengeSummaryContent(
                    challenge = challenge,
                    category = category,
                    calculation = calculation,
                    showProgressBar = true,
                )
            }
        }
    }
}
