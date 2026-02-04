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
    ) {
        suspend fun seed() {
            if (!BuildConfig.DEBUG) return

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
                        sortOrder = index,
                    )
                }
            }
        }

        private suspend fun seedActivityHistory(
            currentWeekStart: LocalDate,
            previousWeekStart: LocalDate,
            nextWeekStart: LocalDate,
        ) {
            val zoneId = ZoneId.systemDefault()
            val now = System.currentTimeMillis()
            val dayMillis = 24 * 60 * 60 * 1000L

            val actions =
                listOf(
                    action(
                        type = UserActionType.OPEN_WEEK,
                        entityType = UserActionEntityType.WEEK,
                        metadata =
                            mapOf(
                                OLD_WEEK_START_DATE to previousWeekStart.toString(),
                                NEW_WEEK_START_DATE to currentWeekStart.toString(),
                                WEEK_START_DATE to currentWeekStart.toString(),
                            ),
                        timestamp = now - dayMillis * 6,
                    ),
                    action(
                        type = UserActionType.CREATE_WORKOUT,
                        entityType = UserActionEntityType.WORKOUT,
                        metadata =
                            mapOf(
                                WEEK_START_DATE to currentWeekStart.toString(),
                                DAY_OF_WEEK to TUESDAY.value.toString(),
                                NEW_ORDER to "0",
                                NEW_TYPE to mockWorkout(2).type,
                                NEW_DESCRIPTION to mockWorkout(2).description,
                            ),
                        timestamp = now - dayMillis * 5,
                    ),
                    action(
                        type = UserActionType.MOVE_WORKOUT_BETWEEN_DAYS,
                        entityType = UserActionEntityType.WORKOUT,
                        metadata =
                            mapOf(
                                WEEK_START_DATE to currentWeekStart.toString(),
                                OLD_DAY_OF_WEEK to THURSDAY.value.toString(),
                                NEW_DAY_OF_WEEK to FRIDAY.value.toString(),
                                OLD_ORDER to "1",
                                NEW_ORDER to "0",
                                NEW_TYPE to mockWorkout(4).type,
                                NEW_DESCRIPTION to mockWorkout(4).description,
                            ),
                        timestamp = now - dayMillis * 4,
                    ),
                    action(
                        type = UserActionType.REORDER_WORKOUT,
                        entityType = UserActionEntityType.WORKOUT,
                        metadata =
                            mapOf(
                                WEEK_START_DATE to currentWeekStart.toString(),
                                OLD_DAY_OF_WEEK to MONDAY.value.toString(),
                                NEW_DAY_OF_WEEK to MONDAY.value.toString(),
                                OLD_ORDER to "1",
                                NEW_ORDER to "0",
                                NEW_TYPE to mockWorkout(1).type,
                                NEW_DESCRIPTION to mockWorkout(1).description,
                            ),
                        timestamp = now - dayMillis * 3,
                    ),
                    action(
                        type = UserActionType.COMPLETE_WORKOUT,
                        entityType = UserActionEntityType.WORKOUT,
                        metadata =
                            mapOf(
                                WEEK_START_DATE to currentWeekStart.toString(),
                                WAS_COMPLETED to "false",
                                IS_COMPLETED to "true",
                                NEW_TYPE to mockWorkout(0).type,
                                NEW_DESCRIPTION to mockWorkout(0).description,
                            ),
                        timestamp = now - dayMillis * 2,
                    ),
                    action(
                        type = UserActionType.CREATE_REST_DAY,
                        entityType = UserActionEntityType.REST_DAY,
                        metadata =
                            mapOf(
                                WEEK_START_DATE to currentWeekStart.toString(),
                                DAY_OF_WEEK to WEDNESDAY.value.toString(),
                                NEW_ORDER to "0",
                            ),
                        timestamp = now - dayMillis * 2 + 2_000,
                    ),
                    action(
                        type = UserActionType.OPEN_WEEK,
                        entityType = UserActionEntityType.WEEK,
                        metadata =
                            mapOf(
                                OLD_WEEK_START_DATE to currentWeekStart.toString(),
                                NEW_WEEK_START_DATE to nextWeekStart.toString(),
                                WEEK_START_DATE to nextWeekStart.toString(),
                            ),
                        timestamp = now - dayMillis,
                    ),
                    action(
                        type = UserActionType.OPEN_WEEK,
                        entityType = UserActionEntityType.WEEK,
                        metadata =
                            mapOf(
                                OLD_WEEK_START_DATE to nextWeekStart.toString(),
                                NEW_WEEK_START_DATE to currentWeekStart.toString(),
                                WEEK_START_DATE to currentWeekStart.toString(),
                            ),
                        timestamp = now - dayMillis + 3_000,
                    ),
                    action(
                        type = UserActionType.CREATE_WORKOUT,
                        entityType = UserActionEntityType.WORKOUT,
                        metadata =
                            mapOf(
                                WEEK_START_DATE to previousWeekStart.toString(),
                                DAY_OF_WEEK to UNPLANNED,
                                NEW_ORDER to "0",
                                NEW_TYPE to mockWorkout(7).type,
                                NEW_DESCRIPTION to mockWorkout(7).description,
                            ),
                        timestamp =
                            previousWeekStart
                                .atStartOfDay(zoneId)
                                .plusHours(10)
                                .toInstant()
                                .toEpochMilli(),
                    ),
                )

            actions.forEach { userActionDao.insert(it) }
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
