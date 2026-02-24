package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TIME_SLOT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_TIME_SLOT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues.UNPLANNED
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.MOVE_WORKOUT_BETWEEN_DAYS
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.REORDER_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_COPY_LAST_WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_DELETE_REST_DAY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_DELETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_MOVE_WORKOUT_BETWEEN_DAYS
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_REORDER_WORKOUT_SAME_DAY
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.updateWorkoutOrderWithRestDayRules
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.mapper.toUi
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import java.time.DayOfWeek
import java.time.LocalDate

internal suspend fun normalizeOrdersAfterDelete(
    repository: WeeklyTrainingRepository,
    deletedWorkoutId: Long,
    dayOfWeek: DayOfWeek?,
    timeSlot: TimeSlot?,
    currentWorkouts: List<WorkoutUi>,
) {
    val remaining =
        currentWorkouts
            .filter { it.id != deletedWorkoutId && it.dayOfWeek == dayOfWeek && it.timeSlot == timeSlot }
            .sortedBy { it.order }

    remaining.forEachIndexed { index, workout ->
        if (workout.order != index) {
            repository.updateWorkoutDayAndOrder(
                workoutId = workout.id,
                dayOfWeek = dayOfWeek,
                timeSlot = timeSlot,
                order = index,
            )
        }
    }
}

internal suspend fun normalizeOrdersForDay(
    repository: WeeklyTrainingRepository,
    dayOfWeek: DayOfWeek?,
    timeSlot: TimeSlot?,
    currentWorkouts: List<WorkoutUi>,
    forceUpdate: Boolean = false,
    skipIds: Set<Long> = emptySet(),
) {
    val workoutsForDay =
        currentWorkouts
            .filter { it.dayOfWeek == dayOfWeek && it.timeSlot == timeSlot }
            .sortedBy { it.order }

    workoutsForDay.forEachIndexed { index, workout ->
        if (workout.id !in skipIds && (forceUpdate || workout.order != index)) {
            repository.updateWorkoutDayAndOrder(
                workoutId = workout.id,
                dayOfWeek = dayOfWeek,
                timeSlot = timeSlot,
                order = index,
            )
        }
    }
}

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
            timeSlot = workout.timeSlot,
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
        workout.eventType.toUserActionEntityType()
    val actionType =
        if (original.dayOfWeek != workout.dayOfWeek || original.timeSlot != workout.timeSlot) {
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
                OLD_TIME_SLOT to (original.timeSlot?.name ?: UNPLANNED),
                NEW_TIME_SLOT to (workout.timeSlot?.name ?: UNPLANNED),
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
        workout.eventType.toUserActionEntityType()
    val actionType =
        if (original.dayOfWeek != workout.dayOfWeek || original.timeSlot != workout.timeSlot) {
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
                OLD_TIME_SLOT to (original.timeSlot?.name ?: UNPLANNED),
                NEW_TIME_SLOT to (workout.timeSlot?.name ?: UNPLANNED),
                OLD_ORDER to original.order.toString(),
                NEW_ORDER to workout.order.toString(),
                NEW_TYPE to workout.type,
                NEW_DESCRIPTION to workout.description,
            ),
    )
}

internal suspend fun restoreDeletedWorkout(
    repository: WeeklyTrainingRepository,
    action: PendingUndoAction.Delete,
): Long {
    val workout = action.workout
    val restoredId =
        repository.insertWorkout(
            Workout(
                id = workout.id,
                weekStartDate = action.weekStartDate,
                dayOfWeek = workout.dayOfWeek,
                type = workout.type,
                description = workout.description,
                isCompleted = workout.isCompleted,
                isRestDay = workout.isRestDay,
                categoryId = workout.categoryId,
                order = workout.order,
                eventType = workout.eventType,
                timeSlot = workout.timeSlot,
            ),
        )

    action.previousPositions.forEach { position ->
        repository.updateWorkoutDayAndOrder(
            workoutId = position.id,
            dayOfWeek = position.dayOfWeek,
            timeSlot = position.timeSlot,
            order = position.order,
        )
    }

    val updatedWorkouts =
        buildUpdatedWorkoutsAfterRestore(
            repository = repository,
            restoredId = restoredId,
            workout = workout,
            weekStartDate = action.weekStartDate,
        )

    val affectedBuckets =
        buildList {
            add(workout.dayOfWeek to workout.timeSlot)
            action.previousPositions.mapTo(this) { it.dayOfWeek to it.timeSlot }
        }
            .distinct()

    affectedBuckets.forEach { (dayOfWeek, timeSlot) ->
        normalizeOrdersForDay(
            repository = repository,
            dayOfWeek = dayOfWeek,
            timeSlot = timeSlot,
            currentWorkouts = updatedWorkouts,
        )
    }

    return restoredId
}

