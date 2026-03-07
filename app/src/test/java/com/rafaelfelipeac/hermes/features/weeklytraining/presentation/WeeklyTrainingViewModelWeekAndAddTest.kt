package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.AddWorkoutRequest
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
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
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyTrainingViewModelWeekAndAddTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun onWeekChanged_updatesSelectedDateAndWeekStart() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.observeWorkoutsForWeekStarts(any()) } returns workoutsFlow

            val viewModel = createViewModel(repository, userActionLogger)
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
    fun onWeekChanged_withWednesdayStart_updatesWeekStartUsingConfiguredBoundary() =
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

            viewModel.onWeekChanged(selectedDate)
            advanceUntilIdle()

            val expectedWeekStart = selectedDate.with(TemporalAdjusters.previousOrSame(WEDNESDAY))

            assertEquals(selectedDate, viewModel.state.value.selectedDate)
            assertEquals(expectedWeekStart, viewModel.state.value.weekStartDate)

            collectJob.cancel()
        }

    @Test
    fun state_withCrossBoundaryWeek_doesNotMixUnassignedFromOtherStorageWeek() =
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
            val firstStorageWeekStart = LocalDate.of(2026, 2, 23)
            val secondStorageWeekStart = LocalDate.of(2026, 3, 2)

            viewModel.onWeekChanged(selectedDate)
            workoutsFlow.value =
                listOf(
                    workout(id = 1, weekStart = firstStorageWeekStart, day = null, order = 0),
                    workout(id = 2, weekStart = secondStorageWeekStart, day = null, order = 0),
                    workout(id = 3, weekStart = secondStorageWeekStart, day = MONDAY, order = 0),
                )
            advanceUntilIdle()

            val workoutIds = viewModel.state.value.workouts.map { it.id }.toSet()

            assertEquals(setOf(1L, 3L), workoutIds)

            collectJob.cancel()
        }

    @Test
    fun addWorkout_usesNextOrderForUnscheduled() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.observeWorkoutsForWeekStarts(any()) } returns workoutsFlow

            val viewModel = createViewModel(repository, userActionLogger)
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

            viewModel.addWorkout(type = "Run", description = "Easy", categoryId = null)
            advanceUntilIdle()

            val requestSlot = slot<AddWorkoutRequest>()

            coVerify(exactly = 1) {
                repository.addWorkout(capture(requestSlot))
            }
            assertEquals(weekStart, requestSlot.captured.weekStartDate)
            assertEquals(null, requestSlot.captured.dayOfWeek)
            assertEquals("Run", requestSlot.captured.type)
            assertEquals("Easy", requestSlot.captured.description)
            assertEquals(UNCATEGORIZED_ID, requestSlot.captured.categoryId)
            assertEquals(2, requestSlot.captured.order)

            collectJob.cancel()
        }

    @Test
    fun addRest_usesNextOrderForUnscheduled() =
        runTest(mainDispatcherRule.testDispatcher) {
            val workoutsFlow = MutableStateFlow(emptyList<Workout>())
            val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val userActionLogger = mockk<UserActionLogger>(relaxed = true)

            every { repository.observeWorkoutsForWeekStarts(any()) } returns workoutsFlow

            val viewModel = createViewModel(repository, userActionLogger)
            val collectJob = backgroundScope.launch { viewModel.state.collect() }
            val selectedDate = LocalDate.of(2026, 2, 2)
            val weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(MONDAY))

            viewModel.onWeekChanged(selectedDate)
            workoutsFlow.value =
                listOf(
                    workout(id = 4, weekStart = weekStart, day = null, order = 0),
                )
            advanceUntilIdle()

            viewModel.addRest()
            advanceUntilIdle()

            val weekStartSlot = slot<LocalDate>()
            val daySlot = slot<DayOfWeek?>()
            val eventTypeSlot = slot<EventType>()
            val orderSlot = slot<Int>()

            coVerify(exactly = 1) {
                repository.addEvent(
                    weekStartDate = capture(weekStartSlot),
                    dayOfWeek = captureNullable(daySlot),
                    eventType = capture(eventTypeSlot),
                    order = capture(orderSlot),
                )
            }
            assertEquals(weekStart, weekStartSlot.captured)
            assertEquals(null, daySlot.captured)
            assertEquals(EventType.REST, eventTypeSlot.captured)
            assertEquals(1, orderSlot.captured)

            collectJob.cancel()
        }
}
