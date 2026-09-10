package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WeeklyTrainingCommandRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WeeklyTrainingCommandResult
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.TUESDAY
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

fun workout(
    id: Long,
    weekStart: LocalDate,
    day: DayOfWeek?,
    order: Int,
    isCompleted: Boolean = false,
    isRestDay: Boolean = false,
    categoryId: Long? = UNCATEGORIZED_ID,
    eventType: EventType? = null,
): Workout {
    return Workout(
        id = id,
        weekStartDate = weekStart,
        dayOfWeek = day,
        type = if (isRestDay) "Rest" else "Run",
        description = EMPTY,
        isCompleted = isCompleted,
        isRestDay = isRestDay,
        categoryId = if (isRestDay) null else categoryId,
        order = order,
        eventType =
            eventType ?: if (isRestDay) {
                EventType.REST
            } else {
                EventType.WORKOUT
            },
    )
}

data class WeeklyTrainingHarness(
    val viewModel: WeeklyTrainingViewModel,
    val repository: WeeklyTrainingRepository,
    val userActionLogger: UserActionLogger,
    val categoryRepository: CategoryRepository,
    val settingsRepository: SettingsRepository,
    val commandRepository: WeeklyTrainingCommandRepository,
    val collectJob: Job,
)

fun createWeeklyTrainingHarness(
    workoutsFlow: MutableStateFlow<List<Workout>>,
    backgroundScope: CoroutineScope,
    weekStartDay: WeekStartDay = WeekStartDay.MONDAY,
): WeeklyTrainingHarness {
    val repository = mockk<WeeklyTrainingRepository>(relaxed = true)
    val userActionLogger = mockk<UserActionLogger>(relaxed = true)
    val categoriesFlow = MutableStateFlow(listOf(defaultCategory()))
    val categoryRepository = mockk<CategoryRepository>(relaxed = true)
    val categorySeeder = mockk<CategorySeeder>(relaxed = true)
    val settingsRepository = mockk<SettingsRepository>()
    val commandRepository = defaultWeeklyTrainingCommandRepository()

    every { repository.observeWorkoutsForWeekStarts(any()) } returns workoutsFlow
    every { categoryRepository.observeCategories() } returns categoriesFlow
    every { settingsRepository.slotModePolicy } returns MutableStateFlow(SlotModePolicy.AUTO_WHEN_MULTIPLE)
    every { settingsRepository.weekStartDay } returns MutableStateFlow(weekStartDay)
    every { settingsRepository.initialWeekStartDay() } returns weekStartDay
    every { settingsRepository.initialSlotModePolicy() } returns SlotModePolicy.AUTO_WHEN_MULTIPLE

    val viewModel =
        WeeklyTrainingViewModel(
            repository,
            userActionLogger,
            categoryRepository,
            categorySeeder,
            settingsRepository,
            commandRepository,
        )
    val collectJob = backgroundScope.launch { viewModel.state.collect() }

    return WeeklyTrainingHarness(
        viewModel = viewModel,
        repository = repository,
        userActionLogger = userActionLogger,
        categoryRepository = categoryRepository,
        settingsRepository = settingsRepository,
        commandRepository = commandRepository,
        collectJob = collectJob,
    )
}

fun defaultCategory(): Category {
    return Category(
        id = UNCATEGORIZED_ID,
        name = "Uncategorized",
        colorId = "uncategorized",
        sortOrder = 0,
        isHidden = false,
        isSystem = true,
    )
}

fun createViewModel(
    repository: WeeklyTrainingRepository,
    userActionLogger: UserActionLogger,
    categoriesFlow: MutableStateFlow<List<Category>> = MutableStateFlow(listOf(defaultCategory())),
    weekStartDay: WeekStartDay = WeekStartDay.MONDAY,
    commandRepository: WeeklyTrainingCommandRepository = defaultWeeklyTrainingCommandRepository(),
): WeeklyTrainingViewModel {
    val categoryRepository = mockk<CategoryRepository>(relaxed = true)
    val categorySeeder = mockk<CategorySeeder>(relaxed = true)
    val settingsRepository = mockk<SettingsRepository>()

    every { categoryRepository.observeCategories() } returns categoriesFlow
    every { settingsRepository.slotModePolicy } returns MutableStateFlow(SlotModePolicy.AUTO_WHEN_MULTIPLE)
    every { settingsRepository.weekStartDay } returns MutableStateFlow(weekStartDay)
    every { settingsRepository.initialWeekStartDay() } returns weekStartDay
    every { settingsRepository.initialSlotModePolicy() } returns SlotModePolicy.AUTO_WHEN_MULTIPLE

    return WeeklyTrainingViewModel(
        repository,
        userActionLogger,
        categoryRepository,
        categorySeeder,
        settingsRepository,
        commandRepository,
    )
}

