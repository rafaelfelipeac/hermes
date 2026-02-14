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
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_ORDER
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
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
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
                    DayPlan(MONDAY, listOf(mockWorkout(0), mockWorkout(1))),
                    DayPlan(TUESDAY, listOf(mockWorkout(2))),
                    DayPlan(WEDNESDAY, listOf(restDay())),
                    DayPlan(THURSDAY, listOf(mockWorkout(3), mockWorkout(4))),
                    DayPlan(FRIDAY, listOf(mockWorkout(5))),
                    DayPlan(SATURDAY, listOf(restDay())),
                    DayPlan(SUNDAY, listOf(mockWorkout(6))),
                    DayPlan(null, listOf(mockWorkout(7))),
                )

            val completedDays = completionProfile.completedDays()

            return plan.flatMap { dayPlan ->
                dayPlan.items.mapIndexed { index, seed ->
                    val isCompleted =
                        !seed.isRestDay &&
                            dayPlan.dayOfWeek != null &&
                            completedDays.contains(dayPlan.dayOfWeek)

                    WorkoutEntity(
                        weekStartDate = weekStartDate,
                        dayOfWeek = dayPlan.dayOfWeek?.value,
                        type = if (seed.isRestDay) EMPTY else seed.type,
                        description = if (seed.isRestDay) EMPTY else seed.description,
                        isCompleted = isCompleted,
                        isRestDay = seed.isRestDay,
                        categoryId =
                            if (seed.isRestDay) {
                                null
                            } else {
                                categoryIdForSeed(seed)
                            },
                        sortOrder = index,
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
                COLOR_RUN -> 2L
                COLOR_CYCLING -> 3L
                COLOR_STRENGTH -> 4L
                COLOR_SWIM -> 5L
                COLOR_MOBILITY -> 6L
                else -> 7L
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

        private fun buildCurrentWeekActions(
            currentWeekStart: LocalDate,
            previousWeekStart: LocalDate,
            nextWeekStart: LocalDate,
            now: Long,
            dayMillis: Long,
        ): List<UserActionEntity> {
            return listOf(
                openWeekAction(
                    oldWeekStart = previousWeekStart,
                    newWeekStart = currentWeekStart,
                    timestamp = now - dayMillis * 6,
                ),
                createWorkoutAction(
                    weekStartDate = currentWeekStart,
                    dayOfWeek = TUESDAY,
                    order = 0,
                    seed = mockWorkout(2),
                    timestamp = now - dayMillis * 5,
                ),
                moveWorkoutAction(
                    weekStartDate = currentWeekStart,
                    dayChange = WorkoutDayChange(oldDay = THURSDAY, newDay = FRIDAY),
                    orderChange = WorkoutOrderChange(oldOrder = 1, newOrder = 0),
                    seed = mockWorkout(4),
                    timestamp = now - dayMillis * 4,
                ),
                moveWorkoutAction(
                    weekStartDate = currentWeekStart,
                    dayChange = WorkoutDayChange(oldDay = MONDAY, newDay = MONDAY),
                    orderChange = WorkoutOrderChange(oldOrder = 1, newOrder = 0),
                    seed = mockWorkout(1),
                    timestamp = now - dayMillis * 3,
                ),
                completeWorkoutAction(
                    weekStartDate = currentWeekStart,
                    seed = mockWorkout(0),
                    timestamp = now - dayMillis * 2,
                ),
                createRestDayAction(
                    weekStartDate = currentWeekStart,
                    dayOfWeek = WEDNESDAY,
                    order = 0,
                    timestamp = now - dayMillis * 2 + 2_000,
                ),
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
                    seed = mockWorkout(7),
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
                    mapOf(
                        WEEK_START_DATE to weekStartDate.toString(),
                        DAY_OF_WEEK to (dayOfWeek?.value?.toString() ?: UNPLANNED),
                        NEW_ORDER to order.toString(),
                        NEW_TYPE to seed.type,
                        NEW_DESCRIPTION to seed.description,
                    ),
                timestamp = timestamp,
            )
        }

        private fun moveWorkoutAction(
            weekStartDate: LocalDate,
            dayChange: WorkoutDayChange,
            orderChange: WorkoutOrderChange,
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
                    mapOf(
                        WEEK_START_DATE to weekStartDate.toString(),
                        OLD_DAY_OF_WEEK to dayChange.oldDay.value.toString(),
                        NEW_DAY_OF_WEEK to dayChange.newDay.value.toString(),
                        OLD_ORDER to orderChange.oldOrder.toString(),
                        NEW_ORDER to orderChange.newOrder.toString(),
                        NEW_TYPE to seed.type,
                        NEW_DESCRIPTION to seed.description,
                    ),
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

        private fun createRestDayAction(
            weekStartDate: LocalDate,
            dayOfWeek: DayOfWeek,
            order: Int,
            timestamp: Long,
        ): UserActionEntity {
            return action(
                type = UserActionType.CREATE_REST_DAY,
                entityType = UserActionEntityType.REST_DAY,
                metadata =
                    mapOf(
                        WEEK_START_DATE to weekStartDate.toString(),
                        DAY_OF_WEEK to dayOfWeek.value.toString(),
                        NEW_ORDER to order.toString(),
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

        private fun mockWorkout(index: Int): WorkoutSeed {
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
                type = types[safeIndex],
                description = descriptions[safeIndex],
                isRestDay = false,
            )
        }

        private fun restDay(): WorkoutSeed {
            return WorkoutSeed(
                type = EMPTY,
                description = EMPTY,
                isRestDay = true,
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

private data class WorkoutSeed(
    val type: String,
    val description: String,
    val isRestDay: Boolean,
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
