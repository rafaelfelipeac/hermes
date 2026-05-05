package com.rafaelfelipeac.hermes.features.progress.presentation

import app.cash.turbine.test
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionRepository
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_RUN
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.AddWorkoutRequest
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import com.rafaelfelipeac.hermes.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters.previousOrSame

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun state_summarizesCurrentWeekAndRecentActivity() =
        runTest(mainDispatcherRule.testDispatcher) {
            val currentWeek = LocalDate.now().with(previousOrSame(DayOfWeek.MONDAY))
            val workouts =
                listOf(
                    workout(1L, currentWeek.minusWeeks(1), DayOfWeek.MONDAY, isCompleted = true, categoryId = 2L),
                    workout(2L, currentWeek, DayOfWeek.TUESDAY, isCompleted = true, categoryId = 2L),
                    workout(3L, currentWeek, DayOfWeek.WEDNESDAY, isCompleted = false, categoryId = 2L),
                )
            val actions =
                listOf(
                    action(
                        id = 1L,
                        actionType = UserActionType.CREATE_WORKOUT,
                        entityType = UserActionEntityType.WORKOUT,
                        timestamp = 1_000L,
                    ),
                    action(
                        id = 2L,
                        actionType = UserActionType.UPDATE_WORKOUT,
                        entityType = UserActionEntityType.WORKOUT,
                        timestamp = 2_000L,
                    ),
                )
            val viewModel = createViewModel(workouts = workouts, actions = actions)

            viewModel.state.test {
                awaitItem()
                val state = awaitItem()

                assertEquals(2, state.thisWeek.plannedWorkouts)
                assertEquals(1, state.thisWeek.completedWorkouts)
                assertEquals(2, state.recentActivities.size)
                assertEquals(2L, state.recentActivities.first().id)
                assertTrue(state.summaryCards.any { it.kind == ProgressSummaryCardKind.THIS_WEEK })
                assertTrue(state.summaryCards.any { it.kind == ProgressSummaryCardKind.CONSISTENCY })
                assertNull(state.emptyReason)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun state_exposesEmptyReasonWhenNoWorkoutHistoryExists() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(workouts = emptyList(), actions = emptyList())

            viewModel.state.test {
                awaitItem()
                val state = awaitItem()

                assertEquals(ProgressEmptyReason.NO_WEEKLY_HISTORY, state.emptyReason)
                assertTrue(state.summaryCards.size >= 2)
                assertTrue(state.recentActivities.isEmpty())

                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun createViewModel(
        workouts: List<Workout>,
        actions: List<UserActionRecord>,
    ): ProgressViewModel {
        return ProgressViewModel(
            weeklyTrainingRepository = FakeWeeklyTrainingRepository(workouts),
            categoryRepository = FakeCategoryRepository(),
            userActionRepository = FakeUserActionRepository(actions),
            settingsRepository = FakeSettingsRepository(),
            stringProvider = FakeStringProvider(),
        )
    }

    private fun workout(
        id: Long,
        weekStart: LocalDate,
        dayOfWeek: DayOfWeek,
        isCompleted: Boolean,
        categoryId: Long?,
    ): Workout {
        return Workout(
            id = id,
            weekStartDate = weekStart,
            dayOfWeek = dayOfWeek,
            type = "Run",
            description = "",
            isCompleted = isCompleted,
            isRestDay = false,
            eventType = EventType.WORKOUT,
            categoryId = categoryId,
            order = id.toInt(),
        )
    }

    private fun action(
        id: Long,
        actionType: UserActionType,
        entityType: UserActionEntityType,
        timestamp: Long,
    ): UserActionRecord {
        return UserActionRecord(
            id = id,
            actionType = actionType.name,
            entityType = entityType.name,
            entityId = null,
            metadata = null,
            timestamp = timestamp,
        )
    }

    private class FakeWeeklyTrainingRepository(
        private val workouts: List<Workout>,
    ) : WeeklyTrainingRepository {
        override fun observeWorkoutsForWeek(weekStartDate: LocalDate): Flow<List<Workout>> = flowOf(emptyList())
        override fun observeAllWorkouts(): Flow<List<Workout>> = flowOf(workouts)
        override fun observeWorkoutsByEventType(eventType: EventType): Flow<List<Workout>> = flowOf(emptyList())
        override fun observeWorkoutsForWeekStarts(weekStartDates: List<LocalDate>): Flow<List<Workout>> = flowOf(emptyList())
        override suspend fun getWorkoutsForWeek(weekStartDate: LocalDate): List<Workout> = emptyList()
        override suspend fun getWorkoutsForWeekStarts(weekStartDates: List<LocalDate>): List<Workout> = emptyList()
        override suspend fun addWorkout(request: AddWorkoutRequest): Long = error("Not used")
        override suspend fun addEvent(
            weekStartDate: LocalDate,
            dayOfWeek: DayOfWeek?,
            eventType: EventType,
            order: Int,
        ): Long = error("Not used")
        override suspend fun insertWorkout(workout: Workout): Long = error("Not used")
        override suspend fun updateWorkoutDayAndOrder(workoutId: Long, dayOfWeek: DayOfWeek?, timeSlot: TimeSlot?, order: Int) = Unit
        override suspend fun updateWorkoutSchedule(workoutId: Long, weekStartDate: LocalDate, dayOfWeek: DayOfWeek?, timeSlot: TimeSlot?, order: Int) = Unit
        override suspend fun updateWorkoutCompletion(workoutId: Long, isCompleted: Boolean) = Unit
        override suspend fun updateWorkoutDetails(workoutId: Long, type: String, description: String, eventType: EventType, categoryId: Long?) = Unit
        override suspend fun assignNullCategoryTo(uncategorizedId: Long) = Unit
        override suspend fun reassignCategory(deletedCategoryId: Long, uncategorizedId: Long) = Unit
        override suspend fun deleteWorkout(workoutId: Long) = Unit
        override suspend fun deleteWorkoutsForWeek(weekStartDate: LocalDate) = Unit
        override suspend fun replaceWorkoutsForWeek(weekStartDate: LocalDate, sourceWorkouts: List<Workout>) = Unit
    }

    private class FakeCategoryRepository : CategoryRepository {
        override fun observeCategories(): Flow<List<Category>> =
            flowOf(
                listOf(
                    Category(UNCATEGORIZED_ID, "Uncategorized", "uncategorized", 0, isHidden = false, isSystem = true),
                    Category(2L, "Run", COLOR_RUN, 1, isHidden = false, isSystem = true),
                ),
            )
        override suspend fun getCategories(): List<Category> = emptyList()
        override suspend fun getCategory(id: Long): Category? = null
        override suspend fun getCount(): Int = 0
        override suspend fun insertCategory(category: Category): Long = error("Not used")
        override suspend fun insertCategories(categories: List<Category>): List<Long> = error("Not used")
        override suspend fun updateCategory(category: Category) = Unit
        override suspend fun updateCategoryName(id: Long, name: String) = Unit
        override suspend fun updateCategoryColor(id: Long, colorId: String) = Unit
        override suspend fun updateCategoryVisibility(id: Long, isHidden: Boolean) = Unit
        override suspend fun updateCategorySortOrder(id: Long, sortOrder: Int) = Unit
        override suspend fun deleteCategory(id: Long) = Unit
    }

    private class FakeUserActionRepository(
        private val actions: List<UserActionRecord>,
    ) : UserActionRepository {
        override fun observeActions(): Flow<List<UserActionRecord>> = flowOf(actions)
    }

    private class FakeSettingsRepository : SettingsRepository {
        override val themeMode = MutableStateFlow(ThemeMode.SYSTEM)
        override val language = MutableStateFlow(AppLanguage.SYSTEM)
        override val slotModePolicy = MutableStateFlow(SlotModePolicy.AUTO_WHEN_MULTIPLE)
        override val weekStartDay = MutableStateFlow(WeekStartDay.MONDAY)
        override val lastBackupExportedAt = MutableStateFlow<String?>(null)
        override val lastBackupImportedAt = MutableStateFlow<String?>(null)
        override val backupFolderUri = MutableStateFlow<String?>(null)
        override val lastSeenTrophyCelebrationToken = MutableStateFlow<String?>(null)
        override fun initialThemeMode(): ThemeMode = themeMode.value
        override fun initialLanguage(): AppLanguage = language.value
        override fun initialSlotModePolicy(): SlotModePolicy = slotModePolicy.value
        override fun initialWeekStartDay(): WeekStartDay = weekStartDay.value
        override suspend fun setThemeMode(mode: ThemeMode) = Unit
        override suspend fun setLanguage(language: AppLanguage) = Unit
        override suspend fun setSlotModePolicy(policy: SlotModePolicy) = Unit
        override suspend fun setWeekStartDay(weekStartDay: WeekStartDay) = Unit
        override suspend fun setLastBackupExportedAt(value: String) = Unit
        override suspend fun setLastBackupImportedAt(value: String) = Unit
        override suspend fun setBackupFolderUri(value: String?) = Unit
        override suspend fun setLastSeenTrophyCelebrationToken(value: String?) = Unit
    }

    private class FakeStringProvider : StringProvider {
        override fun get(
            id: Int,
            vararg args: Any,
        ): String = id.toString()

        override fun getForLanguage(
            languageTag: String?,
            id: Int,
            vararg args: Any,
        ): String = id.toString()
    }
}
