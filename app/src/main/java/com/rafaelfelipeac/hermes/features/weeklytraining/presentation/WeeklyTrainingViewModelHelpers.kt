package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COMPLETE_WEEK_WORKOUTS
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.canonicalStorageWeekStart
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.displayDateForDay
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.RACE_EVENT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.WORKOUT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.updateWorkoutOrderWithRestDayRules
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.weekDates
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import java.time.DayOfWeek
import java.time.LocalDate

internal fun resolveWorkoutChanges(
    currentWorkouts: List<WorkoutUi>,
    workoutId: Long,
    newDayOfWeek: DayOfWeek?,
    newTimeSlot: TimeSlot?,
    newOrder: Int,
): List<WorkoutUi> {
    val updated =
        updateWorkoutOrderWithRestDayRules(
            currentWorkouts,
            workoutId,
            newDayOfWeek,
            newTimeSlot,
            newOrder,
        )

    return updated.mapNotNull { workout ->
        val original = currentWorkouts.firstOrNull { it.id == workout.id } ?: return@mapNotNull null

        if (
            original.dayOfWeek != workout.dayOfWeek ||
            original.timeSlot != workout.timeSlot ||
            original.order != workout.order
        ) {
            workout
        } else {
            null
        }
    }
}

internal fun shouldCelebrateAllWorkoutsCompleted(
    currentWorkouts: List<WorkoutUi>,
    workoutId: Long,
    previousIsCompleted: Boolean,
    newIsCompleted: Boolean,
): Boolean {
    val plannedWorkouts =
        currentWorkouts.filter { workout ->
            workout.eventType in setOf(WORKOUT, RACE_EVENT) && workout.dayOfWeek != null
        }

    val allCompletedBeforeToggle =
        plannedWorkouts.all { workout ->
            if (workout.id == workoutId) {
                previousIsCompleted
            } else {
                workout.isCompleted
            }
        }
    val allCompletedAfterToggle =
        plannedWorkouts.all { workout ->
            if (workout.id == workoutId) {
                newIsCompleted
            } else {
                workout.isCompleted
            }
        }

    return newIsCompleted &&
        plannedWorkouts.isNotEmpty() &&
        !allCompletedBeforeToggle &&
        allCompletedAfterToggle
}

internal suspend fun logCompleteWeekWorkouts(
    userActionLogger: UserActionLogger,
    weekStartDate: LocalDate,
) {
    userActionLogger.log(
        actionType = COMPLETE_WEEK_WORKOUTS,
        entityType = WEEK,
        entityId = weekStartDate.toEpochDay(),
        metadata =
            mapOf(
                WEEK_START_DATE to weekStartDate.toString(),
            ),
    )
}

internal fun workoutsForDisplayWeek(
    workouts: List<Workout>,
    displayWeekStart: LocalDate,
    unassignedStorageWeekStart: LocalDate = canonicalStorageWeekStart(displayWeekStart),
): List<Workout> {
    val displayDates = weekDates(displayWeekStart).toSet()

    return workouts.filter { workout ->
        val dayOfWeek = workout.dayOfWeek

        if (dayOfWeek == null) {
            workout.weekStartDate == unassignedStorageWeekStart
        } else {
            workout.weekStartDate.plusDays((dayOfWeek.value - 1).toLong()) in displayDates
        }
    }
}

internal fun resolveStorageWeekStartDate(
    workout: WorkoutUi,
    weekStartDate: LocalDate,
    displayStartDay: WeekStartDay,
    unassignedStorageWeekStart: LocalDate,
): LocalDate {
    val dayOfWeek = workout.dayOfWeek ?: return unassignedStorageWeekStart
    val displayDate =
        displayDateForDay(
            displayWeekStart = weekStartDate,
            displayStartDay = displayStartDay.dayOfWeek,
            dayOfWeek = dayOfWeek,
        )

    return canonicalStorageWeekStart(displayDate)
}
