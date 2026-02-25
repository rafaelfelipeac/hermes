package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.AFTERNOON
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.MORNING
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.NIGHT
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

    @Test
    fun dayIndicators_useVisualLastItemWhenSlotsArePresent() {
        val weekStart = LocalDate.of(2026, 2, 9)
        val workouts =
            listOf(
                WorkoutUi(
                    id = 1L,
                    dayOfWeek = MONDAY,
                    type = "Run",
                    description = "Morning run",
                    isCompleted = false,
                    isRestDay = false,
                    categoryId = 2L,
                    categoryColorId = "run",
                    categoryName = "Run",
                    order = 2,
                    timeSlot = MORNING,
                ),
                WorkoutUi(
                    id = 2L,
                    dayOfWeek = MONDAY,
                    type = "Gym",
                    description = "Afternoon",
                    isCompleted = false,
                    isRestDay = false,
                    categoryId = 3L,
                    categoryColorId = "strength",
                    categoryName = "Strength",
                    order = 0,
                    timeSlot = AFTERNOON,
                ),
                WorkoutUi(
                    id = 3L,
                    dayOfWeek = MONDAY,
                    type = "Yoga",
                    description = "Night",
                    isCompleted = false,
                    isRestDay = false,
                    categoryId = 4L,
                    categoryColorId = "mobility",
                    categoryName = "Mobility",
                    order = 0,
                    timeSlot = NIGHT,
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

        assertEquals(3L, indicatorWorkout?.workout?.id)
    }

    @Test
    fun dayIndicators_preferWorkoutOwnerWhenDayHasMixedEvents() {
        val weekStart = LocalDate.of(2026, 2, 9)
        val workouts =
            listOf(
                WorkoutUi(
                    id = 1L,
                    dayOfWeek = MONDAY,
                    type = "Run",
                    description = "Morning run",
                    isCompleted = false,
                    isRestDay = false,
                    categoryId = 2L,
                    categoryColorId = "run",
                    categoryName = "Run",
                    order = 1,
                    timeSlot = MORNING,
                ),
                WorkoutUi(
                    id = 2L,
                    dayOfWeek = MONDAY,
                    type = "Strength",
                    description = "Night workout",
                    isCompleted = false,
                    isRestDay = false,
                    categoryId = 3L,
                    categoryColorId = "strength",
                    categoryName = "Strength",
                    order = 0,
                    timeSlot = NIGHT,
                ),
                WorkoutUi(
                    id = 3L,
                    dayOfWeek = MONDAY,
                    type = "",
                    description = "",
                    isCompleted = false,
                    isRestDay = true,
                    categoryId = null,
                    categoryColorId = null,
                    categoryName = null,
                    order = 2,
                    timeSlot = NIGHT,
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
    }

    @Test
    fun dayIndicators_whenWorkoutOrderTies_useLastVisibleWorkout() {
        val weekStart = LocalDate.of(2026, 2, 9)
        val workouts =
            listOf(
                WorkoutUi(
                    id = 10L,
                    dayOfWeek = MONDAY,
                    type = "Swim",
                    description = "Morning",
                    isCompleted = false,
                    isRestDay = false,
                    categoryId = 2L,
                    categoryColorId = "swim",
                    categoryName = "Swim",
                    order = 0,
                    timeSlot = MORNING,
                ),
                WorkoutUi(
                    id = 11L,
                    dayOfWeek = MONDAY,
                    type = "Cycling",
                    description = "Morning",
                    isCompleted = true,
                    isRestDay = false,
                    categoryId = 3L,
                    categoryColorId = "cycling",
                    categoryName = "Cycling",
                    order = 0,
                    timeSlot = MORNING,
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

        assertEquals(11L, indicatorWorkout?.workout?.id)
    }
}
