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
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.ALWAYS_SHOW
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.AUTO_WHEN_MULTIPLE
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.BUSY
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

@Singleton
class DemoDataSeeder
    @Inject
    constructor(
        private val workoutDao: WorkoutDao,
        private val userActionDao: UserActionDao,
        private val stringProvider: StringProvider,
        private val categorySeeder: CategorySeeder,
        private val settingsRepository: SettingsRepository,
    ) {
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

            val today = LocalDate.now()
            val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(MONDAY))
            val previousWeekStart = currentWeekStart.minusWeeks(1)
            val olderWeekStarts =
                listOf(
                    previousWeekStart.minusWeeks(3),
                    previousWeekStart.minusWeeks(2),
                    previousWeekStart.minusWeeks(1),
                    previousWeekStart,
                )
            val nextWeekStart = currentWeekStart.plusWeeks(1)

            val workouts = buildDemoWorkouts(olderWeekStarts, currentWeekStart, nextWeekStart)

            workouts.forEach { workoutDao.insert(it) }

            seedActivityHistory(
                currentWeekStart = currentWeekStart,
                olderWeekStarts = olderWeekStarts,
                nextWeekStart = nextWeekStart,
            )

            return true
        }

        private fun buildDemoWorkouts(
            olderWeekStarts: List<LocalDate>,
            currentWeekStart: LocalDate,
            nextWeekStart: LocalDate,
        ): List<WorkoutEntity> {
            return olderWeekStarts.flatMap { weekStart ->
                buildWeekSchedule(weekStart, CompletionProfile.COMPLETED_MOST)
            } +
                buildWeekSchedule(currentWeekStart, CompletionProfile.COMPLETED_SOME) +
                buildWeekSchedule(nextWeekStart, CompletionProfile.NONE)
        }

        private fun buildWeekSchedule(
            weekStartDate: LocalDate,
            completionProfile: CompletionProfile,
        ): List<WorkoutEntity> {
            val plan =
                listOf(
                    DayPlan(MONDAY, listOf(workoutSeed(0, MORNING), workoutSeed(1, NIGHT))),
                    DayPlan(TUESDAY, listOf(busySeed(MORNING), workoutSeed(2, AFTERNOON), sickSeed(NIGHT))),
                    DayPlan(WEDNESDAY, listOf(restSeed())),
                    DayPlan(THURSDAY, listOf(workoutSeed(3, MORNING), workoutSeed(4, MORNING))),
                    DayPlan(FRIDAY, listOf(workoutSeed(5))),
                    DayPlan(SATURDAY, listOf(restSeed(NIGHT))),
                    DayPlan(SUNDAY, listOf(workoutSeed(6, AFTERNOON))),
                    DayPlan(null, listOf(workoutSeed(7))),
                )

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

            return trainingActions + plannerActions + navigationActions
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
)

private enum class CompletionProfile {
    COMPLETED_MOST,
    COMPLETED_SOME,
    NONE,
    ;

    fun completedDays(): Set<DayOfWeek> {
        return when (this) {
            COMPLETED_MOST -> DayOfWeek.entries.toSet()
            COMPLETED_SOME ->
                setOf(
                    MONDAY,
                    TUESDAY,
                    WEDNESDAY,
                )
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
private val COMPLETED_TROPHY_CATEGORY_ACTION_TYPES =
    listOf(
        UserActionType.UPDATE_CATEGORY_COLOR,
        UserActionType.UPDATE_CATEGORY_VISIBILITY,
        UserActionType.REORDER_CATEGORY,
    )
