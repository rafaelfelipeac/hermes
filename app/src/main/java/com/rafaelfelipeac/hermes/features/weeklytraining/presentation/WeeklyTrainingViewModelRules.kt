package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COMPLETE_RACE_EVENT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.INCOMPLETE_RACE_EVENT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.INCOMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_COMPLETE_RACE_EVENT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_COMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_INCOMPLETE_RACE_EVENT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_INCOMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.canonicalStorageWeekStart
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.mapper.toUi
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi

internal fun copyWorkoutToNextWeek(workout: Workout): Workout {
    val nextWeekStart =
        if (workout.dayOfWeek == null) {
            workout.weekStartDate.plusWeeks(1)
        } else {
            val workoutDate = workout.weekStartDate.plusDays((workout.dayOfWeek.value - 1).toLong())
            canonicalStorageWeekStart(workoutDate.plusWeeks(1))
        }

    return workout.copy(
        id = 0L,
        weekStartDate = nextWeekStart,
        isCompleted = false,
    )
}

internal fun mapWorkoutsToUi(
    workouts: List<Workout>,
    categories: List<CategoryUi>,
): List<WorkoutUi> {
    val categoriesById = categories.associateBy { it.id }
    val fallbackCategory = categoriesById[UNCATEGORIZED_ID]

    return workouts.map { workout ->
        val category =
            if (!workout.eventType.supportsWorkoutCategory()) {
                null
            } else {
                workout.categoryId?.let(categoriesById::get) ?: fallbackCategory
            }
        workout.toUi(category)
    }
}

internal fun EventType.supportsCompletion(): Boolean {
    return this == EventType.WORKOUT || this == EventType.RACE_EVENT
}

private fun EventType.supportsWorkoutCategory(): Boolean {
    return this == EventType.WORKOUT || this == EventType.RACE_EVENT
}

internal fun EventType.toCompletionActionType(isCompleted: Boolean) =
    when (this) {
        EventType.RACE_EVENT -> if (isCompleted) COMPLETE_RACE_EVENT else INCOMPLETE_RACE_EVENT
        else -> if (isCompleted) COMPLETE_WORKOUT else INCOMPLETE_WORKOUT
    }

internal fun EventType.toUndoCompletionActionType(newCompleted: Boolean) =
    when (this) {
        EventType.RACE_EVENT ->
            if (newCompleted) UNDO_COMPLETE_RACE_EVENT else UNDO_INCOMPLETE_RACE_EVENT
        else -> if (newCompleted) UNDO_COMPLETE_WORKOUT else UNDO_INCOMPLETE_WORKOUT
    }

internal fun normalizeCategoryId(
    eventType: EventType,
    categoryId: Long?,
): Long? {
    return if (eventType.supportsWorkoutCategory()) categoryId ?: UNCATEGORIZED_ID else null
}

internal fun resolveCategoryId(
    eventType: EventType,
    categoryId: Long?,
    categories: List<CategoryUi>,
): Long? {
    val normalized = normalizeCategoryId(eventType, categoryId) ?: return null
    return if (categories.any { it.id == normalized }) normalized else UNCATEGORIZED_ID
}

internal fun resolveCategoryNames(
    eventType: EventType,
    normalizedCategoryId: Long?,
    categories: List<CategoryUi>,
    original: WorkoutUi?,
): Pair<String?, String?> {
    val oldCategoryName = original?.takeIf { it.eventType.supportsWorkoutCategory() }?.categoryName
    val newCategoryName =
        if (!eventType.supportsWorkoutCategory() || normalizedCategoryId == null) {
            null
        } else {
            categories.firstOrNull { it.id == normalizedCategoryId }?.name
        }
    return oldCategoryName to newCategoryName
}

internal fun buildDeleteBucketPositions(
    workoutId: Long,
    original: WorkoutUi?,
    currentWorkouts: List<WorkoutUi>,
): List<WorkoutPosition> {
    return original?.let { workout ->
        currentWorkouts
            .asSequence()
            .filter { it.dayOfWeek == workout.dayOfWeek && it.timeSlot == workout.timeSlot }
            .sortedBy { it.order }
            .filter { it.id != workoutId }
            .map {
                WorkoutPosition(
                    id = it.id,
                    weekStartDate = it.weekStartDate,
                    dayOfWeek = it.dayOfWeek,
                    timeSlot = it.timeSlot,
                    order = it.order,
                )
            }.toList()
    }.orEmpty()
}

internal fun nextUnplannedOrder(state: WeeklyTrainingState): Int {
    return state.workouts.count { it.dayOfWeek == null }
}
