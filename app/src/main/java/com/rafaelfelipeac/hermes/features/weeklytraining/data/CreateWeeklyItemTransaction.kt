package com.rafaelfelipeac.hermes.features.weeklytraining.data

import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues.UNPLANNED
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryDao
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.CreateWeeklyItemCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WeeklyTrainingCommandResult
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType

internal class CreateWeeklyItemTransaction(
    private val workoutDao: WorkoutDao,
    private val categoryDao: CategoryDao,
    private val userActionLogger: UserActionLogger,
) {
    suspend fun apply(request: CreateWeeklyItemCommand): WeeklyTrainingCommandResult {
        val nextOrder = nextOrderFor(request)
        val itemId = workoutDao.insert(request.toEntity(nextOrder))
        val categoryName = request.categoryId?.let { categoryDao.getCategory(it)?.name }

        userActionLogger.log(
            actionType = request.eventType.toCreateActionType(),
            entityType = request.eventType.toUserActionEntityType(),
            entityId = itemId,
            metadata =
                mutableMapOf(
                    WEEK_START_DATE to request.displayWeekStart.toString(),
                    DAY_OF_WEEK to (request.dayOfWeek?.value?.toString() ?: UNPLANNED),
                    NEW_ORDER to nextOrder.toString(),
                    NEW_TYPE to request.type,
                    NEW_DESCRIPTION to request.description,
                ).apply {
                    putWorkoutCategoryMetadata(
                        categoryId = request.categoryId,
                        categoryName = categoryName,
                        newCategoryId = request.categoryId,
                        newCategoryName = categoryName,
                    )
                },
        )

        return WeeklyTrainingCommandResult.ItemCreated(itemId)
    }

    private suspend fun nextOrderFor(request: CreateWeeklyItemCommand): Int {
        return workoutDao.getWorkoutsForWeek(request.storageWeekStart)
            .count { workout ->
                workout.dayOfWeek == request.dayOfWeek?.value &&
                    workout.timeSlot == request.timeSlot?.name
            }
    }
}

private fun CreateWeeklyItemCommand.toEntity(order: Int): WorkoutEntity {
    val isRestDay = eventType == EventType.REST
    val supportsWorkoutData = eventType == EventType.WORKOUT || eventType == EventType.RACE_EVENT

    return WorkoutEntity(
        weekStartDate = storageWeekStart,
        dayOfWeek = dayOfWeek?.value,
        type = type.takeIf { supportsWorkoutData } ?: EMPTY,
        description = description.takeIf { supportsWorkoutData } ?: EMPTY,
        isCompleted = false,
        isRestDay = isRestDay,
        eventType = eventType.name,
        timeSlot = timeSlot?.name,
        categoryId = categoryId.takeIf { supportsWorkoutData },
        sortOrder = order,
    )
}

private fun EventType.toCreateActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.CREATE_WORKOUT
        EventType.REST -> UserActionType.CREATE_REST_DAY
        EventType.BUSY -> UserActionType.CREATE_BUSY
        EventType.SICK -> UserActionType.CREATE_SICK
        EventType.RACE_EVENT -> UserActionType.CREATE_RACE_EVENT
    }
}
