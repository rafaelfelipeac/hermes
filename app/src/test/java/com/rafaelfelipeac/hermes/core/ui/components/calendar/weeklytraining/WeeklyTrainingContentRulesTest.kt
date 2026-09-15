package com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining

import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.ALWAYS_SHOW
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.AUTO_WHEN_MULTIPLE
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.RACE_EVENT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.WORKOUT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.AFTERNOON
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.MORNING
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.NIGHT
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDate

class WeeklyTrainingContentRulesTest {
    @Test
    fun buildWeeklyTrainingSections_placesTbdFirstAndRespectsDayOrder() {
        val sections =
            buildWeeklyTrainingSections(
                workouts =
                    listOf(
                        workout(id = 1L, dayOfWeek = null, order = 2),
                        workout(id = 2L, dayOfWeek = WEDNESDAY, order = 0),
                    ),
                dayOrder = listOf(WEDNESDAY, MONDAY),
            )

        assertEquals(
            listOf(
                SectionKey.ToBeDefined,
                SectionKey.Day(WEDNESDAY),
                SectionKey.Day(MONDAY),
            ),
            sections,
        )
    }

    @Test
    fun buildWeeklyTrainingSectionDates_tracksMondayAndSundayStarts() {
        val mondayStart =
            buildWeeklyTrainingSectionDates(
                selectedDate = LocalDate.of(2026, 1, 15),
                dayOrder =
                    listOf(
                        MONDAY,
                        TUESDAY,
                        WEDNESDAY,
                        THURSDAY,
                        FRIDAY,
                        SATURDAY,
                        SUNDAY,
                    ),
            )
        val sundayStart =
            buildWeeklyTrainingSectionDates(
                selectedDate = LocalDate.of(2026, 1, 15),
                dayOrder =
                    listOf(
                        SUNDAY,
                        MONDAY,
                        TUESDAY,
                        WEDNESDAY,
                        THURSDAY,
                        FRIDAY,
                        SATURDAY,
                    ),
            )

        assertEquals(LocalDate.of(2026, 1, 12), mondayStart[SectionKey.Day(MONDAY)])
        assertEquals(LocalDate.of(2026, 1, 18), mondayStart[SectionKey.Day(SUNDAY)])
        assertEquals(LocalDate.of(2026, 1, 11), sundayStart[SectionKey.Day(SUNDAY)])
        assertEquals(LocalDate.of(2026, 1, 17), sundayStart[SectionKey.Day(SATURDAY)])
    }

    @Test
    fun buildWeeklyTrainingWorkoutsBySection_sortsByOrderAndKeepsStableTies() {
        val first = workout(id = 1L, dayOfWeek = MONDAY, order = 1)
        val second = workout(id = 2L, dayOfWeek = MONDAY, order = 1)
        val third = workout(id = 3L, dayOfWeek = TUESDAY, order = 0)
        val sections = listOf(SectionKey.Day(MONDAY), SectionKey.Day(TUESDAY))

        val grouped =
            buildWeeklyTrainingWorkoutsBySection(
                workouts = listOf(second, first, third),
                sections = sections,
            )

        assertEquals(listOf(second, first), grouped[SectionKey.Day(MONDAY)])
        assertEquals(listOf(third), grouped[SectionKey.Day(TUESDAY)])
    }

    @Test
    fun buildWeeklyTrainingWorkoutsBySlot_usesMorningForNullSlot() {
        val morning = workout(id = 1L, dayOfWeek = MONDAY, order = 0, timeSlot = MORNING)
        val nullSlot = workout(id = 2L, dayOfWeek = MONDAY, order = 1, timeSlot = null)
        val afternoon = workout(id = 3L, dayOfWeek = MONDAY, order = 2, timeSlot = AFTERNOON)
        val night = workout(id = 4L, dayOfWeek = MONDAY, order = 3, timeSlot = NIGHT)

        val grouped =
            buildWeeklyTrainingWorkoutsBySlot(
                workouts = listOf(afternoon, nullSlot, night, morning),
            )

        assertEquals(listOf(morning, nullSlot), grouped[MORNING])
        assertEquals(listOf(afternoon), grouped[AFTERNOON])
        assertEquals(listOf(night), grouped[NIGHT])
    }

    @Test
    fun shouldUseSlotMode_respectsPolicyThresholds() {
        assertTrue(shouldUseSlotMode(ALWAYS_SHOW, dayItemCount = 1))
        assertFalse(shouldUseSlotMode(AUTO_WHEN_MULTIPLE, dayItemCount = 1))
        assertTrue(shouldUseSlotMode(AUTO_WHEN_MULTIPLE, dayItemCount = 2))
    }

    @Test
    fun shouldDeemphasize_onlyDimsOtherWorkoutCategories() {
        val focused = workout(id = 1L, dayOfWeek = MONDAY, order = 0, categoryId = 10L)
        val otherWorkout = workout(id = 2L, dayOfWeek = MONDAY, order = 1, categoryId = 20L)
        val race = workout(id = 3L, dayOfWeek = MONDAY, order = 2, eventType = RACE_EVENT)

        assertFalse(focused.shouldDeemphasize(10L))
        assertTrue(otherWorkout.shouldDeemphasize(10L))
        assertFalse(race.shouldDeemphasize(10L))
    }
}

private fun workout(
    id: Long,
    dayOfWeek: java.time.DayOfWeek?,
    order: Int,
    timeSlot: com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot? = null,
    categoryId: Long? = null,
    eventType: com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType = WORKOUT,
): WorkoutUi {
    return WorkoutUi(
        id = id,
        weekStartDate = LocalDate.of(2026, 1, 12),
        dayOfWeek = dayOfWeek,
        type = "Type$id",
        description = "",
        isCompleted = false,
        isRestDay = false,
        categoryId = categoryId,
        categoryColorId = null,
        categoryName = null,
        order = order,
        eventType = eventType,
        timeSlot = timeSlot,
    )
}
