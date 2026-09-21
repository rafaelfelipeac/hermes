package com.rafaelfelipeac.hermes.features.challenges.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderHairline
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeCalculationResult

@Composable
internal fun ChallengeDetailSummaryCard(
    challenge: Challenge,
    category: Category?,
    calculation: ChallengeCalculationResult,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        border = BorderStroke(BorderHairline, challengeFrameColor()),
    ) {
        ChallengeSummarySurface(
            challenge = challenge,
            category = category,
            calculation = calculation,
            showProgressBar = true,
        )
    }
}
