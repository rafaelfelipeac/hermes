package com.rafaelfelipeac.hermes.features.weeklytraining.data

import androidx.room.withTransaction
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.IS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WAS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WeeklyTrainingCommandRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WeeklyTrainingCommandResult
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutCompletionCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutDeleteCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomWeeklyTrainingCommandRepository
    @Inject
    constructor(
        private val database: HermesDatabase,
        private val userActionLogger: UserActionLogger,
    ) : WeeklyTrainingCommandRepository {
        private val categoryDao = database.categoryDao()
        private val workoutDao = database.workoutDao()

        override suspend fun updateCompletion(request: WorkoutCompletionCommand): WeeklyTrainingCommandResult {
            return database.withTransaction {
                val workout =
                    workoutDao.getWorkout(request.workoutId)
                        ?: return@withTransaction WeeklyTrainingCommandResult.NoChange
                val eventType = workout.eventType.toEventType(workout.isRestDay)

                if (!eventType.supportsCompletion() || workout.isCompleted == request.isCompleted) {
                    return@withTransaction WeeklyTrainingCommandResult.NoChange
                }

                val categoryName = workout.categoryId?.let { categoryDao.getCategory(it)?.name }
                workoutDao.updateCompletion(
                    id = request.workoutId,
                    isCompleted = request.isCompleted,
                )

                userActionLogger.log(
                    actionType = eventType.toCompletionActionType(request.isCompleted),
                    entityType = eventType.toUserActionEntityType(),
                    entityId = request.workoutId,
                    metadata =
                        mutableMapOf(
                            WEEK_START_DATE to request.displayWeekStart.toString(),
                            WAS_COMPLETED to workout.isCompleted.toString(),
                            IS_COMPLETED to request.isCompleted.toString(),
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

                WeeklyTrainingCommandResult.CompletionChanged(
                    previousCompleted = workout.isCompleted,
                    eventType = eventType,
                )
            }
        }

        override suspend fun deleteWorkout(request: WorkoutDeleteCommand): WeeklyTrainingCommandResult {
            return database.withTransaction {
                val workout =
                    workoutDao.getWorkout(request.workoutId)
                        ?: return@withTransaction WeeklyTrainingCommandResult.NoChange
                val eventType = workout.eventType.toEventType(workout.isRestDay)
                val categoryName = workout.categoryId?.let { categoryDao.getCategory(it)?.name }

                workoutDao.deleteById(request.workoutId)
                normalizeOrdersAfterDelete(workout)

                userActionLogger.log(
                    actionType = eventType.toDeleteActionType(),
                    entityType = eventType.toUserActionEntityType(),
                    entityId = request.workoutId,
                    metadata =
                        mutableMapOf(
                            WEEK_START_DATE to request.displayWeekStart.toString(),
                            OLD_TYPE to workout.type,
                            OLD_DESCRIPTION to workout.description,
                        ).apply {
                            putWorkoutCategoryMetadata(
                                categoryId = workout.categoryId,
                                categoryName = categoryName,
                                newCategoryName = null,
                                oldCategoryId = workout.categoryId,
                                oldCategoryName = categoryName,
                            )
                        },
                )

                WeeklyTrainingCommandResult.WorkoutDeleted
            }
        }

        private suspend fun normalizeOrdersAfterDelete(deletedWorkout: WorkoutEntity) {
            val remaining =
                workoutDao.getWorkoutsForWeek(deletedWorkout.weekStartDate)
                    .filter {
                        it.id != deletedWorkout.id &&
                            it.dayOfWeek == deletedWorkout.dayOfWeek &&
                            it.timeSlot == deletedWorkout.timeSlot
                    }.sortedBy { it.sortOrder }

            remaining.forEachIndexed { index, workout ->
                if (workout.sortOrder != index) {
                    workoutDao.updateSchedule(
                        id = workout.id,
                        weekStartDate = workout.weekStartDate,
                        dayOfWeek = workout.dayOfWeek,
                        timeSlot = workout.timeSlot,
                        order = index,
                    )
                }
            }
        }
    }

private fun String.toEventType(isRestDay: Boolean): EventType {
    return runCatching { EventType.valueOf(this) }
        .getOrDefault(if (isRestDay) EventType.REST else EventType.WORKOUT)
}

private fun EventType.supportsCompletion(): Boolean {
    return this == EventType.WORKOUT || this == EventType.RACE_EVENT
}

private fun EventType.toCompletionActionType(isCompleted: Boolean): UserActionType {
    return when (this) {
        EventType.RACE_EVENT ->
            if (isCompleted) UserActionType.COMPLETE_RACE_EVENT else UserActionType.INCOMPLETE_RACE_EVENT
        else ->
            if (isCompleted) UserActionType.COMPLETE_WORKOUT else UserActionType.INCOMPLETE_WORKOUT
    }
}

private fun EventType.toDeleteActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.DELETE_WORKOUT
        EventType.REST -> UserActionType.DELETE_REST_DAY
        EventType.BUSY -> UserActionType.DELETE_BUSY
        EventType.SICK -> UserActionType.DELETE_SICK
        EventType.RACE_EVENT -> UserActionType.DELETE_RACE_EVENT
    }
}

private fun EventType.toUserActionEntityType(): UserActionEntityType {
    return when (this) {
        EventType.WORKOUT -> UserActionEntityType.WORKOUT
        EventType.REST -> UserActionEntityType.REST
        EventType.BUSY -> UserActionEntityType.BUSY
        EventType.SICK -> UserActionEntityType.SICK
        EventType.RACE_EVENT -> UserActionEntityType.RACE_EVENT
    }
}

private fun MutableMap<String, String>.putWorkoutCategoryMetadata(
    categoryId: Long?,
    categoryName: String?,
    newCategoryName: String?,
    oldCategoryId: Long? = null,
    oldCategoryName: String? = null,
) {
    categoryId?.let { put(CATEGORY_ID, it.toString()) }
    if (!categoryName.isNullOrBlank()) {
        put(CATEGORY_NAME, categoryName)
    }
    if (!newCategoryName.isNullOrBlank()) {
        put(NEW_CATEGORY_NAME, newCategoryName)
    }
    oldCategoryId?.let { put(OLD_CATEGORY_ID, it.toString()) }
    if (!oldCategoryName.isNullOrBlank()) {
        put(OLD_CATEGORY_NAME, oldCategoryName)
    }
}
