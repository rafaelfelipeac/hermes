@file:Suppress(
    "LongMethod",
    "LongParameterList",
    "MaxLineLength",
    "TooManyFunctions",
    "UnusedParameter",
)

package com.rafaelfelipeac.hermes.core.debug

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.UNSUPPORTED_USER_ACTION_ENTITY_TYPE
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.time.TimeConstants.HOURS_PER_DAY
import com.rafaelfelipeac.hermes.core.time.TimeConstants.MILLIS_PER_SECOND
import com.rafaelfelipeac.hermes.core.time.TimeConstants.MINUTES_PER_HOUR
import com.rafaelfelipeac.hermes.core.time.TimeConstants.SECONDS_PER_MINUTE
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionEntity
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_END_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_LIFECYCLE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_TARGET_QUANTITY
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_TARGET_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_TITLE
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
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.CYCLING_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.MOBILITY_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.OTHER_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.RUN_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.STRENGTH_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.SWIM_ID
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import com.rafaelfelipeac.hermes.features.pacecalculator.domain.PaceCalculatorMode
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.AFTERNOON
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.MORNING
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

internal fun buildActivityHistoryActions(
    stringProvider: StringProvider,
    currentWeekStart: LocalDate,
    olderWeekStarts: List<LocalDate>,
    nextWeekStart: LocalDate,
    now: Long,
    zoneId: ZoneId = ZoneId.systemDefault(),
): List<UserActionEntity> {
    return buildHistoricTrophyActions(stringProvider, olderWeekStarts, zoneId) +
        buildCurrentWeekActions(
            stringProvider = stringProvider,
            currentWeekStart = currentWeekStart,
            previousWeekStart = olderWeekStarts.last(),
            nextWeekStart = nextWeekStart,
            now = now,
            dayMillis = HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE * MILLIS_PER_SECOND,
        )
}

