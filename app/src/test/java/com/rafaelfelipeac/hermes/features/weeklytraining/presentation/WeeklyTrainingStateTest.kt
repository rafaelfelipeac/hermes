package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek.MONDAY
import java.time.LocalDate

class WeeklyTrainingStateTest {
    @Test
    fun dayIndicators_useLastItemForDay() {
        val weekStart = LocalDate.of(2026, 2, 9)
        val workouts =
            listOf(
                WorkoutUi(
                    id = 1L,
                    dayOfWeek = MONDAY,
                    type = "Run",
                    description = "Easy",
                    isCompleted = false,
                    isRestDay = false,
                    categoryId = 2L,
                    categoryColorId = "run",
                    categoryName = "Run",
                    order = 0,
                ),
                WorkoutUi(
                    id = 2L,
                    dayOfWeek = MONDAY,
                    type = "Strength",
                    description = "Gym",
                    isCompleted = true,
                    isRestDay = false,
                    categoryId = 4L,
                    categoryColorId = "strength",
                    categoryName = "Strength",
                    order = 1,
                ),
            )

        val state =
            WeeklyTrainingState(
                selectedDate = weekStart,
                weekStartDate = weekStart,
                workouts = workouts,
                isWeekLoaded = true,
                categories = emptyList(),
            )

        val indicatorWorkout = state.dayIndicators[MONDAY]

        assertEquals(2L, indicatorWorkout?.workout?.id)
        assertEquals(false, indicatorWorkout?.isDayCompleted)
    }
}
