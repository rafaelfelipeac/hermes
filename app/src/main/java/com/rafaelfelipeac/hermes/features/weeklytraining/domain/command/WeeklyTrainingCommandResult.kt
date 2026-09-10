package com.rafaelfelipeac.hermes.features.weeklytraining.domain.command

import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType

sealed interface WeeklyTrainingCommandResult {
    data class CompletionChanged(
        val previousCompleted: Boolean,
        val eventType: EventType,
    ) : WeeklyTrainingCommandResult

    data object ScheduleChanged : WeeklyTrainingCommandResult

    data object DetailsChanged : WeeklyTrainingCommandResult

    data object WorkoutDeleted : WeeklyTrainingCommandResult

    data object NoChange : WeeklyTrainingCommandResult
}
