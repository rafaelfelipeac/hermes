package com.rafaelfelipeac.hermes.features.weeklytraining.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.core.useraction.domain.UserAction
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.IS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TIME_SLOT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_TIME_SLOT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WAS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.RACE_EVENT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COMPLETE_RACE_EVENT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.MOVE_WORKOUT_BETWEEN_DAYS
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.REORDER_WORKOUT
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryEntity
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WeeklyTrainingCommandResult
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutCompletionCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutDeleteCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutScheduleChange
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutScheduleCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.DayOfWeek
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class RoomWeeklyTrainingCommandRepositoryTest {
    private lateinit var context: Context
    private lateinit var database: HermesDatabase
    private lateinit var logger: FakeUserActionLogger
    private lateinit var repository: RoomWeeklyTrainingCommandRepository

    @Before
    fun setUp() =
        runTest {
            context = ApplicationProvider.getApplicationContext()
            database =
                Room.inMemoryDatabaseBuilder(context, HermesDatabase::class.java)
                    .allowMainThreadQueries()
                    .build()
            logger = FakeUserActionLogger()
            repository = RoomWeeklyTrainingCommandRepository(database, logger)
        }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun updateCompletion_updatesWorkoutAndLogsFromPersistedState() =
        runTest {
            seedCategory()
            seedWorkout(sampleWorkout())

            val result = repository.updateCompletion(completionCommand(isCompleted = true))

            assertEquals(
                WeeklyTrainingCommandResult.CompletionChanged(
                    previousCompleted = false,
                    eventType = EventType.WORKOUT,
                ),
                result,
            )
            assertEquals(true, database.workoutDao().getWorkout(WORKOUT_ID)?.isCompleted)
            logger.assertCompletionLoggedOnce(
                actionType = COMPLETE_WORKOUT,
                entityType = WORKOUT,
            )
        }

    @Test
    fun updateCompletion_logsRaceEventTypeFromPersistedState() =
        runTest {
            seedCategory()
            seedWorkout(sampleWorkout(eventType = EventType.RACE_EVENT))

            val result = repository.updateCompletion(completionCommand(isCompleted = true))

            assertEquals(
                WeeklyTrainingCommandResult.CompletionChanged(
                    previousCompleted = false,
                    eventType = EventType.RACE_EVENT,
                ),
                result,
            )
            logger.assertCompletionLoggedOnce(
                actionType = COMPLETE_RACE_EVENT,
                entityType = RACE_EVENT,
            )
        }

    @Test
    fun updateCompletion_returnsNoChangeWhenPersistedStateAlreadyMatches() =
        runTest {
            seedWorkout(sampleWorkout(isCompleted = true))

            val result = repository.updateCompletion(completionCommand(isCompleted = true))

            assertEquals(WeeklyTrainingCommandResult.NoChange, result)
            assertEquals(true, database.workoutDao().getWorkout(WORKOUT_ID)?.isCompleted)
            assertTrue(logger.actions.isEmpty())
        }

    @Test
    fun updateCompletion_returnsNoChangeForEventThatDoesNotSupportCompletion() =
        runTest {
            seedWorkout(sampleWorkout(eventType = EventType.REST, isRestDay = true))

            val result = repository.updateCompletion(completionCommand(isCompleted = true))

            assertEquals(WeeklyTrainingCommandResult.NoChange, result)
            assertEquals(false, database.workoutDao().getWorkout(WORKOUT_ID)?.isCompleted)
            assertTrue(logger.actions.isEmpty())
        }

    @Test
    fun updateCompletion_rollsBackWhenLoggerFails() =
        runTest {
            seedWorkout(sampleWorkout())
            logger.failNextLog = true

            val result = runCatching { repository.updateCompletion(completionCommand(isCompleted = true)) }

            assertTrue(result.isFailure)
            assertEquals(false, database.workoutDao().getWorkout(WORKOUT_ID)?.isCompleted)
            assertTrue(logger.actions.isEmpty())
        }

    @Test
    fun deleteWorkout_deletesWorkoutNormalizesBucketAndLogsFromPersistedState() =
        runTest {
            seedCategory()
            seedWorkout(sampleWorkout(id = 10, sortOrder = 0))
            seedWorkout(sampleWorkout(id = WORKOUT_ID, sortOrder = 1))
            seedWorkout(sampleWorkout(id = 30, sortOrder = 2))

            val result = repository.deleteWorkout(deleteCommand())

            assertEquals(WeeklyTrainingCommandResult.WorkoutDeleted, result)
            assertEquals(null, database.workoutDao().getWorkout(WORKOUT_ID))
            assertEquals(0, database.workoutDao().getWorkout(10)?.sortOrder)
            assertEquals(1, database.workoutDao().getWorkout(30)?.sortOrder)
            logger.assertDeleteLoggedOnce(
                actionType = DELETE_WORKOUT,
                entityType = WORKOUT,
            )
        }

    @Test
    fun deleteWorkout_returnsNoChangeForMissingWorkout() =
        runTest {
            val result = repository.deleteWorkout(deleteCommand())

            assertEquals(WeeklyTrainingCommandResult.NoChange, result)
            assertTrue(logger.actions.isEmpty())
        }

    @Test
    fun deleteWorkout_rollsBackWhenLoggerFails() =
        runTest {
            seedWorkout(sampleWorkout())
            logger.failNextLog = true

            val result = runCatching { repository.deleteWorkout(deleteCommand()) }

            assertTrue(result.isFailure)
            assertEquals(sampleWorkout(), database.workoutDao().getWorkout(WORKOUT_ID))
            assertTrue(logger.actions.isEmpty())
        }

    @Test
    fun updateSchedule_updatesChangesAndLogsMoveFromPersistedState() =
        runTest {
            seedCategory()
            seedWorkout(sampleWorkout(id = 10, sortOrder = 0, timeSlot = TimeSlot.MORNING.name))
            seedWorkout(sampleWorkout(id = WORKOUT_ID, sortOrder = 1, timeSlot = TimeSlot.MORNING.name))

            val result =
                repository.updateSchedule(
                    scheduleCommand(
                        WorkoutScheduleChange(
                            workoutId = 10,
                            weekStartDate = LocalDate.parse("2026-09-07"),
                            dayOfWeek = DayOfWeek.MONDAY,
                            timeSlot = TimeSlot.MORNING,
                            order = 0,
                        ),
                        WorkoutScheduleChange(
                            workoutId = WORKOUT_ID,
                            weekStartDate = LocalDate.parse("2026-09-07"),
                            dayOfWeek = DayOfWeek.TUESDAY,
                            timeSlot = TimeSlot.AFTERNOON,
                            order = 0,
                        ),
                    ),
                )

            assertEquals(WeeklyTrainingCommandResult.ScheduleChanged, result)
            val movedWorkout = database.workoutDao().getWorkout(WORKOUT_ID)
            assertEquals(DayOfWeek.TUESDAY.value, movedWorkout?.dayOfWeek)
            assertEquals(TimeSlot.AFTERNOON.name, movedWorkout?.timeSlot)
            assertEquals(0, movedWorkout?.sortOrder)
            logger.assertScheduleLoggedOnce(
                actionType = MOVE_WORKOUT_BETWEEN_DAYS,
                oldDayOfWeek = DayOfWeek.MONDAY.value.toString(),
                newDayOfWeek = DayOfWeek.TUESDAY.value.toString(),
                oldTimeSlot = TimeSlot.MORNING.name,
                newTimeSlot = TimeSlot.AFTERNOON.name,
                oldOrder = "1",
                newOrder = "0",
            )
        }

    @Test
    fun updateSchedule_logsReorderWhenDayAndSlotDoNotChange() =
        runTest {
            seedCategory()
            seedWorkout(sampleWorkout(sortOrder = 1, timeSlot = TimeSlot.MORNING.name))

            val result =
                repository.updateSchedule(
                    scheduleCommand(
                        WorkoutScheduleChange(
                            workoutId = WORKOUT_ID,
                            weekStartDate = LocalDate.parse("2026-09-07"),
                            dayOfWeek = DayOfWeek.MONDAY,
                            timeSlot = TimeSlot.MORNING,
                            order = 0,
                        ),
                    ),
                )

            assertEquals(WeeklyTrainingCommandResult.ScheduleChanged, result)
            logger.assertScheduleLoggedOnce(
                actionType = REORDER_WORKOUT,
                oldDayOfWeek = DayOfWeek.MONDAY.value.toString(),
                newDayOfWeek = DayOfWeek.MONDAY.value.toString(),
                oldTimeSlot = TimeSlot.MORNING.name,
                newTimeSlot = TimeSlot.MORNING.name,
                oldOrder = "1",
                newOrder = "0",
            )
        }

    @Test
    fun updateSchedule_rollsBackWhenLoggerFails() =
        runTest {
            val original = sampleWorkout(sortOrder = 1)
            seedWorkout(original)
            logger.failNextLog = true

            val result =
                runCatching {
                    repository.updateSchedule(
                        scheduleCommand(
                            WorkoutScheduleChange(
                                workoutId = WORKOUT_ID,
                                weekStartDate = LocalDate.parse("2026-09-07"),
                                dayOfWeek = DayOfWeek.TUESDAY,
                                timeSlot = null,
                                order = 0,
                            ),
                        ),
                    )
                }

            assertTrue(result.isFailure)
            assertEquals(original, database.workoutDao().getWorkout(WORKOUT_ID))
            assertTrue(logger.actions.isEmpty())
        }

    private suspend fun seedCategory() {
        database.categoryDao().insert(
            CategoryEntity(
                id = WORKOUT_CATEGORY_ID,
                name = CATEGORY_NAME_VALUE,
                colorId = "run",
                sortOrder = 0,
                isHidden = false,
                isSystem = true,
            ),
        )
    }

    private suspend fun seedWorkout(workout: WorkoutEntity) {
        database.workoutDao().insert(workout)
    }

    private fun completionCommand(isCompleted: Boolean) =
        WorkoutCompletionCommand(
            workoutId = WORKOUT_ID,
            isCompleted = isCompleted,
            displayWeekStart = LocalDate.parse("2026-09-07"),
        )

    private fun deleteCommand() =
        WorkoutDeleteCommand(
            workoutId = WORKOUT_ID,
            displayWeekStart = LocalDate.parse("2026-09-07"),
        )

    private fun scheduleCommand(vararg changes: WorkoutScheduleChange) =
        WorkoutScheduleCommand(
            movedWorkoutId = WORKOUT_ID,
            displayWeekStart = LocalDate.parse("2026-09-07"),
            changes = changes.toList(),
        )

    private fun sampleWorkout(
        id: Long = WORKOUT_ID,
        eventType: EventType = EventType.WORKOUT,
        isCompleted: Boolean = false,
        isRestDay: Boolean = false,
        dayOfWeek: Int? = DayOfWeek.MONDAY.value,
        timeSlot: String? = null,
        sortOrder: Int = 0,
    ) = WorkoutEntity(
        id = id,
        weekStartDate = LocalDate.parse("2026-09-07"),
        dayOfWeek = dayOfWeek,
        type = WORKOUT_TYPE,
        description = WORKOUT_DESCRIPTION,
        isCompleted = isCompleted,
        isRestDay = isRestDay,
        eventType = eventType.name,
        timeSlot = timeSlot,
        categoryId = WORKOUT_CATEGORY_ID,
        sortOrder = sortOrder,
    )

    private class FakeUserActionLogger : UserActionLogger {
        val actions = mutableListOf<UserAction>()
        var failNextLog = false

        override suspend fun log(action: UserAction) {
            if (failNextLog) {
                failNextLog = false
                error("Forced logger failure")
            }

            actions.add(action)
        }

        private fun assertCommonAction(
            actionType: UserActionType,
            entityType: UserActionEntityType,
        ): UserAction {
            val action = actions.single()
            assertEquals(actionType, action.actionType)
            assertEquals(entityType, action.entityType)
            assertEquals(WORKOUT_ID, action.entityId)
            assertEquals("2026-09-07", action.metadata?.get(WEEK_START_DATE))
            return action
        }

        fun assertCompletionLoggedOnce(
            actionType: UserActionType,
            entityType: UserActionEntityType,
        ) {
            val action = assertCommonAction(actionType, entityType)
            assertEquals("false", action.metadata?.get(WAS_COMPLETED))
            assertEquals("true", action.metadata?.get(IS_COMPLETED))
            assertEquals(WORKOUT_TYPE, action.metadata?.get(NEW_TYPE))
            assertEquals(WORKOUT_DESCRIPTION, action.metadata?.get(NEW_DESCRIPTION))
            assertEquals(WORKOUT_CATEGORY_ID.toString(), action.metadata?.get(CATEGORY_ID))
            assertEquals(CATEGORY_NAME_VALUE, action.metadata?.get(CATEGORY_NAME))
            assertEquals(CATEGORY_NAME_VALUE, action.metadata?.get(NEW_CATEGORY_NAME))
        }

        fun assertDeleteLoggedOnce(
            actionType: UserActionType,
            entityType: UserActionEntityType,
        ) {
            val action = assertCommonAction(actionType, entityType)
            assertEquals(WORKOUT_TYPE, action.metadata?.get(OLD_TYPE))
            assertEquals(WORKOUT_DESCRIPTION, action.metadata?.get(OLD_DESCRIPTION))
            assertEquals(WORKOUT_CATEGORY_ID.toString(), action.metadata?.get(CATEGORY_ID))
            assertEquals(CATEGORY_NAME_VALUE, action.metadata?.get(CATEGORY_NAME))
            assertEquals(WORKOUT_CATEGORY_ID.toString(), action.metadata?.get(OLD_CATEGORY_ID))
            assertEquals(CATEGORY_NAME_VALUE, action.metadata?.get(OLD_CATEGORY_NAME))
        }

        fun assertScheduleLoggedOnce(
            actionType: UserActionType,
            oldDayOfWeek: String,
            newDayOfWeek: String,
            oldTimeSlot: String,
            newTimeSlot: String,
            oldOrder: String,
            newOrder: String,
        ) {
            val action = assertCommonAction(actionType, WORKOUT)
            assertEquals(oldDayOfWeek, action.metadata?.get(OLD_DAY_OF_WEEK))
            assertEquals(newDayOfWeek, action.metadata?.get(NEW_DAY_OF_WEEK))
            assertEquals(oldTimeSlot, action.metadata?.get(OLD_TIME_SLOT))
            assertEquals(newTimeSlot, action.metadata?.get(NEW_TIME_SLOT))
            assertEquals(oldOrder, action.metadata?.get(OLD_ORDER))
            assertEquals(newOrder, action.metadata?.get(NEW_ORDER))
            assertEquals(WORKOUT_TYPE, action.metadata?.get(NEW_TYPE))
            assertEquals(WORKOUT_DESCRIPTION, action.metadata?.get(NEW_DESCRIPTION))
            assertEquals(WORKOUT_CATEGORY_ID.toString(), action.metadata?.get(CATEGORY_ID))
            assertEquals(CATEGORY_NAME_VALUE, action.metadata?.get(CATEGORY_NAME))
            assertEquals(CATEGORY_NAME_VALUE, action.metadata?.get(NEW_CATEGORY_NAME))
            assertEquals(WORKOUT_CATEGORY_ID.toString(), action.metadata?.get(OLD_CATEGORY_ID))
            assertEquals(CATEGORY_NAME_VALUE, action.metadata?.get(OLD_CATEGORY_NAME))
        }
    }

    private companion object {
        const val WORKOUT_ID = 100L
        const val WORKOUT_CATEGORY_ID = 200L
        const val CATEGORY_NAME_VALUE = "Run"
        const val WORKOUT_TYPE = "Easy run"
        const val WORKOUT_DESCRIPTION = "Aerobic"
    }
}
