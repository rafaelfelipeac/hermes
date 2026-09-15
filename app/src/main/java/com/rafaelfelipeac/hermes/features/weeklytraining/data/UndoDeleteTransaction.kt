package com.rafaelfelipeac.hermes.features.weeklytraining.data

import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TIME_SLOT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues.UNPLANNED
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryDao
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.UndoDeleteCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WeeklyTrainingCommandResult
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutScheduleChange
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import java.time.LocalDate

internal class UndoDeleteTransaction(
    private val workoutDao: WorkoutDao,
    private val categoryDao: CategoryDao,
    private val userActionLogger: UserActionLogger,
) {
    suspend fun apply(request: UndoDeleteCommand): WeeklyTrainingCommandResult {
        val restoredId = workoutDao.insertOrReplace(request.workout.toEntity())
        request.previousPositions.forEach { restorePosition(it) }
        normalizeAffectedBuckets(request)
        logUndoDelete(
            request = request,
            restoredId = restoredId,
            categoryName = request.workout.categoryId?.let { categoryDao.getCategory(it)?.name },
        )
        return WeeklyTrainingCommandResult.UndoApplied
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

    private suspend fun normalizeAffectedBuckets(request: UndoDeleteCommand) {
        val affectedBuckets =
            buildList {
                add(
                    RestoredDeleteBucket(
                        weekStartDate = request.workout.weekStartDate,
                        dayOfWeek = request.workout.dayOfWeek?.value,
                        timeSlot = request.workout.timeSlot?.name,
                    ),
                )
                request.previousPositions.mapTo(this) {
                    RestoredDeleteBucket(it.weekStartDate, it.dayOfWeek?.value, it.timeSlot?.name)
                }
            }.distinct()

        affectedBuckets.forEach { bucket ->
            normalizeOrdersForBucket(
                weekStartDate = bucket.weekStartDate,
                dayOfWeek = bucket.dayOfWeek,
                timeSlot = bucket.timeSlot,
            )
        }
    }

    private suspend fun normalizeOrdersForBucket(
        weekStartDate: LocalDate,
        dayOfWeek: Int?,
        timeSlot: String?,
    ) {
        workoutDao.getWorkoutsForWeek(weekStartDate)
            .filter { it.dayOfWeek == dayOfWeek && it.timeSlot == timeSlot }
            .sortedBy { it.sortOrder }
            .forEachIndexed { index, workout ->
                if (workout.sortOrder != index) {
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

    private suspend fun logUndoDelete(
        request: UndoDeleteCommand,
        restoredId: Long,
        categoryName: String?,
    ) {
        val workout = request.workout

        userActionLogger.log(
            actionType = workout.eventType.toUndoDeleteActionType(),
            entityType = workout.eventType.toUserActionEntityType(),
            entityId = restoredId,
            metadata =
                mutableMapOf(
                    WEEK_START_DATE to request.displayWeekStart.toString(),
                    DAY_OF_WEEK to (workout.dayOfWeek?.value?.toString() ?: UNPLANNED),
                    NEW_TIME_SLOT to (workout.timeSlot?.name ?: UNPLANNED),
                    NEW_ORDER to workout.order.toString(),
                    NEW_TYPE to workout.type,
                    NEW_DESCRIPTION to workout.description,
                ).apply {
                    putWorkoutCategoryMetadata(
                        categoryId = workout.categoryId,
                        categoryName = categoryName,
                        newCategoryName = categoryName,
                    )
                },
        )
    }
}

private data class RestoredDeleteBucket(
    val weekStartDate: LocalDate,
    val dayOfWeek: Int?,
    val timeSlot: String?,
)

private fun EventType.toUndoDeleteActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.UNDO_DELETE_WORKOUT
        EventType.REST -> UserActionType.UNDO_DELETE_REST_DAY
        EventType.BUSY -> UserActionType.UNDO_DELETE_BUSY
        EventType.SICK -> UserActionType.UNDO_DELETE_SICK
        EventType.RACE_EVENT -> UserActionType.UNDO_DELETE_RACE_EVENT
    }
}