internal suspend fun buildUpdatedWorkoutsAfterRestore(
    repository: WeeklyTrainingRepository,
    restoredId: Long,
    workout: WorkoutUi,
    weekStartDate: LocalDate,
): List<WorkoutUi> {
    val latestWorkouts =
        repository.getWorkoutsForWeek(weekStartDate).map { it.toUi(null) }
    val restoredWorkout = workout.copy(id = restoredId)

    return if (latestWorkouts.any { it.id == restoredId }) {
        latestWorkouts
    } else {
        latestWorkouts + restoredWorkout
    }
}

internal suspend fun undoMoveOrReorder(
    action: PendingUndoAction.MoveOrReorder,
    currentWorkouts: List<WorkoutUi>,
    repository: WeeklyTrainingRepository,
    userActionLogger: UserActionLogger,
) {
    val movedWorkout = currentWorkouts.firstOrNull { it.id == action.movedWorkoutId }
    val previousPosition =
        action.previousPositions.firstOrNull { it.id == action.movedWorkoutId }
    val previousPositionsById = action.previousPositions.associateBy { it.id }

    action.previousPositions.forEach { position ->
        repository.updateWorkoutDayAndOrder(
            workoutId = position.id,
            dayOfWeek = position.dayOfWeek,
            timeSlot = position.timeSlot,
            order = position.order,
        )
    }

    val updatedWorkouts =
        currentWorkouts.map { workout ->
            val position = previousPositionsById[workout.id]

            if (position == null) {
                workout
            } else {
                workout.copy(
                    dayOfWeek = position.dayOfWeek,
                    timeSlot = position.timeSlot,
                    order = position.order,
                )
            }
        }

    action.previousPositions
        .map { it.dayOfWeek to it.timeSlot }
        .distinct()
        .forEach { (dayOfWeek, timeSlot) ->
            normalizeOrdersForDay(
                repository = repository,
                dayOfWeek = dayOfWeek,
                timeSlot = timeSlot,
                currentWorkouts = updatedWorkouts,
                forceUpdate = true,
                skipIds = setOf(action.movedWorkoutId),
            )
        }

    if (movedWorkout != null && previousPosition != null) {
        val updated =
            movedWorkout.copy(
                dayOfWeek = previousPosition.dayOfWeek,
                timeSlot = previousPosition.timeSlot,
                order = previousPosition.order,
            )

        logUndoWorkoutChange(
            original = movedWorkout,
            workout = updated,
            weekStartDate = action.weekStartDate,
            userActionLogger = userActionLogger,
        )
    }
}

internal suspend fun undoDelete(
    action: PendingUndoAction.Delete,
    repository: WeeklyTrainingRepository,
    userActionLogger: UserActionLogger,
) {
    val restoredId = restoreDeletedWorkout(repository, action)
    val workout = action.workout
    val entityType =
        workout.eventType.toUserActionEntityType()
    val actionType =
        if (workout.eventType == EventType.WORKOUT) UNDO_DELETE_WORKOUT else UNDO_DELETE_REST_DAY

    userActionLogger.log(
        actionType = actionType,
        entityType = entityType,
        entityId = restoredId,
        metadata =
            mapOf(
                WEEK_START_DATE to action.weekStartDate.toString(),
                DAY_OF_WEEK to (workout.dayOfWeek?.value?.toString() ?: UNPLANNED),
                NEW_ORDER to workout.order.toString(),
                NEW_TYPE to workout.type,
                NEW_DESCRIPTION to workout.description,
            ),
    )
}

internal suspend fun undoReplaceWeek(
    action: PendingUndoAction.ReplaceWeek,
    repository: WeeklyTrainingRepository,
    userActionLogger: UserActionLogger,
) {
    repository.deleteWorkoutsForWeek(action.weekStartDate)

    action.previousWorkouts
        .sortedBy { it.id }
        .forEach { workout ->
            repository.insertWorkout(workout)
        }

    userActionLogger.log(
        actionType = UNDO_COPY_LAST_WEEK,
        entityType = WEEK,
        metadata =
            mapOf(
                WEEK_START_DATE to action.weekStartDate.toString(),
                OLD_WEEK_START_DATE to action.weekStartDate.minusWeeks(1).toString(),
                NEW_WEEK_START_DATE to action.weekStartDate.toString(),
            ),
    )
}

internal data class WorkoutChangeDependencies(
    val repository: WeeklyTrainingRepository,
    val userActionLogger: UserActionLogger,
    val weekStartDate: LocalDate,
)
