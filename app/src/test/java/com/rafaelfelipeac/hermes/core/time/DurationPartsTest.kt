package com.rafaelfelipeac.hermes.core.time

import org.junit.Assert.assertEquals
import org.junit.Test

class DurationPartsTest {
    @Test
    fun secondsToDurationParts_clampsNegativeDurationsToZero() {
        assertEquals(DurationParts(), secondsToDurationParts(-1L))
    }

    @Test
    fun secondsToDurationParts_splitsMinuteAndHourBoundaries() {
        assertEquals(DurationParts(seconds = 59), secondsToDurationParts(59L))
        assertEquals(DurationParts(minutes = 1), secondsToDurationParts(60L))
        assertEquals(DurationParts(minutes = 59, seconds = 59), secondsToDurationParts(3_599L))
        assertEquals(DurationParts(hours = 1), secondsToDurationParts(3_600L))
    }

    @Test
    fun durationPartsToSeconds_combinesParts() {
        assertEquals(3_723L, durationPartsToSeconds(hours = 1L, minutes = 2L, seconds = 3L))
    }
}
