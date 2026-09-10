package com.rafaelfelipeac.hermes.features.weeklytraining.domain.command

import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import java.time.DayOfWeek
import java.time.LocalDate

data class CreateWeeklyItemCommand(
    val eventType: EventType,
    val storageWeekStart: LocalDate,
    val displayWeekStart: LocalDate,
    val dayOfWeek: DayOfWeek?,
    val timeSlot: TimeSlot?,
    val type: String,
    val description: String,
    val categoryId: Long?,
)
