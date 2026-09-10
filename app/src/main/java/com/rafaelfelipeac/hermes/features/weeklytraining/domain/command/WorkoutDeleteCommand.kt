package com.rafaelfelipeac.hermes.features.weeklytraining.domain.command

import java.time.LocalDate

data class WorkoutDeleteCommand(
    val workoutId: Long,
    val displayWeekStart: LocalDate,
)
