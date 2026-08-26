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
            maxHours = MAX_TIME_HOURS,
        )
    val paceSecondsPerUnit =
        durationSeconds(
            hoursText = null,
            minutesText = input.paceMinutesText,
            secondsText = input.paceSecondsText,
            maxHours = 0L,
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
    timeSeconds: Long?,
): PaceCalculatorResultUi {
    if (distanceMeters == null || timeSeconds == null || timeSeconds <= 0L) return PaceCalculatorResultUi()

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
    paceSecondsPerUnit: Long?,
): PaceCalculatorResultUi {
    if (distanceMeters == null || paceSecondsPerUnit == null || paceSecondsPerUnit <= 0L) {
        return PaceCalculatorResultUi()
    }

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
    timeSeconds: Long?,
    paceSecondsPerUnit: Long?,
): PaceCalculatorResultUi {
    if (paceSecondsPerUnit == null || timeSeconds == null) {
        return PaceCalculatorResultUi()
    }
    return if (paceSecondsPerUnit <= 0L || timeSeconds <= 0L) {
        PaceCalculatorResultUi()
    } else {
        val distance =
            PaceCalculator.calculateDistanceMeters(
                timeSeconds = timeSeconds.toDouble(),
                paceSecondsPerUnit = paceSecondsPerUnit.toDouble(),
                paceUnitDistanceMeters = input.paceUnitMeters,
            )
        PaceCalculatorResultUi(
            distanceMeters = distance,
            timeSeconds = timeSeconds,
        )
    }
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
    maxHours: Long,
): Long? {
    val hours = hoursText?.toLongOrNull() ?: 0L
    val minutes = minutesText.toLongOrNull() ?: 0L
    val seconds = secondsText.toLongOrNull() ?: 0L
    if (hours !in 0L..maxHours || minutes !in 0L..MAX_MINUTES_OR_SECONDS || seconds !in 0L..MAX_MINUTES_OR_SECONDS) {
        return null
    }
    return hours * 3600L + minutes * 60L + seconds
}

internal fun validWholeNumberInput(
    value: String,
    maxValue: Int,
    maxDigits: Int = maxValue.toString().length,
): Boolean =
    value.isEmpty() ||
        (value.length <= maxDigits && value.all(Char::isDigit) && value.toIntOrNull()?.let { it <= maxValue } == true)

internal fun sanitizedWholeNumberInput(
    value: String,
    maxValue: Int,
): String? = value.takeIf { validWholeNumberInput(it, maxValue) }

internal fun validDistanceInput(value: String): Boolean {
    if (value.isEmpty()) return true
    val normalized = value.replace(',', '.')
    val parts = normalized.split('.', limit = 2)
    val hasValidStructure =
        normalized.count { it == '.' } <= 1 &&
            parts.first().length <= MAX_DISTANCE_WHOLE_DIGITS &&
            parts.getOrNull(1)?.length.orZero() <= MAX_DISTANCE_DECIMALS &&
            normalized.all { it.isDigit() || it == '.' }
    val hasValidValue =
        (normalized.toDoubleOrNull()?.let { it <= MAX_DISTANCE_VALUE } ?: normalized.endsWith('.'))
    return hasValidStructure && hasValidValue
}

private fun Int?.orZero(): Int = this ?: 0

internal const val MAX_TIME_HOURS = 999L
internal const val MAX_MINUTES_OR_SECONDS = 59L
internal const val MAX_PACE_MINUTES = 99
private const val MAX_DISTANCE_VALUE = 9_999.999
private const val MAX_DISTANCE_WHOLE_DIGITS = 4
private const val MAX_DISTANCE_DECIMALS = 3
