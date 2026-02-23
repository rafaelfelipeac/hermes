package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.AUTO_WHEN_MULTIPLE
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.WORKOUT
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutDayIndicator
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import java.time.DayOfWeek
import java.time.LocalDate

data class WeeklyTrainingState(
    val selectedDate: LocalDate,
    val weekStartDate: LocalDate,
    val workouts: List<WorkoutUi>,
    val isWeekLoaded: Boolean,
    val categories: List<CategoryUi>,
    val slotModePolicy: SlotModePolicy = AUTO_WHEN_MULTIPLE,
) {
    val dayIndicators: Map<DayOfWeek, WorkoutDayIndicator> =
        workouts
            .filter { it.dayOfWeek != null }
            .groupBy { requireNotNull(it.dayOfWeek) }
            .mapNotNull { (day, items) ->
                val lastItem = items.maxByOrNull { it.order } ?: return@mapNotNull null
                val isDayCompleted = items.all { it.eventType != WORKOUT || it.isCompleted }
                day to WorkoutDayIndicator(workout = lastItem, isDayCompleted = isDayCompleted)
            }
            .toMap()
}