internal fun buildCompletedTrophyActions(
    stringProvider: StringProvider,
    currentWeekStart: LocalDate,
    zoneId: ZoneId = ZoneId.systemDefault(),
): List<UserActionEntity> {
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
            val seed = workoutSeedForCategory(stringProvider, categoryId)

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
                        stringProvider = stringProvider,
                        weekStartDate = weekStartDate,
                        dayOfWeek = dayOfWeek,
                        order = completionIndex,
                        seed = seed,
                        entityId = workoutId,
                        timestamp = createdAt,
                    )
                actions +=
                    completeWorkoutAction(
                        stringProvider = stringProvider,
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
                        stringProvider = stringProvider,
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
                stringProvider = stringProvider,
                type = actionType,
                categoryId = categoryId,
                categoryName = categoryNameForId(stringProvider, categoryId),
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
                timeSlot = if (index % 2 == 0) TimeSlot.NIGHT else AFTERNOON,
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

    repeat(COMPLETED_TROPHY_CHALLENGE_CREATIONS) { index ->
        val challengeId = COMPLETED_TROPHY_CHALLENGE_ID_START + index
        val createdDate = finalWeek.minusDays((COMPLETED_TROPHY_CHALLENGE_CREATIONS - index).toLong())

        actions +=
            createChallengeAction(
                challengeId = challengeId,
                title = "$COMPLETED_TROPHY_CHALLENGE_TITLE_PREFIX ${index + 1}",
                targetType = ChallengeTargetType.TOTAL,
                targetQuantity = 100L + index,
                startDate = createdDate,
                endDate = createdDate.plusDays(13),
                timestamp =
                    createdDate
                        .atStartOfDay(zoneId)
                        .plusHours(10)
                        .toInstant()
                        .toEpochMilli(),
            )
    }

    val personalRecordSeed = personalRecordSeeds(stringProvider).first()
    val personalRecordDate = finalWeek.plusDays(6)
    repeat(COMPLETED_TROPHY_PERSONAL_RECORD_FAMILIES) { index ->
        val familyId = COMPLETED_TROPHY_PERSONAL_RECORD_FAMILY_ID_START + index
        actions +=
            createPersonalRecordFamilyAction(
                stringProvider = stringProvider,
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
                stringProvider = stringProvider,
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

private fun buildHistoricTrophyActions(
    stringProvider: StringProvider,
    olderWeekStarts: List<LocalDate>,
    zoneId: ZoneId,
): List<UserActionEntity> {
    val weekA = olderWeekStarts[0]
    val weekB = olderWeekStarts[1]
    val weekC = olderWeekStarts[2]
    val weekD = olderWeekStarts[3]

    return listOf(
        createWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekA,
            dayOfWeek = DayOfWeek.MONDAY,
            order = 0,
            seed = workoutSeed(stringProvider, 2, MORNING),
            entityId = DEMO_RUN_WORKOUT_A1_ID,
            timestamp = weekTimestamp(weekA, zoneId, dayOffset = 0, hour = 7),
        ),
        createWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekA,
            dayOfWeek = DayOfWeek.TUESDAY,
            order = 0,
            seed = workoutSeed(stringProvider, 0, TimeSlot.NIGHT),
            entityId = DEMO_STRENGTH_WORKOUT_A1_ID,
            timestamp = weekTimestamp(weekA, zoneId, dayOffset = 1, hour = 19),
        ),
        createWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekA,
            dayOfWeek = DayOfWeek.THURSDAY,
            order = 0,
            seed = workoutSeed(stringProvider, 6, MORNING),
            entityId = DEMO_RUN_WORKOUT_A2_ID,
            timestamp = weekTimestamp(weekA, zoneId, dayOffset = 3, hour = 8),
        ),
        completeWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekA,
            seed = workoutSeed(stringProvider, 2, MORNING),
            entityId = DEMO_RUN_WORKOUT_A1_ID,
            timestamp = weekTimestamp(weekA, zoneId, dayOffset = 0, hour = 18),
        ),
        completeWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekA,
            seed = workoutSeed(stringProvider, 0, TimeSlot.NIGHT),
            entityId = DEMO_STRENGTH_WORKOUT_A1_ID,
            timestamp = weekTimestamp(weekA, zoneId, dayOffset = 1, hour = 20),
        ),
        completeWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekA,
            seed = workoutSeed(stringProvider, 6, MORNING),
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
            stringProvider = stringProvider,
            weekStartDate = weekB,
            dayChange = WorkoutDayChange(oldDay = DayOfWeek.TUESDAY, newDay = DayOfWeek.WEDNESDAY),
            orderChange = WorkoutOrderChange(oldOrder = 0, newOrder = 0),
            slotChange = WorkoutSlotChange(oldTimeSlot = MORNING, newTimeSlot = AFTERNOON),
            seed = workoutSeed(stringProvider, 2, AFTERNOON),
            entityId = DEMO_RUN_WORKOUT_B1_ID,
            timestamp = weekTimestamp(weekB, zoneId, dayOffset = 0, hour = 8),
        ),
        moveWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekB,
            dayChange = WorkoutDayChange(oldDay = DayOfWeek.THURSDAY, newDay = DayOfWeek.THURSDAY),
            orderChange = WorkoutOrderChange(oldOrder = 1, newOrder = 0),
            slotChange = WorkoutSlotChange(oldTimeSlot = MORNING, newTimeSlot = MORNING),
            seed = workoutSeed(stringProvider, 0, MORNING),
            entityId = DEMO_STRENGTH_WORKOUT_B1_ID,
            timestamp = weekTimestamp(weekB, zoneId, dayOffset = 1, hour = 7),
        ),
        moveWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekB,
            dayChange = WorkoutDayChange(oldDay = DayOfWeek.FRIDAY, newDay = DayOfWeek.SATURDAY),
            orderChange = WorkoutOrderChange(oldOrder = 0, newOrder = 0),
            slotChange = WorkoutSlotChange(oldTimeSlot = MORNING, newTimeSlot = TimeSlot.NIGHT),
            seed = workoutSeed(stringProvider, 6, TimeSlot.NIGHT),
            entityId = DEMO_RUN_WORKOUT_B2_ID,
            timestamp = weekTimestamp(weekB, zoneId, dayOffset = 2, hour = 18),
        ),
        completeWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekB,
            seed = workoutSeed(stringProvider, 2, AFTERNOON),
            entityId = DEMO_RUN_WORKOUT_B1_ID,
            timestamp = weekTimestamp(weekB, zoneId, dayOffset = 2, hour = 20),
        ),
        completeWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekB,
            seed = workoutSeed(stringProvider, 0, MORNING),
            entityId = DEMO_STRENGTH_WORKOUT_B1_ID,
            timestamp = weekTimestamp(weekB, zoneId, dayOffset = 3, hour = 19),
        ),
        completeWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekB,
            seed = workoutSeed(stringProvider, 6, TimeSlot.NIGHT),
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
            stringProvider = stringProvider,
            weekStartDate = weekC,
            dayChange = WorkoutDayChange(oldDay = DayOfWeek.MONDAY, newDay = DayOfWeek.TUESDAY),
            orderChange = WorkoutOrderChange(oldOrder = 0, newOrder = 0),
            slotChange = WorkoutSlotChange(oldTimeSlot = MORNING, newTimeSlot = AFTERNOON),
            seed = workoutSeed(stringProvider, 2, AFTERNOON),
            entityId = DEMO_RUN_WORKOUT_C1_ID,
            timestamp = weekTimestamp(weekC, zoneId, dayOffset = 0, hour = 7),
        ),
        moveWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekC,
            dayChange = WorkoutDayChange(oldDay = DayOfWeek.WEDNESDAY, newDay = DayOfWeek.THURSDAY),
            orderChange = WorkoutOrderChange(oldOrder = 0, newOrder = 0),
            slotChange = WorkoutSlotChange(oldTimeSlot = MORNING, newTimeSlot = TimeSlot.NIGHT),
            seed = workoutSeed(stringProvider, 6, TimeSlot.NIGHT),
            entityId = DEMO_RUN_WORKOUT_C2_ID,
            timestamp = weekTimestamp(weekC, zoneId, dayOffset = 1, hour = 18),
        ),
        moveWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekC,
            dayChange = WorkoutDayChange(oldDay = DayOfWeek.THURSDAY, newDay = DayOfWeek.THURSDAY),
            orderChange = WorkoutOrderChange(oldOrder = 1, newOrder = 0),
            slotChange = WorkoutSlotChange(oldTimeSlot = TimeSlot.NIGHT, newTimeSlot = TimeSlot.NIGHT),
            seed = workoutSeed(stringProvider, 5, TimeSlot.NIGHT),
            entityId = DEMO_MOBILITY_WORKOUT_C1_ID,
            timestamp = weekTimestamp(weekC, zoneId, dayOffset = 2, hour = 20),
        ),
        completeWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekC,
            seed = workoutSeed(stringProvider, 2, AFTERNOON),
            entityId = DEMO_RUN_WORKOUT_C1_ID,
            timestamp = weekTimestamp(weekC, zoneId, dayOffset = 2, hour = 21),
        ),
        completeWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekC,
            seed = workoutSeed(stringProvider, 6, TimeSlot.NIGHT),
            entityId = DEMO_RUN_WORKOUT_C2_ID,
            timestamp = weekTimestamp(weekC, zoneId, dayOffset = 4, hour = 20),
        ),
        completeWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekC,
            seed = workoutSeed(stringProvider, 5, TimeSlot.NIGHT),
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
            stringProvider = stringProvider,
            weekStartDate = weekD,
            dayChange = WorkoutDayChange(oldDay = DayOfWeek.MONDAY, newDay = DayOfWeek.TUESDAY),
            orderChange = WorkoutOrderChange(oldOrder = 0, newOrder = 0),
            slotChange = WorkoutSlotChange(oldTimeSlot = TimeSlot.NIGHT, newTimeSlot = MORNING),
            seed = workoutSeed(stringProvider, 0, MORNING),
            entityId = DEMO_STRENGTH_WORKOUT_D1_ID,
            timestamp = weekTimestamp(weekD, zoneId, dayOffset = 0, hour = 7),
        ),
        moveWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekD,
            dayChange = WorkoutDayChange(oldDay = DayOfWeek.THURSDAY, newDay = DayOfWeek.THURSDAY),
            orderChange = WorkoutOrderChange(oldOrder = 1, newOrder = 0),
            slotChange = WorkoutSlotChange(oldTimeSlot = MORNING, newTimeSlot = MORNING),
            seed = workoutSeed(stringProvider, 2, MORNING),
            entityId = DEMO_RUN_WORKOUT_D1_ID,
            timestamp = weekTimestamp(weekD, zoneId, dayOffset = 1, hour = 7),
        ),
        createWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekD,
            dayOfWeek = DayOfWeek.SATURDAY,
            order = 0,
            seed = workoutSeed(stringProvider, 6, AFTERNOON),
            entityId = DEMO_RUN_WORKOUT_D2_ID,
            timestamp = weekTimestamp(weekD, zoneId, dayOffset = 2, hour = 17),
        ),
        completeWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekD,
            seed = workoutSeed(stringProvider, 0, MORNING),
            entityId = DEMO_STRENGTH_WORKOUT_D1_ID,
            timestamp = weekTimestamp(weekD, zoneId, dayOffset = 2, hour = 19),
        ),
        completeWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekD,
            seed = workoutSeed(stringProvider, 2, MORNING),
            entityId = DEMO_RUN_WORKOUT_D1_ID,
            timestamp = weekTimestamp(weekD, zoneId, dayOffset = 4, hour = 19),
        ),
        completeWorkoutAction(
            stringProvider = stringProvider,
            weekStartDate = weekD,
            seed = workoutSeed(stringProvider, 6, AFTERNOON),
            entityId = DEMO_RUN_WORKOUT_D2_ID,
            timestamp = weekTimestamp(weekD, zoneId, dayOffset = 5, hour = 19),
        ),
        completeWeekAction(
            weekStartDate = weekD,
            timestamp = weekTimestamp(weekD, zoneId, dayOffset = 6, hour = 20),
        ),
        categoryAction(
            stringProvider = stringProvider,
            type = UserActionType.UPDATE_CATEGORY_COLOR,
            categoryId = RUN_ID,
            categoryName = categoryNameForId(stringProvider, RUN_ID),
            timestamp = weekTimestamp(weekD, zoneId, dayOffset = 6, hour = 21),
        ),
        categoryAction(
            stringProvider = stringProvider,
            type = UserActionType.UPDATE_CATEGORY_VISIBILITY,
            categoryId = STRENGTH_ID,
            categoryName = categoryNameForId(stringProvider, STRENGTH_ID),
            timestamp = weekTimestamp(weekD, zoneId, dayOffset = 6, hour = 21, minute = 10),
        ),
        categoryAction(
            stringProvider = stringProvider,
            type = UserActionType.REORDER_CATEGORY,
            categoryId = MOBILITY_ID,
            categoryName = categoryNameForId(stringProvider, MOBILITY_ID),
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

private fun buildCurrentWeekActions(
    stringProvider: StringProvider,
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
                stringProvider = stringProvider,
                weekStartDate = currentWeekStart,
                dayOfWeek = DayOfWeek.TUESDAY,
                order = 0,
                seed = workoutSeed(stringProvider, 2, AFTERNOON),
                entityId = DEMO_RUN_WORKOUT_CURRENT_ID,
                timestamp = now - dayMillis * 5,
            ),
            moveWorkoutAction(
                stringProvider = stringProvider,
                weekStartDate = currentWeekStart,
                dayChange = WorkoutDayChange(oldDay = DayOfWeek.THURSDAY, newDay = DayOfWeek.FRIDAY),
                orderChange = WorkoutOrderChange(oldOrder = 1, newOrder = 0),
                slotChange = WorkoutSlotChange(oldTimeSlot = MORNING, newTimeSlot = AFTERNOON),
                seed = workoutSeed(stringProvider, 4, AFTERNOON),
                entityId = DEMO_CYCLING_WORKOUT_CURRENT_ID,
                timestamp = now - dayMillis * 4,
            ),
            moveWorkoutAction(
                stringProvider = stringProvider,
                weekStartDate = currentWeekStart,
                dayChange = WorkoutDayChange(oldDay = DayOfWeek.MONDAY, newDay = DayOfWeek.MONDAY),
                orderChange = WorkoutOrderChange(oldOrder = 1, newOrder = 0),
                slotChange = WorkoutSlotChange(oldTimeSlot = TimeSlot.NIGHT, newTimeSlot = MORNING),
                seed = workoutSeed(stringProvider, 1, MORNING),
                entityId = DEMO_OTHER_WORKOUT_CURRENT_ID,
                timestamp = now - dayMillis * 3,
            ),
            completeWorkoutAction(
                stringProvider = stringProvider,
                weekStartDate = currentWeekStart,
                seed = workoutSeed(stringProvider, 0, MORNING),
                entityId = DEMO_STRENGTH_WORKOUT_CURRENT_ID,
                timestamp = now - dayMillis * 2,
            ),
        )
    val plannerActions =
        listOf(
            createNonWorkoutAction(
                weekStartDate = currentWeekStart,
                dayOfWeek = DayOfWeek.WEDNESDAY,
                order = 0,
                entityType = UserActionEntityType.REST,
                timestamp = now - dayMillis * 2 + 2_000,
            ),
            createNonWorkoutAction(
                weekStartDate = currentWeekStart,
                dayOfWeek = DayOfWeek.TUESDAY,
                order = 0,
                entityType = UserActionEntityType.BUSY,
                timeSlot = MORNING,
                timestamp = now - dayMillis * 2 + 3_000,
            ),
            createNonWorkoutAction(
                weekStartDate = currentWeekStart,
                dayOfWeek = DayOfWeek.TUESDAY,
                order = 0,
                entityType = UserActionEntityType.SICK,
                timeSlot = TimeSlot.NIGHT,
                timestamp = now - dayMillis * 2 + 4_000,
            ),
            changeSlotModeAction(
                weekStartDate = currentWeekStart,
                oldPolicy = com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.AUTO_WHEN_MULTIPLE.name,
                newPolicy = com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.ALWAYS_SHOW.name,
                timestamp = now - dayMillis * 2 + 5_000,
            ),
            categoryAction(
                stringProvider = stringProvider,
                type = UserActionType.UPDATE_CATEGORY_COLOR,
                categoryId = RUN_ID,
                categoryName = categoryNameForId(stringProvider, RUN_ID),
                timestamp = now - dayMillis * 2 + 6_000,
            ),
            categoryAction(
                stringProvider = stringProvider,
                type = UserActionType.UPDATE_CATEGORY_VISIBILITY,
                categoryId = CYCLING_ID,
                categoryName = categoryNameForId(stringProvider, CYCLING_ID),
                timestamp = now - dayMillis * 2 + 7_000,
            ),
            categoryAction(
                stringProvider = stringProvider,
                type = UserActionType.REORDER_CATEGORY,
                categoryId = STRENGTH_ID,
                categoryName = categoryNameForId(stringProvider, STRENGTH_ID),
                timestamp = now - dayMillis * 2 + 8_000,
            ),
            categoryAction(
                stringProvider = stringProvider,
                type = UserActionType.UPDATE_CATEGORY_COLOR,
                categoryId = SWIM_ID,
                categoryName = categoryNameForId(stringProvider, SWIM_ID),
                timestamp = now - dayMillis * 2 + 9_000,
            ),
            categoryAction(
                stringProvider = stringProvider,
                type = UserActionType.UPDATE_CATEGORY_VISIBILITY,
                categoryId = MOBILITY_ID,
                categoryName = categoryNameForId(stringProvider, MOBILITY_ID),
                timestamp = now - dayMillis * 2 + 10_000,
            ),
            categoryAction(
                stringProvider = stringProvider,
                type = UserActionType.REORDER_CATEGORY,
                categoryId = OTHER_ID,
                categoryName = categoryNameForId(stringProvider, OTHER_ID),
                timestamp = now - dayMillis * 2 + 11_000,
            ),
            categoryAction(
                stringProvider = stringProvider,
                type = UserActionType.UPDATE_CATEGORY_COLOR,
                categoryId = RUN_ID,
                categoryName = categoryNameForId(stringProvider, RUN_ID),
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
    stringProvider: StringProvider,
    weekStartDate: LocalDate,
    dayOfWeek: DayOfWeek?,
    order: Int,
    seed: WorkoutSeed,
    entityId: Long,
    timestamp: Long,
): UserActionEntity {
    val categoryId = categoryIdForSeed(stringProvider, seed)
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
                put(CATEGORY_NAME, categoryNameForId(stringProvider, categoryId))
                seed.timeSlot?.let { put(NEW_TIME_SLOT, it.name) }
            },
        timestamp = timestamp,
    )
}

