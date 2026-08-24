@file:Suppress("LargeClass", "LongParameterList", "TooManyFunctions")

package com.rafaelfelipeac.hermes.core.debug

import com.rafaelfelipeac.hermes.BuildConfig
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.AppConstants.UNSUPPORTED_USER_ACTION_ENTITY_TYPE
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionDao
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionEntity
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.IS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TIME_SLOT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_TIME_SLOT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PACE_CALCULATOR_MODE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_COMPARISON_RULE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_ENTRY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_FAMILY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_FAMILY_TITLE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_METRIC_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_NORMALIZED_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_RECORD_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_UNIT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.RESULT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WAS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataSerializer
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues.UNPLANNED
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_CYCLING
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_MOBILITY
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_RUN
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_STRENGTH
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_SWIM
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.CYCLING_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.MOBILITY_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.OTHER_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.RUN_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.STRENGTH_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.SWIM_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.pacecalculator.domain.PaceCalculatorMode
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordDao
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordEntryEntity
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordFamilyEntity
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.ALWAYS_SHOW
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.AUTO_WHEN_MULTIPLE
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
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
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

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

@Singleton
class DemoDataSeeder
    @Inject
    constructor(
        private val workoutDao: WorkoutDao,
        private val userActionDao: UserActionDao,
        private val personalRecordDao: PersonalRecordDao,
        private val stringProvider: StringProvider,
        private val categorySeeder: CategorySeeder,
        private val settingsRepository: SettingsRepository,
    ) {
        suspend fun clearDatabase(): Boolean {
            if (!BuildConfig.DEBUG) return false

            workoutDao.deleteAll()
            userActionDao.deleteAll()
            personalRecordDao.deleteAllEntries()
            personalRecordDao.deleteAllFamilies()
            categorySeeder.ensureSeeded()
            settingsRepository.setLastSeenTrophyCelebrationToken(null)

            return true
        }

        suspend fun seedCompletedTrophies(): Boolean {
            var didSeed = false

            if (BuildConfig.DEBUG && seed()) {
                val currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(MONDAY))
                buildCompletedTrophyActions(currentWeekStart).forEach { userActionDao.insert(it) }
                didSeed = true
            }

            return didSeed
        }

        suspend fun seedLockedTrophies(): Boolean {
            if (!BuildConfig.DEBUG) return false

            categorySeeder.ensureSeeded()

            workoutDao.deleteAll()
            userActionDao.deleteAll()
            personalRecordDao.deleteAllEntries()
            personalRecordDao.deleteAllFamilies()
            settingsRepository.setLastSeenTrophyCelebrationToken(null)

            val currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(MONDAY))
            val nextWeekStart = currentWeekStart.plusWeeks(1)
            val workouts =
                buildWeekSchedule(currentWeekStart, CompletionProfile.NONE) +
                    buildWeekSchedule(nextWeekStart, CompletionProfile.NONE)

            workouts.forEach { workoutDao.insert(it) }

            return true
        }

        suspend fun seed(): Boolean {
            if (!BuildConfig.DEBUG) return false

            categorySeeder.ensureSeeded()

            workoutDao.deleteAll()
            userActionDao.deleteAll()
            personalRecordDao.deleteAllEntries()
            personalRecordDao.deleteAllFamilies()

            val today = LocalDate.now()
            val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(MONDAY))
            val previousWeekStart = currentWeekStart.minusWeeks(1)
            val activityHistoryWeekStarts =
                listOf(
                    previousWeekStart.minusWeeks(3),
                    previousWeekStart.minusWeeks(2),
                    previousWeekStart.minusWeeks(1),
                    previousWeekStart,
                )
            val progressHistoryWeekStarts =
                (1..7)
                    .map { weekOffset -> currentWeekStart.minusWeeks(weekOffset.toLong()) }
                    .reversed()
            val nextWeekStart = currentWeekStart.plusWeeks(1)

            val workouts =
                buildDemoWorkouts(
                    historyWeekStarts = progressHistoryWeekStarts,
                    currentWeekStart = currentWeekStart,
                    nextWeekStart = nextWeekStart,
                )

            workouts.forEach { workoutDao.insert(it) }

            seedPersonalRecords(today)

            seedActivityHistory(
                currentWeekStart = currentWeekStart,
                olderWeekStarts = activityHistoryWeekStarts,
                nextWeekStart = nextWeekStart,
            )

            return true
        }

        @Suppress("LongMethod")
        private suspend fun seedPersonalRecords(today: LocalDate) {
            val zoneId = ZoneId.systemDefault()

            personalRecordSeeds().forEachIndexed { sortOrder, seed ->
                val familyCreatedAt =
                    today
                        .minusDays(seed.entries.maxOf { it.daysAgo } + 1)
                        .atStartOfDay(zoneId)
                        .toInstant()
                        .toEpochMilli()
                val family =
                    PersonalRecordFamilyEntity(
                        categoryId = seed.categoryId,
                        title = seed.title,
                        metricType = seed.metricType,
                        defaultUnit = seed.unit,
                        comparisonRule = seed.comparisonRule,
                        manualCurrentEntryId = null,
                        sortOrder = sortOrder,
                        createdAt = familyCreatedAt,
                        updatedAt = System.currentTimeMillis(),
                    )
                val familyId = personalRecordDao.insertFamily(family)

                userActionDao.insert(
                    createPersonalRecordFamilyAction(
                        familyId = familyId,
                        seed = seed,
                        timestamp = familyCreatedAt,
                    ),
                )

                val entryIds =
                    seed.entries.map { entrySeed ->
                        val recordDate = today.minusDays(entrySeed.daysAgo)
                        val timestamp =
                            recordDate
                                .atStartOfDay(zoneId)
                                .plusHours(DEMO_PERSONAL_RECORD_ENTRY_HOUR)
                                .toInstant()
                                .toEpochMilli()
                        val entryId =
                            personalRecordDao.insertEntry(
                                PersonalRecordEntryEntity(
                                    familyId = familyId,
                                    value = entrySeed.value,
                                    unit = seed.unit,
                                    customUnitLabel = seed.customUnitLabel,
                                    recordDate = recordDate,
                                    note = null,
                                    createdAt = timestamp,
                                    updatedAt = timestamp,
                                ),
                            )

                        userActionDao.insert(
                            createPersonalRecordEntryAction(
                                familyId = familyId,
                                entryId = entryId,
                                seed = seed,
                                entrySeed = entrySeed,
                                recordDate = recordDate,
                                timestamp = timestamp,
                            ),
                        )
                        entryId
                    }

                seed.manualCurrentEntryIndex?.let { selectedIndex ->
                    personalRecordDao.updateFamily(
                        family.copy(
                            id = familyId,
                            manualCurrentEntryId = entryIds[selectedIndex],
                        ),
                    )
                }
            }
        }

        @Suppress("LongMethod")
        private fun personalRecordSeeds(): List<PersonalRecordSeed> {
            return listOf(
                PersonalRecordSeed(
                    title = stringProvider.get(R.string.mock_personal_record_5_km),
                    categoryId = RUN_ID,
                    metricType = PersonalRecordMetricType.TIME,
                    unit = PersonalRecordUnit.SECOND,
                    comparisonRule = PersonalRecordComparisonRule.LOWER_IS_BETTER,
                    entries =
                        listOf(
                            PersonalRecordEntrySeed(value = 1_532.0, daysAgo = 45),
                            PersonalRecordEntrySeed(value = 1_485.0, daysAgo = 24),
                            PersonalRecordEntrySeed(value = 1_438.0, daysAgo = 7),
                        ),
                ),
                PersonalRecordSeed(
                    title = stringProvider.get(R.string.mock_personal_record_longest_run),
                    categoryId = RUN_ID,
                    metricType = PersonalRecordMetricType.DISTANCE,
                    unit = PersonalRecordUnit.KILOMETER,
                    comparisonRule = PersonalRecordComparisonRule.HIGHER_IS_BETTER,
                    entries =
                        listOf(
                            PersonalRecordEntrySeed(value = 10.0, daysAgo = 52),
                            PersonalRecordEntrySeed(value = 15.0, daysAgo = 31),
                            PersonalRecordEntrySeed(value = 21.1, daysAgo = 10),
                        ),
                ),
                PersonalRecordSeed(
                    title = stringProvider.get(R.string.mock_personal_record_deadlift),
                    categoryId = STRENGTH_ID,
                    metricType = PersonalRecordMetricType.WEIGHT,
                    unit = PersonalRecordUnit.KILOGRAM,
                    comparisonRule = PersonalRecordComparisonRule.HIGHER_IS_BETTER,
                    entries =
                        listOf(
                            PersonalRecordEntrySeed(value = 100.0, daysAgo = 60),
                            PersonalRecordEntrySeed(value = 110.0, daysAgo = 35),
                            PersonalRecordEntrySeed(value = 120.0, daysAgo = 12),
                        ),
                ),
                PersonalRecordSeed(
                    title = stringProvider.get(R.string.mock_personal_record_cycling_power),
                    categoryId = CYCLING_ID,
                    metricType = PersonalRecordMetricType.POWER,
                    unit = PersonalRecordUnit.WATT,
                    comparisonRule = PersonalRecordComparisonRule.HIGHER_IS_BETTER,
                    entries =
                        listOf(
                            PersonalRecordEntrySeed(value = 245.0, daysAgo = 48),
                            PersonalRecordEntrySeed(value = 268.0, daysAgo = 27),
                            PersonalRecordEntrySeed(value = 286.0, daysAgo = 5),
                        ),
                ),
                PersonalRecordSeed(
                    title = stringProvider.get(R.string.mock_personal_record_push_ups),
                    categoryId = STRENGTH_ID,
                    metricType = PersonalRecordMetricType.REPS,
                    unit = PersonalRecordUnit.REP,
                    comparisonRule = PersonalRecordComparisonRule.HIGHER_IS_BETTER,
                    entries =
                        listOf(
                            PersonalRecordEntrySeed(value = 24.0, daysAgo = 42),
                            PersonalRecordEntrySeed(value = 31.0, daysAgo = 19),
                            PersonalRecordEntrySeed(value = 38.0, daysAgo = 3),
                        ),
                ),
                PersonalRecordSeed(
                    title = stringProvider.get(R.string.mock_personal_record_weekly_consistency),
                    categoryId = OTHER_ID,
                    metricType = PersonalRecordMetricType.CUSTOM,
                    unit = PersonalRecordUnit.CUSTOM,
                    comparisonRule = PersonalRecordComparisonRule.MANUAL,
                    customUnitLabel = stringProvider.get(R.string.mock_personal_record_sessions_unit),
                    entries =
                        listOf(
                            PersonalRecordEntrySeed(value = 3.0, daysAgo = 20),
                            PersonalRecordEntrySeed(value = 5.0, daysAgo = 13),
                            PersonalRecordEntrySeed(value = 4.0, daysAgo = 6),
                        ),
                    manualCurrentEntryIndex = 1,
                ),
            )
        }

        private fun buildDemoWorkouts(
            historyWeekStarts: List<LocalDate>,
            currentWeekStart: LocalDate,
            nextWeekStart: LocalDate,
        ): List<WorkoutEntity> {
            return historyWeekStarts.mapIndexed { index, weekStart ->
                buildWeekSchedule(
                    weekStartDate = weekStart,
                    completionProfile = completionProfileForHistoryWeek(index),
                    plan = historyWeekPlanForIndex(index),
                )
            }.flatten() +
                buildWeekSchedule(currentWeekStart, CompletionProfile.SOME) +
                buildWeekSchedule(nextWeekStart, CompletionProfile.NONE) +
                buildDemoRaceEvents(currentWeekStart, nextWeekStart)
        }

        private fun buildWeekSchedule(
            weekStartDate: LocalDate,
            completionProfile: CompletionProfile,
            plan: List<DayPlan> = defaultWeekPlan(),
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
                                categoryIdForSeed(seed)
                            },
                        sortOrder = orderInSlot,
                    )
                }
            }
        }

        private fun defaultWeekPlan(): List<DayPlan> {
            return listOf(
                DayPlan(MONDAY, listOf(workoutSeed(0, MORNING), workoutSeed(1, NIGHT))),
                DayPlan(TUESDAY, listOf(busySeed(MORNING), workoutSeed(2, AFTERNOON), sickSeed(NIGHT))),
                DayPlan(WEDNESDAY, listOf(restSeed())),
                DayPlan(THURSDAY, listOf(workoutSeed(3, MORNING), workoutSeed(4, MORNING))),
                DayPlan(FRIDAY, listOf(workoutSeed(5))),
                DayPlan(SATURDAY, listOf(restSeed(NIGHT))),
                DayPlan(SUNDAY, listOf(workoutSeed(6, AFTERNOON))),
                DayPlan(null, listOf(workoutSeed(7))),
            )
        }

        private fun historyWeekPlanForIndex(index: Int): List<DayPlan> {
            return when (index) {
                0 -> defaultWeekPlan()
                1 ->
                    defaultWeekPlan()
                        .withAddedWorkout(MONDAY, workoutSeed(8, AFTERNOON))
                        .withAddedWorkout(THURSDAY, workoutSeed(9, NIGHT))
                2 ->
                    defaultWeekPlan()
                        .withAddedWorkout(TUESDAY, workoutSeed(8, MORNING))
                3 ->
                    defaultWeekPlan()
                        .withAddedWorkout(WEDNESDAY, workoutSeed(8, AFTERNOON))
                        .withAddedWorkout(FRIDAY, workoutSeed(9, MORNING))
                4 ->
                    defaultWeekPlan()
                        .withAddedWorkout(MONDAY, workoutSeed(8, NIGHT))
                        .withAddedWorkout(THURSDAY, workoutSeed(9, AFTERNOON))
                        .withAddedWorkout(SUNDAY, workoutSeed(10, MORNING))
                5 ->
                    defaultWeekPlan()
                        .withAddedWorkout(TUESDAY, workoutSeed(8, AFTERNOON))
                        .withAddedWorkout(SATURDAY, workoutSeed(9, MORNING))
                        .withAddedWorkout(SUNDAY, workoutSeed(10, NIGHT))
                else ->
                    defaultWeekPlan()
                        .withAddedWorkout(WEDNESDAY, workoutSeed(8, MORNING))
            }
        }

        private fun buildDemoRaceEvents(
            currentWeekStart: LocalDate,
            nextWeekStart: LocalDate,
        ): List<WorkoutEntity> {
            val raceEvents =
                listOf(
                    RaceEventPlan(
                        eventDate = currentWeekStart.plusDays(4),
                        seed = raceEventSeed(0, RUN_ID),
                    ),
                    RaceEventPlan(
                        eventDate = currentWeekStart.plusDays(6),
                        seed = raceEventSeed(2, CYCLING_ID),
                    ),
                    RaceEventPlan(
                        eventDate = nextWeekStart.plusDays(2),
                        seed = raceEventSeed(4, SWIM_ID),
                    ),
                    RaceEventPlan(
                        eventDate = nextWeekStart.plusDays(5),
                        seed = raceEventSeed(6, OTHER_ID),
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

        @Suppress("LongMethod")
        private fun buildCompletedTrophyActions(currentWeekStart: LocalDate): List<UserActionEntity> {
            val zoneId = ZoneId.systemDefault()
            val historyStart = currentWeekStart.minusWeeks(COMPLETED_TROPHY_HISTORY_WEEKS.toLong() + 12)
            val completedWeeks =
                List(COMPLETED_TROPHY_HISTORY_WEEKS) { index ->
                    historyStart.plusWeeks(index.toLong())
                }
            val actions = mutableListOf<UserActionEntity>()
            var nextEntityId = COMPLETED_TROPHY_ENTITY_ID_START
            val categoryIds = completedTrophyCategoryIds()

            completedWeeks.forEachIndexed { weekIndex, weekStartDate ->
                if (weekIndex < COMPLETED_TROPHY_COPIED_WEEKS) {
                    actions +=
                        copyLastWeekAction(
                            weekStartDate = weekStartDate,
                            timestamp = weekTimestamp(weekStartDate, zoneId, dayOffset = 0, hour = 6),
                        )
                }

                categoryIds.forEachIndexed { categoryIndex, categoryId ->
                    val seed = workoutSeedForCategory(categoryId)

                    repeat(COMPLETED_TROPHY_WORKOUTS_PER_CATEGORY_PER_WEEK) { completionIndex ->
                        val dayOfWeek = completedTrophyDayOfWeek(categoryIndex, completionIndex)
                        val createdAt =
                            weekTimestamp(
                                weekStartDate = weekStartDate,
                                zoneId = zoneId,
                                dayOffset = (dayOfWeek.value - 1).toLong(),
                                hour = 7L + categoryIndex,
                                minute = (completionIndex * 6).toLong(),
                            )
                        val workoutId = nextEntityId++

                        actions +=
                            createWorkoutAction(
                                weekStartDate = weekStartDate,
                                dayOfWeek = dayOfWeek,
                                order = completionIndex,
                                seed = seed,
                                entityId = workoutId,
                                timestamp = createdAt,
                            )
                        actions +=
                            completeWorkoutAction(
                                weekStartDate = weekStartDate,
                                seed = seed,
                                entityId = workoutId,
                                timestamp = createdAt + 60_000,
                            )
                    }

                    repeat(COMPLETED_TROPHY_PLANNING_ACTIONS_PER_CATEGORY_PER_WEEK) { planningIndex ->
                        val workoutId = nextEntityId++
                        val oldDay = completedTrophyDayOfWeek(categoryIndex, planningIndex)
                        val newDay = completedTrophyDayOfWeek(categoryIndex + 1, planningIndex + 1)

                        actions +=
                            moveWorkoutAction(
                                weekStartDate = weekStartDate,
                                dayChange = WorkoutDayChange(oldDay = oldDay, newDay = newDay),
                                orderChange = WorkoutOrderChange(oldOrder = 0, newOrder = 0),
                                slotChange =
                                    seed.timeSlot?.let { oldTimeSlot ->
                                        WorkoutSlotChange(
                                            oldTimeSlot = oldTimeSlot,
                                            newTimeSlot = alternativeTimeSlot(oldTimeSlot),
                                        )
                                    },
                                seed = seed,
                                entityId = workoutId,
                                timestamp =
                                    weekTimestamp(
                                        weekStartDate = weekStartDate,
                                        zoneId = zoneId,
                                        dayOffset = oldDay.value.toLong() - 1,
                                        hour = 17L + planningIndex,
                                        minute = categoryIndex.toLong(),
                                    ),
                            )
                    }
                }

                actions +=
                    completeWeekAction(
                        weekStartDate = weekStartDate,
                        timestamp = weekTimestamp(weekStartDate, zoneId, dayOffset = 6, hour = 21),
                    )
            }

            val finalWeek = completedWeeks.last()

            repeat(COMPLETED_TROPHY_CATEGORY_ACTIONS) { index ->
                val categoryId = categoryIds[index % categoryIds.size]
                val actionType =
                    COMPLETED_TROPHY_CATEGORY_ACTION_TYPES[
                        index % COMPLETED_TROPHY_CATEGORY_ACTION_TYPES.size,
                    ]
                actions +=
                    categoryAction(
                        type = actionType,
                        categoryId = categoryId,
                        categoryName = categoryNameForId(categoryId),
                        timestamp =
                            weekTimestamp(
                                weekStartDate = finalWeek,
                                zoneId = zoneId,
                                dayOffset = 6,
                                hour = 22,
                                minute = index.toLong(),
                            ),
                    )
            }

            repeat(COMPLETED_TROPHY_BACKUP_SUCCESSES) { index ->
                actions +=
                    settingsResultAction(
                        type = if (index % 2 == 0) UserActionType.EXPORT_BACKUP else UserActionType.IMPORT_BACKUP,
                        timestamp =
                            weekTimestamp(
                                weekStartDate = finalWeek,
                                zoneId = zoneId,
                                dayOffset = 6,
                                hour = 23,
                                minute = index.toLong(),
                            ),
                    )
            }

            repeat(COMPLETED_TROPHY_PROTECTED_TIME_BLOCKS) { index ->
                val dayOfWeek = completedTrophyDayOfWeek(index, 0)
                val entityType =
                    if (index % 2 == 0) {
                        UserActionEntityType.REST_DAY
                    } else {
                        UserActionEntityType.BUSY
                    }

                actions +=
                    createNonWorkoutAction(
                        weekStartDate = finalWeek,
                        dayOfWeek = dayOfWeek,
                        order = index,
                        entityType = entityType,
                        timeSlot = if (index % 2 == 0) NIGHT else AFTERNOON,
                        timestamp =
                            weekTimestamp(
                                weekStartDate = finalWeek,
                                zoneId = zoneId,
                                dayOffset = (dayOfWeek.value - 1).toLong(),
                                hour = 15,
                                minute = index.toLong(),
                            ),
                    )
            }

            val personalRecordSeed = personalRecordSeeds().first()
            val personalRecordDate = finalWeek.plusDays(6)
            repeat(COMPLETED_TROPHY_PERSONAL_RECORD_FAMILIES) { index ->
                val familyId = COMPLETED_TROPHY_PERSONAL_RECORD_FAMILY_ID_START + index
                actions +=
                    createPersonalRecordFamilyAction(
                        familyId = familyId,
                        seed = personalRecordSeed,
                        timestamp =
                            weekTimestamp(
                                weekStartDate = finalWeek,
                                zoneId = zoneId,
                                dayOffset = 6,
                                hour = 12,
                                minute = index.toLong(),
                            ),
                    )
            }
            repeat(COMPLETED_TROPHY_PERSONAL_RECORD_ENTRIES) { index ->
                actions +=
                    createPersonalRecordEntryAction(
                        familyId = COMPLETED_TROPHY_PERSONAL_RECORD_FAMILY_ID_START,
                        entryId = COMPLETED_TROPHY_PERSONAL_RECORD_ENTRY_ID_START + index,
                        seed = personalRecordSeed,
                        entrySeed = PersonalRecordEntrySeed(value = 1_500.0 - index, daysAgo = 0),
                        recordDate = personalRecordDate,
                        timestamp =
                            weekTimestamp(
                                weekStartDate = finalWeek,
                                zoneId = zoneId,
                                dayOffset = 6,
                                hour = 13,
                                minute = index.toLong(),
                            ),
                    )
            }
            repeat(COMPLETED_TROPHY_PACE_CALCULATIONS) { index ->
                actions +=
                    paceCalculatorAction(
                        mode = PaceCalculatorMode.entries[index % PaceCalculatorMode.entries.size],
                        timestamp =
                            weekTimestamp(
                                weekStartDate = finalWeek,
                                zoneId = zoneId,
                                dayOffset = 6,
                                hour = 14,
                                minute = index.toLong(),
                            ),
                    )
            }

            return actions
        }

        private fun categoryIdForSeed(seed: WorkoutSeed): Long {
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
                    other -> CategoryDefaults.COLOR_OTHER
                    else -> CategoryDefaults.COLOR_OTHER
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

        private suspend fun seedActivityHistory(
            currentWeekStart: LocalDate,
            olderWeekStarts: List<LocalDate>,
            nextWeekStart: LocalDate,
        ) {
            buildActivityHistoryActions(
                currentWeekStart = currentWeekStart,
                olderWeekStarts = olderWeekStarts,
                nextWeekStart = nextWeekStart,
            ).forEach { userActionDao.insert(it) }
        }

        private fun buildActivityHistoryActions(
            currentWeekStart: LocalDate,
            olderWeekStarts: List<LocalDate>,
            nextWeekStart: LocalDate,
        ): List<UserActionEntity> {
            val zoneId = ZoneId.systemDefault()
            val now = System.currentTimeMillis()
            val dayMillis = 24 * 60 * 60 * 1000L

            return buildHistoricTrophyActions(
                olderWeekStarts = olderWeekStarts,
                zoneId = zoneId,
            ) +
                buildCurrentWeekActions(
                    currentWeekStart = currentWeekStart,
                    previousWeekStart = olderWeekStarts.last(),
                    nextWeekStart = nextWeekStart,
                    now = now,
                    dayMillis = dayMillis,
                )
        }

        @Suppress("LongMethod")
        private fun buildHistoricTrophyActions(
            olderWeekStarts: List<LocalDate>,
            zoneId: ZoneId,
        ): List<UserActionEntity> {
            val weekA = olderWeekStarts[0]
            val weekB = olderWeekStarts[1]
            val weekC = olderWeekStarts[2]
            val weekD = olderWeekStarts[3]

            return listOf(
                createWorkoutAction(
                    weekStartDate = weekA,
                    dayOfWeek = MONDAY,
                    order = 0,
                    seed = workoutSeed(2, MORNING),
                    entityId = DEMO_RUN_WORKOUT_A1_ID,
                    timestamp = weekTimestamp(weekA, zoneId, dayOffset = 0, hour = 7),
                ),
                createWorkoutAction(
                    weekStartDate = weekA,
                    dayOfWeek = TUESDAY,
                    order = 0,
                    seed = workoutSeed(0, NIGHT),
                    entityId = DEMO_STRENGTH_WORKOUT_A1_ID,
                    timestamp = weekTimestamp(weekA, zoneId, dayOffset = 1, hour = 19),
                ),
                createWorkoutAction(
                    weekStartDate = weekA,
                    dayOfWeek = THURSDAY,
                    order = 0,
                    seed = workoutSeed(6, MORNING),
                    entityId = DEMO_RUN_WORKOUT_A2_ID,
                    timestamp = weekTimestamp(weekA, zoneId, dayOffset = 3, hour = 8),
                ),
                completeWorkoutAction(
                    weekStartDate = weekA,
                    seed = workoutSeed(2, MORNING),
                    entityId = DEMO_RUN_WORKOUT_A1_ID,
                    timestamp = weekTimestamp(weekA, zoneId, dayOffset = 0, hour = 18),
                ),
                completeWorkoutAction(
                    weekStartDate = weekA,
                    seed = workoutSeed(0, NIGHT),
                    entityId = DEMO_STRENGTH_WORKOUT_A1_ID,
                    timestamp = weekTimestamp(weekA, zoneId, dayOffset = 1, hour = 20),
                ),
                completeWorkoutAction(
                    weekStartDate = weekA,
                    seed = workoutSeed(6, MORNING),
                    entityId = DEMO_RUN_WORKOUT_A2_ID,
                    timestamp = weekTimestamp(weekA, zoneId, dayOffset = 3, hour = 18),
                ),
                completeWeekAction(
                    weekStartDate = weekA,
                    timestamp = weekTimestamp(weekA, zoneId, dayOffset = 6, hour = 20),
                ),
                copyLastWeekAction(
                    weekStartDate = weekB,
                    timestamp = weekTimestamp(weekB, zoneId, dayOffset = 0, hour = 6),
                ),
                moveWorkoutAction(
                    weekStartDate = weekB,
                    dayChange = WorkoutDayChange(oldDay = TUESDAY, newDay = WEDNESDAY),
                    orderChange = WorkoutOrderChange(oldOrder = 0, newOrder = 0),
                    slotChange = WorkoutSlotChange(oldTimeSlot = MORNING, newTimeSlot = AFTERNOON),
                    seed = workoutSeed(2, AFTERNOON),
                    entityId = DEMO_RUN_WORKOUT_B1_ID,
                    timestamp = weekTimestamp(weekB, zoneId, dayOffset = 0, hour = 8),
                ),
                moveWorkoutAction(
                    weekStartDate = weekB,
                    dayChange = WorkoutDayChange(oldDay = THURSDAY, newDay = THURSDAY),
                    orderChange = WorkoutOrderChange(oldOrder = 1, newOrder = 0),
                    slotChange = WorkoutSlotChange(oldTimeSlot = MORNING, newTimeSlot = MORNING),
                    seed = workoutSeed(0, MORNING),
                    entityId = DEMO_STRENGTH_WORKOUT_B1_ID,
                    timestamp = weekTimestamp(weekB, zoneId, dayOffset = 1, hour = 7),
                ),
                moveWorkoutAction(
                    weekStartDate = weekB,
                    dayChange = WorkoutDayChange(oldDay = FRIDAY, newDay = SATURDAY),
                    orderChange = WorkoutOrderChange(oldOrder = 0, newOrder = 0),
                    slotChange = WorkoutSlotChange(oldTimeSlot = MORNING, newTimeSlot = NIGHT),
                    seed = workoutSeed(6, NIGHT),
                    entityId = DEMO_RUN_WORKOUT_B2_ID,
                    timestamp = weekTimestamp(weekB, zoneId, dayOffset = 2, hour = 18),
                ),
                completeWorkoutAction(
                    weekStartDate = weekB,
                    seed = workoutSeed(2, AFTERNOON),
                    entityId = DEMO_RUN_WORKOUT_B1_ID,
                    timestamp = weekTimestamp(weekB, zoneId, dayOffset = 2, hour = 20),
                ),
                completeWorkoutAction(
                    weekStartDate = weekB,
                    seed = workoutSeed(0, MORNING),
                    entityId = DEMO_STRENGTH_WORKOUT_B1_ID,
                    timestamp = weekTimestamp(weekB, zoneId, dayOffset = 3, hour = 19),
                ),
                completeWorkoutAction(
                    weekStartDate = weekB,
                    seed = workoutSeed(6, NIGHT),
                    entityId = DEMO_RUN_WORKOUT_B2_ID,
                    timestamp = weekTimestamp(weekB, zoneId, dayOffset = 5, hour = 20),
                ),
                completeWeekAction(
                    weekStartDate = weekB,
                    timestamp = weekTimestamp(weekB, zoneId, dayOffset = 6, hour = 21),
                ),
                copyLastWeekAction(
                    weekStartDate = weekC,
                    timestamp = weekTimestamp(weekC, zoneId, dayOffset = 0, hour = 6),
                ),
                moveWorkoutAction(
                    weekStartDate = weekC,
                    dayChange = WorkoutDayChange(oldDay = MONDAY, newDay = TUESDAY),
                    orderChange = WorkoutOrderChange(oldOrder = 0, newOrder = 0),
                    slotChange = WorkoutSlotChange(oldTimeSlot = MORNING, newTimeSlot = AFTERNOON),
                    seed = workoutSeed(2, AFTERNOON),
                    entityId = DEMO_RUN_WORKOUT_C1_ID,
                    timestamp = weekTimestamp(weekC, zoneId, dayOffset = 0, hour = 7),
                ),
                moveWorkoutAction(
                    weekStartDate = weekC,
                    dayChange = WorkoutDayChange(oldDay = WEDNESDAY, newDay = THURSDAY),
                    orderChange = WorkoutOrderChange(oldOrder = 0, newOrder = 0),
                    slotChange = WorkoutSlotChange(oldTimeSlot = MORNING, newTimeSlot = NIGHT),
                    seed = workoutSeed(6, NIGHT),
                    entityId = DEMO_RUN_WORKOUT_C2_ID,
                    timestamp = weekTimestamp(weekC, zoneId, dayOffset = 1, hour = 18),
                ),
                moveWorkoutAction(
                    weekStartDate = weekC,
                    dayChange = WorkoutDayChange(oldDay = THURSDAY, newDay = THURSDAY),
                    orderChange = WorkoutOrderChange(oldOrder = 1, newOrder = 0),
                    slotChange = WorkoutSlotChange(oldTimeSlot = NIGHT, newTimeSlot = NIGHT),
                    seed = workoutSeed(5, NIGHT),
                    entityId = DEMO_MOBILITY_WORKOUT_C1_ID,
                    timestamp = weekTimestamp(weekC, zoneId, dayOffset = 2, hour = 20),
                ),
                completeWorkoutAction(
                    weekStartDate = weekC,
                    seed = workoutSeed(2, AFTERNOON),
                    entityId = DEMO_RUN_WORKOUT_C1_ID,
                    timestamp = weekTimestamp(weekC, zoneId, dayOffset = 2, hour = 21),
                ),
                completeWorkoutAction(
                    weekStartDate = weekC,
                    seed = workoutSeed(6, NIGHT),
                    entityId = DEMO_RUN_WORKOUT_C2_ID,
                    timestamp = weekTimestamp(weekC, zoneId, dayOffset = 4, hour = 20),
                ),
                completeWorkoutAction(
                    weekStartDate = weekC,
                    seed = workoutSeed(5, NIGHT),
                    entityId = DEMO_MOBILITY_WORKOUT_C1_ID,
                    timestamp = weekTimestamp(weekC, zoneId, dayOffset = 5, hour = 20),
                ),
                completeWeekAction(
                    weekStartDate = weekC,
                    timestamp = weekTimestamp(weekC, zoneId, dayOffset = 6, hour = 20),
                ),
                copyLastWeekAction(
                    weekStartDate = weekD,
                    timestamp = weekTimestamp(weekD, zoneId, dayOffset = 0, hour = 6),
                ),
                moveWorkoutAction(
                    weekStartDate = weekD,
                    dayChange = WorkoutDayChange(oldDay = MONDAY, newDay = TUESDAY),
                    orderChange = WorkoutOrderChange(oldOrder = 0, newOrder = 0),
                    slotChange = WorkoutSlotChange(oldTimeSlot = NIGHT, newTimeSlot = MORNING),
                    seed = workoutSeed(0, MORNING),
                    entityId = DEMO_STRENGTH_WORKOUT_D1_ID,
                    timestamp = weekTimestamp(weekD, zoneId, dayOffset = 0, hour = 7),
                ),
                moveWorkoutAction(
                    weekStartDate = weekD,
                    dayChange = WorkoutDayChange(oldDay = THURSDAY, newDay = THURSDAY),
                    orderChange = WorkoutOrderChange(oldOrder = 1, newOrder = 0),
                    slotChange = WorkoutSlotChange(oldTimeSlot = MORNING, newTimeSlot = MORNING),
                    seed = workoutSeed(2, MORNING),
                    entityId = DEMO_RUN_WORKOUT_D1_ID,
                    timestamp = weekTimestamp(weekD, zoneId, dayOffset = 1, hour = 7),
                ),
                createWorkoutAction(
                    weekStartDate = weekD,
                    dayOfWeek = SATURDAY,
                    order = 0,
                    seed = workoutSeed(6, AFTERNOON),
                    entityId = DEMO_RUN_WORKOUT_D2_ID,
                    timestamp = weekTimestamp(weekD, zoneId, dayOffset = 2, hour = 17),
                ),
                completeWorkoutAction(
                    weekStartDate = weekD,
                    seed = workoutSeed(0, MORNING),
                    entityId = DEMO_STRENGTH_WORKOUT_D1_ID,
                    timestamp = weekTimestamp(weekD, zoneId, dayOffset = 2, hour = 19),
                ),
                completeWorkoutAction(
                    weekStartDate = weekD,
                    seed = workoutSeed(2, MORNING),
                    entityId = DEMO_RUN_WORKOUT_D1_ID,
                    timestamp = weekTimestamp(weekD, zoneId, dayOffset = 4, hour = 19),
                ),
                completeWorkoutAction(
                    weekStartDate = weekD,
                    seed = workoutSeed(6, AFTERNOON),
                    entityId = DEMO_RUN_WORKOUT_D2_ID,
                    timestamp = weekTimestamp(weekD, zoneId, dayOffset = 5, hour = 19),
                ),
                completeWeekAction(
                    weekStartDate = weekD,
                    timestamp = weekTimestamp(weekD, zoneId, dayOffset = 6, hour = 20),
                ),
                categoryAction(
                    type = UserActionType.UPDATE_CATEGORY_COLOR,
                    categoryId = RUN_ID,
                    categoryName = categoryNameForId(RUN_ID),
                    timestamp = weekTimestamp(weekD, zoneId, dayOffset = 6, hour = 21),
                ),
                categoryAction(
                    type = UserActionType.UPDATE_CATEGORY_VISIBILITY,
                    categoryId = STRENGTH_ID,
                    categoryName = categoryNameForId(STRENGTH_ID),
                    timestamp = weekTimestamp(weekD, zoneId, dayOffset = 6, hour = 21, minute = 10),
                ),
                categoryAction(
                    type = UserActionType.REORDER_CATEGORY,
                    categoryId = MOBILITY_ID,
                    categoryName = categoryNameForId(MOBILITY_ID),
                    timestamp = weekTimestamp(weekD, zoneId, dayOffset = 6, hour = 21, minute = 20),
                ),
                settingsResultAction(
                    type = UserActionType.EXPORT_BACKUP,
                    timestamp = weekTimestamp(weekD, zoneId, dayOffset = 6, hour = 21, minute = 30),
                ),
                settingsResultAction(
                    type = UserActionType.IMPORT_BACKUP,
                    timestamp = weekTimestamp(weekD, zoneId, dayOffset = 6, hour = 21, minute = 40),
                ),
            )
        }

        @Suppress("LongMethod")
        private fun buildCurrentWeekActions(
            currentWeekStart: LocalDate,
            previousWeekStart: LocalDate,
            nextWeekStart: LocalDate,
            now: Long,
            dayMillis: Long,
        ): List<UserActionEntity> {
            val trainingActions =
                listOf(
                    openWeekAction(
                        oldWeekStart = previousWeekStart,
                        newWeekStart = currentWeekStart,
                        timestamp = now - dayMillis * 6,
                    ),
                    createWorkoutAction(
                        weekStartDate = currentWeekStart,
                        dayOfWeek = TUESDAY,
                        order = 0,
                        seed = workoutSeed(2, AFTERNOON),
                        entityId = DEMO_RUN_WORKOUT_CURRENT_ID,
                        timestamp = now - dayMillis * 5,
                    ),
                    moveWorkoutAction(
                        weekStartDate = currentWeekStart,
                        dayChange = WorkoutDayChange(oldDay = THURSDAY, newDay = FRIDAY),
                        orderChange = WorkoutOrderChange(oldOrder = 1, newOrder = 0),
                        slotChange = WorkoutSlotChange(oldTimeSlot = MORNING, newTimeSlot = AFTERNOON),
                        seed = workoutSeed(4, AFTERNOON),
                        entityId = DEMO_CYCLING_WORKOUT_CURRENT_ID,
                        timestamp = now - dayMillis * 4,
                    ),
                    moveWorkoutAction(
                        weekStartDate = currentWeekStart,
                        dayChange = WorkoutDayChange(oldDay = MONDAY, newDay = MONDAY),
                        orderChange = WorkoutOrderChange(oldOrder = 1, newOrder = 0),
                        slotChange = WorkoutSlotChange(oldTimeSlot = NIGHT, newTimeSlot = MORNING),
                        seed = workoutSeed(1, MORNING),
                        entityId = DEMO_OTHER_WORKOUT_CURRENT_ID,
                        timestamp = now - dayMillis * 3,
                    ),
                    completeWorkoutAction(
                        weekStartDate = currentWeekStart,
                        seed = workoutSeed(0, MORNING),
                        entityId = DEMO_STRENGTH_WORKOUT_CURRENT_ID,
                        timestamp = now - dayMillis * 2,
                    ),
                )
            val plannerActions =
                listOf(
                    createNonWorkoutAction(
                        weekStartDate = currentWeekStart,
                        dayOfWeek = WEDNESDAY,
                        order = 0,
                        entityType = UserActionEntityType.REST,
                        timestamp = now - dayMillis * 2 + 2_000,
                    ),
                    createNonWorkoutAction(
                        weekStartDate = currentWeekStart,
                        dayOfWeek = TUESDAY,
                        order = 0,
                        entityType = UserActionEntityType.BUSY,
                        timeSlot = MORNING,
                        timestamp = now - dayMillis * 2 + 3_000,
                    ),
                    createNonWorkoutAction(
                        weekStartDate = currentWeekStart,
                        dayOfWeek = TUESDAY,
                        order = 0,
                        entityType = UserActionEntityType.SICK,
                        timeSlot = NIGHT,
                        timestamp = now - dayMillis * 2 + 4_000,
                    ),
                    changeSlotModeAction(
                        weekStartDate = currentWeekStart,
                        oldPolicy = AUTO_WHEN_MULTIPLE.name,
                        newPolicy = ALWAYS_SHOW.name,
                        timestamp = now - dayMillis * 2 + 5_000,
                    ),
                    categoryAction(
                        type = UserActionType.UPDATE_CATEGORY_COLOR,
                        categoryId = RUN_ID,
                        categoryName = categoryNameForId(RUN_ID),
                        timestamp = now - dayMillis * 2 + 6_000,
                    ),
                    categoryAction(
                        type = UserActionType.UPDATE_CATEGORY_VISIBILITY,
                        categoryId = CYCLING_ID,
                        categoryName = categoryNameForId(CYCLING_ID),
                        timestamp = now - dayMillis * 2 + 7_000,
                    ),
                    categoryAction(
                        type = UserActionType.REORDER_CATEGORY,
                        categoryId = STRENGTH_ID,
                        categoryName = categoryNameForId(STRENGTH_ID),
                        timestamp = now - dayMillis * 2 + 8_000,
                    ),
                    categoryAction(
                        type = UserActionType.UPDATE_CATEGORY_COLOR,
                        categoryId = SWIM_ID,
                        categoryName = categoryNameForId(SWIM_ID),
                        timestamp = now - dayMillis * 2 + 9_000,
                    ),
                    categoryAction(
                        type = UserActionType.UPDATE_CATEGORY_VISIBILITY,
                        categoryId = MOBILITY_ID,
                        categoryName = categoryNameForId(MOBILITY_ID),
                        timestamp = now - dayMillis * 2 + 10_000,
                    ),
                    categoryAction(
                        type = UserActionType.REORDER_CATEGORY,
                        categoryId = OTHER_ID,
                        categoryName = categoryNameForId(OTHER_ID),
                        timestamp = now - dayMillis * 2 + 11_000,
                    ),
                    categoryAction(
                        type = UserActionType.UPDATE_CATEGORY_COLOR,
                        categoryId = RUN_ID,
                        categoryName = categoryNameForId(RUN_ID),
                        timestamp = now - dayMillis * 2 + 12_000,
                    ),
                    settingsResultAction(
                        type = UserActionType.EXPORT_BACKUP,
                        timestamp = now - dayMillis * 2 + 13_000,
                    ),
                )
            val navigationActions =
                listOf(
                    openWeekAction(
                        oldWeekStart = currentWeekStart,
                        newWeekStart = nextWeekStart,
                        timestamp = now - dayMillis,
                    ),
                    openWeekAction(
                        oldWeekStart = nextWeekStart,
                        newWeekStart = currentWeekStart,
                        timestamp = now - dayMillis + 3_000,
                    ),
                )
            val builderActions =
                listOf(
                    paceCalculatorAction(PaceCalculatorMode.PACE, now - dayMillis * 5 + 1_000),
                    paceCalculatorAction(PaceCalculatorMode.TIME, now - dayMillis * 4 + 1_000),
                    paceCalculatorAction(PaceCalculatorMode.DISTANCE, now - dayMillis * 3 + 1_000),
                    paceCalculatorAction(PaceCalculatorMode.PACE, now - dayMillis * 2 + 1_000),
                )

            return trainingActions + plannerActions + builderActions + navigationActions
        }

        private fun openWeekAction(
            oldWeekStart: LocalDate,
            newWeekStart: LocalDate,
            timestamp: Long,
        ): UserActionEntity {
            return action(
                type = UserActionType.OPEN_WEEK,
                entityType = UserActionEntityType.WEEK,
                metadata =
                    mapOf(
                        OLD_WEEK_START_DATE to oldWeekStart.toString(),
                        NEW_WEEK_START_DATE to newWeekStart.toString(),
                        WEEK_START_DATE to newWeekStart.toString(),
                    ),
                timestamp = timestamp,
            )
        }

        private fun createWorkoutAction(
            weekStartDate: LocalDate,
            dayOfWeek: DayOfWeek?,
            order: Int,
            seed: WorkoutSeed,
            entityId: Long,
            timestamp: Long,
        ): UserActionEntity {
            val categoryId = categoryIdForSeed(seed)
            return action(
                type = UserActionType.CREATE_WORKOUT,
                entityType = UserActionEntityType.WORKOUT,
                entityId = entityId,
                metadata =
                    buildMap {
                        put(WEEK_START_DATE, weekStartDate.toString())
                        put(DAY_OF_WEEK, dayOfWeek?.value?.toString() ?: UNPLANNED)
                        put(NEW_ORDER, order.toString())
                        put(NEW_TYPE, seed.type)
                        put(NEW_DESCRIPTION, seed.description)
                        put(CATEGORY_ID, categoryId.toString())
                        put(CATEGORY_NAME, categoryNameForId(categoryId))
                        seed.timeSlot?.let { put(NEW_TIME_SLOT, it.name) }
                    },
                timestamp = timestamp,
            )
        }

        private fun moveWorkoutAction(
            weekStartDate: LocalDate,
            dayChange: WorkoutDayChange,
            orderChange: WorkoutOrderChange,
            slotChange: WorkoutSlotChange? = null,
            seed: WorkoutSeed,
            entityId: Long,
            timestamp: Long,
        ): UserActionEntity {
            val actionType =
                if (dayChange.oldDay == dayChange.newDay) {
                    UserActionType.REORDER_WORKOUT
                } else {
                    UserActionType.MOVE_WORKOUT_BETWEEN_DAYS
                }
            val categoryId = categoryIdForSeed(seed)

            return action(
                type = actionType,
                entityType = UserActionEntityType.WORKOUT,
                entityId = entityId,
                metadata =
                    buildMap {
                        put(WEEK_START_DATE, weekStartDate.toString())
                        put(OLD_DAY_OF_WEEK, dayChange.oldDay.value.toString())
                        put(NEW_DAY_OF_WEEK, dayChange.newDay.value.toString())
                        put(OLD_ORDER, orderChange.oldOrder.toString())
                        put(NEW_ORDER, orderChange.newOrder.toString())
                        slotChange?.let {
                            put(OLD_TIME_SLOT, it.oldTimeSlot.name)
                            put(NEW_TIME_SLOT, it.newTimeSlot.name)
                        }
                        put(NEW_TYPE, seed.type)
                        put(NEW_DESCRIPTION, seed.description)
                        put(CATEGORY_ID, categoryId.toString())
                        put(CATEGORY_NAME, categoryNameForId(categoryId))
                    },
                timestamp = timestamp,
            )
        }

        private fun completeWorkoutAction(
            weekStartDate: LocalDate,
            seed: WorkoutSeed,
            entityId: Long,
            timestamp: Long,
        ): UserActionEntity {
            val categoryId = categoryIdForSeed(seed)
            return action(
                type = UserActionType.COMPLETE_WORKOUT,
                entityType = UserActionEntityType.WORKOUT,
                entityId = entityId,
                metadata =
                    buildMap {
                        put(WEEK_START_DATE, weekStartDate.toString())
                        put(WAS_COMPLETED, "false")
                        put(IS_COMPLETED, "true")
                        put(NEW_TYPE, seed.type)
                        put(NEW_DESCRIPTION, seed.description)
                        put(CATEGORY_ID, categoryId.toString())
                        put(CATEGORY_NAME, categoryNameForId(categoryId))
                        seed.timeSlot?.let { put(NEW_TIME_SLOT, it.name) }
                    },
                timestamp = timestamp,
            )
        }

        private fun completeWeekAction(
            weekStartDate: LocalDate,
            timestamp: Long,
        ): UserActionEntity {
            return action(
                type = UserActionType.COMPLETE_WEEK_WORKOUTS,
                entityType = UserActionEntityType.WEEK,
                metadata = mapOf(WEEK_START_DATE to weekStartDate.toString()),
                timestamp = timestamp,
            )
        }

        private fun copyLastWeekAction(
            weekStartDate: LocalDate,
            timestamp: Long,
        ): UserActionEntity {
            return action(
                type = UserActionType.COPY_LAST_WEEK,
                entityType = UserActionEntityType.WEEK,
                metadata = mapOf(WEEK_START_DATE to weekStartDate.toString()),
                timestamp = timestamp,
            )
        }

        private fun categoryAction(
            type: UserActionType,
            categoryId: Long,
            categoryName: String,
            timestamp: Long,
        ): UserActionEntity {
            return action(
                type = type,
                entityType = UserActionEntityType.CATEGORY,
                entityId = categoryId,
                metadata = mapOf(CATEGORY_NAME to categoryName),
                timestamp = timestamp,
            )
        }

        private fun createPersonalRecordFamilyAction(
            familyId: Long,
            seed: PersonalRecordSeed,
            timestamp: Long,
        ): UserActionEntity {
            return action(
                type = UserActionType.CREATE_PERSONAL_RECORD_FAMILY,
                entityType = UserActionEntityType.PERSONAL_RECORD,
                entityId = familyId,
                metadata =
                    mapOf(
                        PERSONAL_RECORD_FAMILY_ID to familyId.toString(),
                        PERSONAL_RECORD_CATEGORY_ID to seed.categoryId.toString(),
                        PERSONAL_RECORD_CATEGORY_NAME to categoryNameForId(seed.categoryId),
                        PERSONAL_RECORD_METRIC_TYPE to seed.metricType.name,
                        PERSONAL_RECORD_UNIT to seed.unit.name,
                        PERSONAL_RECORD_COMPARISON_RULE to seed.comparisonRule.name,
                    ),
                timestamp = timestamp,
            )
        }

        private fun createPersonalRecordEntryAction(
            familyId: Long,
            entryId: Long,
            seed: PersonalRecordSeed,
            entrySeed: PersonalRecordEntrySeed,
            recordDate: LocalDate,
            timestamp: Long,
        ): UserActionEntity {
            return action(
                type = UserActionType.CREATE_PERSONAL_RECORD_ENTRY,
                entityType = UserActionEntityType.PERSONAL_RECORD,
                entityId = entryId,
                metadata =
                    mapOf(
                        PERSONAL_RECORD_ENTRY_ID to entryId.toString(),
                        PERSONAL_RECORD_FAMILY_ID to familyId.toString(),
                        PERSONAL_RECORD_FAMILY_TITLE to seed.title,
                        PERSONAL_RECORD_CATEGORY_ID to seed.categoryId.toString(),
                        PERSONAL_RECORD_CATEGORY_NAME to categoryNameForId(seed.categoryId),
                        PERSONAL_RECORD_METRIC_TYPE to seed.metricType.name,
                        PERSONAL_RECORD_UNIT to seed.unit.name,
                        PERSONAL_RECORD_COMPARISON_RULE to seed.comparisonRule.name,
                        PERSONAL_RECORD_RECORD_DATE to recordDate.toString(),
                        PERSONAL_RECORD_NEW_VALUE to entrySeed.value.toString(),
                        PERSONAL_RECORD_NORMALIZED_VALUE to entrySeed.value.toString(),
                    ),
                timestamp = timestamp,
            )
        }

        private fun paceCalculatorAction(
            mode: PaceCalculatorMode,
            timestamp: Long,
        ): UserActionEntity {
            return action(
                type = UserActionType.USE_PACE_CALCULATOR,
                entityType = UserActionEntityType.APP,
                metadata = mapOf(PACE_CALCULATOR_MODE to mode.name),
                timestamp = timestamp,
            )
        }

        private fun settingsResultAction(
            type: UserActionType,
            timestamp: Long,
        ): UserActionEntity {
            return action(
                type = type,
                entityType = UserActionEntityType.SETTINGS,
                metadata = mapOf(RESULT to RESULT_SUCCESS),
                timestamp = timestamp,
            )
        }

        private fun createNonWorkoutAction(
            weekStartDate: LocalDate,
            dayOfWeek: DayOfWeek,
            order: Int,
            entityType: UserActionEntityType,
            timeSlot: TimeSlot? = null,
            timestamp: Long,
        ): UserActionEntity {
            val actionType =
                when (entityType) {
                    UserActionEntityType.REST,
                    UserActionEntityType.REST_DAY,
                    -> UserActionType.CREATE_REST_DAY
                    UserActionEntityType.BUSY -> UserActionType.CREATE_BUSY
                    UserActionEntityType.SICK -> UserActionType.CREATE_SICK
                    else ->
                        throw IllegalArgumentException(
                            UNSUPPORTED_USER_ACTION_ENTITY_TYPE.format(entityType),
                        )
                }
            return action(
                type = actionType,
                entityType = entityType,
                metadata =
                    buildMap {
                        put(WEEK_START_DATE, weekStartDate.toString())
                        put(DAY_OF_WEEK, dayOfWeek.value.toString())
                        put(NEW_ORDER, order.toString())
                        timeSlot?.let { put(NEW_TIME_SLOT, it.name) }
                    },
                timestamp = timestamp,
            )
        }

        private fun changeSlotModeAction(
            weekStartDate: LocalDate,
            oldPolicy: String,
            newPolicy: String,
            timestamp: Long,
        ): UserActionEntity {
            return action(
                type = UserActionType.CHANGE_SLOT_MODE,
                entityType = UserActionEntityType.SETTINGS,
                metadata =
                    mapOf(
                        WEEK_START_DATE to weekStartDate.toString(),
                        OLD_VALUE to oldPolicy,
                        NEW_VALUE to newPolicy,
                    ),
                timestamp = timestamp,
            )
        }

        private fun action(
            type: UserActionType,
            entityType: UserActionEntityType,
            entityId: Long? = null,
            metadata: Map<String, String>,
            timestamp: Long,
        ): UserActionEntity {
            return UserActionEntity(
                actionType = type.name,
                entityType = entityType.name,
                entityId = entityId,
                metadata = UserActionMetadataSerializer.toJson(metadata),
                timestamp = timestamp,
            )
        }

        private fun categoryNameForId(categoryId: Long): String {
            return when (categoryId) {
                RUN_ID -> stringProvider.get(R.string.categories_category_run)
                CYCLING_ID -> stringProvider.get(R.string.categories_category_cycling)
                STRENGTH_ID -> stringProvider.get(R.string.categories_category_strength)
                SWIM_ID -> stringProvider.get(R.string.categories_category_swim)
                MOBILITY_ID -> stringProvider.get(R.string.categories_category_mobility)
                else -> stringProvider.get(R.string.category_other)
            }
        }

        private fun weekTimestamp(
            weekStartDate: LocalDate,
            zoneId: ZoneId,
            dayOffset: Long,
            hour: Long,
            minute: Long = 0,
        ): Long {
            return weekStartDate
                .atStartOfDay(zoneId)
                .plusDays(dayOffset)
                .plusHours(hour)
                .plusMinutes(minute)
                .toInstant()
                .toEpochMilli()
        }

        private fun workoutSeed(
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

        private fun workoutSeedForCategory(categoryId: Long): WorkoutSeed {
            return when (categoryId) {
                RUN_ID -> workoutSeed(index = 2, timeSlot = MORNING)
                CYCLING_ID -> workoutSeed(index = 4, timeSlot = AFTERNOON)
                STRENGTH_ID -> workoutSeed(index = 0, timeSlot = MORNING)
                SWIM_ID -> workoutSeed(index = 3, timeSlot = NIGHT)
                MOBILITY_ID -> workoutSeed(index = 5, timeSlot = NIGHT)
                else ->
                    WorkoutSeed(
                        eventType = WORKOUT,
                        type = stringProvider.get(R.string.category_other),
                        description = stringProvider.get(R.string.mock_workout_description_core),
                        timeSlot = AFTERNOON,
                    )
            }
        }

        private fun completedTrophyDayOfWeek(
            categoryIndex: Int,
            offset: Int,
        ): DayOfWeek {
            val index = (categoryIndex + offset) % 7
            return DayOfWeek.of(index + 1)
        }

        private fun alternativeTimeSlot(timeSlot: TimeSlot): TimeSlot {
            return if (timeSlot == MORNING) AFTERNOON else MORNING
        }

        private fun completedTrophyCategoryIds(): List<Long> {
            return listOf(
                RUN_ID,
                CYCLING_ID,
                STRENGTH_ID,
                SWIM_ID,
                MOBILITY_ID,
                OTHER_ID,
            )
        }

        private fun restSeed(timeSlot: TimeSlot? = null): WorkoutSeed {
            return WorkoutSeed(
                eventType = REST,
                type = EMPTY,
                description = EMPTY,
                timeSlot = timeSlot,
            )
        }

        private fun busySeed(timeSlot: TimeSlot? = null): WorkoutSeed {
            return WorkoutSeed(
                eventType = BUSY,
                type = EMPTY,
                description = EMPTY,
                timeSlot = timeSlot,
            )
        }

        private fun sickSeed(timeSlot: TimeSlot? = null): WorkoutSeed {
            return WorkoutSeed(
                eventType = SICK,
                type = EMPTY,
                description = EMPTY,
                timeSlot = timeSlot,
            )
        }
    }

private data class PersonalRecordSeed(
    val title: String,
    val categoryId: Long,
    val metricType: PersonalRecordMetricType,
    val unit: PersonalRecordUnit,
    val comparisonRule: PersonalRecordComparisonRule,
    val entries: List<PersonalRecordEntrySeed>,
    val customUnitLabel: String? = null,
    val manualCurrentEntryIndex: Int? = null,
)

private data class PersonalRecordEntrySeed(
    val value: Double,
    val daysAgo: Long,
)

private data class DayPlan(
    val dayOfWeek: DayOfWeek?,
    val items: List<WorkoutSeed>,
)

private data class WorkoutDayChange(
    val oldDay: DayOfWeek,
    val newDay: DayOfWeek,
)

private data class WorkoutOrderChange(
    val oldOrder: Int,
    val newOrder: Int,
)

private data class WorkoutSlotChange(
    val oldTimeSlot: TimeSlot,
    val newTimeSlot: TimeSlot,
)

private data class WorkoutSeed(
    val eventType: EventType,
    val type: String,
    val description: String,
    val timeSlot: TimeSlot? = null,
    val categoryId: Long? = null,
)

private data class RaceEventPlan(
    val eventDate: LocalDate,
    val seed: WorkoutSeed,
)

private enum class CompletionProfile {
    LIGHT,
    SOME,
    BALANCED,
    HEAVY,
    COMPLETED_MOST,
    NONE,
    ;

    fun completedDays(): Set<DayOfWeek> {
        return when (this) {
            LIGHT ->
                setOf(
                    MONDAY,
                )
            SOME ->
                setOf(
                    MONDAY,
                    TUESDAY,
                )
            BALANCED ->
                setOf(
                    MONDAY,
                    TUESDAY,
                    THURSDAY,
                )
            HEAVY ->
                setOf(
                    MONDAY,
                    TUESDAY,
                    THURSDAY,
                    FRIDAY,
                )
            COMPLETED_MOST -> DayOfWeek.entries.toSet()
            NONE -> emptySet()
        }
    }
}

private const val RESULT_SUCCESS = "success"
private const val DEMO_RUN_WORKOUT_A1_ID = 10_001L
private const val DEMO_STRENGTH_WORKOUT_A1_ID = 10_002L
private const val DEMO_RUN_WORKOUT_A2_ID = 10_003L
private const val DEMO_RUN_WORKOUT_B1_ID = 10_101L
private const val DEMO_STRENGTH_WORKOUT_B1_ID = 10_102L
private const val DEMO_RUN_WORKOUT_B2_ID = 10_103L
private const val DEMO_RUN_WORKOUT_C1_ID = 10_201L
private const val DEMO_RUN_WORKOUT_C2_ID = 10_202L
private const val DEMO_MOBILITY_WORKOUT_C1_ID = 10_203L
private const val DEMO_STRENGTH_WORKOUT_D1_ID = 10_301L
private const val DEMO_RUN_WORKOUT_D1_ID = 10_302L
private const val DEMO_RUN_WORKOUT_D2_ID = 10_303L
private const val DEMO_RUN_WORKOUT_CURRENT_ID = 10_401L
private const val DEMO_CYCLING_WORKOUT_CURRENT_ID = 10_402L
private const val DEMO_OTHER_WORKOUT_CURRENT_ID = 10_403L
private const val DEMO_STRENGTH_WORKOUT_CURRENT_ID = 10_404L
private const val COMPLETED_TROPHY_HISTORY_WEEKS = 52
private const val COMPLETED_TROPHY_WORKOUTS_PER_CATEGORY_PER_WEEK = 4
private const val COMPLETED_TROPHY_PLANNING_ACTIONS_PER_CATEGORY_PER_WEEK = 2
private const val COMPLETED_TROPHY_COPIED_WEEKS = 3
private const val COMPLETED_TROPHY_CATEGORY_ACTIONS = 10
private const val COMPLETED_TROPHY_BACKUP_SUCCESSES = 5
private const val COMPLETED_TROPHY_PROTECTED_TIME_BLOCKS = 20
private const val COMPLETED_TROPHY_ENTITY_ID_START = 50_000L
private const val COMPLETED_TROPHY_PERSONAL_RECORD_FAMILIES = 15
private const val COMPLETED_TROPHY_PERSONAL_RECORD_ENTRIES = 50
private const val COMPLETED_TROPHY_PACE_CALCULATIONS = 10
private const val COMPLETED_TROPHY_PERSONAL_RECORD_FAMILY_ID_START = 60_000L
private const val COMPLETED_TROPHY_PERSONAL_RECORD_ENTRY_ID_START = 70_000L
private const val DEMO_PERSONAL_RECORD_ENTRY_HOUR = 12L
private val COMPLETED_TROPHY_CATEGORY_ACTION_TYPES =
    listOf(
        UserActionType.UPDATE_CATEGORY_COLOR,
        UserActionType.UPDATE_CATEGORY_VISIBILITY,
        UserActionType.REORDER_CATEGORY,
    )
