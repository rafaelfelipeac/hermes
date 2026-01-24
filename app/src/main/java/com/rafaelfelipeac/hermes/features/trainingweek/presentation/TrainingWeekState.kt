package com.rafaelfelipeac.hermes.features.trainingweek.presentation

import com.rafaelfelipeac.hermes.core.ui.components.calendar.DayIndicator
import com.rafaelfelipeac.hermes.features.trainingweek.presentation.model.WorkoutUi
import java.time.DayOfWeek
import java.time.LocalDate

data class TrainingWeekState(
    val selectedDate: LocalDate,
    val weekStartDate: LocalDate,
    val workouts: List<WorkoutUi>,
) {
    val dayIndicators: Map<DayOfWeek, DayIndicator> =
        workouts
            .filter { it.dayOfWeek != null }
            .groupBy { it.dayOfWeek!! }
            .mapNotNull { (day, items) ->
                when {
                    items.any { !it.isRestDay && !it.isCompleted } ->
                        day to DayIndicator.Workout
                    items.any { !it.isRestDay } ->
                        day to DayIndicator.Completed
                    else -> null
                }
            }
            .toMap()
}
