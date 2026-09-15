package com.rafaelfelipeac.hermes.features.pacecalculator.presentation

import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.measurement.MeasurementConstants.METERS_PER_KILOMETER
import com.rafaelfelipeac.hermes.features.pacecalculator.domain.PaceCalculatorMode.PACE
import com.rafaelfelipeac.hermes.features.pacecalculator.domain.PaceCalculatorMode.TIME
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PaceCalculatorInputTest {
    @Test
    fun parsePaceCalculatorDecimal_acceptsDotAndComma() {
        val dotValue = parsePaceCalculatorDecimal("1.609")
        val commaValue = parsePaceCalculatorDecimal("1,609")

        assertNotNull(dotValue)
        assertNotNull(commaValue)
        assertEquals(1.609, dotValue ?: Double.NaN, TOLERANCE)
        assertEquals(1.609, commaValue ?: Double.NaN, TOLERANCE)
    }

    @Test
    fun calculatePaceCalculatorResult_acceptsLocalizedPresetValue() {
        val result =
            calculatePaceCalculatorResult(
                input(
                    mode = PACE,
                    distanceText = "1,609",
                    timeMinutesText = "8",
                ),
            )

        val paceSecondsPerUnit = result.paceSecondsPerUnit
        assertNotNull(paceSecondsPerUnit)
        assertEquals(298.322, paceSecondsPerUnit ?: Double.NaN, 0.01)
    }

    @Test
    fun calculatePaceCalculatorResult_roundsFinishTime() {
        val result =
            calculatePaceCalculatorResult(
                input(
                    mode = TIME,
                    distanceText = "1.001",
                    paceMinutesText = "5",
                ),
            )

        assertEquals(300L, result.finishTimeSeconds)
    }

    @Test
    fun calculatePaceCalculatorResult_rejectsNegativeDistance() {
        val result =
            calculatePaceCalculatorResult(
                input(
                    mode = PACE,
                    distanceText = "-5",
                    timeMinutesText = "30",
                ),
            )

        assertNull(result.paceSecondsPerUnit)
    }

    @Test
    fun calculatePaceCalculatorResult_rejectsOutOfRangeTimeParts() {
        val result =
            calculatePaceCalculatorResult(
                input(
                    mode = PACE,
                    distanceText = "5",
                    timeMinutesText = OUT_OF_RANGE_SECONDS_TEXT,
                ),
            )

        assertNull(result.paceSecondsPerUnit)
    }

    @Test
    fun validWholeNumberInput_rejectsImpossibleValues() {
        assertEquals(true, validWholeNumberInput(MAX_SECONDS_TEXT, MAX_SECONDS_OR_MINUTES))
        assertEquals(false, validWholeNumberInput(OUT_OF_RANGE_SECONDS_TEXT, MAX_SECONDS_OR_MINUTES))
        assertEquals(false, validWholeNumberInput("33333", MAX_TIME_HOURS.toInt()))
    }

    @Test
    fun sanitizedWholeNumberInput_preservesValidLeadingZero() {
        assertEquals("09", sanitizedWholeNumberInput("09", MAX_SECONDS_OR_MINUTES))
        assertEquals("04", sanitizedWholeNumberInput("04", MAX_SECONDS_OR_MINUTES))
        assertEquals("00", sanitizedWholeNumberInput("00", MAX_SECONDS_OR_MINUTES))
        assertNull(sanitizedWholeNumberInput("0666666", MAX_SECONDS_OR_MINUTES))
    }

    @Test
    fun validDistanceInput_limitsPrecisionAndMagnitude() {
        assertEquals(true, validDistanceInput("42.195"))
        assertEquals(true, validDistanceInput("13,109"))
        assertEquals(false, validDistanceInput("10000"))
        assertEquals(false, validDistanceInput("5.1234"))
    }

    private fun input(
        mode: com.rafaelfelipeac.hermes.features.pacecalculator.domain.PaceCalculatorMode,
        distanceText: String,
        timeMinutesText: String = EMPTY,
        paceMinutesText: String = EMPTY,
    ) = PaceCalculatorInput(
        mode = mode,
        distanceText = distanceText,
        timeHoursText = EMPTY,
        timeMinutesText = timeMinutesText,
        timeSecondsText = EMPTY,
        paceMinutesText = paceMinutesText,
        paceSecondsText = EMPTY,
        paceUnitMeters = METERS_PER_KILOMETER,
        distanceUnitMeters = METERS_PER_KILOMETER,
    )

    private companion object {
        const val TOLERANCE = 0.000001
        const val MAX_SECONDS_OR_MINUTES = 59
        const val MAX_SECONDS_TEXT = "59"
        const val OUT_OF_RANGE_SECONDS_TEXT = "60"
    }
}
