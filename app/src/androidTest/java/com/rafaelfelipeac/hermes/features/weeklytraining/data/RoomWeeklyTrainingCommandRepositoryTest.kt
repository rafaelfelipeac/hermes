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
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WAS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.RACE_EVENT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COMPLETE_RACE_EVENT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryEntity
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WeeklyTrainingCommandResult
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutCompletionCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
            logger.assertLoggedOnce(
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
            logger.assertLoggedOnce(
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

    private fun sampleWorkout(
        eventType: EventType = EventType.WORKOUT,
        isCompleted: Boolean = false,
        isRestDay: Boolean = false,
    ) = WorkoutEntity(
        id = WORKOUT_ID,
        weekStartDate = LocalDate.parse("2026-09-07"),
        dayOfWeek = 1,
        type = WORKOUT_TYPE,
        description = WORKOUT_DESCRIPTION,
        isCompleted = isCompleted,
        isRestDay = isRestDay,
        eventType = eventType.name,
        timeSlot = null,
        categoryId = WORKOUT_CATEGORY_ID,
        sortOrder = 0,
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

        fun assertLoggedOnce(
            actionType: UserActionType,
            entityType: UserActionEntityType,
        ) {
            val action = actions.single()
            assertEquals(actionType, action.actionType)
            assertEquals(entityType, action.entityType)
            assertEquals(WORKOUT_ID, action.entityId)
            assertEquals("2026-09-07", action.metadata?.get(WEEK_START_DATE))
            assertEquals("false", action.metadata?.get(WAS_COMPLETED))
            assertEquals("true", action.metadata?.get(IS_COMPLETED))
            assertEquals(WORKOUT_TYPE, action.metadata?.get(NEW_TYPE))
            assertEquals(WORKOUT_DESCRIPTION, action.metadata?.get(NEW_DESCRIPTION))
            assertEquals(WORKOUT_CATEGORY_ID.toString(), action.metadata?.get(CATEGORY_ID))
            assertEquals(CATEGORY_NAME_VALUE, action.metadata?.get(CATEGORY_NAME))
            assertEquals(CATEGORY_NAME_VALUE, action.metadata?.get(NEW_CATEGORY_NAME))
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
