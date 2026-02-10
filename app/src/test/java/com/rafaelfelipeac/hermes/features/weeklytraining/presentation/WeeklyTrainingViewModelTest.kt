package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import com.rafaelfelipeac.hermes.test.MainDispatcherRule
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
import java.time.DayOfWeek
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.TUESDAY
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyTrainingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun onWeekChanged_updatesSelectedDateAndWeekStart() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.observeWorkoutsForWeek(any()) } returns workoutsFlow

            val viewModel = WeeklyTrainingViewModel(repository, userActionLogger)
            val collectJob = backgroundScope.launch { viewModel.state.collect() }
            val selectedDate = LocalDate.of(2026, 1, 15)

            viewModel.onWeekChanged(selectedDate)
            advanceUntilIdle()

            val expectedWeekStart = selectedDate.with(TemporalAdjusters.previousOrSame(MONDAY))

            assertEquals(selectedDate, viewModel.state.value.selectedDate)
            assertEquals(expectedWeekStart, viewModel.state.value.weekStartDate)

            collectJob.cancel()
        }

    @Test
    fun addWorkout_usesNextOrderForUnscheduled() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.observeWorkoutsForWeek(any()) } returns workoutsFlow

            val viewModel = WeeklyTrainingViewModel(repository, userActionLogger)
            val collectJob = backgroundScope.launch { viewModel.state.collect() }
            val selectedDate = LocalDate.of(2026, 1, 15)
            val weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(MONDAY))

            viewModel.onWeekChanged(selectedDate)
            workoutsFlow.value =
                listOf(
                    workout(id = 1, weekStart = weekStart, day = null, order = 0),
                    workout(id = 2, weekStart = weekStart, day = null, order = 1),
                    workout(id = 3, weekStart = weekStart, day = MONDAY, order = 0),
                )
            advanceUntilIdle()

            viewModel.addWorkout(type = "Run", description = "Easy")
            advanceUntilIdle()

            val weekStartSlot = slot<LocalDate>()
            val daySlot = slot<DayOfWeek?>()
            val orderSlot = slot<Int>()

            coVerify(exactly = 1) {
                repository.addWorkout(
                    weekStartDate = capture(weekStartSlot),
                    dayOfWeek = captureNullable(daySlot),
                    type = "Run",
                    description = "Easy",
                    order = capture(orderSlot),
                )
            }
            assertEquals(weekStart, weekStartSlot.captured)
            assertEquals(null, daySlot.captured)
            assertEquals(2, orderSlot.captured)

            collectJob.cancel()
        }

    @Test
    fun addRestDay_usesNextOrderForUnscheduled() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.observeWorkoutsForWeek(any()) } returns workoutsFlow

            val viewModel = WeeklyTrainingViewModel(repository, userActionLogger)
            val collectJob = backgroundScope.launch { viewModel.state.collect() }
            val selectedDate = LocalDate.of(2026, 2, 2)
            val weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(MONDAY))

            viewModel.onWeekChanged(selectedDate)
            workoutsFlow.value =
                listOf(
                    workout(id = 4, weekStart = weekStart, day = null, order = 0),
                )
            advanceUntilIdle()

            viewModel.addRestDay()
            advanceUntilIdle()

            val weekStartSlot = slot<LocalDate>()
            val daySlot = slot<DayOfWeek?>()
            val orderSlot = slot<Int>()

            coVerify(exactly = 1) {
                repository.addRestDay(
                    weekStartDate = capture(weekStartSlot),
                    dayOfWeek = captureNullable(daySlot),
                    order = capture(orderSlot),
                )
            }
            assertEquals(weekStart, weekStartSlot.captured)
            assertEquals(null, daySlot.captured)
            assertEquals(1, orderSlot.captured)

            collectJob.cancel()
        }

    @Test
    fun moveWorkout_normalizesOrdersForSourceDay() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.observeWorkoutsForWeek(any()) } returns workoutsFlow

            val viewModel = WeeklyTrainingViewModel(repository, userActionLogger)
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

            viewModel.moveWorkout(restDay.id, TUESDAY, 0)
            advanceUntilIdle()

            coVerify(exactly = 1) {
                repository.updateWorkoutDayAndOrder(
                    workoutId = mondayWorkout.id,
                    dayOfWeek = MONDAY,
                    order = 0,
                )
            }
            coVerify(exactly = 1) {
                repository.updateWorkoutDayAndOrder(
                    workoutId = restDay.id,
                    dayOfWeek = TUESDAY,
                    order = 0,
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

            every { repository.observeWorkoutsForWeek(any()) } returns workoutsFlow

            val viewModel = WeeklyTrainingViewModel(repository, userActionLogger)
            val collectJob = backgroundScope.launch { viewModel.state.collect() }

            viewModel.updateWorkoutCompletion(
                workout =
                    WorkoutUi(
                        id = 42,
                        dayOfWeek = null,
                        type = "Bike",
                        description = "Tempo",
                        isCompleted = false,
                        isRestDay = false,
                        order = 0,
                    ),
                isCompleted = true,
            )
            viewModel.updateWorkoutDetails(
                workoutId = 43,
                type = "Bike",
                description = "Tempo",
                isRestDay = false,
            )
            viewModel.deleteWorkout(workoutId = 44)
            advanceUntilIdle()

            coVerify(exactly = 1) { repository.updateWorkoutCompletion(42, true) }
            coVerify(exactly = 1) {
                repository.updateWorkoutDetails(
                    workoutId = 43,
                    type = "Bike",
                    description = "Tempo",
                    isRestDay = false,
                )
            }
            coVerify(exactly = 1) { repository.deleteWorkout(44) }

            collectJob.cancel()
        }

    @Test
    fun undoMove_restoresPreviousPosition() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.observeWorkoutsForWeek(any()) } returns workoutsFlow

            val viewModel = WeeklyTrainingViewModel(repository, userActionLogger)
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

            viewModel.moveWorkout(movedWorkout.id, TUESDAY, 0)
            runCurrent()

            viewModel.undoLastAction()
            runCurrent()

            coVerify(exactly = 1) {
                repository.updateWorkoutDayAndOrder(
                    workoutId = movedWorkout.id,
                    dayOfWeek = MONDAY,
                    order = 1,
                )
            }
            coVerify(exactly = 1) {
                repository.updateWorkoutDayAndOrder(
                    workoutId = mondayWorkout.id,
                    dayOfWeek = MONDAY,
                    order = 0,
                )
            }

            collectJob.cancel()
        }

    @Test
    fun undoDelete_restoresWorkout() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.observeWorkoutsForWeek(any()) } returns workoutsFlow

            val viewModel = WeeklyTrainingViewModel(repository, userActionLogger)
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

            every { repository.observeWorkoutsForWeek(any()) } returns workoutsFlow

            val viewModel = WeeklyTrainingViewModel(repository, userActionLogger)
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
}

private fun workout(
    id: Long,
    weekStart: LocalDate,
    day: DayOfWeek?,
    order: Int,
    isCompleted: Boolean = false,
    isRestDay: Boolean = false,
): Workout {
    return Workout(
        id = id,
        weekStartDate = weekStart,
        dayOfWeek = day,
        type = if (isRestDay) "Rest" else "Run",
        description = EMPTY,
        isCompleted = isCompleted,
        isRestDay = isRestDay,
        order = order,
    )
}
