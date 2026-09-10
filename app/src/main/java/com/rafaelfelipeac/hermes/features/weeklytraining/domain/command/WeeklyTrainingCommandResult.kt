package com.rafaelfelipeac.hermes.features.weeklytraining.domain.command

import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout

sealed interface WeeklyTrainingCommandResult {
    data class WeekCopied(
        val previousWorkouts: List<Workout>,
    ) : WeeklyTrainingCommandResult

    data class CompletionChanged(
        val previousCompleted: Boolean,
        val eventType: EventType,
    ) : WeeklyTrainingCommandResult

    data object ScheduleChanged : WeeklyTrainingCommandResult

    data object DetailsChanged : WeeklyTrainingCommandResult

    data object WorkoutDeleted : WeeklyTrainingCommandResult

    data object NoChange : WeeklyTrainingCommandResult
}
