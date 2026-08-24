package com.rafaelfelipeac.hermes.features.pacecalculator.presentation

import com.rafaelfelipeac.hermes.features.pacecalculator.domain.PaceCalculatorMode.PACE
import com.rafaelfelipeac.hermes.features.pacecalculator.domain.PaceCalculatorMode.TIME
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PaceCalculatorInputTest {
    @Test
    fun parsePaceCalculatorDecimal_acceptsDotAndComma() {
        assertEquals(1.609, parsePaceCalculatorDecimal("1.609")!!, TOLERANCE)
        assertEquals(1.609, parsePaceCalculatorDecimal("1,609")!!, TOLERANCE)
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

        assertEquals(298.322, result.paceSecondsPerUnit!!, 0.01)
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
                    timeMinutesText = "60",
                ),
            )

        assertNull(result.paceSecondsPerUnit)
    }

    @Test
    fun validWholeNumberInput_rejectsImpossibleValues() {
        assertEquals(true, validWholeNumberInput("59", 59))
        assertEquals(false, validWholeNumberInput("60", 59))
        assertEquals(false, validWholeNumberInput("33333", MAX_TIME_HOURS.toInt()))
    }

    @Test
    fun sanitizedWholeNumberInput_preservesValidLeadingZero() {
        assertEquals("09", sanitizedWholeNumberInput("09", 59))
        assertEquals("04", sanitizedWholeNumberInput("04", 59))
        assertEquals("00", sanitizedWholeNumberInput("00", 59))
        assertNull(sanitizedWholeNumberInput("0666666", 59))
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
        timeMinutesText: String = "",
        paceMinutesText: String = "",
    ) = PaceCalculatorInput(
        mode = mode,
        distanceText = distanceText,
        timeHoursText = "",
        timeMinutesText = timeMinutesText,
        timeSecondsText = "",
        paceMinutesText = paceMinutesText,
        paceSecondsText = "",
        paceUnitMeters = 1_000.0,
        distanceUnitMeters = 1_000.0,
    )

    private companion object {
        const val TOLERANCE = 0.000001
    }
}