private fun moveWorkoutAction(
    stringProvider: StringProvider,
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
    val categoryId = categoryIdForSeed(stringProvider, seed)

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
                put(CATEGORY_NAME, categoryNameForId(stringProvider, categoryId))
            },
        timestamp = timestamp,
    )
}

private fun completeWorkoutAction(
    stringProvider: StringProvider,
    weekStartDate: LocalDate,
    seed: WorkoutSeed,
    entityId: Long,
    timestamp: Long,
): UserActionEntity {
    val categoryId = categoryIdForSeed(stringProvider, seed)
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
                put(CATEGORY_NAME, categoryNameForId(stringProvider, categoryId))
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
    stringProvider: StringProvider,
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

internal fun createPersonalRecordFamilyAction(
    stringProvider: StringProvider,
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
                PERSONAL_RECORD_CATEGORY_NAME to categoryNameForId(stringProvider, seed.categoryId),
                PERSONAL_RECORD_METRIC_TYPE to seed.metricType.name,
                PERSONAL_RECORD_UNIT to seed.unit.name,
                PERSONAL_RECORD_COMPARISON_RULE to seed.comparisonRule.name,
            ),
        timestamp = timestamp,
    )
}

internal fun createPersonalRecordEntryAction(
    stringProvider: StringProvider,
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
                PERSONAL_RECORD_CATEGORY_NAME to categoryNameForId(stringProvider, seed.categoryId),
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

private fun createChallengeAction(
    challengeId: Long,
    title: String,
    targetType: ChallengeTargetType,
    targetQuantity: Long,
    startDate: LocalDate,
    endDate: LocalDate,
    timestamp: Long,
): UserActionEntity {
    return action(
        type = UserActionType.CREATE_CHALLENGE,
        entityType = UserActionEntityType.CHALLENGE,
        entityId = challengeId,
        metadata =
            mapOf(
                CHALLENGE_ID to challengeId.toString(),
                CHALLENGE_TITLE to title,
                CHALLENGE_TARGET_TYPE to targetType.name,
                CHALLENGE_TARGET_QUANTITY to targetQuantity.toString(),
                CHALLENGE_START_DATE to startDate.toString(),
                CHALLENGE_END_DATE to endDate.toString(),
                CHALLENGE_LIFECYCLE to ChallengeLifecycle.ACTIVE.name,
            ),
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

internal fun categoryNameForId(
    stringProvider: StringProvider,
    categoryId: Long,
): String {
    return when (categoryId) {
        RUN_ID -> stringProvider.get(R.string.categories_category_run)
        CYCLING_ID -> stringProvider.get(R.string.categories_category_cycling)
        STRENGTH_ID -> stringProvider.get(R.string.categories_category_strength)
        SWIM_ID -> stringProvider.get(R.string.categories_category_swim)
        MOBILITY_ID -> stringProvider.get(R.string.categories_category_mobility)
        else -> stringProvider.get(R.string.category_other)
    }
}

internal fun weekTimestamp(
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
    return listOf(RUN_ID, CYCLING_ID, STRENGTH_ID, SWIM_ID, MOBILITY_ID, OTHER_ID)
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
private const val COMPLETED_TROPHY_CHALLENGE_CREATIONS = 15
private const val COMPLETED_TROPHY_ENTITY_ID_START = 50_000L
private const val COMPLETED_TROPHY_PERSONAL_RECORD_FAMILIES = 15
private const val COMPLETED_TROPHY_PERSONAL_RECORD_ENTRIES = 50
private const val COMPLETED_TROPHY_PACE_CALCULATIONS = 10
private const val COMPLETED_TROPHY_CHALLENGE_ID_START = 55_000L
private const val COMPLETED_TROPHY_CHALLENGE_TITLE_PREFIX = "Demo challenge"
private const val COMPLETED_TROPHY_PERSONAL_RECORD_FAMILY_ID_START = 60_000L
private const val COMPLETED_TROPHY_PERSONAL_RECORD_ENTRY_ID_START = 70_000L
private val COMPLETED_TROPHY_CATEGORY_ACTION_TYPES =
    listOf(
        UserActionType.UPDATE_CATEGORY_COLOR,
        UserActionType.UPDATE_CATEGORY_VISIBILITY,
        UserActionType.REORDER_CATEGORY,
    )
