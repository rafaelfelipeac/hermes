package com.rafaelfelipeac.hermes.features.weeklytraining.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

data class AddWorkoutRequest(
    val weekStartDate: LocalDate,
    val dayOfWeek: DayOfWeek?,
    val type: String,
    val description: String,
    val categoryId: Long?,
    val order: Int,
)
