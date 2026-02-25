package com.rafaelfelipeac.hermes.features.weeklytraining.domain

import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import java.time.DayOfWeek

fun updateWorkoutOrderWithRestDayRules(
    workouts: List<WorkoutUi>,
    workoutId: Long,
    newDayOfWeek: DayOfWeek?,
    newTimeSlot: TimeSlot?,
    newOrder: Int,
): List<WorkoutUi> {
    val target = workouts.firstOrNull { it.id == workoutId } ?: return workouts
    val remaining = workouts.filterNot { it.id == workoutId }
    val sourceDay = target.dayOfWeek
    val sourceSlot = target.timeSlot
    val normalizedTargetSlot = if (newDayOfWeek == null) null else newTimeSlot
    val updatedTarget = target.copy(dayOfWeek = newDayOfWeek, timeSlot = normalizedTargetSlot)

    val destinationList =
        remaining
            .filter { it.dayOfWeek == newDayOfWeek && it.timeSlot == normalizedTargetSlot }
            .sortedBy { it.order }
            .toMutableList()
    val clampedOrder = newOrder.coerceIn(MIN_WORKOUT_ORDER, destinationList.size)
    destinationList.add(clampedOrder, updatedTarget)
    val normalizedDestination = destinationList.mapIndexed { index, workout -> workout.copy(order = index) }

    val normalizedSource =
        if (sourceDay == newDayOfWeek && sourceSlot == normalizedTargetSlot) {
            emptyList()
        } else {
            remaining
                .filter { it.dayOfWeek == sourceDay && it.timeSlot == sourceSlot }
                .sortedBy { it.order }
                .mapIndexed { index, workout -> workout.copy(order = index) }
        }

    val untouched =
        remaining.filterNot {
            (it.dayOfWeek == sourceDay && it.timeSlot == sourceSlot) ||
                (it.dayOfWeek == newDayOfWeek && it.timeSlot == normalizedTargetSlot)
        }

    return untouched + normalizedSource + normalizedDestination
}

private const val MIN_WORKOUT_ORDER = 0
