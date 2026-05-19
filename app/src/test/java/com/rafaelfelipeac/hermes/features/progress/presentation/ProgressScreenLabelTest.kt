package com.rafaelfelipeac.hermes.features.progress.presentation

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class ProgressScreenLabelTest {
    @Test
    fun chartLabel_usesShortMonthAndDayInEnglish() {
        val week =
            ProgressWeekBarUi(
                weekStartDate = LocalDate.of(2026, 5, 4),
                plannedWorkouts = 1,
                completedWorkouts = 0,
                completionPercent = 0,
                isCurrentWeek = false,
            )

        val label = week.chartLabel(Locale.US)

        assertEquals("May 4", label)
    }

    @Test
    fun chartLabel_usesShortMonthAndDayInPortugueseBrazil() {
        val week =
            ProgressWeekBarUi(
                weekStartDate = LocalDate.of(2026, 5, 4),
                plannedWorkouts = 1,
                completedWorkouts = 0,
                completionPercent = 0,
                isCurrentWeek = false,
            )

        val label = week.chartLabel(Locale.forLanguageTag("pt-BR"))

        assertEquals("mai. 4", label)
    }
}
