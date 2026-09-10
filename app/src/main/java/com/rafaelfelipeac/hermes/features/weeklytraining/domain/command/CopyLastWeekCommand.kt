package com.rafaelfelipeac.hermes.features.weeklytraining.domain.command

import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import java.time.LocalDate

data class CopyLastWeekCommand(
    val targetStorageWeekStarts: List<LocalDate>,
    val targetDisplayWeekStart: LocalDate,
    val targetUnassignedStorageWeekStart: LocalDate,
    val sourceDisplayWeekStart: LocalDate,
    val replacementWorkouts: List<Workout>,
)
