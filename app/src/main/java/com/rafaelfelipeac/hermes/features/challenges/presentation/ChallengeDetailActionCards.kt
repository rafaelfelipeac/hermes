package com.rafaelfelipeac.hermes.features.challenges.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.currentLocale
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderHairline
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeCalculationResult
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuantity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuickAddValue
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeStatus
import java.util.Locale

@Composable
internal fun ChallengeTodayCard(calculation: ChallengeCalculationResult) {
    val currentLocale = currentLocale()

    Column(verticalArrangement = Arrangement.spacedBy(SpacingMd)) {
        ChallengeSurfaceCard {
            Text(
                text = stringResource(R.string.challenges_today_label),
                style = typography.titleMedium,
            )
            if (calculation.todayRemaining == null) {
                Text(
                    text =
                        stringResource(
                            if (calculation.status == ChallengeStatus.NOT_STARTED) {
                                R.string.challenges_today_not_started
                            } else {
                                R.string.challenges_today_period_ended
                            },
                        ),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        ChallengeTodayMetricCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.challenges_today_completed_label),
                            value = ChallengeQuantity.format(calculation.todayProgress, currentLocale),
                        )
                        ChallengeTodayMetricCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.challenges_today_target_label),
                            value = ChallengeQuantity.format(calculation.requiredPace, currentLocale),
                        )
                    }
                    ChallengeTodayMetricCard(
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(R.string.challenges_today_remaining_label),
                        value = ChallengeQuantity.format(calculation.todayRemaining, currentLocale),
                    )
                }
            }
        }

        ChallengeAccumulatedDelayCard(calculation, currentLocale)
    }
}

@Composable
private fun ChallengeAccumulatedDelayCard(
    calculation: ChallengeCalculationResult,
    currentLocale: Locale,
) {
    ChallengeSurfaceCard {
        Text(
            text = stringResource(R.string.challenges_debt_label),
            style = typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.challenges_accumulated_delay_explanation),
            style = typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )
        Text(
            text = ChallengeQuantity.format(calculation.carriedDebt, currentLocale),
            style = typography.headlineSmall,
            color = colorScheme.primary,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(SpacingSm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            ChallengeTodayMetricCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.challenges_expected_through_today),
                value = ChallengeQuantity.format(calculation.expectedTotal, currentLocale),
            )
            ChallengeTodayMetricCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.challenges_completed_in_challenge),
                value = ChallengeQuantity.format(calculation.completedTotal, currentLocale),
            )
        }
    }
}

@Composable
private fun ChallengeSurfaceCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(SpacingMd),
            verticalArrangement = Arrangement.spacedBy(SpacingSm),
            content = content,
        )
    }
}

@Composable
internal fun ChallengeQuickAddCard(
    quickAdds: List<ChallengeQuickAddValue>,
    onQuickAdd: (ChallengeQuickAddValue) -> Unit,
) {
    val currentLocale = currentLocale()

    Card(
        modifier = Modifier.fillMaxWidth().testTag(CHALLENGES_TAG_DETAIL_QUICK_ADD),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(SpacingMd),
            verticalArrangement = Arrangement.spacedBy(SpacingSm),
        ) {
            Text(
                text = stringResource(R.string.challenges_quick_add_title),
                style = typography.titleMedium,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                quickAdds.forEach { quickAdd ->
                    FilledTonalButton(
                        onClick = { onQuickAdd(quickAdd) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text =
                                stringResource(
                                    R.string.challenges_quick_add_button,
                                    ChallengeQuantity.format(quickAdd.quantity, currentLocale),
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeTodayMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(SpacingMd),
            verticalArrangement = Arrangement.spacedBy(SpacingXs),
        ) {
            Text(
                text = label,
                style = typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = typography.titleMedium,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
