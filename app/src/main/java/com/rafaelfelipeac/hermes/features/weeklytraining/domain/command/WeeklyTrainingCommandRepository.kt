package com.rafaelfelipeac.hermes.features.weeklytraining.domain.command

interface WeeklyTrainingCommandRepository {
    suspend fun updateCompletion(request: WorkoutCompletionCommand): WeeklyTrainingCommandResult
}
