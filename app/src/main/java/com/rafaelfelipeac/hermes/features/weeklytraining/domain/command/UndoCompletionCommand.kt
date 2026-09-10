package com.rafaelfelipeac.hermes.features.weeklytraining.domain.command

import java.time.LocalDate

data class UndoCompletionCommand(
    val workoutId: Long,
    val previousCompleted: Boolean,
    val newCompleted: Boolean,
    val displayWeekStart: LocalDate,
)
