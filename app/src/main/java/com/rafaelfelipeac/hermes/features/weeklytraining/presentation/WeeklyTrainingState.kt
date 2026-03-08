package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.AUTO_WHEN_MULTIPLE
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay.MONDAY
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.BUSY
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.REST
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.SICK
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.WORKOUT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.AFTERNOON
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.MORNING
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.NIGHT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.orderedDays
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WeeklyHeaderSummaryUi
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
    val weekStartDay: WeekStartDay = MONDAY,
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
    val dayOrder: List<DayOfWeek> = orderedDays(weekStartDay.dayOfWeek)
    val weeklyHeaderSummary: WeeklyHeaderSummaryUi? =
        workouts
            .filter { it.dayOfWeek != null }
            .let { scheduledItems ->
                val plannedWorkouts = scheduledItems.count { it.eventType == WORKOUT }

                if (plannedWorkouts == 0) {
                    null
                } else {
                    val completedWorkouts =
                        scheduledItems.count {
                            it.eventType == WORKOUT && it.isCompleted
                        }

                    WeeklyHeaderSummaryUi(
                        plannedWorkouts = plannedWorkouts,
                        completedWorkouts = completedWorkouts,
                        plannedRestEvents = scheduledItems.count { it.eventType == REST },
                        plannedBusyEvents = scheduledItems.count { it.eventType == BUSY },
                        plannedSickEvents = scheduledItems.count { it.eventType == SICK },
                        progress = (completedWorkouts.toFloat() / plannedWorkouts).coerceIn(0f, 1f),
                    )
                }
            }
}

private fun slotRank(slot: TimeSlot?): Int {
    return when (slot ?: MORNING) {
        MORNING -> 0
        AFTERNOON -> 1
        NIGHT -> 2
    }
}
