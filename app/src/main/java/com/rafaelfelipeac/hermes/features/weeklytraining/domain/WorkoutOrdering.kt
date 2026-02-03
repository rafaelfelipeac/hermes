package com.rafaelfelipeac.hermes.features.weeklytraining.domain

import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import java.time.DayOfWeek

fun updateWorkoutOrderWithRestDayRules(
    workouts: List<WorkoutUi>,
    workoutId: Long,
    newDayOfWeek: DayOfWeek?,
    newOrder: Int,
): List<WorkoutUi> {
    val target = workouts.firstOrNull { it.id == workoutId } ?: return workouts
    val remaining = workouts.filterNot { it.id == workoutId }
    val sourceDay = target.dayOfWeek

    val adjusted =
        remaining.map { workout ->
            if (newDayOfWeek == workout.dayOfWeek && target.isRestDay) {
                workout.copy(dayOfWeek = null)
            } else if (!target.isRestDay && workout.isRestDay && workout.dayOfWeek == newDayOfWeek) {
                workout.copy(dayOfWeek = null)
            } else {
                workout
            }
        }.toMutableList()

    val updatedTarget = target.copy(dayOfWeek = newDayOfWeek)
    val destinationList =
        adjusted
            .filter { it.dayOfWeek == newDayOfWeek }
            .sortedBy { it.order }
            .toMutableList()
    val clampedOrder =
        newOrder.coerceIn(MIN_WORKOUT_ORDER, destinationList.size)
    destinationList.add(clampedOrder, updatedTarget)
    val normalizedDestination =
        destinationList.mapIndexed { index, workout ->
            workout.copy(order = index)
        }

    val sourceList =
        adjusted
            .filter { it.dayOfWeek == sourceDay }
            .sortedBy { it.order }
            .mapIndexed { index, workout -> workout.copy(order = index) }

    val tbdList =
        adjusted
            .filter { it.dayOfWeek == null && it.id != updatedTarget.id }
            .sortedBy { it.order }
            .mapIndexed { index, workout -> workout.copy(order = index) }

    val untouched =
        adjusted.filterNot {
            it.dayOfWeek == sourceDay || it.dayOfWeek == newDayOfWeek || it.dayOfWeek == null
        }

    return untouched + sourceList + tbdList + normalizedDestination
}

private const val MIN_WORKOUT_ORDER = 0
