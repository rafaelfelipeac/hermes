package com.rafaelfelipeac.hermes.features.weeklytraining.data

import androidx.room.withTransaction
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.IS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TIME_SLOT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_TIME_SLOT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WAS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues.UNPLANNED
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.canonicalStorageWeekStart
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WeeklyTrainingCommandRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WeeklyTrainingCommandResult
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutCompletionCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutDeleteCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutDetailsCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutScheduleChange
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutScheduleCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import java.time.LocalDate
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

        override suspend fun updateSchedule(request: WorkoutScheduleCommand): WeeklyTrainingCommandResult {
            if (request.changes.isEmpty()) {
                return WeeklyTrainingCommandResult.NoChange
            }

            return database.withTransaction {
                val persistedById =
                    request.changes
                        .map { it.workoutId }
                        .distinct()
                        .associateWith { workoutDao.getWorkout(it) }

                val movedWorkout =
                    persistedById[request.movedWorkoutId]
                        ?: return@withTransaction WeeklyTrainingCommandResult.NoChange
                if (persistedById.values.any { it == null }) {
                    return@withTransaction WeeklyTrainingCommandResult.NoChange
                }

                request.changes.forEach { change ->
                    workoutDao.updateSchedule(
                        id = change.workoutId,
                        weekStartDate = change.weekStartDate,
                        dayOfWeek = change.dayOfWeek?.value,
                        timeSlot = change.timeSlot?.name,
                        order = change.order,
                    )
                }

                val movedChange =
                    request.changes.firstOrNull { it.workoutId == request.movedWorkoutId }
                        ?: return@withTransaction WeeklyTrainingCommandResult.NoChange
                val movedEventType = movedWorkout.eventType.toEventType(movedWorkout.isRestDay)
                val categoryName = movedWorkout.categoryId?.let { categoryDao.getCategory(it)?.name }

                userActionLogger.log(
                    actionType = movedEventType.toScheduleActionType(movedWorkout, movedChange),
                    entityType = movedEventType.toUserActionEntityType(),
                    entityId = request.movedWorkoutId,
                    metadata =
                        mutableMapOf(
                            WEEK_START_DATE to request.displayWeekStart.toString(),
                            OLD_DAY_OF_WEEK to (movedWorkout.dayOfWeek?.toString() ?: UNPLANNED),
                            NEW_DAY_OF_WEEK to (movedChange.dayOfWeek?.value?.toString() ?: UNPLANNED),
                            OLD_TIME_SLOT to (movedWorkout.timeSlot ?: UNPLANNED),
                            NEW_TIME_SLOT to (movedChange.timeSlot?.name ?: UNPLANNED),
                            OLD_ORDER to movedWorkout.sortOrder.toString(),
                            NEW_ORDER to movedChange.order.toString(),
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

                WeeklyTrainingCommandResult.ScheduleChanged
            }
        }

        override suspend fun updateDetails(request: WorkoutDetailsCommand): WeeklyTrainingCommandResult {
            return database.withTransaction {
                val workout =
                    workoutDao.getWorkout(request.workoutId)
                        ?: return@withTransaction WeeklyTrainingCommandResult.NoChange
                val originalEventType = workout.eventType.toEventType(workout.isRestDay)
                val updatePlan = buildDetailsUpdatePlan(workout, request)
                val oldCategoryName =
                    workout.categoryId
                        ?.takeIf { originalEventType.supportsCategory() }
                        ?.let { categoryDao.getCategory(it)?.name }
                val newCategoryName =
                    request.categoryId
                        ?.takeIf { request.eventType.supportsCategory() }
                        ?.let { categoryDao.getCategory(it)?.name }

                if (updatePlan.dateChanged) {
                    workoutDao.updateSchedule(
                        id = request.workoutId,
                        weekStartDate = updatePlan.weekStartDate,
                        dayOfWeek = updatePlan.dayOfWeek,
                        timeSlot = null,
                        order = updatePlan.order,
                    )
                    if (request.eventType == EventType.RACE_EVENT) {
                        normalizeOrdersAfterMove(
                            movedWorkoutId = request.workoutId,
                            weekStartDate = workout.weekStartDate,
                            dayOfWeek = workout.dayOfWeek,
                            timeSlot = workout.timeSlot,
                        )
                    }
                }

                workoutDao.updateDetails(
                    id = request.workoutId,
                    type = request.type,
                    description = request.description,
                    isRestDay = request.eventType == EventType.REST,
                    eventType = request.eventType.name,
                    categoryId = request.categoryId,
                )

                logDetailsUpdate(
                    DetailsMetadataInput(
                        request = request,
                        original = workout,
                        originalEventType = originalEventType,
                        updatePlan = updatePlan,
                        oldCategoryName = oldCategoryName,
                        newCategoryName = newCategoryName,
                    ),
                )

                WeeklyTrainingCommandResult.DetailsChanged
            }
        }

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

        private suspend fun logDetailsUpdate(input: DetailsMetadataInput) {
            userActionLogger.log(
                actionType =
                    resolveDetailsActionType(
                        originalEventType = input.originalEventType,
                        newEventType = input.request.eventType,
                        dateChanged = input.updatePlan.dateChanged,
                    ),
                entityType =
                    if (input.request.eventType != EventType.WORKOUT) {
                        input.request.eventType.toUserActionEntityType()
                    } else {
                        input.originalEventType.toUserActionEntityType()
                    },
                entityId = input.request.workoutId,
                metadata = buildDetailsMetadata(input),
            )
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

        private suspend fun normalizeOrdersAfterMove(
            movedWorkoutId: Long,
            weekStartDate: LocalDate,
            dayOfWeek: Int?,
            timeSlot: String?,
        ) {
            val remaining =
                workoutDao.getWorkoutsForWeek(weekStartDate)
                    .filter {
                        it.id != movedWorkoutId &&
                            it.dayOfWeek == dayOfWeek &&
                            it.timeSlot == timeSlot
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

        private suspend fun buildDetailsUpdatePlan(
            workout: WorkoutEntity,
            request: WorkoutDetailsCommand,
        ): DetailsUpdatePlan {
            val originalDate = workout.dayOfWeek?.let { workout.weekStartDate.plusDays((it - 1).toLong()) }
            val targetDate = request.targetDate ?: originalDate
            val targetStorageWeekStart = targetDate?.let(::canonicalStorageWeekStart) ?: workout.weekStartDate
            val targetDayOfWeek = targetDate?.dayOfWeek?.value ?: workout.dayOfWeek
            val dateChanged =
                targetDate != null &&
                    request.eventType in setOf(EventType.WORKOUT, EventType.RACE_EVENT) &&
                    (workout.weekStartDate != targetStorageWeekStart || workout.dayOfWeek != targetDayOfWeek)
            val order =
                if (dateChanged) {
                    workoutDao.getWorkoutsForWeek(targetStorageWeekStart)
                        .count { candidate ->
                            candidate.id != request.workoutId &&
                                candidate.dayOfWeek == targetDayOfWeek &&
                                candidate.timeSlot == null
                        }
                } else {
                    workout.sortOrder
                }

            return DetailsUpdatePlan(
                dateChanged = dateChanged,
                weekStartDate = targetStorageWeekStart,
                dayOfWeek = targetDayOfWeek,
                order = order,
            )
        }
    }

private data class DetailsUpdatePlan(
    val dateChanged: Boolean,
    val weekStartDate: LocalDate,
    val dayOfWeek: Int?,
    val order: Int,
)

private data class DetailsMetadataInput(
    val request: WorkoutDetailsCommand,
    val original: WorkoutEntity,
    val originalEventType: EventType,
    val updatePlan: DetailsUpdatePlan,
    val oldCategoryName: String?,
    val newCategoryName: String?,
)

private fun String.toEventType(isRestDay: Boolean): EventType {
    return runCatching { EventType.valueOf(this) }
        .getOrDefault(if (isRestDay) EventType.REST else EventType.WORKOUT)
}

private fun EventType.supportsCompletion(): Boolean {
    return this == EventType.WORKOUT || this == EventType.RACE_EVENT
}

private fun EventType.supportsCategory(): Boolean {
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

private fun EventType.toScheduleActionType(
    original: WorkoutEntity,
    change: WorkoutScheduleChange,
): UserActionType {
    return if (original.dayOfWeek != change.dayOfWeek?.value || original.timeSlot != change.timeSlot?.name) {
        toMoveActionType()
    } else {
        toReorderActionType()
    }
}

private fun EventType.toReorderActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.REORDER_WORKOUT
        EventType.REST -> UserActionType.REORDER_REST
        EventType.BUSY -> UserActionType.REORDER_BUSY
        EventType.SICK -> UserActionType.REORDER_SICK
        EventType.RACE_EVENT -> UserActionType.REORDER_RACE_EVENT
    }
}

private fun EventType.toMoveActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.MOVE_WORKOUT_BETWEEN_DAYS
        EventType.REST -> UserActionType.MOVE_REST
        EventType.BUSY -> UserActionType.MOVE_BUSY
        EventType.SICK -> UserActionType.MOVE_SICK
        EventType.RACE_EVENT -> UserActionType.MOVE_RACE_EVENT
    }
}

private fun resolveDetailsActionType(
    originalEventType: EventType,
    newEventType: EventType,
    dateChanged: Boolean,
): UserActionType {
    return when {
        dateChanged -> newEventType.toMoveActionType()
        originalEventType != newEventType ->
            when (newEventType) {
                EventType.WORKOUT -> UserActionType.CONVERT_REST_DAY_TO_WORKOUT
                EventType.RACE_EVENT -> newEventType.toUpdateActionType()
                else -> UserActionType.CONVERT_WORKOUT_TO_REST_DAY
            }
        newEventType != EventType.WORKOUT -> newEventType.toUpdateActionType()
        else -> UserActionType.UPDATE_WORKOUT
    }
}

private fun EventType.toUpdateActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.UPDATE_WORKOUT
        EventType.REST -> UserActionType.UPDATE_REST_DAY
        EventType.BUSY -> UserActionType.UPDATE_BUSY
        EventType.SICK -> UserActionType.UPDATE_SICK
        EventType.RACE_EVENT -> UserActionType.UPDATE_RACE_EVENT
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

private fun buildDetailsMetadata(input: DetailsMetadataInput): Map<String, String> {
    return if (input.updatePlan.dateChanged) {
        buildMovedDetailsMetadata(input)
    } else {
        buildUpdatedDetailsMetadata(input)
    }
}

private fun buildMovedDetailsMetadata(input: DetailsMetadataInput): Map<String, String> {
    val request = input.request
    val original = input.original
    val updatePlan = input.updatePlan

    return mutableMapOf(
        WEEK_START_DATE to updatePlan.weekStartDate.toString(),
        OLD_WEEK_START_DATE to original.weekStartDate.toString(),
        NEW_WEEK_START_DATE to updatePlan.weekStartDate.toString(),
        OLD_DAY_OF_WEEK to (original.dayOfWeek?.toString() ?: updatePlan.dayOfWeek?.toString().orEmpty()),
        NEW_DAY_OF_WEEK to (updatePlan.dayOfWeek?.toString() ?: UNPLANNED),
        OLD_ORDER to original.sortOrder.toString(),
        NEW_ORDER to updatePlan.order.toString(),
        OLD_TYPE to original.type,
        NEW_TYPE to request.type,
        OLD_DESCRIPTION to original.description,
        NEW_DESCRIPTION to request.description,
    ).apply {
        putWorkoutCategoryMetadata(
            categoryId = request.categoryId,
            categoryName = input.newCategoryName,
            newCategoryId = request.categoryId,
            newCategoryName = input.newCategoryName,
            oldCategoryId = original.categoryId.takeIf { input.originalEventType.supportsCategory() },
            oldCategoryName = input.oldCategoryName,
        )
    }
}

private fun buildUpdatedDetailsMetadata(input: DetailsMetadataInput): Map<String, String> {
    val request = input.request
    val original = input.original
    val weekStartDate =
        if (request.eventType == EventType.RACE_EVENT && request.targetDate != null) {
            input.updatePlan.weekStartDate
        } else {
            request.displayWeekStart
        }

    return mutableMapOf(
        WEEK_START_DATE to weekStartDate.toString(),
        OLD_TYPE to original.type,
        NEW_TYPE to request.type,
        OLD_DESCRIPTION to original.description,
        NEW_DESCRIPTION to request.description,
    ).apply {
        if (request.eventType != EventType.REST) {
            putWorkoutCategoryMetadata(
                categoryId = request.categoryId,
                categoryName = input.newCategoryName,
                newCategoryId = request.categoryId,
                newCategoryName = input.newCategoryName,
                oldCategoryId = original.categoryId.takeIf { input.originalEventType.supportsCategory() },
                oldCategoryName = input.oldCategoryName,
            )
        }
    }
}

private fun MutableMap<String, String>.putWorkoutCategoryMetadata(
    categoryId: Long?,
    categoryName: String?,
    newCategoryId: Long? = null,
    newCategoryName: String?,
    oldCategoryId: Long? = null,
    oldCategoryName: String? = null,
) {
    categoryId?.let { put(CATEGORY_ID, it.toString()) }
    newCategoryId?.let { put(NEW_CATEGORY_ID, it.toString()) }
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
