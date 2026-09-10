package com.rafaelfelipeac.hermes.features.weeklytraining.domain.command

interface WeeklyTrainingCommandRepository {
    suspend fun updateSchedule(request: WorkoutScheduleCommand): WeeklyTrainingCommandResult

    suspend fun updateCompletion(request: WorkoutCompletionCommand): WeeklyTrainingCommandResult

    suspend fun deleteWorkout(request: WorkoutDeleteCommand): WeeklyTrainingCommandResult
}
