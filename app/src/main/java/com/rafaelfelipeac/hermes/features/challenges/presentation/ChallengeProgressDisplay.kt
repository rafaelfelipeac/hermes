package com.rafaelfelipeac.hermes.features.challenges.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.currentLocale
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ChallengeProgressBarHeight
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeCalculationResult
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuantity
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun challengeProgressLabel(calculation: ChallengeCalculationResult): String {
    val locale = currentLocale()
    val progressValue =
        stringResource(
            R.string.challenges_progress_value,
            ChallengeQuantity.format(calculation.completedTotal, locale),
            ChallengeQuantity.format(calculation.plannedTotal, locale),
        )
    val progressPercent =
        stringResource(
            R.string.challenges_progress_percent,
            challengeProgressPercent(calculation, locale),
        )
    return stringResource(R.string.challenges_progress_value_with_percent, progressValue, progressPercent)
}

internal fun challengeProgressPercent(
    calculation: ChallengeCalculationResult,
    locale: Locale,
): String {
    if (calculation.plannedTotal <= 0L) return formatChallengeProgressPercent(0.0, locale)
    val exactPercent = calculation.completedTotal.toDouble() / calculation.plannedTotal.toDouble() * 100.0
    val roundedPercent = (exactPercent * 10.0).roundToInt() / 10.0
    val displayPercent =
        if (calculation.completedTotal < calculation.plannedTotal && roundedPercent >= 100.0) 99.9 else roundedPercent
    return formatChallengeProgressPercent(displayPercent, locale)
}

private fun formatChallengeProgressPercent(
    value: Double,
    locale: Locale,
): String =
    NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }.format(value)

@Composable
internal fun ChallengeProgressBar(
    progress: Double,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(ChallengeProgressBarHeight)
                .clip(shapes.small)
                .background(colorScheme.surfaceVariant),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(progress.toFloat().coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(color),
        )
    }
}
