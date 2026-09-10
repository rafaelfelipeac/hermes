package com.rafaelfelipeac.hermes.features.weeklytraining.domain.command

import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import java.time.LocalDate

data class UndoCopyLastWeekCommand(
    val targetStorageWeekStarts: List<LocalDate>,
    val targetDisplayWeekStart: LocalDate,
    val targetUnassignedStorageWeekStart: LocalDate,
    val previousWorkouts: List<Workout>,
)
