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
