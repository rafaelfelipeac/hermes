package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import java.time.LocalDate

data class WeekQuery(
    val displayWeekStart: LocalDate,
    val weekStarts: List<LocalDate>,
    val unassignedStorageWeekStart: LocalDate,
)
