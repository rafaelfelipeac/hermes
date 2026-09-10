package com.rafaelfelipeac.hermes.features.weeklytraining.domain.command

import java.time.LocalDate

data class UndoScheduleCommand(
    val movedWorkoutId: Long,
    val displayWeekStart: LocalDate,
    val previousPositions: List<WorkoutScheduleChange>,
)
