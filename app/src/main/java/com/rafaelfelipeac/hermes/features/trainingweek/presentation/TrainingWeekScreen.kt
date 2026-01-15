package com.rafaelfelipeac.hermes.features.trainingweek.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.unit.dp
import com.rafaelfelipeac.hermes.core.ui.components.WeeklyCalendarHeader
import com.rafaelfelipeac.hermes.core.ui.components.WeeklyTrainingContent
import com.rafaelfelipeac.hermes.core.ui.components.WorkoutId
import com.rafaelfelipeac.hermes.core.ui.components.WorkoutUi
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Composable
fun TrainingWeekScreen(
    modifier: Modifier = Modifier,
    viewModel: TrainingWeekViewModel = hiltViewModel()
) {
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    val weekStartDate = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val workoutsByWeek = remember {
        mutableStateMapOf<LocalDate, List<WorkoutUi>>().apply {
            val firstWeek = weekStartDate
            val secondWeek = weekStartDate.plusWeeks(1)
            put(firstWeek, sampleWorkoutsForWeek(firstWeek, 0))
            put(secondWeek, sampleWorkoutsForWeek(secondWeek, 1))
        }
    }
    val workouts = workoutsByWeek[weekStartDate].orEmpty()
    val daysWithWorkouts = workouts.mapNotNull { it.dayOfWeek }.toSet()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        WeeklyCalendarHeader(
            selectedDate = selectedDate,
            weekStartDate = weekStartDate,
            daysWithWorkouts = daysWithWorkouts,
            onDateSelected = { selectedDate = it },
            onWeekChanged = { selectedDate = it }
        )

        WeeklyTrainingContent(
            selectedWeekStartDate = weekStartDate,
            workouts = workouts,
            onAddWorkout = {
                val nextId = "${weekStartDate}#${workouts.size + 1}"
                val nextOrder = workouts.count { it.dayOfWeek == null }
                workoutsByWeek[weekStartDate] = workouts + WorkoutUi(
                    id = nextId,
                    dayOfWeek = null,
                    type = "Workout",
                    description = "New workout",
                    isCompleted = false,
                    order = nextOrder
                )
            },
            onWorkoutMoved = { workoutId, newDayOfWeek, newOrder ->
                workoutsByWeek[weekStartDate] =
                    updateWorkoutOrder(workouts, workoutId, newDayOfWeek, newOrder)
            },
            onWorkoutCompletionChanged = { workoutId, isCompleted ->
                workoutsByWeek[weekStartDate] = workouts.map { workout ->
                    if (workout.id == workoutId) {
                        workout.copy(isCompleted = isCompleted)
                    } else {
                        workout
                    }
                }
            }
        )
    }
}

private fun updateWorkoutOrder(
    workouts: List<WorkoutUi>,
    workoutId: WorkoutId,
    newDayOfWeek: DayOfWeek?,
    newOrder: Int
): List<WorkoutUi> {
    val target = workouts.firstOrNull { it.id == workoutId } ?: return workouts
    val remaining = workouts.filterNot { it.id == workoutId }

    val (sameDay, otherDays) = remaining.partition { it.dayOfWeek == newDayOfWeek }
    val reorderedSameDay = sameDay
        .sortedBy { it.order }
        .toMutableList()
    val clampedOrder = newOrder.coerceIn(0, reorderedSameDay.size)
    reorderedSameDay.add(clampedOrder, target.copy(dayOfWeek = newDayOfWeek))

    val normalizedSameDay = reorderedSameDay.mapIndexed { index, workout ->
        workout.copy(order = index)
    }

    return otherDays + normalizedSameDay
}

private fun sampleWorkoutsForWeek(weekStartDate: LocalDate, seed: Int): List<WorkoutUi> {
    val baseId = weekStartDate.toString()
    return if (seed % 2 == 0) {
        listOf(
            WorkoutUi(
                id = "$baseId#1",
                dayOfWeek = null,
                type = "Run",
                description = "Easy 5k",
                isCompleted = false,
                order = 0
            ),
            WorkoutUi(
                id = "$baseId#2",
                dayOfWeek = DayOfWeek.MONDAY,
                type = "Swim",
                description = "Intervals 10x100",
                isCompleted = false,
                order = 0
            ),
            WorkoutUi(
                id = "$baseId#3",
                dayOfWeek = DayOfWeek.WEDNESDAY,
                type = "Bike",
                description = "Tempo 45 min",
                isCompleted = true,
                order = 0
            )
        )
    } else {
        listOf(
            WorkoutUi(
                id = "$baseId#1",
                dayOfWeek = DayOfWeek.TUESDAY,
                type = "Strength",
                description = "Upper body 40 min",
                isCompleted = false,
                order = 0
            ),
            WorkoutUi(
                id = "$baseId#2",
                dayOfWeek = DayOfWeek.THURSDAY,
                type = "Run",
                description = "Intervals 6x800",
                isCompleted = false,
                order = 0
            ),
            WorkoutUi(
                id = "$baseId#3",
                dayOfWeek = DayOfWeek.SATURDAY,
                type = "Long ride",
                description = "2h endurance",
                isCompleted = false,
                order = 0
            )
        )
    }
}
