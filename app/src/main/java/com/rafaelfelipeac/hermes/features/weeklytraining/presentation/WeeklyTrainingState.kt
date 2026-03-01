package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.AUTO_WHEN_MULTIPLE
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.WORKOUT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.AFTERNOON
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.MORNING
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.NIGHT
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
    private val sourceIndexById = workouts.withIndex().associate { it.value.id to it.index }

    val dayIndicators: Map<DayOfWeek, WorkoutDayIndicator> =
        workouts
            .filter { it.dayOfWeek != null }
            .groupBy { requireNotNull(it.dayOfWeek) }
            .mapNotNull { (day, items) ->
                val workoutItems = items.filter { it.eventType == WORKOUT }
                val indicatorPool = workoutItems.ifEmpty { items }
                val lastItem =
                    indicatorPool.maxWithOrNull(
                        compareBy<WorkoutUi> { slotRank(it.timeSlot) }
                            .thenBy { it.order }
                            .thenBy { sourceIndexById[it.id] ?: Int.MIN_VALUE },
                    )
                        ?: return@mapNotNull null
                val isDayCompleted = items.all { it.eventType != WORKOUT || it.isCompleted }

                day to WorkoutDayIndicator(workout = lastItem, isDayCompleted = isDayCompleted)
            }
            .toMap()
}

private fun slotRank(slot: TimeSlot?): Int {
    return when (slot ?: MORNING) {
        MORNING -> 0
        AFTERNOON -> 1
        NIGHT -> 2
    }
}
