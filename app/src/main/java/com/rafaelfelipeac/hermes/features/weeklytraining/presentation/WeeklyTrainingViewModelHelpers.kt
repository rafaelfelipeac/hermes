package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues.UNPLANNED
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.REST_DAY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.MOVE_WORKOUT_BETWEEN_DAYS
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.REORDER_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_MOVE_WORKOUT_BETWEEN_DAYS
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_REORDER_WORKOUT_SAME_DAY
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.updateWorkoutOrderWithRestDayRules
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import java.time.DayOfWeek
import java.time.LocalDate

internal suspend fun normalizeOrdersAfterDelete(
    repository: WeeklyTrainingRepository,
    deletedWorkoutId: Long,
    dayOfWeek: DayOfWeek?,
    currentWorkouts: List<WorkoutUi>,
) {
    val remaining =
        currentWorkouts
            .filter { it.id != deletedWorkoutId && it.dayOfWeek == dayOfWeek }
            .sortedBy { it.order }

    remaining.forEachIndexed { index, workout ->
        if (workout.order != index) {
            repository.updateWorkoutDayAndOrder(
                workoutId = workout.id,
                dayOfWeek = dayOfWeek,
                order = index,
            )
        }
    }
}

internal suspend fun normalizeOrdersForDay(
    repository: WeeklyTrainingRepository,
    dayOfWeek: DayOfWeek?,
    currentWorkouts: List<WorkoutUi>,
) {
    val workoutsForDay =
        currentWorkouts
            .filter { it.dayOfWeek == dayOfWeek }
            .sortedBy { it.order }

    workoutsForDay.forEachIndexed { index, workout ->
        if (workout.order != index) {
            repository.updateWorkoutDayAndOrder(
                workoutId = workout.id,
                dayOfWeek = dayOfWeek,
                order = index,
            )
        }
    }
}

internal fun resolveWorkoutChanges(
    currentWorkouts: List<WorkoutUi>,
    workoutId: Long,
    newDayOfWeek: DayOfWeek?,
    newOrder: Int,
): List<WorkoutUi> {
    val updated =
        updateWorkoutOrderWithRestDayRules(
            currentWorkouts,
            workoutId,
            newDayOfWeek,
            newOrder,
        )

    return updated.mapNotNull { workout ->
        val original = currentWorkouts.firstOrNull { it.id == workout.id } ?: return@mapNotNull null

        if (original.dayOfWeek != workout.dayOfWeek || original.order != workout.order) {
            workout
        } else {
            null
        }
    }
}

internal suspend fun persistWorkoutChanges(
    dependencies: WorkoutChangeDependencies,
    changes: List<WorkoutUi>,
    currentWorkouts: List<WorkoutUi>,
    movedWorkoutId: Long,
) {
    changes.forEach { workout ->
        val original = currentWorkouts.firstOrNull { it.id == workout.id } ?: return@forEach

        dependencies.repository.updateWorkoutDayAndOrder(
            workoutId = workout.id,
            dayOfWeek = workout.dayOfWeek,
            order = workout.order,
        )

        if (workout.id == movedWorkoutId) {
            logWorkoutChange(
                userActionLogger = dependencies.userActionLogger,
                original = original,
                workout = workout,
                weekStartDate = dependencies.weekStartDate,
            )
        }
    }
}

internal suspend fun logWorkoutChange(
    userActionLogger: UserActionLogger,
    original: WorkoutUi,
    workout: WorkoutUi,
    weekStartDate: LocalDate,
) {
    val entityType =
        if (workout.isRestDay) REST_DAY else WORKOUT
    val actionType =
        if (original.dayOfWeek != workout.dayOfWeek) {
            MOVE_WORKOUT_BETWEEN_DAYS
        } else {
            REORDER_WORKOUT
        }

    userActionLogger.log(
        actionType = actionType,
        entityType = entityType,
        entityId = workout.id,
        metadata =
            mapOf(
                WEEK_START_DATE to weekStartDate.toString(),
                OLD_DAY_OF_WEEK to (original.dayOfWeek?.value?.toString() ?: UNPLANNED),
                NEW_DAY_OF_WEEK to (workout.dayOfWeek?.value?.toString() ?: UNPLANNED),
                OLD_ORDER to original.order.toString(),
                NEW_ORDER to workout.order.toString(),
                NEW_TYPE to workout.type,
                NEW_DESCRIPTION to workout.description,
            ),
    )
}

internal suspend fun logUndoWorkoutChange(
    userActionLogger: UserActionLogger,
    original: WorkoutUi,
    workout: WorkoutUi,
    weekStartDate: LocalDate,
) {
    val entityType =
        if (workout.isRestDay) REST_DAY else WORKOUT
    val actionType =
        if (original.dayOfWeek != workout.dayOfWeek) {
            UNDO_MOVE_WORKOUT_BETWEEN_DAYS
        } else {
            UNDO_REORDER_WORKOUT_SAME_DAY
        }

    userActionLogger.log(
        actionType = actionType,
        entityType = entityType,
        entityId = workout.id,
        metadata =
            mapOf(
                WEEK_START_DATE to weekStartDate.toString(),
                OLD_DAY_OF_WEEK to (original.dayOfWeek?.value?.toString() ?: UNPLANNED),
                NEW_DAY_OF_WEEK to (workout.dayOfWeek?.value?.toString() ?: UNPLANNED),
                OLD_ORDER to original.order.toString(),
                NEW_ORDER to workout.order.toString(),
                NEW_TYPE to workout.type,
                NEW_DESCRIPTION to workout.description,
            ),
    )
}

internal data class WorkoutChangeDependencies(
    val repository: WeeklyTrainingRepository,
    val userActionLogger: UserActionLogger,
    val weekStartDate: LocalDate,
)
