package com.rafaelfelipeac.hermes.features.weeklytraining.domain.command

import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import java.time.LocalDate

data class WorkoutDetailsCommand(
    val workoutId: Long,
    val type: String,
    val description: String,
    val eventType: EventType,
    val categoryId: Long?,
    val displayWeekStart: LocalDate,
    val targetDate: LocalDate? = null,
)
