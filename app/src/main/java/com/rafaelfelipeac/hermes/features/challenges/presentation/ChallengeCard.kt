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
import androidx.compose.ui.platform.testTag
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderHairline
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeCalculationResult
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle

@Composable
internal fun ChallengeCard(
    challenge: Challenge,
    category: Category?,
    calculation: ChallengeCalculationResult?,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Surface(
                color = category?.let { categoryAccentColor(it.colorId) } ?: colorScheme.primary,
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(SpacingXs),
            ) {}

            Column(
                modifier = Modifier.weight(1f).padding(SpacingMd),
                verticalArrangement = Arrangement.spacedBy(SpacingSm),
            ) {
                ChallengeSummaryContent(
                    challenge = challenge,
                    category = category,
                    calculation = calculation,
                    showProgressBar = challenge.lifecycle == ChallengeLifecycle.ACTIVE,
                    modifier = Modifier.testTag(CHALLENGES_TAG_ACTIVE_CARD_PROGRESS),
                )
            }
        }
    }
}
