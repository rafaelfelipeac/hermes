package com.rafaelfelipeac.hermes.features.pacecalculator.presentation

import com.rafaelfelipeac.hermes.features.pacecalculator.domain.PaceCalculator
import com.rafaelfelipeac.hermes.features.pacecalculator.domain.PaceCalculatorMode
import kotlin.math.roundToLong

internal data class PaceCalculatorInput(
    val mode: PaceCalculatorMode,
    val distanceText: String,
    val timeHoursText: String,
    val timeMinutesText: String,
    val timeSecondsText: String,
    val paceMinutesText: String,
    val paceSecondsText: String,
    val paceUnitMeters: Double,
    val distanceUnitMeters: Double,
)

internal data class PaceCalculatorResultUi(
    val paceSecondsPerUnit: Double? = null,
    val finishTimeSeconds: Long? = null,
    val distanceMeters: Double? = null,
    val timeSeconds: Long? = null,
)

internal fun calculatePaceCalculatorResult(input: PaceCalculatorInput): PaceCalculatorResultUi {
    val distanceMeters =
        parsePaceCalculatorDecimal(input.distanceText)
            ?.times(input.distanceUnitMeters)
            ?.takeIf { it.isFinite() && it > 0.0 }
    val timeSeconds =
        durationSeconds(
            hoursText = input.timeHoursText,
            minutesText = input.timeMinutesText,
            secondsText = input.timeSecondsText,
        )
    val paceSecondsPerUnit =
        durationSeconds(
            hoursText = null,
            minutesText = input.paceMinutesText,
            secondsText = input.paceSecondsText,
        )

    return when (input.mode) {
        PaceCalculatorMode.PACE -> calculatePace(input, distanceMeters, timeSeconds)
        PaceCalculatorMode.TIME -> calculateTime(input, distanceMeters, paceSecondsPerUnit)
        PaceCalculatorMode.DISTANCE -> calculateDistance(input, timeSeconds, paceSecondsPerUnit)
    }
}

private fun calculatePace(
    input: PaceCalculatorInput,
    distanceMeters: Double?,
    timeSeconds: Long,
): PaceCalculatorResultUi {
    if (distanceMeters == null || timeSeconds <= 0L) return PaceCalculatorResultUi()

    val paceSeconds =
        PaceCalculator.calculatePaceSecondsPerUnit(
            distanceMeters = distanceMeters,
            timeSeconds = timeSeconds.toDouble(),
            paceUnitDistanceMeters = input.paceUnitMeters,
        )
    return PaceCalculatorResultUi(
        paceSecondsPerUnit = paceSeconds,
        finishTimeSeconds = timeSeconds,
    )
}

private fun calculateTime(
    input: PaceCalculatorInput,
    distanceMeters: Double?,
    paceSecondsPerUnit: Long,
): PaceCalculatorResultUi {
    if (distanceMeters == null || paceSecondsPerUnit <= 0L) return PaceCalculatorResultUi()

    val finishSeconds =
        PaceCalculator.calculateFinishTimeSeconds(
            distanceMeters = distanceMeters,
            paceSecondsPerUnit = paceSecondsPerUnit.toDouble(),
            paceUnitDistanceMeters = input.paceUnitMeters,
        )
    return PaceCalculatorResultUi(
        finishTimeSeconds = finishSeconds.roundToLong(),
        distanceMeters = distanceMeters,
    )
}

private fun calculateDistance(
    input: PaceCalculatorInput,
    timeSeconds: Long,
    paceSecondsPerUnit: Long,
): PaceCalculatorResultUi {
    if (paceSecondsPerUnit <= 0L || timeSeconds <= 0L) return PaceCalculatorResultUi()

    val distance =
        PaceCalculator.calculateDistanceMeters(
            timeSeconds = timeSeconds.toDouble(),
            paceSecondsPerUnit = paceSecondsPerUnit.toDouble(),
            paceUnitDistanceMeters = input.paceUnitMeters,
        )
    return PaceCalculatorResultUi(
        distanceMeters = distance,
        timeSeconds = timeSeconds,
    )
}

internal fun parsePaceCalculatorDecimal(valueText: String): Double? {
    return valueText
        .trim()
        .replace(',', '.')
        .toDoubleOrNull()
}

private fun durationSeconds(
    hoursText: String?,
    minutesText: String,
    secondsText: String,
): Long {
    val hours = hoursText?.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
    val minutes = minutesText.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
    val seconds = secondsText.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
    return hours * 3600L + minutes * 60L + seconds
}
