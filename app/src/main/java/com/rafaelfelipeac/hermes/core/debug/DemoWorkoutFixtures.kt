package com.rafaelfelipeac.hermes.core.debug

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_CYCLING
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_MOBILITY
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_OTHER
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_RUN
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_STRENGTH
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_SWIM
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.CYCLING_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.MOBILITY_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.OTHER_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.RUN_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.STRENGTH_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.SWIM_ID
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.BUSY
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.RACE_EVENT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.REST
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.SICK
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.WORKOUT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.AFTERNOON
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.MORNING
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.NIGHT
import java.time.DayOfWeek
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

internal fun buildDemoWorkouts(
    stringProvider: StringProvider,
    historyWeekStarts: List<LocalDate>,
    currentWeekStart: LocalDate,
    nextWeekStart: LocalDate,
): List<WorkoutEntity> {
    return historyWeekStarts.flatMapIndexed { index, weekStart ->
        buildWeekSchedule(
            stringProvider = stringProvider,
            weekStartDate = weekStart,
            completionProfile = completionProfileForHistoryWeek(index),
            plan = historyWeekPlanForIndex(stringProvider, index),
        )
    } +
        buildWeekSchedule(stringProvider, currentWeekStart, CompletionProfile.SOME) +
        buildWeekSchedule(stringProvider, nextWeekStart, CompletionProfile.NONE) +
        buildDemoRaceEvents(stringProvider, currentWeekStart, nextWeekStart)
}

internal fun buildLockedTrophyWorkouts(
    stringProvider: StringProvider,
    currentWeekStart: LocalDate,
    nextWeekStart: LocalDate,
): List<WorkoutEntity> {
    return buildWeekSchedule(stringProvider, currentWeekStart, CompletionProfile.NONE) +
        buildWeekSchedule(stringProvider, nextWeekStart, CompletionProfile.NONE)
}

private fun completionProfileForHistoryWeek(index: Int): CompletionProfile {
    return when (index) {
        0 -> CompletionProfile.NONE
        1 -> CompletionProfile.LIGHT
        2 -> CompletionProfile.SOME
        3 -> CompletionProfile.BALANCED
        4 -> CompletionProfile.HEAVY
        5 -> CompletionProfile.COMPLETED_MOST
        else -> CompletionProfile.BALANCED
    }
}

private fun List<DayPlan>.withAddedWorkout(
    dayOfWeek: DayOfWeek?,
    workout: WorkoutSeed,
): List<DayPlan> {
    var didUpdateDay = false
    val updatedPlans =
        map { plan ->
            if (plan.dayOfWeek == dayOfWeek) {
                didUpdateDay = true
                plan.copy(items = plan.items + workout)
            } else {
                plan
            }
        }.toMutableList()

    if (!didUpdateDay) {
        updatedPlans += DayPlan(dayOfWeek, listOf(workout))
    }

    return updatedPlans
}

private fun buildWeekSchedule(
    stringProvider: StringProvider,
    weekStartDate: LocalDate,
    completionProfile: CompletionProfile,
    plan: List<DayPlan> = defaultWeekPlan(stringProvider),
): List<WorkoutEntity> {
    val completedDays = completionProfile.completedDays()

    return plan.flatMap { dayPlan ->
        val slotOrderByDay = mutableMapOf<TimeSlot?, Int>()

        dayPlan.items.map { seed ->
            val orderInSlot = slotOrderByDay.getOrDefault(seed.timeSlot, 0)
            slotOrderByDay[seed.timeSlot] = orderInSlot + 1
            val isCompleted =
                seed.eventType == WORKOUT &&
                    dayPlan.dayOfWeek != null &&
                    completedDays.contains(dayPlan.dayOfWeek)

            WorkoutEntity(
                weekStartDate = weekStartDate,
                dayOfWeek = dayPlan.dayOfWeek?.value,
                type = if (seed.eventType == WORKOUT) seed.type else EMPTY,
                description = if (seed.eventType == WORKOUT) seed.description else EMPTY,
                isCompleted = isCompleted,
                isRestDay = seed.eventType == REST,
                eventType = seed.eventType.name,
                timeSlot = seed.timeSlot?.name,
                categoryId =
                    if (seed.eventType != WORKOUT) {
                        null
                    } else {
                        categoryIdForSeed(stringProvider, seed)
                    },
                sortOrder = orderInSlot,
            )
        }
    }
}

