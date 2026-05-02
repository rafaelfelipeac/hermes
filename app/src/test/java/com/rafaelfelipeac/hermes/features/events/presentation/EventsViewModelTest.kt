package com.rafaelfelipeac.hermes.features.events.presentation

import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.domain.UserAction
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.canonicalStorageWeekStart
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.AddWorkoutRequest
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.RACE_EVENT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import com.rafaelfelipeac.hermes.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class EventsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_seedsDefaultCategoriesBeforeFirstEventCreation() =
        runTest(mainDispatcherRule.testDispatcher) {
            val categoryRepository = FakeCategoryRepository(initialCategories = emptyList())
            val viewModel = createViewModel(categoryRepository = categoryRepository)
            val collectJob = backgroundScope.launch { viewModel.state.collect {} }

            advanceUntilIdle()

            assertEquals(7, viewModel.state.value.categories.size)
            assertEquals(UNCATEGORIZED_ID, viewModel.state.value.categories.first().id)

            collectJob.cancel()
        }

    @Test
    fun state_observesOnlyRaceEventsWithCategories() =
        runTest(mainDispatcherRule.testDispatcher) {
            val eventDate = LocalDate.now().plusDays(14)
            val raceEvent =
                workout(
                    id = 1L,
                    eventDate = eventDate,
                    eventType = RACE_EVENT,
                    categoryId = CATEGORY_ID,
                    type = EVENT_TITLE,
                )
            val repository =
                FakeWeeklyTrainingRepository(
                    initialWorkouts =
                        listOf(
                            raceEvent,
                            workout(
                                id = 2L,
                                eventDate = eventDate,
                                eventType = EventType.WORKOUT,
                                type = WORKOUT_TITLE,
                            ),
                        ),
                )
            val viewModel = createViewModel(repository = repository)
            val collectJob = backgroundScope.launch { viewModel.state.collect {} }

            advanceUntilIdle()

            assertEquals(listOf(1L), viewModel.state.value.events.map { it.id })
            assertEquals(EVENT_TITLE, viewModel.state.value.events.single().type)
            assertEquals(CATEGORY_NAME, viewModel.state.value.events.single().categoryName)

            collectJob.cancel()
        }

    @Test
    fun addRaceEvent_schedulesIntoDerivedWeekAndLogsCreate() =
        runTest(mainDispatcherRule.testDispatcher) {
            val eventDate = LocalDate.now().plusDays(30)
            val repository =
                FakeWeeklyTrainingRepository(
                    initialWorkouts =
                        listOf(
                            workout(
                                id = 1L,
                                eventDate = eventDate,
                                eventType = RACE_EVENT,
                                order = 0,
                            ),
                        ),
                )
            val logger = RecordingUserActionLogger()
            val viewModel = createViewModel(repository = repository, logger = logger)
            val collectJob = backgroundScope.launch { viewModel.state.collect {} }

            advanceUntilIdle()
            viewModel.addRaceEvent(
                title = EVENT_TITLE,
                description = EVENT_DESCRIPTION,
                categoryId = CATEGORY_ID,
                eventDate = eventDate,
            )
            advanceUntilIdle()

            val inserted = repository.workouts.value.single { it.id == INSERTED_ID }
            val expectedWeekStart = canonicalStorageWeekStart(eventDate)
            assertEquals(expectedWeekStart, inserted.weekStartDate)
            assertEquals(eventDate.dayOfWeek, inserted.dayOfWeek)
            assertEquals(RACE_EVENT, inserted.eventType)
            assertEquals(1, inserted.order)
            assertEquals(CATEGORY_ID, inserted.categoryId)

            val action = logger.actions.single()
            assertEquals(UserActionType.CREATE_RACE_EVENT, action.actionType)
            assertEquals(UserActionEntityType.RACE_EVENT, action.entityType)
            assertEquals(INSERTED_ID, action.entityId)
            assertEquals(EVENT_TITLE, action.metadata?.get(NEW_TYPE))
            assertEquals("1", action.metadata?.get(NEW_ORDER))

            collectJob.cancel()
        }

    @Test
    fun updateRaceEvent_reschedulesIntoNewDateAndLogsMove() =
        runTest(mainDispatcherRule.testDispatcher) {
            val originalDate = LocalDate.now().plusDays(10)
            val newDate = LocalDate.now().plusDays(40)
            val repository =
                FakeWeeklyTrainingRepository(
                    initialWorkouts =
                        listOf(
                            workout(id = EVENT_ID, eventDate = originalDate, eventType = RACE_EVENT),
                        ),
                )
            val logger = RecordingUserActionLogger()
            val viewModel = createViewModel(repository = repository, logger = logger)
            val collectJob = backgroundScope.launch { viewModel.state.collect {} }

            advanceUntilIdle()
            viewModel.updateRaceEvent(
                eventId = EVENT_ID,
                title = UPDATED_TITLE,
                description = UPDATED_DESCRIPTION,
                categoryId = null,
                eventDate = newDate,
            )
            advanceUntilIdle()

            val updated = repository.workouts.value.single { it.id == EVENT_ID }
            val expectedWeekStart = canonicalStorageWeekStart(newDate)
            assertEquals(expectedWeekStart, updated.weekStartDate)
            assertEquals(newDate.dayOfWeek, updated.dayOfWeek)
            assertEquals(UPDATED_TITLE, updated.type)
            assertEquals(UPDATED_DESCRIPTION, updated.description)
            assertEquals(UNCATEGORIZED_ID, updated.categoryId)

            val action = logger.actions.single()
            assertEquals(UserActionType.MOVE_RACE_EVENT, action.actionType)
            assertEquals(expectedWeekStart.toString(), action.metadata?.get(NEW_WEEK_START_DATE))
            assertEquals(newDate.dayOfWeek.value.toString(), action.metadata?.get(NEW_DAY_OF_WEEK))

            collectJob.cancel()
        }

    @Test
    fun updateRaceEventCompletion_updatesStateAndLogsCompletion() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository =
                FakeWeeklyTrainingRepository(
                    initialWorkouts =
                        listOf(
                            workout(
                                id = EVENT_ID,
                                eventDate = LocalDate.now().plusDays(5),
                                eventType = RACE_EVENT,
                                isCompleted = false,
                            ),
                        ),
                )
            val logger = RecordingUserActionLogger()
            val viewModel = createViewModel(repository = repository, logger = logger)
            val collectJob = backgroundScope.launch { viewModel.state.collect {} }

            advanceUntilIdle()
            viewModel.updateRaceEventCompletion(eventId = EVENT_ID, isCompleted = true)
            advanceUntilIdle()

            assertEquals(true, repository.workouts.value.single().isCompleted)
            assertEquals(UserActionType.COMPLETE_RACE_EVENT, logger.actions.single().actionType)

            collectJob.cancel()
        }

    @Test
    fun undoRaceEventCompletion_restoresPreviousStateAndLogsUndo() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository =
                FakeWeeklyTrainingRepository(
                    initialWorkouts =
                        listOf(
                            workout(
                                id = EVENT_ID,
                                eventDate = LocalDate.now().plusDays(5),
                                eventType = RACE_EVENT,
                                isCompleted = false,
                            ),
                        ),
                )
            val logger = RecordingUserActionLogger()
            val viewModel = createViewModel(repository = repository, logger = logger)
            val collectJob = backgroundScope.launch { viewModel.state.collect {} }

            advanceUntilIdle()
            viewModel.updateRaceEventCompletion(eventId = EVENT_ID, isCompleted = true)
            runCurrent()
            viewModel.undoLastAction()
            advanceUntilIdle()

            assertEquals(false, repository.workouts.value.single().isCompleted)
            assertEquals(
                listOf(UserActionType.COMPLETE_RACE_EVENT, UserActionType.UNDO_COMPLETE_RACE_EVENT),
                logger.actions.map { it.actionType },
            )
            assertEquals(null, viewModel.undoUiState.value)

            collectJob.cancel()
        }

    @Test
    fun undoRaceEventIncomplete_restoresPreviousStateAndLogsUndo() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository =
                FakeWeeklyTrainingRepository(
                    initialWorkouts =
                        listOf(
                            workout(
                                id = EVENT_ID,
                                eventDate = LocalDate.now().plusDays(5),
                                eventType = RACE_EVENT,
                                isCompleted = true,
                            ),
                        ),
                )
            val logger = RecordingUserActionLogger()
            val viewModel = createViewModel(repository = repository, logger = logger)
            val collectJob = backgroundScope.launch { viewModel.state.collect {} }

            advanceUntilIdle()
            viewModel.updateRaceEventCompletion(eventId = EVENT_ID, isCompleted = false)
            runCurrent()
            viewModel.undoLastAction()
            advanceUntilIdle()

            assertEquals(true, repository.workouts.value.single().isCompleted)
            assertEquals(
                listOf(UserActionType.INCOMPLETE_RACE_EVENT, UserActionType.UNDO_INCOMPLETE_RACE_EVENT),
                logger.actions.map { it.actionType },
            )
            assertEquals(null, viewModel.undoUiState.value)

            collectJob.cancel()
        }

    @Test
    fun deleteRaceEvent_removesEventAndLogsDelete() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository =
                FakeWeeklyTrainingRepository(
                    initialWorkouts =
                        listOf(
                            workout(id = EVENT_ID, eventDate = LocalDate.now().plusDays(5), eventType = RACE_EVENT),
                        ),
                )
            val logger = RecordingUserActionLogger()
            val viewModel = createViewModel(repository = repository, logger = logger)
            val collectJob = backgroundScope.launch { viewModel.state.collect {} }

            advanceUntilIdle()
            viewModel.deleteRaceEvent(EVENT_ID)
            advanceUntilIdle()

            assertEquals(emptyList<Workout>(), repository.workouts.value)
            assertEquals(UserActionType.DELETE_RACE_EVENT, logger.actions.single().actionType)

            collectJob.cancel()
        }

    @Test
    fun undoDeleteRaceEvent_restoresEventAndLogsUndoDelete() =
        runTest(mainDispatcherRule.testDispatcher) {
            val deletedEvent =
                workout(
                    id = EVENT_ID,
                    eventDate = LocalDate.now().plusDays(5),
                    eventType = RACE_EVENT,
                    type = EVENT_TITLE,
                    description = EVENT_DESCRIPTION,
                )
            val repository = FakeWeeklyTrainingRepository(initialWorkouts = listOf(deletedEvent))
            val logger = RecordingUserActionLogger()
            val viewModel = createViewModel(repository = repository, logger = logger)
            val collectJob = backgroundScope.launch { viewModel.state.collect {} }

            advanceUntilIdle()
            viewModel.deleteRaceEvent(EVENT_ID)
            runCurrent()
            viewModel.undoLastAction()
            advanceUntilIdle()

            assertEquals(listOf(deletedEvent), repository.workouts.value)
            assertEquals(
                listOf(UserActionType.DELETE_RACE_EVENT, UserActionType.UNDO_DELETE_RACE_EVENT),
                logger.actions.map { it.actionType },
            )
            assertEquals(null, viewModel.undoUiState.value)

            collectJob.cancel()
        }

    private fun createViewModel(
        repository: FakeWeeklyTrainingRepository = FakeWeeklyTrainingRepository(),
        categoryRepository: FakeCategoryRepository = FakeCategoryRepository(),
        logger: RecordingUserActionLogger = RecordingUserActionLogger(),
    ): EventsViewModel {
        return EventsViewModel(
            repository = repository,
            categoryRepository = categoryRepository,
            categorySeeder = CategorySeeder(categoryRepository, FakeStringProvider()),
            userActionLogger = logger,
        )
    }

    private class FakeWeeklyTrainingRepository(
        initialWorkouts: List<Workout> = emptyList(),
    ) : WeeklyTrainingRepository {
        val workouts = MutableStateFlow(initialWorkouts)
        private var nextId = INSERTED_ID

        override fun observeWorkoutsForWeek(weekStartDate: LocalDate): Flow<List<Workout>> {
            return workouts.map { items -> items.filter { it.weekStartDate == weekStartDate } }
        }

        override fun observeAllWorkouts(): Flow<List<Workout>> = workouts

        override fun observeWorkoutsByEventType(eventType: EventType): Flow<List<Workout>> {
            return workouts.map { items -> items.filter { it.eventType == eventType } }
        }

        override fun observeWorkoutsForWeekStarts(weekStartDates: List<LocalDate>): Flow<List<Workout>> {
            return workouts.map { items -> items.filter { it.weekStartDate in weekStartDates } }
        }

        override suspend fun getWorkoutsForWeek(weekStartDate: LocalDate): List<Workout> {
            return workouts.value.filter { it.weekStartDate == weekStartDate }
        }

        override suspend fun getWorkoutsForWeekStarts(weekStartDates: List<LocalDate>): List<Workout> {
            return workouts.value.filter { it.weekStartDate in weekStartDates }
        }

        override suspend fun addWorkout(request: AddWorkoutRequest): Long = error("Not needed")

        override suspend fun addEvent(
            weekStartDate: LocalDate,
            dayOfWeek: DayOfWeek?,
            eventType: EventType,
            order: Int,
        ): Long = error("Not needed")

        override suspend fun insertWorkout(workout: Workout): Long {
            val id = if (workout.id == 0L) nextId++ else workout.id
            workouts.update { items -> items + workout.copy(id = id) }
            return id
        }

        override suspend fun updateWorkoutDayAndOrder(
            workoutId: Long,
            dayOfWeek: DayOfWeek?,
            timeSlot: TimeSlot?,
            order: Int,
        ) = error("Not needed")

        override suspend fun updateWorkoutSchedule(
            workoutId: Long,
            weekStartDate: LocalDate,
            dayOfWeek: DayOfWeek?,
            timeSlot: TimeSlot?,
            order: Int,
        ) {
            workouts.update { items ->
                items.map { workout ->
                    if (workout.id == workoutId) {
                        workout.copy(
                            weekStartDate = weekStartDate,
                            dayOfWeek = dayOfWeek,
                            timeSlot = timeSlot,
                            order = order,
                        )
                    } else {
                        workout
                    }
                }
            }
        }

        override suspend fun updateWorkoutCompletion(
            workoutId: Long,
            isCompleted: Boolean,
        ) {
            workouts.update { items ->
                items.map { workout ->
                    if (workout.id == workoutId) {
                        workout.copy(isCompleted = isCompleted)
                    } else {
                        workout
                    }
                }
            }
        }

        override suspend fun updateWorkoutDetails(
            workoutId: Long,
            type: String,
            description: String,
            eventType: EventType,
            categoryId: Long?,
        ) {
            workouts.update { items ->
                items.map { workout ->
                    if (workout.id == workoutId) {
                        workout.copy(
                            type = type,
                            description = description,
                            eventType = eventType,
                            isRestDay = eventType == EventType.REST,
                            categoryId = categoryId,
                        )
                    } else {
                        workout
                    }
                }
            }
        }

        override suspend fun assignNullCategoryTo(uncategorizedId: Long) = error("Not needed")

        override suspend fun reassignCategory(
            deletedCategoryId: Long,
            uncategorizedId: Long,
        ) = error("Not needed")

        override suspend fun deleteWorkout(workoutId: Long) {
            workouts.update { items -> items.filterNot { it.id == workoutId } }
        }

        override suspend fun deleteWorkoutsForWeek(weekStartDate: LocalDate) = error("Not needed")

        override suspend fun replaceWorkoutsForWeek(
            weekStartDate: LocalDate,
            sourceWorkouts: List<Workout>,
        ) = error("Not needed")
    }

    private class FakeCategoryRepository(
        initialCategories: List<Category> = defaultCategories(),
    ) : CategoryRepository {
        private val categories = MutableStateFlow(initialCategories)

        override fun observeCategories(): Flow<List<Category>> = categories

        override suspend fun getCategories(): List<Category> = categories.value

        override suspend fun getCategory(id: Long): Category? = categories.value.firstOrNull { it.id == id }

        override suspend fun getCount(): Int = categories.value.size

        override suspend fun insertCategory(category: Category): Long {
            categories.update { items -> items + category }
            return category.id
        }

        override suspend fun insertCategories(categories: List<Category>): List<Long> {
            this.categories.update { items -> items + categories }
            return categories.map { it.id }
        }

        override suspend fun updateCategory(category: Category) = error("Not needed")

        override suspend fun updateCategoryName(
            id: Long,
            name: String,
        ) = error("Not needed")

        override suspend fun updateCategoryColor(
            id: Long,
            colorId: String,
        ) = error("Not needed")

        override suspend fun updateCategoryVisibility(
            id: Long,
            isHidden: Boolean,
        ) = error("Not needed")

        override suspend fun updateCategorySortOrder(
            id: Long,
            sortOrder: Int,
        ) = error("Not needed")

        override suspend fun deleteCategory(id: Long) = error("Not needed")

        companion object {
            private fun defaultCategories(): List<Category> {
                return listOf(
                    Category(
                        id = UNCATEGORIZED_ID,
                        name = UNCATEGORIZED_NAME,
                        colorId = UNCATEGORIZED_COLOR_ID,
                        sortOrder = 0,
                        isHidden = false,
                        isSystem = true,
                    ),
                    Category(
                        id = CATEGORY_ID,
                        name = CATEGORY_NAME,
                        colorId = CATEGORY_COLOR_ID,
                        sortOrder = 1,
                        isHidden = false,
                        isSystem = false,
                    ),
                )
            }
        }
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

    private class RecordingUserActionLogger : UserActionLogger {
        val actions = mutableListOf<UserAction>()

        override suspend fun log(action: UserAction) {
            actions += action
        }
    }

    private companion object {
        const val EVENT_ID = 10L
        const val INSERTED_ID = 99L
        const val CATEGORY_ID = 7L
        const val CATEGORY_NAME = "Race"
        const val CATEGORY_COLOR_ID = "blue"
        const val UNCATEGORIZED_NAME = "Uncategorized"
        const val UNCATEGORIZED_COLOR_ID = "uncategorized"
        const val EVENT_TITLE = "Half Marathon"
        const val UPDATED_TITLE = "City Marathon"
        const val WORKOUT_TITLE = "Easy Run"
        const val EVENT_DESCRIPTION = "Race prep"
        const val UPDATED_DESCRIPTION = "Updated race prep"
    }
}

private fun workout(
    id: Long,
    eventDate: LocalDate,
    eventType: EventType,
    type: String = "Race",
    description: String = "",
    categoryId: Long? = UNCATEGORIZED_ID,
    order: Int = 0,
    isCompleted: Boolean = false,
): Workout {
    return Workout(
        id = id,
        weekStartDate = canonicalStorageWeekStart(eventDate),
        dayOfWeek = eventDate.dayOfWeek,
        type = type,
        description = description,
        isCompleted = isCompleted,
        isRestDay = eventType == EventType.REST,
        categoryId = categoryId,
        order = order,
        eventType = eventType,
        timeSlot = null,
    )
}
