package com.rafaelfelipeac.hermes.features.weeklytraining.domain.command

interface WeeklyTrainingCommandRepository {
    suspend fun copyLastWeek(request: CopyLastWeekCommand): WeeklyTrainingCommandResult

    suspend fun updateSchedule(request: WorkoutScheduleCommand): WeeklyTrainingCommandResult

    suspend fun updateDetails(request: WorkoutDetailsCommand): WeeklyTrainingCommandResult

    suspend fun updateCompletion(request: WorkoutCompletionCommand): WeeklyTrainingCommandResult

    suspend fun deleteWorkout(request: WorkoutDeleteCommand): WeeklyTrainingCommandResult
}
