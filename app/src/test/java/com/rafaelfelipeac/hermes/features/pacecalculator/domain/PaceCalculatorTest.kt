package com.rafaelfelipeac.hermes.features.pacecalculator.domain

import com.rafaelfelipeac.hermes.core.measurement.MeasurementConstants.METERS_PER_KILOMETER
import org.junit.Assert.assertEquals
import org.junit.Test

class PaceCalculatorTest {
    @Test
    fun calculatePaceSecondsPerUnit_returnsPaceForDistanceAndTime() {
        val paceSeconds =
            PaceCalculator.calculatePaceSecondsPerUnit(
                distanceMeters = 10_000.0,
                timeSeconds = 3_600.0,
                paceUnitDistanceMeters = METERS_PER_KILOMETER,
            )

        assertEquals(360.0, paceSeconds, 0.000001)
    }

    @Test
    fun calculateFinishTimeSeconds_returnsTimeForDistanceAndPace() {
        val finishTimeSeconds =
            PaceCalculator.calculateFinishTimeSeconds(
                distanceMeters = 21_097.5,
                paceSecondsPerUnit = 300.0,
                paceUnitDistanceMeters = METERS_PER_KILOMETER,
            )

        assertEquals(6_329.25, finishTimeSeconds, 0.000001)
    }

    @Test
    fun calculateDistanceMeters_returnsDistanceForTimeAndPace() {
        val distanceMeters =
            PaceCalculator.calculateDistanceMeters(
                timeSeconds = 3_600.0,
                paceSecondsPerUnit = 360.0,
                paceUnitDistanceMeters = METERS_PER_KILOMETER,
            )

        assertEquals(10_000.0, distanceMeters, 0.000001)
    }
}
