package com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model

import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import java.time.DayOfWeek
import java.time.LocalDate

typealias WorkoutId = Long

data class WorkoutUi(
    val id: WorkoutId,
    val weekStartDate: LocalDate = LocalDate.MIN,
    val dayOfWeek: DayOfWeek?,
    val type: String,
    val description: String,
    val isCompleted: Boolean,
    val isRestDay: Boolean,
    val categoryId: Long?,
    val categoryColorId: String?,
    val categoryName: String?,
    val order: Int,
    val eventType: EventType =
        if (isRestDay) {
            EventType.REST
        } else {
            EventType.WORKOUT
        },
    val timeSlot: TimeSlot? = null,
)
