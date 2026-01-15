package com.rafaelfelipeac.hermes.features.trainingweek.presentation

import com.rafaelfelipeac.hermes.core.ui.components.calendar.WorkoutUi
import java.time.DayOfWeek
import java.time.LocalDate

data class TrainingWeekState(
    val selectedDate: LocalDate,
    val weekStartDate: LocalDate,
    val workouts: List<WorkoutUi>
) {
    val daysWithWorkouts: Set<DayOfWeek> = workouts.mapNotNull { it.dayOfWeek }.toSet()
}
