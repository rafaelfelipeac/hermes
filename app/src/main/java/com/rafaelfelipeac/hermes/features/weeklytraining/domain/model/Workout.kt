package com.rafaelfelipeac.hermes.features.weeklytraining.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

data class Workout(
    val id: Long,
    val weekStartDate: LocalDate,
    val dayOfWeek: DayOfWeek?,
    val type: String,
    val description: String,
    val isCompleted: Boolean,
    val isRestDay: Boolean,
    val categoryId: Long?,
    val order: Int,
    val eventType: EventType =
        if (isRestDay) {
            EventType.REST
        } else {
            EventType.WORKOUT
        },
    val timeSlot: TimeSlot? = null,
)
