package com.rafaelfelipeac.hermes.features.weeklytraining.domain.command

import java.time.LocalDate

data class WorkoutCompletionCommand(
    val workoutId: Long,
    val isCompleted: Boolean,
    val displayWeekStart: LocalDate,
)
