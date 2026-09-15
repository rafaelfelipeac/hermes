package com.rafaelfelipeac.hermes.features.pacecalculator.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.measurement.MeasurementConstants.METERS_PER_KILOMETER
import com.rafaelfelipeac.hermes.core.measurement.MeasurementConstants.METERS_PER_MILE
import com.rafaelfelipeac.hermes.core.strings.formatElapsedTime
import com.rafaelfelipeac.hermes.core.time.secondsToDurationParts
import com.rafaelfelipeac.hermes.core.ui.currentLocale
import com.rafaelfelipeac.hermes.features.pacecalculator.domain.PaceCalculatorMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.KILOMETERS
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.MILES
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.meters
import com.rafaelfelipeac.hermes.features.settings.presentation.distanceUnitLabel
import com.rafaelfelipeac.hermes.features.settings.presentation.paceUnitLabel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

internal fun PaceCalculatorResultUi.hasResult(): Boolean {
    return paceSecondsPerUnit != null ||
        finishTimeSeconds != null ||
        distanceMeters != null
}

private const val FIVE_K_METERS = 5_000.0
private const val TEN_K_METERS = 10_000.0
private const val FIFTEEN_K_METERS = 15_000.0
private const val HALF_MARATHON_METERS = 21_097.5
private const val MARATHON_METERS = 42_195.0
private const val FIVE_MILES = 5.0
private const val TEN_MILES = 10.0

internal data class PaceCalculatorResultLabels(
    val primary: String,
    val secondary: String,
)

@Composable
internal fun PaceCalculatorResultUi.labels(
    mode: PaceCalculatorMode,
    settingsDistanceUnit: DistanceUnit,
    settingsPaceUnit: PaceUnit,
): PaceCalculatorResultLabels {
    val currentLocale = currentLocale()

    return when (mode) {
        PaceCalculatorMode.PACE ->
            PaceCalculatorResultLabels(
                primary =
                    paceSecondsPerUnit?.let {
                        formatPaceSeconds(
                            paceSecondsPerUnit = it,
                            unitLabel = paceUnitLabel(settingsPaceUnit),
                        )
                    }.orEmpty(),
                secondary = finishTimeSeconds?.let(::formatElapsedTime).orEmpty(),
            )
        PaceCalculatorMode.TIME ->
            PaceCalculatorResultLabels(
                primary = finishTimeSeconds?.let(::formatElapsedTime).orEmpty(),
                secondary =
                    distanceMeters?.let {
                        formatDistance(
                            distanceMeters = it,
                            distanceUnitMeters = settingsDistanceUnit.meters(),
                            unitLabel = distanceUnitLabel(settingsDistanceUnit),
                            locale = currentLocale,
                        )
                    }.orEmpty(),
            )
        PaceCalculatorMode.DISTANCE ->
            PaceCalculatorResultLabels(
                primary =
                    distanceMeters?.let {
                        formatDistance(
                            distanceMeters = it,
                            distanceUnitMeters = settingsDistanceUnit.meters(),
                            unitLabel = distanceUnitLabel(settingsDistanceUnit),
                            locale = currentLocale,
                        )
                    }.orEmpty(),
                secondary = timeSeconds?.let(::formatElapsedTime).orEmpty(),
            )
    }
}

internal data class PaceDistancePreset(
    val labelRes: Int,
    val valueMeters: Double,
)

internal fun paceDistancePresets(distanceUnit: DistanceUnit): List<PaceDistancePreset> {
    val halfMarathon =
        PaceDistancePreset(
            R.string.pace_calculator_preset_half_marathon,
            HALF_MARATHON_METERS,
        )
    val marathon = PaceDistancePreset(R.string.pace_calculator_preset_marathon, MARATHON_METERS)
    return when (distanceUnit) {
        KILOMETERS ->
            listOf(
                PaceDistancePreset(R.string.pace_calculator_preset_1_km, METERS_PER_KILOMETER),
                PaceDistancePreset(R.string.pace_calculator_preset_5k, FIVE_K_METERS),
                PaceDistancePreset(R.string.pace_calculator_preset_10k, TEN_K_METERS),
                PaceDistancePreset(R.string.pace_calculator_preset_15k, FIFTEEN_K_METERS),
                halfMarathon,
                marathon,
            )

        MILES ->
            listOf(
                PaceDistancePreset(R.string.pace_calculator_preset_1_mile, METERS_PER_MILE),
                PaceDistancePreset(
                    R.string.pace_calculator_preset_5_miles,
                    FIVE_MILES * METERS_PER_MILE,
                ),
                PaceDistancePreset(
                    R.string.pace_calculator_preset_10_miles,
                    TEN_MILES * METERS_PER_MILE,
                ),
                halfMarathon,
                marathon,
            )
    }
}

internal fun formatDistance(
    distanceMeters: Double,
    distanceUnitMeters: Double,
    unitLabel: String,
    locale: Locale,
): String {
    val value = distanceMeters / distanceUnitMeters
    val formatter = NumberFormat.getNumberInstance(locale)
    formatter.minimumFractionDigits = if (value % 1.0 == 0.0) 0 else 1
    formatter.maximumFractionDigits = 2
    return "${formatter.format(value)} $unitLabel"
}

internal fun formatPaceSeconds(
    paceSecondsPerUnit: Double,
    unitLabel: String,
): String {
    val totalSeconds = paceSecondsPerUnit.roundToLong()
    val (_, minutes, seconds) = secondsToDurationParts(totalSeconds)
    return "%d:%02d %s".format(minutes, seconds, unitLabel)
}

@Composable
internal fun paceCalculatorModeLabel(mode: PaceCalculatorMode): String {
    return when (mode) {
        PaceCalculatorMode.PACE -> stringResource(R.string.pace_calculator_mode_pace)
        PaceCalculatorMode.TIME -> stringResource(R.string.pace_calculator_mode_time)
        PaceCalculatorMode.DISTANCE -> stringResource(R.string.pace_calculator_mode_distance)
    }
}

@Composable
internal fun paceCalculatorResultLabel(mode: PaceCalculatorMode): String {
    return when (mode) {
        PaceCalculatorMode.PACE -> stringResource(R.string.pace_calculator_result_pace)
        PaceCalculatorMode.TIME -> stringResource(R.string.pace_calculator_result_time)
        PaceCalculatorMode.DISTANCE -> stringResource(R.string.pace_calculator_result_distance)
    }
}

internal fun paceCalculatorModeTestTag(mode: PaceCalculatorMode): String {
    return when (mode) {
        PaceCalculatorMode.PACE -> PACE_CALCULATOR_MODE_PACE_TAG
        PaceCalculatorMode.TIME -> PACE_CALCULATOR_MODE_TIME_TAG
        PaceCalculatorMode.DISTANCE -> PACE_CALCULATOR_MODE_DISTANCE_TAG
    }
}

internal fun formatDistanceInput(
    meters: Double,
    distanceUnit: DistanceUnit,
    locale: Locale,
): String {
    return NumberFormat.getNumberInstance(locale).format(meters / distanceUnit.meters())
}