fun defaultWeeklyTrainingCommandRepository(): WeeklyTrainingCommandRepository {
    val commandRepository = mockk<WeeklyTrainingCommandRepository>()
    coEvery {
        commandRepository.copyLastWeek(any())
    } returns WeeklyTrainingCommandResult.WeekCopied(emptyList())
    coEvery {
        commandRepository.undoCopyLastWeek(any())
    } returns WeeklyTrainingCommandResult.UndoApplied
    coEvery {
        commandRepository.updateSchedule(any())
    } returns WeeklyTrainingCommandResult.ScheduleChanged
    coEvery {
        commandRepository.undoSchedule(any())
    } returns WeeklyTrainingCommandResult.UndoApplied
    coEvery {
        commandRepository.updateDetails(any())
    } returns WeeklyTrainingCommandResult.DetailsChanged
    coEvery {
        commandRepository.updateCompletion(any())
    } returns
        WeeklyTrainingCommandResult.CompletionChanged(
            previousCompleted = false,
            eventType = EventType.WORKOUT,
        )
    coEvery {
        commandRepository.undoCompletion(any())
    } returns WeeklyTrainingCommandResult.UndoApplied
    coEvery {
        commandRepository.deleteWorkout(any())
    } returns WeeklyTrainingCommandResult.WorkoutDeleted
    coEvery {
        commandRepository.undoDelete(any())
    } returns WeeklyTrainingCommandResult.UndoApplied
    return commandRepository
}

data class CopyLastWeekFixture(
    val selectedDate: LocalDate,
    val weekStart: LocalDate,
    val previousWeekStart: LocalDate,
    val sourceWorkout: Workout,
    val sourceRestDay: Workout,
)

fun copyLastWeekFixture(): CopyLastWeekFixture {
    val selectedDate = LocalDate.of(2026, 8, 20)
    val weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(MONDAY))
    val previousWeekStart = weekStart.minusWeeks(1)
    val sourceWorkout =
        workout(
            id = 300,
            weekStart = previousWeekStart,
            day = MONDAY,
            order = 0,
            isCompleted = true,
        )
    val sourceRestDay =
        workout(
            id = 301,
            weekStart = previousWeekStart,
            day = TUESDAY,
            order = 1,
            isRestDay = true,
        )

    return CopyLastWeekFixture(
        selectedDate = selectedDate,
        weekStart = weekStart,
        previousWeekStart = previousWeekStart,
        sourceWorkout = sourceWorkout,
        sourceRestDay = sourceRestDay,
    )
}

data class CopyLastWeekUndoFixture(
    val selectedDate: LocalDate,
    val weekStart: LocalDate,
    val previousWeekStart: LocalDate,
    val sourceWorkout: Workout,
    val previousTargetWorkout: Workout,
    val previousTargetRestDay: Workout,
)

fun copyLastWeekUndoFixture(): CopyLastWeekUndoFixture {
    val selectedDate = LocalDate.of(2026, 9, 7)
    val weekStart = selectedDate.with(TemporalAdjusters.previousOrSame(MONDAY))
    val previousWeekStart = weekStart.minusWeeks(1)
    val sourceWorkout =
        workout(
            id = 400,
            weekStart = previousWeekStart,
            day = MONDAY,
            order = 0,
        )
    val previousTargetWorkout =
        workout(
            id = 401,
            weekStart = weekStart,
            day = TUESDAY,
            order = 0,
            isCompleted = true,
        )
    val previousTargetRestDay =
        workout(
            id = 402,
            weekStart = weekStart,
            day = null,
            order = 0,
            isRestDay = true,
        )

    return CopyLastWeekUndoFixture(
        selectedDate = selectedDate,
        weekStart = weekStart,
        previousWeekStart = previousWeekStart,
        sourceWorkout = sourceWorkout,
        previousTargetWorkout = previousTargetWorkout,
        previousTargetRestDay = previousTargetRestDay,
    )
}
