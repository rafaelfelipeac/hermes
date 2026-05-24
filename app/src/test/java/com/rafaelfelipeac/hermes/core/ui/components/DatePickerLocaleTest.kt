package com.rafaelfelipeac.hermes.core.ui.components

import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.temporal.WeekFields
import java.util.Locale

class DatePickerLocaleTest {
    @Test
    fun withWeekStartDayMapsToExpectedFirstDayOfWeek() {
        val baseLocale = Locale.US

        WeekStartDay.entries.forEach { weekStartDay ->
            val locale = baseLocale.withWeekStartDay(weekStartDay)
            assertEquals(
                weekStartDay.dayOfWeek,
                WeekFields.of(locale).firstDayOfWeek,
            )
        }
    }

    @Test
    fun withWeekStartDayKeepsTheOriginalLocaleLanguage() {
        val locale = Locale.US.withWeekStartDay(WeekStartDay.WEDNESDAY)

        assertEquals("en", locale.language)
        assertEquals("US", locale.country)
    }
}
