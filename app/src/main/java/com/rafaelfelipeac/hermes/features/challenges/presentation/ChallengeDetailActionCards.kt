package com.rafaelfelipeac.hermes.features.challenges.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
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

@Composable
internal fun ChallengeTodayCard(calculation: ChallengeCalculationResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerLow),
        border = BorderStroke(BorderHairline, colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(SpacingMd),
            verticalArrangement = Arrangement.spacedBy(SpacingSm),
        ) {
            val currentLocale = currentLocale()

            Text(
                text = stringResource(R.string.challenges_today_label),
                style = typography.titleMedium,
            )
            Text(
                text =
                    if (calculation.todayTarget != null) {
                        stringResource(
                            R.string.challenges_today_value_daily,
                            ChallengeQuantity.format(calculation.todayProgress, currentLocale),
                            ChallengeQuantity.format(calculation.todayRemaining ?: 0L, currentLocale),
                        )
                    } else {
                        stringResource(
                            R.string.challenges_today_value_total,
                            ChallengeQuantity.format(calculation.todayProgress, currentLocale),
                            ChallengeQuantity.format(calculation.todayRemaining ?: 0L, currentLocale),
                        )
                    },
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
            Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
                Row(horizontalArrangement = Arrangement.spacedBy(SpacingSm), modifier = Modifier.fillMaxWidth()) {
                    ChallengeTodayMetricCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.challenges_today_completed_label),
                        value = ChallengeQuantity.format(calculation.todayProgress, currentLocale),
                        containerColor = colorScheme.surfaceVariant,
                        contentColor = colorScheme.onSurfaceVariant,
                    )
                    ChallengeTodayMetricCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.challenges_today_remaining_label),
                        value = ChallengeQuantity.format(calculation.todayRemaining ?: 0L, currentLocale),
                        containerColor = colorScheme.surfaceVariant,
                        contentColor = colorScheme.onSurfaceVariant,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(SpacingSm), modifier = Modifier.fillMaxWidth()) {
                    ChallengeTodayMetricCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.challenges_required_pace_label),
                        value = ChallengeQuantity.format(calculation.requiredPace, currentLocale),
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer,
                    )
                    ChallengeTodayMetricCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.challenges_debt_label),
                        value = ChallengeQuantity.format(calculation.carriedDebt, currentLocale),
                        containerColor = colorScheme.secondaryContainer,
                        contentColor = colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
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
    containerColor: Color,
    contentColor: Color,
) {
    Card(
        modifier = modifier,
        shape = shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
    ) {
        Column(
            modifier = Modifier.padding(SpacingMd),
            verticalArrangement = Arrangement.spacedBy(SpacingXs),
        ) {
            Text(
                text = label,
                style = typography.labelSmall,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = typography.titleMedium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
