package com.rafaelfelipeac.hermes.features.weeklytraining.data

import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TIME_SLOT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_TIME_SLOT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues.UNPLANNED
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryDao
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.UndoScheduleCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WeeklyTrainingCommandResult
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutScheduleChange
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import java.time.LocalDate

internal class UndoScheduleTransaction(
    private val workoutDao: WorkoutDao,
    private val categoryDao: CategoryDao,
    private val userActionLogger: UserActionLogger,
) {
    suspend fun apply(request: UndoScheduleCommand): WeeklyTrainingCommandResult {
        val plan = buildPlan(request) ?: return WeeklyTrainingCommandResult.NoChange

        request.previousPositions.forEach { restorePosition(it) }
        request.previousPositions
            .map { RestoredScheduleBucket(it.weekStartDate, it.dayOfWeek?.value, it.timeSlot?.name) }
            .distinct()
            .forEach { bucket ->
                normalizeOrdersForBucket(
                    weekStartDate = bucket.weekStartDate,
                    dayOfWeek = bucket.dayOfWeek,
                    timeSlot = bucket.timeSlot,
                    skipIds = setOf(request.movedWorkoutId),
                )
            }

        logUndoSchedule(request, plan.movedWorkout, plan.movedPosition)
        return WeeklyTrainingCommandResult.UndoApplied
    }

    private suspend fun buildPlan(request: UndoScheduleCommand): UndoSchedulePlan? {
        val persistedById =
            request.previousPositions
                .map { it.workoutId }
                .distinct()
                .associateWith { workoutDao.getWorkout(it) }
        val movedWorkout =
            persistedById[request.movedWorkoutId]
                ?: return null
        val movedPosition =
            request.previousPositions.firstOrNull { it.workoutId == request.movedWorkoutId }
        return if (movedPosition == null || persistedById.values.any { it == null }) {
            null
        } else {
            UndoSchedulePlan(movedWorkout, movedPosition)
        }
    }

    private suspend fun restorePosition(position: WorkoutScheduleChange) {
        workoutDao.updateSchedule(
            id = position.workoutId,
            weekStartDate = position.weekStartDate,
            dayOfWeek = position.dayOfWeek?.value,
            timeSlot = position.timeSlot?.name,
            order = position.order,
        )
    }

    private suspend fun normalizeOrdersForBucket(
        weekStartDate: LocalDate,
        dayOfWeek: Int?,
        timeSlot: String?,
        skipIds: Set<Long>,
    ) {
        workoutDao.getWorkoutsForWeek(weekStartDate)
            .filter { it.dayOfWeek == dayOfWeek && it.timeSlot == timeSlot }
            .sortedBy { it.sortOrder }
            .forEachIndexed { index, workout ->
                if (workout.id !in skipIds) {
                    workoutDao.updateSchedule(
                        id = workout.id,
                        weekStartDate = workout.weekStartDate,
                        dayOfWeek = dayOfWeek,
                        timeSlot = timeSlot,
                        order = index,
                    )
                }
            }
    }

    private suspend fun logUndoSchedule(
        request: UndoScheduleCommand,
        movedWorkout: WorkoutEntity,
        movedPosition: WorkoutScheduleChange,
    ) {
        val eventType = movedWorkout.eventType.toEventType(movedWorkout.isRestDay)
        val categoryName = movedWorkout.categoryId?.let { categoryDao.getCategory(it)?.name }

        userActionLogger.log(
            actionType = eventType.toUndoScheduleActionType(movedWorkout, movedPosition),
            entityType = eventType.toUserActionEntityType(),
            entityId = request.movedWorkoutId,
            metadata =
                mutableMapOf(
                    WEEK_START_DATE to request.displayWeekStart.toString(),
                    OLD_DAY_OF_WEEK to (movedWorkout.dayOfWeek?.toString() ?: UNPLANNED),
                    NEW_DAY_OF_WEEK to (movedPosition.dayOfWeek?.value?.toString() ?: UNPLANNED),
                    OLD_TIME_SLOT to (movedWorkout.timeSlot ?: UNPLANNED),
                    NEW_TIME_SLOT to (movedPosition.timeSlot?.name ?: UNPLANNED),
                    OLD_ORDER to movedWorkout.sortOrder.toString(),
                    NEW_ORDER to movedPosition.order.toString(),
                    NEW_TYPE to movedWorkout.type,
                    NEW_DESCRIPTION to movedWorkout.description,
                ).apply {
                    putWorkoutCategoryMetadata(
                        categoryId = movedWorkout.categoryId,
                        categoryName = categoryName,
                        newCategoryId = movedWorkout.categoryId,
                        newCategoryName = categoryName,
                        oldCategoryId = movedWorkout.categoryId,
                        oldCategoryName = categoryName,
                    )
                },
        )
    }
}

private data class UndoSchedulePlan(
    val movedWorkout: WorkoutEntity,
    val movedPosition: WorkoutScheduleChange,
)

private data class RestoredScheduleBucket(
    val weekStartDate: LocalDate,
    val dayOfWeek: Int?,
    val timeSlot: String?,
)

private fun EventType.toUndoScheduleActionType(
    original: WorkoutEntity,
    restored: WorkoutScheduleChange,
): UserActionType {
    return if (original.dayOfWeek != restored.dayOfWeek?.value || original.timeSlot != restored.timeSlot?.name) {
        toUndoMoveActionType()
    } else {
        toUndoReorderActionType()
    }
}

private fun EventType.toUndoReorderActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.UNDO_REORDER_WORKOUT_SAME_DAY
        EventType.REST -> UserActionType.UNDO_REORDER_REST
        EventType.BUSY -> UserActionType.UNDO_REORDER_BUSY
        EventType.SICK -> UserActionType.UNDO_REORDER_SICK
        EventType.RACE_EVENT -> UserActionType.UNDO_REORDER_RACE_EVENT
    }
}

private fun EventType.toUndoMoveActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.UNDO_MOVE_WORKOUT_BETWEEN_DAYS
        EventType.REST -> UserActionType.UNDO_MOVE_REST
        EventType.BUSY -> UserActionType.UNDO_MOVE_BUSY
        EventType.SICK -> UserActionType.UNDO_MOVE_SICK
        EventType.RACE_EVENT -> UserActionType.UNDO_MOVE_RACE_EVENT
    }
}

internal fun EventType.toUndoCompletionActionType(wasCompleted: Boolean): UserActionType {
    return when (this) {
        EventType.RACE_EVENT ->
            if (wasCompleted) UserActionType.UNDO_COMPLETE_RACE_EVENT else UserActionType.UNDO_INCOMPLETE_RACE_EVENT
        else ->
            if (wasCompleted) UserActionType.UNDO_COMPLETE_WORKOUT else UserActionType.UNDO_INCOMPLETE_WORKOUT
    }
}
