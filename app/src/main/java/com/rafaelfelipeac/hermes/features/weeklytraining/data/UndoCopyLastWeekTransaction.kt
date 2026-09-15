package com.rafaelfelipeac.hermes.features.weeklytraining.data

import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_COPY_LAST_WEEK
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.UndoCopyLastWeekCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WeeklyTrainingCommandResult
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.weekDates

internal class UndoCopyLastWeekTransaction(
    private val workoutDao: WorkoutDao,
    private val userActionLogger: UserActionLogger,
) {
    suspend fun apply(request: UndoCopyLastWeekCommand): WeeklyTrainingCommandResult {
        val previousEntities = request.previousWorkouts.map { it.toEntity() }
        workoutDao.replaceWorkoutsForDisplayWeek(
            targetStorageWeekStarts = request.targetStorageWeekStarts,
            targetDisplayDates = weekDates(request.targetDisplayWeekStart),
            targetUnassignedStorageWeekStart = request.targetUnassignedStorageWeekStart,
            replacementWorkouts = previousEntities,
        )

        userActionLogger.log(
            actionType = UNDO_COPY_LAST_WEEK,
            entityType = WEEK,
            entityId = request.targetDisplayWeekStart.toEpochDay(),
            metadata =
                mapOf(
                    WEEK_START_DATE to request.targetDisplayWeekStart.toString(),
                ),
        )

        return WeeklyTrainingCommandResult.UndoApplied
    }
}
