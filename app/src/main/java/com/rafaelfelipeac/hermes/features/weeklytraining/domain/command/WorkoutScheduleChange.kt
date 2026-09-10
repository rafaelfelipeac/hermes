package com.rafaelfelipeac.hermes.features.weeklytraining.domain.command

import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import java.time.DayOfWeek
import java.time.LocalDate

data class WorkoutScheduleChange(
    val workoutId: Long,
    val weekStartDate: LocalDate,
    val dayOfWeek: DayOfWeek?,
    val timeSlot: TimeSlot?,
    val order: Int,
)
