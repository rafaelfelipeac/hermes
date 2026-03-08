package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TIME_SLOT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_TIME_SLOT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.MOVE_WORKOUT_BETWEEN_DAYS
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.REORDER_WORKOUT
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.AFTERNOON
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.MORNING
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import com.rafaelfelipeac.hermes.test.MainDispatcherRule
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyTrainingViewModelMoveAndUpdateTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun moveWorkout_normalizesOrdersForSourceDay() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.observeWorkoutsForWeekStarts(any()) } returns workoutsFlow

            val viewModel = createViewModel(repository, userActionLogger)
            val collectJob = backgroundScope.launch { viewModel.state.collect() }
            val selectedDate = LocalDate.of(2026, 3, 4)
            val weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(MONDAY))
            val restDay =
                workout(
                    id = 10,
                    weekStart = weekStart,
                    day = MONDAY,
                    order = 0,
                    isRestDay = true,
                )
            val mondayWorkout =
                workout(
                    id = 11,
                    weekStart = weekStart,
                    day = MONDAY,
                    order = 1,
                )

            viewModel.onWeekChanged(selectedDate)
            workoutsFlow.value = listOf(restDay, mondayWorkout)
            advanceUntilIdle()

            viewModel.moveWorkout(restDay.id, TUESDAY, null, 0)
            advanceUntilIdle()

            coVerify(exactly = 1) {
                repository.updateWorkoutSchedule(
                    workoutId = mondayWorkout.id,
                    weekStartDate = weekStart,
                    dayOfWeek = MONDAY,
                    timeSlot = null,
                    order = 0,
                )
            }
            coVerify(exactly = 1) {
                repository.updateWorkoutSchedule(
                    workoutId = restDay.id,
                    weekStartDate = weekStart,
                    dayOfWeek = TUESDAY,
                    timeSlot = null,
                    order = 0,
                )
            }

            collectJob.cancel()
        }

    @Test
    fun moveWorkout_toTbdInCrossBoundaryWeek_movesToActiveUnassignedBucket() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.observeWorkoutsForWeekStarts(any()) } returns workoutsFlow

            val viewModel =
                createViewModel(
                    repository = repository,
                    userActionLogger = userActionLogger,
                    weekStartDay = WeekStartDay.WEDNESDAY,
                )
            val collectJob = backgroundScope.launch { viewModel.state.collect() }
            val selectedDate = LocalDate.of(2026, 3, 1)
            val activeUnassignedBucket = LocalDate.of(2026, 2, 23)
            val originalStorageWeek = LocalDate.of(2026, 3, 2)
            val workoutId = 21L

            viewModel.onWeekChanged(selectedDate)
            workoutsFlow.value =
                listOf(
                    workout(id = workoutId, weekStart = originalStorageWeek, day = TUESDAY, order = 0),
                )
            advanceUntilIdle()

            viewModel.moveWorkout(workoutId, null, null, 0)
            advanceUntilIdle()

            coVerify(exactly = 1) {
                repository.updateWorkoutSchedule(
                    workoutId = workoutId,
                    weekStartDate = activeUnassignedBucket,
                    dayOfWeek = null,
                    timeSlot = null,
                    order = 0,
                )
            }

            collectJob.cancel()
        }

    @Test
    fun moveWorkout_betweenSlots_sameDay_logsMoveAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.observeWorkoutsForWeekStarts(any()) } returns workoutsFlow

            val viewModel = createViewModel(repository, userActionLogger)
            val collectJob = backgroundScope.launch { viewModel.state.collect() }
            val selectedDate = LocalDate.of(2026, 3, 4)
            val weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(MONDAY))
            val mondayWorkoutA =
                workout(id = 11, weekStart = weekStart, day = MONDAY, order = 0)
                    .copy(timeSlot = MORNING)
            val mondayWorkoutB =
                workout(id = 12, weekStart = weekStart, day = MONDAY, order = 1)
                    .copy(timeSlot = MORNING)

            viewModel.onWeekChanged(selectedDate)
            workoutsFlow.value = listOf(mondayWorkoutA, mondayWorkoutB)
            runCurrent()
            clearMocks(userActionLogger)

            viewModel.moveWorkout(mondayWorkoutB.id, MONDAY, AFTERNOON, 0)
            advanceUntilIdle()

            val metadataSlot = slot<Map<String, String>>()
            coVerify(exactly = 1) {
                userActionLogger.log(
                    actionType = MOVE_WORKOUT_BETWEEN_DAYS,
                    entityType = any(),
                    entityId = mondayWorkoutB.id,
                    metadata = capture(metadataSlot),
                    timestamp = any(),
                )
            }
            assertEquals(MORNING.name, metadataSlot.captured[OLD_TIME_SLOT])
            assertEquals(AFTERNOON.name, metadataSlot.captured[NEW_TIME_SLOT])

            collectJob.cancel()
        }

    @Test
    fun moveWorkout_sameDaySameSlot_logsReorderAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.observeWorkoutsForWeekStarts(any()) } returns workoutsFlow

            val viewModel = createViewModel(repository, userActionLogger)
            val collectJob = backgroundScope.launch { viewModel.state.collect() }
            val selectedDate = LocalDate.of(2026, 3, 4)
            val weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(MONDAY))
            val mondayWorkoutA =
                workout(id = 11, weekStart = weekStart, day = MONDAY, order = 0)
                    .copy(timeSlot = MORNING)
            val mondayWorkoutB =
                workout(id = 12, weekStart = weekStart, day = MONDAY, order = 1)
                    .copy(timeSlot = MORNING)

            viewModel.onWeekChanged(selectedDate)
            workoutsFlow.value = listOf(mondayWorkoutA, mondayWorkoutB)
            runCurrent()
            clearMocks(userActionLogger)

            viewModel.moveWorkout(mondayWorkoutB.id, MONDAY, MORNING, 0)
            advanceUntilIdle()

            coVerify(exactly = 1) {
                userActionLogger.log(
                    actionType = REORDER_WORKOUT,
                    entityType = any(),
                    entityId = mondayWorkoutB.id,
                    metadata = any(),
                    timestamp = any(),
                )
            }

            collectJob.cancel()
        }

    @Test
    fun updateAndDelete_delegateToRepository() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.observeWorkoutsForWeekStarts(any()) } returns workoutsFlow

            val viewModel = createViewModel(repository, userActionLogger)
            val collectJob = backgroundScope.launch { viewModel.state.collect() }
            val selectedDate = LocalDate.of(2026, 4, 7)
            val weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(MONDAY))

            viewModel.onWeekChanged(selectedDate)
            workoutsFlow.value =
                listOf(
                    workout(id = 42, weekStart = weekStart, day = null, order = 0),
                    workout(id = 43, weekStart = weekStart, day = null, order = 1),
                    workout(id = 44, weekStart = weekStart, day = null, order = 2),
                )
            runCurrent()

            viewModel.updateWorkoutCompletion(
                workout =
                    WorkoutUi(
                        id = 42,
                        dayOfWeek = null,
                        type = "Bike",
                        description = "Tempo",
                        isCompleted = false,
                        isRestDay = false,
                        categoryId = UNCATEGORIZED_ID,
                        categoryColorId = "uncategorized",
                        categoryName = "Uncategorized",
                        order = 0,
                    ),
                isCompleted = true,
            )
            viewModel.updateWorkoutDetails(
                workoutId = 43,
                type = "Bike",
                description = "Tempo",
                eventType = EventType.WORKOUT,
                categoryId = null,
            )
            viewModel.deleteWorkout(workoutId = 44)
            advanceUntilIdle()

            coVerify(exactly = 1) { repository.updateWorkoutCompletion(42, true) }
            coVerify(exactly = 1) {
                repository.updateWorkoutDetails(
                    workoutId = 43,
                    type = "Bike",
                    description = "Tempo",
                    eventType = EventType.WORKOUT,
                    categoryId = UNCATEGORIZED_ID,
                )
            }
            coVerify(exactly = 1) { repository.deleteWorkout(44) }

            collectJob.cancel()
        }

    @Test
    fun updateWorkoutCompletion_whenCompletesAllPlannedWorkouts_emitsCelebrationMessage() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.observeWorkoutsForWeekStarts(any()) } returns workoutsFlow

            val viewModel = createViewModel(repository, userActionLogger)
            val collectJob = backgroundScope.launch { viewModel.state.collect() }
            val undoStates = mutableListOf<UndoState?>()
            val undoJob = backgroundScope.launch { viewModel.undoUiState.collect(undoStates::add) }
            val selectedDate = LocalDate.of(2026, 4, 7)
            val weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(MONDAY))

            viewModel.onWeekChanged(selectedDate)
            workoutsFlow.value =
                listOf(
                    workout(id = 42, weekStart = weekStart, day = MONDAY, order = 0, isCompleted = false),
                    workout(id = 43, weekStart = weekStart, day = TUESDAY, order = 1, isCompleted = true),
                )
            advanceUntilIdle()

            val targetWorkout = viewModel.state.value.workouts.first { it.id == 42L }
            viewModel.updateWorkoutCompletion(workout = targetWorkout, isCompleted = true)
            runCurrent()

            assertEquals(
                UndoMessage.CompletedWeek,
                undoStates.lastOrNull()?.message,
            )

            undoJob.cancel()
            collectJob.cancel()
        }

    @Test
    fun undoMove_restoresPreviousPosition() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.observeWorkoutsForWeekStarts(any()) } returns workoutsFlow

            val viewModel = createViewModel(repository, userActionLogger)
            val collectJob = backgroundScope.launch { viewModel.state.collect() }
            val selectedDate = LocalDate.of(2026, 5, 12)
            val weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(MONDAY))
            val mondayWorkout =
                workout(
                    id = 21,
                    weekStart = weekStart,
                    day = MONDAY,
                    order = 0,
                )
            val movedWorkout =
                workout(
                    id = 22,
                    weekStart = weekStart,
                    day = MONDAY,
                    order = 1,
                )

            viewModel.onWeekChanged(selectedDate)
            workoutsFlow.value = listOf(mondayWorkout, movedWorkout)
            runCurrent()

            viewModel.moveWorkout(movedWorkout.id, TUESDAY, null, 0)
            runCurrent()

            viewModel.undoLastAction()
            runCurrent()

            coVerify(exactly = 1) {
                repository.updateWorkoutSchedule(
                    workoutId = movedWorkout.id,
                    weekStartDate = weekStart,
                    dayOfWeek = MONDAY,
                    timeSlot = null,
                    order = 1,
                )
            }
            coVerify(exactly = 1) {
                repository.updateWorkoutSchedule(
                    workoutId = mondayWorkout.id,
                    weekStartDate = weekStart,
                    dayOfWeek = MONDAY,
                    timeSlot = null,
                    order = 0,
                )
            }

            collectJob.cancel()
        }
}