private fun defaultWeekPlan(stringProvider: StringProvider): List<DayPlan> {
    return listOf(
        DayPlan(MONDAY, listOf(workoutSeed(stringProvider, 0, MORNING), workoutSeed(stringProvider, 1, NIGHT))),
        DayPlan(TUESDAY, listOf(busySeed(MORNING), workoutSeed(stringProvider, 2, AFTERNOON), sickSeed(NIGHT))),
        DayPlan(WEDNESDAY, listOf(restSeed())),
        DayPlan(THURSDAY, listOf(workoutSeed(stringProvider, 3, MORNING), workoutSeed(stringProvider, 4, MORNING))),
        DayPlan(FRIDAY, listOf(workoutSeed(stringProvider, 5))),
        DayPlan(SATURDAY, listOf(restSeed(NIGHT))),
        DayPlan(SUNDAY, listOf(workoutSeed(stringProvider, 6, AFTERNOON))),
        DayPlan(null, listOf(workoutSeed(stringProvider, 7))),
    )
}

private fun historyWeekPlanForIndex(
    stringProvider: StringProvider,
    index: Int,
): List<DayPlan> {
    return when (index) {
        0 -> defaultWeekPlan(stringProvider)
        1 ->
            defaultWeekPlan(stringProvider)
                .withAddedWorkout(MONDAY, workoutSeed(stringProvider, 8, AFTERNOON))
                .withAddedWorkout(THURSDAY, workoutSeed(stringProvider, 9, NIGHT))
        2 ->
            defaultWeekPlan(stringProvider)
                .withAddedWorkout(TUESDAY, workoutSeed(stringProvider, 8, MORNING))
        3 ->
            defaultWeekPlan(stringProvider)
                .withAddedWorkout(WEDNESDAY, workoutSeed(stringProvider, 8, AFTERNOON))
                .withAddedWorkout(FRIDAY, workoutSeed(stringProvider, 9, MORNING))
        4 ->
            defaultWeekPlan(stringProvider)
                .withAddedWorkout(MONDAY, workoutSeed(stringProvider, 8, NIGHT))
                .withAddedWorkout(THURSDAY, workoutSeed(stringProvider, 9, AFTERNOON))
                .withAddedWorkout(SUNDAY, workoutSeed(stringProvider, 10, MORNING))
        5 ->
            defaultWeekPlan(stringProvider)
                .withAddedWorkout(TUESDAY, workoutSeed(stringProvider, 8, AFTERNOON))
                .withAddedWorkout(SATURDAY, workoutSeed(stringProvider, 9, MORNING))
                .withAddedWorkout(SUNDAY, workoutSeed(stringProvider, 10, NIGHT))
        else ->
            defaultWeekPlan(stringProvider)
                .withAddedWorkout(WEDNESDAY, workoutSeed(stringProvider, 8, MORNING))
    }
}

private fun buildDemoRaceEvents(
    stringProvider: StringProvider,
    currentWeekStart: LocalDate,
    nextWeekStart: LocalDate,
): List<WorkoutEntity> {
    val raceEvents =
        listOf(
            RaceEventPlan(
                eventDate = currentWeekStart.plusDays(4),
                seed = raceEventSeed(stringProvider, 0, RUN_ID),
            ),
            RaceEventPlan(
                eventDate = currentWeekStart.plusDays(6),
                seed = raceEventSeed(stringProvider, 2, CYCLING_ID),
            ),
            RaceEventPlan(
                eventDate = nextWeekStart.plusDays(2),
                seed = raceEventSeed(stringProvider, 4, SWIM_ID),
            ),
            RaceEventPlan(
                eventDate = nextWeekStart.plusDays(5),
                seed = raceEventSeed(stringProvider, 6, OTHER_ID),
            ),
        )

    return raceEvents.map { plan ->
        val weekStartDate = plan.eventDate.with(TemporalAdjusters.previousOrSame(MONDAY))

        WorkoutEntity(
            weekStartDate = weekStartDate,
            dayOfWeek = plan.eventDate.dayOfWeek.value,
            type = plan.seed.type,
            description = plan.seed.description,
            isCompleted = false,
            isRestDay = false,
            eventType = RACE_EVENT.name,
            timeSlot = null,
            categoryId = plan.seed.categoryId,
            sortOrder = 0,
        )
    }
}

internal fun categoryIdForSeed(
    stringProvider: StringProvider,
    seed: WorkoutSeed,
): Long {
    val run = stringProvider.get(R.string.mock_workout_type_cardio)
    val swim = stringProvider.get(R.string.mock_workout_type_yoga)
    val cycling = stringProvider.get(R.string.mock_workout_type_hiits)
    val strength = stringProvider.get(R.string.mock_workout_type_strength)
    val mobility = stringProvider.get(R.string.mock_workout_type_mobility)
    val other = stringProvider.get(R.string.category_other)

    val colorId =
        when (seed.type) {
            run -> COLOR_RUN
            swim -> COLOR_SWIM
            cycling -> COLOR_CYCLING
            strength -> COLOR_STRENGTH
            mobility -> COLOR_MOBILITY
            other -> COLOR_OTHER
            else -> COLOR_OTHER
        }

    return when (colorId) {
        COLOR_RUN -> RUN_ID
        COLOR_CYCLING -> CYCLING_ID
        COLOR_STRENGTH -> STRENGTH_ID
        COLOR_SWIM -> SWIM_ID
        COLOR_MOBILITY -> MOBILITY_ID
        else -> OTHER_ID
    }
}

