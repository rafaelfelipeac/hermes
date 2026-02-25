package com.rafaelfelipeac.hermes.core.debug

import com.rafaelfelipeac.hermes.BuildConfig
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionDao
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionEntity
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
    ) {
        suspend fun seed() {
            if (!BuildConfig.DEBUG) return

            categorySeeder.ensureSeeded()

            workoutDao.deleteAll()
            userActionDao.deleteAll()

            val today = LocalDate.now()
            val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(MONDAY))
            val previousWeekStart = currentWeekStart.minusWeeks(1)
            val nextWeekStart = currentWeekStart.plusWeeks(1)

            val workouts =
                buildWeekSchedule(previousWeekStart, CompletionProfile.COMPLETED_MOST)
                    .plus(buildWeekSchedule(currentWeekStart, CompletionProfile.COMPLETED_SOME))
                    .plus(buildWeekSchedule(nextWeekStart, CompletionProfile.NONE))

            workouts.forEach { workoutDao.insert(it) }

            seedActivityHistory(
                currentWeekStart = currentWeekStart,
                previousWeekStart = previousWeekStart,
                nextWeekStart = nextWeekStart,
            )
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
            previousWeekStart: LocalDate,
            nextWeekStart: LocalDate,
        ) {
            buildActivityHistoryActions(
                currentWeekStart = currentWeekStart,
                previousWeekStart = previousWeekStart,
                nextWeekStart = nextWeekStart,
            ).forEach { userActionDao.insert(it) }
        }

        private fun buildActivityHistoryActions(
            currentWeekStart: LocalDate,
            previousWeekStart: LocalDate,
            nextWeekStart: LocalDate,
        ): List<UserActionEntity> {
            val zoneId = ZoneId.systemDefault()
            val now = System.currentTimeMillis()
            val dayMillis = 24 * 60 * 60 * 1000L

            return buildCurrentWeekActions(
                currentWeekStart = currentWeekStart,
                previousWeekStart = previousWeekStart,
                nextWeekStart = nextWeekStart,
                now = now,
                dayMillis = dayMillis,
            ) +
                buildPreviousWeekActions(
                    previousWeekStart = previousWeekStart,
                    zoneId = zoneId,
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
                        timestamp = now - dayMillis * 5,
                    ),
                    moveWorkoutAction(
                        weekStartDate = currentWeekStart,
                        dayChange = WorkoutDayChange(oldDay = THURSDAY, newDay = FRIDAY),
                        orderChange = WorkoutOrderChange(oldOrder = 1, newOrder = 0),
                        slotChange = WorkoutSlotChange(oldTimeSlot = MORNING, newTimeSlot = AFTERNOON),
                        seed = workoutSeed(4, AFTERNOON),
                        timestamp = now - dayMillis * 4,
                    ),
                    moveWorkoutAction(
                        weekStartDate = currentWeekStart,
                        dayChange = WorkoutDayChange(oldDay = MONDAY, newDay = MONDAY),
                        orderChange = WorkoutOrderChange(oldOrder = 1, newOrder = 0),
                        slotChange = WorkoutSlotChange(oldTimeSlot = NIGHT, newTimeSlot = MORNING),
                        seed = workoutSeed(1, MORNING),
                        timestamp = now - dayMillis * 3,
                    ),
                    completeWorkoutAction(
                        weekStartDate = currentWeekStart,
                        seed = workoutSeed(0, MORNING),
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

        private fun buildPreviousWeekActions(
            previousWeekStart: LocalDate,
            zoneId: ZoneId,
        ): List<UserActionEntity> {
            return listOf(
                createWorkoutAction(
                    weekStartDate = previousWeekStart,
                    dayOfWeek = null,
                    order = 0,
                    seed = workoutSeed(7),
                    timestamp =
                        previousWeekStart
                            .atStartOfDay(zoneId)
                            .plusHours(10)
                            .toInstant()
                            .toEpochMilli(),
                ),
            )
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
            timestamp: Long,
        ): UserActionEntity {
            return action(
                type = UserActionType.CREATE_WORKOUT,
                entityType = UserActionEntityType.WORKOUT,
                metadata =
                    buildMap {
                        put(WEEK_START_DATE, weekStartDate.toString())
                        put(DAY_OF_WEEK, dayOfWeek?.value?.toString() ?: UNPLANNED)
                        put(NEW_ORDER, order.toString())
                        put(NEW_TYPE, seed.type)
                        put(NEW_DESCRIPTION, seed.description)
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
            timestamp: Long,
        ): UserActionEntity {
            val actionType =
                if (dayChange.oldDay == dayChange.newDay) {
                    UserActionType.REORDER_WORKOUT
                } else {
                    UserActionType.MOVE_WORKOUT_BETWEEN_DAYS
                }

            return action(
                type = actionType,
                entityType = UserActionEntityType.WORKOUT,
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
                    },
                timestamp = timestamp,
            )
        }

        private fun completeWorkoutAction(
            weekStartDate: LocalDate,
            seed: WorkoutSeed,
            timestamp: Long,
        ): UserActionEntity {
            return action(
                type = UserActionType.COMPLETE_WORKOUT,
                entityType = UserActionEntityType.WORKOUT,
                metadata =
                    mapOf(
                        WEEK_START_DATE to weekStartDate.toString(),
                        WAS_COMPLETED to "false",
                        IS_COMPLETED to "true",
                        NEW_TYPE to seed.type,
                        NEW_DESCRIPTION to seed.description,
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
                            "Unsupported UserActionEntityType: $entityType in DemoDataSeeder",
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
            metadata: Map<String, String>,
            timestamp: Long,
        ): UserActionEntity {
            return UserActionEntity(
                actionType = type.name,
                entityType = entityType.name,
                entityId = null,
                metadata = UserActionMetadataSerializer.toJson(metadata),
                timestamp = timestamp,
            )
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
