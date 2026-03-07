package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.BUSY
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.REST
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.SICK
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.AFTERNOON
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.MORNING
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.NIGHT
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
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

    @Test
    fun weeklyHeaderSummary_countsScheduledEventsAndProgress() {
        val weekStart = LocalDate.of(2026, 2, 9)
        val workouts =
            listOf(
                WorkoutUi(
                    id = 1L,
                    dayOfWeek = MONDAY,
                    type = "Run",
                    description = "Easy",
                    isCompleted = true,
                    isRestDay = false,
                    categoryId = null,
                    categoryColorId = null,
                    categoryName = null,
                    order = 0,
                ),
                WorkoutUi(
                    id = 2L,
                    dayOfWeek = TUESDAY,
                    type = "Gym",
                    description = "Upper",
                    isCompleted = false,
                    isRestDay = false,
                    categoryId = null,
                    categoryColorId = null,
                    categoryName = null,
                    order = 0,
                ),
                WorkoutUi(
                    id = 3L,
                    dayOfWeek = WEDNESDAY,
                    type = "",
                    description = "",
                    isCompleted = false,
                    isRestDay = false,
                    categoryId = null,
                    categoryColorId = null,
                    categoryName = null,
                    order = 0,
                    eventType = REST,
                ),
                WorkoutUi(
                    id = 4L,
                    dayOfWeek = THURSDAY,
                    type = "",
                    description = "",
                    isCompleted = false,
                    isRestDay = false,
                    categoryId = null,
                    categoryColorId = null,
                    categoryName = null,
                    order = 0,
                    eventType = BUSY,
                ),
                WorkoutUi(
                    id = 5L,
                    dayOfWeek = THURSDAY,
                    type = "",
                    description = "",
                    isCompleted = false,
                    isRestDay = false,
                    categoryId = null,
                    categoryColorId = null,
                    categoryName = null,
                    order = 1,
                    eventType = SICK,
                ),
                WorkoutUi(
                    id = 6L,
                    dayOfWeek = null,
                    type = "Unplanned",
                    description = "Should not count",
                    isCompleted = true,
                    isRestDay = false,
                    categoryId = null,
                    categoryColorId = null,
                    categoryName = null,
                    order = 0,
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

        val summary = state.weeklyHeaderSummary

        assertEquals(2, summary?.plannedWorkouts)
        assertEquals(1, summary?.completedWorkouts)
        assertEquals(1, summary?.plannedRestEvents)
        assertEquals(1, summary?.plannedBusyEvents)
        assertEquals(1, summary?.plannedSickEvents)
        assertEquals(0.5f, summary?.progress)
    }

    @Test
    fun weeklyHeaderSummary_isNullWhenThereAreNoPlannedWorkouts() {
        val weekStart = LocalDate.of(2026, 2, 9)
        val workouts =
            listOf(
                WorkoutUi(
                    id = 1L,
                    dayOfWeek = MONDAY,
                    type = "",
                    description = "",
                    isCompleted = false,
                    isRestDay = false,
                    categoryId = null,
                    categoryColorId = null,
                    categoryName = null,
                    order = 0,
                    eventType = REST,
                ),
                WorkoutUi(
                    id = 2L,
                    dayOfWeek = TUESDAY,
                    type = "",
                    description = "",
                    isCompleted = false,
                    isRestDay = false,
                    categoryId = null,
                    categoryColorId = null,
                    categoryName = null,
                    order = 1,
                    eventType = BUSY,
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

        assertNull(state.weeklyHeaderSummary)
    }
}