internal fun workoutSeed(
    stringProvider: StringProvider,
    index: Int,
    timeSlot: TimeSlot? = null,
): WorkoutSeed {
    val types =
        listOf(
            stringProvider.get(R.string.mock_workout_type_strength),
            stringProvider.get(R.string.mock_workout_type_upper),
            stringProvider.get(R.string.mock_workout_type_cardio),
            stringProvider.get(R.string.mock_workout_type_yoga),
            stringProvider.get(R.string.mock_workout_type_hiits),
            stringProvider.get(R.string.mock_workout_type_mobility),
            stringProvider.get(R.string.mock_workout_type_long_run),
            stringProvider.get(R.string.mock_workout_type_core),
        )
    val descriptions =
        listOf(
            stringProvider.get(R.string.mock_workout_description_strength),
            stringProvider.get(R.string.mock_workout_description_upper),
            stringProvider.get(R.string.mock_workout_description_cardio),
            stringProvider.get(R.string.mock_workout_description_yoga),
            stringProvider.get(R.string.mock_workout_description_hiits),
            stringProvider.get(R.string.mock_workout_description_mobility),
            stringProvider.get(R.string.mock_workout_description_long_run),
            stringProvider.get(R.string.mock_workout_description_core),
        )

    val safeIndex = index % types.size

    return WorkoutSeed(
        eventType = WORKOUT,
        type = types[safeIndex],
        description = descriptions[safeIndex],
        timeSlot = timeSlot,
    )
}

private fun raceEventSeed(
    stringProvider: StringProvider,
    index: Int,
    categoryId: Long? = null,
): WorkoutSeed {
    val titles =
        listOf(
            stringProvider.get(R.string.mock_workout_type_long_run),
            stringProvider.get(R.string.mock_workout_type_cardio),
            stringProvider.get(R.string.mock_workout_type_strength),
            stringProvider.get(R.string.mock_workout_type_hiits),
        )
    val descriptions =
        listOf(
            stringProvider.get(R.string.mock_workout_description_long_run),
            stringProvider.get(R.string.mock_workout_description_cardio),
            stringProvider.get(R.string.mock_workout_description_strength),
            stringProvider.get(R.string.mock_workout_description_hiits),
        )

    val safeIndex = index % titles.size

    return WorkoutSeed(
        eventType = RACE_EVENT,
        type = titles[safeIndex],
        description = descriptions[safeIndex],
        categoryId = categoryId,
    )
}

internal fun workoutSeedForCategory(
    stringProvider: StringProvider,
    categoryId: Long,
): WorkoutSeed {
    return when (categoryId) {
        RUN_ID -> workoutSeed(stringProvider, index = 2, timeSlot = MORNING)
        CYCLING_ID -> workoutSeed(stringProvider, index = 4, timeSlot = AFTERNOON)
        STRENGTH_ID -> workoutSeed(stringProvider, index = 0, timeSlot = MORNING)
        SWIM_ID -> workoutSeed(stringProvider, index = 3, timeSlot = NIGHT)
        MOBILITY_ID -> workoutSeed(stringProvider, index = 5, timeSlot = NIGHT)
        else ->
            WorkoutSeed(
                eventType = WORKOUT,
                type = stringProvider.get(R.string.category_other),
                description = stringProvider.get(R.string.mock_workout_description_core),
                timeSlot = AFTERNOON,
            )
    }
}

internal fun restSeed(timeSlot: TimeSlot? = null): WorkoutSeed {
    return WorkoutSeed(
        eventType = REST,
        type = EMPTY,
        description = EMPTY,
        timeSlot = timeSlot,
    )
}

internal fun busySeed(timeSlot: TimeSlot? = null): WorkoutSeed {
    return WorkoutSeed(
        eventType = BUSY,
        type = EMPTY,
        description = EMPTY,
        timeSlot = timeSlot,
    )
}

internal fun sickSeed(timeSlot: TimeSlot? = null): WorkoutSeed {
    return WorkoutSeed(
        eventType = SICK,
        type = EMPTY,
        description = EMPTY,
        timeSlot = timeSlot,
    )
}
