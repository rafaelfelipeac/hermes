package com.rafaelfelipeac.hermes.core.debug

import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import java.time.DayOfWeek
import java.time.LocalDate

internal data class PersonalRecordSeed(
    val title: String,
    val categoryId: Long,
    val metricType: com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType,
    val unit: com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit,
    val comparisonRule: com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule,
    val entries: List<PersonalRecordEntrySeed>,
    val customUnitLabel: String? = null,
    val manualCurrentEntryIndex: Int? = null,
)

internal data class PersonalRecordEntrySeed(
    val value: Double,
    val daysAgo: Long,
)

internal data class DayPlan(
    val dayOfWeek: DayOfWeek?,
    val items: List<WorkoutSeed>,
)

internal data class WorkoutDayChange(
    val oldDay: DayOfWeek,
    val newDay: DayOfWeek,
)

internal data class WorkoutOrderChange(
    val oldOrder: Int,
    val newOrder: Int,
)

internal data class WorkoutSlotChange(
    val oldTimeSlot: TimeSlot,
    val newTimeSlot: TimeSlot,
)

internal data class WorkoutSeed(
    val eventType: EventType,
    val type: String,
    val description: String,
    val timeSlot: TimeSlot? = null,
    val categoryId: Long? = null,
)

internal data class RaceEventPlan(
    val eventDate: LocalDate,
    val seed: WorkoutSeed,
)

internal const val DEMO_PERSONAL_RECORD_ENTRY_HOUR = 12L

internal enum class CompletionProfile {
    LIGHT,
    SOME,
    BALANCED,
    HEAVY,
    COMPLETED_MOST,
    NONE,
    ;

    fun completedDays(): Set<DayOfWeek> {
        return when (this) {
            LIGHT -> setOf(DayOfWeek.MONDAY)
            SOME -> setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
            BALANCED -> setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
            HEAVY -> setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
            COMPLETED_MOST -> DayOfWeek.entries.toSet()
            NONE -> emptySet()
        }
    }
}
