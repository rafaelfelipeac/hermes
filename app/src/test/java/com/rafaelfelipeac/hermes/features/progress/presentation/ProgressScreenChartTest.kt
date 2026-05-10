package com.rafaelfelipeac.hermes.features.progress.presentation

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ProgressScreenChartTest {
    @Test
    fun plannedFraction_scalesRelativeToMaxPlanned() {
        val week =
            ProgressWeekBarUi(
                weekStartDate = LocalDate.of(2026, 5, 4),
                plannedWorkouts = 5,
                completedWorkouts = 4,
                completionPercent = 80,
                isCurrentWeek = false,
            )

        assertEquals(0.5f, week.plannedFraction(maxPlannedWorkouts = 10), 0.0001f)
    }

    @Test
    fun plannedFraction_returnsZeroWhenNoWorkoutsArePlanned() {
        val week =
            ProgressWeekBarUi(
                weekStartDate = LocalDate.of(2026, 5, 4),
                plannedWorkouts = 0,
                completedWorkouts = 0,
                completionPercent = 0,
                isCurrentWeek = false,
            )

        assertEquals(0f, week.plannedFraction(maxPlannedWorkouts = 0), 0.0001f)
    }

    @Test
    fun completedFraction_scalesCompletedWorkoutsInsidePlannedBar() {
        val week =
            ProgressWeekBarUi(
                weekStartDate = LocalDate.of(2026, 5, 4),
                plannedWorkouts = 10,
                completedWorkouts = 9,
                completionPercent = 90,
                isCurrentWeek = true,
            )

        assertEquals(0.9f, week.completedFraction(), 0.0001f)
    }

    @Test
    fun completedFraction_clampsAbovePlannedCount() {
        val week =
            ProgressWeekBarUi(
                weekStartDate = LocalDate.of(2026, 5, 4),
                plannedWorkouts = 3,
                completedWorkouts = 5,
                completionPercent = 100,
                isCurrentWeek = true,
            )

        assertEquals(1f, week.completedFraction(), 0.0001f)
    }
}
