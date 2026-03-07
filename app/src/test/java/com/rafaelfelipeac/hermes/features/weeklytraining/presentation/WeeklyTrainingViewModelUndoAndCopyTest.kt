package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COPY_LAST_WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_COPY_LAST_WEEK
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import com.rafaelfelipeac.hermes.test.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.TUESDAY
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyTrainingViewModelUndoAndCopyTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun undoDelete_restoresWorkout() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.observeWorkoutsForWeekStarts(any()) } returns workoutsFlow

            val viewModel = createViewModel(repository, userActionLogger)
            val collectJob = backgroundScope.launch { viewModel.state.collect() }
            val selectedDate = LocalDate.of(2026, 6, 3)
            val weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(MONDAY))
            val deletedWorkout =
                workout(
                    id = 99,
                    weekStart = weekStart,
                    day = TUESDAY,
                    order = 0,
                )

            viewModel.onWeekChanged(selectedDate)
            workoutsFlow.value = listOf(deletedWorkout)
            runCurrent()

            viewModel.deleteWorkout(workoutId = deletedWorkout.id)
            runCurrent()

            viewModel.undoLastAction()
            runCurrent()

            val workoutSlot = slot<Workout>()
            coVerify(exactly = 1) { repository.insertWorkout(capture(workoutSlot)) }
            assertEquals(deletedWorkout.id, workoutSlot.captured.id)
            assertEquals(weekStart, workoutSlot.captured.weekStartDate)
            assertEquals(deletedWorkout.dayOfWeek, workoutSlot.captured.dayOfWeek)
            assertEquals(deletedWorkout.order, workoutSlot.captured.order)

            collectJob.cancel()
        }

    @Test
    fun undoCompletion_restoresPreviousState() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.observeWorkoutsForWeekStarts(any()) } returns workoutsFlow

            val viewModel = createViewModel(repository, userActionLogger)
            val collectJob = backgroundScope.launch { viewModel.state.collect() }
            val selectedDate = LocalDate.of(2026, 7, 10)
            val weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(MONDAY))
            val workout =
                workout(
                    id = 120,
                    weekStart = weekStart,
                    day = MONDAY,
                    order = 0,
                    isCompleted = false,
                )

            viewModel.onWeekChanged(selectedDate)
            workoutsFlow.value = listOf(workout)
            runCurrent()

            viewModel.updateWorkoutCompletion(
                workout =
                    WorkoutUi(
                        id = workout.id,
                        dayOfWeek = workout.dayOfWeek,
                        type = workout.type,
                        description = workout.description,
                        isCompleted = workout.isCompleted,
                        isRestDay = workout.isRestDay,
                        categoryId = workout.categoryId,
                        categoryColorId = "uncategorized",
                        categoryName = "Uncategorized",
                        order = workout.order,
                    ),
                isCompleted = true,
            )
            runCurrent()

            viewModel.undoLastAction()
            runCurrent()

            coVerify(exactly = 1) { repository.updateWorkoutCompletion(120, true) }
            coVerify(exactly = 1) { repository.updateWorkoutCompletion(120, false) }

            collectJob.cancel()
        }

    @Test
    fun copyLastWeek_replacesCurrentWeekAndResetsCompletion() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val harness = createWeeklyTrainingHarness(workoutsFlow, backgroundScope)
            val viewModel = harness.viewModel
            val repository = harness.repository
            val userActionLogger = harness.userActionLogger
            val collectJob = harness.collectJob
            val fixture = copyLastWeekFixture()

            coEvery { repository.getWorkoutsForWeekStarts(listOf(fixture.previousWeekStart)) } returns
                listOf(
                    fixture.sourceWorkout,
                    fixture.sourceRestDay,
                )

            val replacedWorkouts = emptyList<Workout>()

            coEvery {
                repository.replaceWorkoutsForDisplayWeek(
                    targetStorageWeekStarts = listOf(fixture.weekStart),
                    targetDisplayWeekStart = fixture.weekStart,
                    targetUnassignedStorageWeekStart = fixture.weekStart,
                    replacementWorkouts = any(),
                )
            } returns Result.success(replacedWorkouts)

            viewModel.onWeekChanged(fixture.selectedDate)
            runCurrent()
            viewModel.copyLastWeek()
            runCurrent()

            val replacements = slot<List<Workout>>()

            coVerify(exactly = 1) {
                repository.replaceWorkoutsForDisplayWeek(
                    targetStorageWeekStarts = listOf(fixture.weekStart),
                    targetDisplayWeekStart = fixture.weekStart,
                    targetUnassignedStorageWeekStart = fixture.weekStart,
                    replacementWorkouts = capture(replacements),
                )
            }
            assertEquals(
                setOf(
                    fixture.sourceWorkout.copy(id = 0L, weekStartDate = fixture.weekStart, isCompleted = false),
                    fixture.sourceRestDay.copy(id = 0L, weekStartDate = fixture.weekStart, isCompleted = false),
                ),
                replacements.captured.toSet(),
            )
            coVerify(exactly = 1) {
                userActionLogger.log(
                    actionType = COPY_LAST_WEEK,
                    entityType = WEEK,
                    entityId = null,
                    metadata = any(),
                    timestamp = any(),
                )
            }

            collectJob.cancel()
        }

    @Test
    fun copyLastWeek_undoRestoresPreviousWeekState() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val harness = createWeeklyTrainingHarness(workoutsFlow, backgroundScope)
            val viewModel = harness.viewModel
            val repository = harness.repository
            val userActionLogger = harness.userActionLogger
            val collectJob = harness.collectJob
            val fixture = copyLastWeekUndoFixture()

            coEvery { repository.getWorkoutsForWeekStarts(listOf(fixture.previousWeekStart)) } returns
                listOf(
                    fixture.sourceWorkout,
                )
            coEvery {
                repository.replaceWorkoutsForDisplayWeek(
                    targetStorageWeekStarts = listOf(fixture.weekStart),
                    targetDisplayWeekStart = fixture.weekStart,
                    targetUnassignedStorageWeekStart = fixture.weekStart,
                    replacementWorkouts = any(),
                )
            } returns
                Result.success(
                    listOf(
                        fixture.previousTargetWorkout,
                        fixture.previousTargetRestDay,
                    ),
                )
            coEvery { repository.getWorkoutsForWeekStarts(listOf(fixture.weekStart)) } returns
                listOf(
                    fixture.sourceWorkout.copy(id = 999, weekStartDate = fixture.weekStart),
                )

            viewModel.onWeekChanged(fixture.selectedDate)
            runCurrent()
            viewModel.copyLastWeek()
            runCurrent()
            viewModel.undoLastAction()
            runCurrent()

            val restoredWorkouts = mutableListOf<Workout>()
            coVerify(exactly = 1) { repository.deleteWorkout(any()) }
            coVerify(exactly = 2) { repository.insertWorkout(capture(restoredWorkouts)) }
            coVerify(exactly = 1) {
                userActionLogger.log(
                    actionType = UNDO_COPY_LAST_WEEK,
                    entityType = WEEK,
                    entityId = fixture.weekStart.toEpochDay(),
                    metadata = any(),
                    timestamp = any(),
                )
            }
            assertEquals(
                setOf(fixture.previousTargetWorkout, fixture.previousTargetRestDay),
                restoredWorkouts.toSet(),
            )

            collectJob.cancel()
        }

    @Test
    fun copyLastWeek_withEmptyPreviousWeek_emitsMessage() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.observeWorkoutsForWeekStarts(any()) } returns workoutsFlow

            val viewModel = createViewModel(repository, userActionLogger)
            val collectJob = backgroundScope.launch { viewModel.state.collect() }
            val messages = mutableListOf<WeeklyTrainingMessage>()
            val messageJob = backgroundScope.launch { viewModel.messages.collect(messages::add) }
            val selectedDate = LocalDate.of(2026, 10, 1)
            val weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(MONDAY))
            val previousWeekStart = weekStart.minusWeeks(1)

            coEvery { repository.getWorkoutsForWeekStarts(listOf(previousWeekStart)) } returns emptyList()

            viewModel.onWeekChanged(selectedDate)
            runCurrent()
            viewModel.copyLastWeek()
            runCurrent()

            assertEquals(
                listOf(WeeklyTrainingMessage.NothingToCopyFromLastWeek),
                messages,
            )
            coVerify(exactly = 0) { repository.replaceWorkoutsForDisplayWeek(any(), any(), any(), any()) }
            assertEquals(null, viewModel.undoUiState.value)

            messageJob.cancel()
            collectJob.cancel()
        }
}
