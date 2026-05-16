package com.rafaelfelipeac.hermes.core.strings

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class RelativeDateFormatterTest {
    private val today = LocalDate.of(2026, 5, 11)
    private val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)

    @Test
    fun relativeDateText_usesRelativeLabelsAndFallback() {
        assertEquals(
            "Today",
            relativeDateText(
                date = today,
                today = today,
                todayLabel = "Today",
                tomorrowLabel = "Tomorrow",
                yesterdayLabel = "Yesterday",
                formatter = formatter,
            ),
        )
        assertEquals(
            "Tomorrow",
            relativeDateText(
                date = today.plusDays(1),
                today = today,
                todayLabel = "Today",
                tomorrowLabel = "Tomorrow",
                yesterdayLabel = "Yesterday",
                formatter = formatter,
            ),
        )
        assertEquals(
            "Yesterday",
            relativeDateText(
                date = today.minusDays(1),
                today = today,
                todayLabel = "Today",
                tomorrowLabel = "Tomorrow",
                yesterdayLabel = "Yesterday",
                formatter = formatter,
            ),
        )
        assertEquals(
            "May 14",
            relativeDateText(
                date = today.plusDays(3),
                today = today,
                todayLabel = "Today",
                tomorrowLabel = "Tomorrow",
                yesterdayLabel = "Yesterday",
                formatter = formatter,
            ),
        )
    }

    @Test
    fun relativeDaysUntilText_usesRelativeLabelsAndFallback() {
        assertEquals(
            "Today",
            relativeDaysUntilText(
                daysUntil = 0,
                todayLabel = "Today",
                tomorrowLabel = "Tomorrow",
                yesterdayLabel = "Yesterday",
                fallbackText = "0 days away",
            ),
        )
        assertEquals(
            "Tomorrow",
            relativeDaysUntilText(
                daysUntil = 1,
                todayLabel = "Today",
                tomorrowLabel = "Tomorrow",
                yesterdayLabel = "Yesterday",
                fallbackText = "1 days away",
            ),
        )
        assertEquals(
            "Yesterday",
            relativeDaysUntilText(
                daysUntil = -1,
                todayLabel = "Today",
                tomorrowLabel = "Tomorrow",
                yesterdayLabel = "Yesterday",
                fallbackText = "-1 days away",
            ),
        )
        assertEquals(
            "3 days away",
            relativeDaysUntilText(
                daysUntil = 3,
                todayLabel = "Today",
                tomorrowLabel = "Tomorrow",
                yesterdayLabel = "Yesterday",
                fallbackText = "3 days away",
            ),
        )
    }
}
