package com.rafaelfelipeac.hermes.core.ui.components.calendar

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class WeeklyCalendarHeaderFormattingTest {
    @Test
    fun formatWeekRange_handlesSameMonthSameYearAndCrossYearRanges() {
        assertEquals(
            "12-18 Jan 2026",
            formatWeekRange(
                start = LocalDate.of(2026, 1, 12),
                end = LocalDate.of(2026, 1, 18),
                locale = Locale.US,
            ),
        )
        assertEquals(
            "30 Jan - 5 Feb 2026",
            formatWeekRange(
                start = LocalDate.of(2026, 1, 30),
                end = LocalDate.of(2026, 2, 5),
                locale = Locale.US,
            ),
        )
        assertEquals(
            "29 Dec 2026 - 4 Jan 2027",
            formatWeekRange(
                start = LocalDate.of(2026, 12, 29),
                end = LocalDate.of(2027, 1, 4),
                locale = Locale.US,
            ),
        )
    }
}
