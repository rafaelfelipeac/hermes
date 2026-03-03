package com.rafaelfelipeac.hermes.features.weeklytraining.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDate

class WeekDateUtilsTest {
    @Test
    fun weekStart_alignsToConfiguredDay() {
        val date = LocalDate.of(2026, 3, 1)

        assertEquals(LocalDate.of(2026, 2, 23), weekStart(date, MONDAY))
        assertEquals(LocalDate.of(2026, 2, 25), weekStart(date, WEDNESDAY))
        assertEquals(LocalDate.of(2026, 2, 28), weekStart(date, SATURDAY))
    }

    @Test
    fun storageWeekStartsForDisplayWeek_returnsCanonicalBuckets() {
        val displayStart = LocalDate.of(2026, 3, 4)

        assertEquals(
            listOf(
                LocalDate.of(2026, 3, 2),
                LocalDate.of(2026, 3, 9),
            ),
            storageWeekStartsForDisplayWeek(displayStart),
        )
    }

    @Test
    fun orderedDays_rotatesFromConfiguredStart() {
        assertEquals(
            listOf(
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY,
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
            ),
            orderedDays(WEDNESDAY),
        )
    }
}
