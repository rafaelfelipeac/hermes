package com.rafaelfelipeac.hermes.features.weeklytraining.domain.command

import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import java.time.LocalDate

data class UndoDeleteCommand(
    val workout: Workout,
    val displayWeekStart: LocalDate,
    val previousPositions: List<WorkoutScheduleChange>,
)
