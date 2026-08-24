package com.rafaelfelipeac.hermes.core.strings

import org.junit.Assert.assertEquals
import org.junit.Test

class ElapsedTimeFormatterTest {
    @Test
    fun formatElapsedTime_usesMinutesAndPaddedSecondsBelowOneHour() {
        assertEquals("5:09", formatElapsedTime(309L))
        assertEquals("55:19", formatElapsedTime(3_319L))
    }

    @Test
    fun formatElapsedTime_usesHoursAndPaddedMinutesAndSecondsFromOneHour() {
        assertEquals("1:05:09", formatElapsedTime(3_909L))
        assertEquals("12:00:01", formatElapsedTime(43_201L))
    }
}
