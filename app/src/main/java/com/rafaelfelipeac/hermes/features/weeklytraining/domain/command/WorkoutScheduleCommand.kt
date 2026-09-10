package com.rafaelfelipeac.hermes.features.weeklytraining.domain.command

import java.time.LocalDate

data class WorkoutScheduleCommand(
    val movedWorkoutId: Long,
    val displayWeekStart: LocalDate,
    val changes: List<WorkoutScheduleChange>,
)
