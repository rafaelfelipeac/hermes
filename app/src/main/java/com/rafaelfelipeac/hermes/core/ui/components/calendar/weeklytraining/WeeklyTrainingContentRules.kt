package com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining

import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.ALWAYS_SHOW
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.AUTO_WHEN_MULTIPLE
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.WORKOUT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.AFTERNOON
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.MORNING
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.NIGHT
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import java.time.DayOfWeek
import java.time.LocalDate

internal fun buildWeeklyTrainingSections(
    workouts: List<WorkoutUi>,
    dayOrder: List<DayOfWeek>,
): List<SectionKey> {
    return buildList {
        if (workouts.any { it.dayOfWeek == null }) {
            add(SectionKey.ToBeDefined)
        }

        dayOrder.forEach { dayOfWeek ->
            add(SectionKey.Day(dayOfWeek))
        }
    }
}

internal fun buildWeeklyTrainingSectionDates(
    selectedDate: LocalDate,
    dayOrder: List<DayOfWeek>,
): Map<SectionKey, LocalDate> {
    val selectedIndex = dayOrder.indexOf(selectedDate.dayOfWeek).coerceAtLeast(0)
    val weekStartDate = selectedDate.minusDays(selectedIndex.toLong())

    return buildMap {
        dayOrder.forEachIndexed { index, dayOfWeek ->
            put(SectionKey.Day(dayOfWeek), weekStartDate.plusDays(index.toLong()))
        }
    }
}

internal fun buildWeeklyTrainingWorkoutsBySection(
    workouts: List<WorkoutUi>,
    sections: List<SectionKey>,
): Map<SectionKey, List<WorkoutUi>> {
    return sections.associateWith { section ->
        workouts
            .filter { it.dayOfWeek == section.dayOfWeekOrNull() }
            .sortedBy { it.order }
    }
}

internal fun buildWeeklyTrainingWorkoutsBySlot(workouts: List<WorkoutUi>): Map<TimeSlot, List<WorkoutUi>> {
    return listOf(MORNING, AFTERNOON, NIGHT).associateWith { slot ->
        workouts
            .filter { effectiveSlot(it.timeSlot) == slot }
            .sortedBy { it.order }
    }
}

internal fun buildWeeklyTrainingDayUsesSlots(
    workouts: List<WorkoutUi>,
    sections: List<SectionKey>,
    slotModePolicy: SlotModePolicy,
): Map<DayOfWeek, Boolean> {
    return sections
        .mapNotNull { section -> (section as? SectionKey.Day)?.dayOfWeek }
        .associateWith { day ->
            shouldUseSlotMode(slotModePolicy, workouts.count { it.dayOfWeek == day })
        }
}

internal fun shouldUseSlotMode(
    policy: SlotModePolicy,
    dayItemCount: Int,
): Boolean {
    return when (policy) {
        ALWAYS_SHOW -> true
        AUTO_WHEN_MULTIPLE -> dayItemCount >= AUTO_SLOT_THRESHOLD
    }
}

internal fun effectiveSlot(timeSlot: TimeSlot?): TimeSlot {
    return timeSlot ?: MORNING
}

internal fun WorkoutUi.shouldDeemphasize(focusedCategoryId: Long?): Boolean {
    return focusedCategoryId != null &&
        eventType == WORKOUT &&
        categoryId != focusedCategoryId
}

internal fun TimeSlot.labelRes(): Int {
    return when (this) {
        MORNING -> com.rafaelfelipeac.hermes.R.string.weekly_training_slot_morning
        AFTERNOON -> com.rafaelfelipeac.hermes.R.string.weekly_training_slot_afternoon
        NIGHT -> com.rafaelfelipeac.hermes.R.string.weekly_training_slot_night
    }
}

private const val AUTO_SLOT_THRESHOLD = 2
