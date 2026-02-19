package com.rafaelfelipeac.hermes.features.categories.presentation

import com.rafaelfelipeac.hermes.core.useraction.domain.UserAction
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues.CATEGORY_HIDDEN
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues.CATEGORY_VISIBLE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.CATEGORY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_CATEGORY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_CATEGORY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.REORDER_CATEGORY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.RESTORE_DEFAULT_CATEGORIES
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_CATEGORY_COLOR
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_CATEGORY_VISIBILITY
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import com.rafaelfelipeac.hermes.test.MainDispatcherRule
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.every
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun addCategory_logsCreateMetadata() =
        runTest(mainDispatcherRule.testDispatcher) {
            val logger = FakeUserActionLogger()
            val repository = mockk<CategoryRepository>(relaxed = true)
            val workoutRepository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val categoriesFlow =
                MutableStateFlow(
                    listOf(
                        defaultCategory(id = UNCATEGORIZED_ID, name = "Uncategorized", sortOrder = 0),
                        defaultCategory(id = 2L, name = "Run", sortOrder = 1),
                    ),
                )

            coEvery { categorySeeder.ensureSeeded() } returns Unit
            every { repository.observeCategories() } returns categoriesFlow
            coEvery { repository.insertCategory(any()) } returns 42L

            val viewModel =
                CategoriesViewModel(
                    repository = repository,
                    workoutRepository = workoutRepository,
                    categorySeeder = categorySeeder,
                    userActionLogger = logger,
                )
            primeState(viewModel)

            viewModel.addCategory(name = "Yoga", colorId = "purple")
            advanceUntilIdle()

            val action = logger.actions.last()
            assertEquals(CREATE_CATEGORY, action.actionType)
            assertEquals(CATEGORY, action.entityType)
            assertEquals(42L, action.entityId)
            assertEquals(mapOf(CATEGORY_NAME to "Yoga"), action.metadata)
        }

    @Test
    fun renameCategory_logsOldAndNewValues() =
        runTest(mainDispatcherRule.testDispatcher) {
            val logger = FakeUserActionLogger()
            val repository = mockk<CategoryRepository>(relaxed = true)
            val workoutRepository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val categoriesFlow =
                MutableStateFlow(
                    listOf(defaultCategory(id = 2L, name = "Run", sortOrder = 1)),
                )

            coEvery { categorySeeder.ensureSeeded() } returns Unit
            every { repository.observeCategories() } returns categoriesFlow

            val viewModel =
                CategoriesViewModel(
                    repository = repository,
                    workoutRepository = workoutRepository,
                    categorySeeder = categorySeeder,
                    userActionLogger = logger,
                )
            primeState(viewModel)

            viewModel.renameCategory(categoryId = 2L, newName = "Jog")
            advanceUntilIdle()

            val action = logger.actions.last()
            assertEquals(UPDATE_CATEGORY_NAME, action.actionType)
            assertEquals(
                mapOf(
                    CATEGORY_NAME to "Jog",
                    OLD_VALUE to "Run",
                    NEW_VALUE to "Jog",
                ),
                action.metadata,
            )
        }

    @Test
    fun updateCategoryColor_logsOldAndNewValues() =
        runTest(mainDispatcherRule.testDispatcher) {
            val logger = FakeUserActionLogger()
            val repository = mockk<CategoryRepository>(relaxed = true)
            val workoutRepository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val categoriesFlow =
                MutableStateFlow(
                    listOf(
                        defaultCategory(id = 2L, name = "Run", sortOrder = 1, colorId = "run"),
                    ),
                )

            coEvery { categorySeeder.ensureSeeded() } returns Unit
            every { repository.observeCategories() } returns categoriesFlow

            val viewModel =
                CategoriesViewModel(
                    repository = repository,
                    workoutRepository = workoutRepository,
                    categorySeeder = categorySeeder,
                    userActionLogger = logger,
                )
            primeState(viewModel)

            viewModel.updateCategoryColor(categoryId = 2L, colorId = "red")
            advanceUntilIdle()

            val action = logger.actions.last()
            assertEquals(UPDATE_CATEGORY_COLOR, action.actionType)
            assertEquals(
                mapOf(
                    CATEGORY_NAME to "Run",
                    OLD_VALUE to "run",
                    NEW_VALUE to "red",
                ),
                action.metadata,
            )
        }

    @Test
    fun updateCategoryVisibility_logsVisibilityValues() =
        runTest(mainDispatcherRule.testDispatcher) {
            val logger = FakeUserActionLogger()
            val repository = mockk<CategoryRepository>(relaxed = true)
            val workoutRepository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val categoriesFlow =
                MutableStateFlow(
                    listOf(
                        defaultCategory(id = 2L, name = "Run", sortOrder = 1),
                    ),
                )

            coEvery { categorySeeder.ensureSeeded() } returns Unit
            every { repository.observeCategories() } returns categoriesFlow

            val viewModel =
                CategoriesViewModel(
                    repository = repository,
                    workoutRepository = workoutRepository,
                    categorySeeder = categorySeeder,
                    userActionLogger = logger,
                )
            primeState(viewModel)

            viewModel.updateCategoryVisibility(categoryId = 2L, isHidden = true)
            advanceUntilIdle()

            val action = logger.actions.last()
            assertEquals(UPDATE_CATEGORY_VISIBILITY, action.actionType)
            assertEquals(
                mapOf(
                    CATEGORY_NAME to "Run",
                    OLD_VALUE to CATEGORY_VISIBLE,
                    NEW_VALUE to CATEGORY_HIDDEN,
                ),
                action.metadata,
            )
        }

    @Test
    fun moveCategory_logsReorderAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val logger = FakeUserActionLogger()
            val repository = mockk<CategoryRepository>(relaxed = true)
            val workoutRepository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val categoriesFlow =
                MutableStateFlow(
                    listOf(
                        defaultCategory(id = 2L, name = "Run", sortOrder = 1),
                        defaultCategory(id = 3L, name = "Cycling", sortOrder = 2),
                    ),
                )

            coEvery { categorySeeder.ensureSeeded() } returns Unit
            every { repository.observeCategories() } returns categoriesFlow

            val viewModel =
                CategoriesViewModel(
                    repository = repository,
                    workoutRepository = workoutRepository,
                    categorySeeder = categorySeeder,
                    userActionLogger = logger,
                )
            primeState(viewModel)

            viewModel.moveCategoryDown(categoryId = 2L)
            advanceUntilIdle()

            val action = logger.actions.last()
            assertEquals(REORDER_CATEGORY, action.actionType)
            assertEquals(2L, action.entityId)
            assertEquals(mapOf(CATEGORY_NAME to "Run"), action.metadata)
        }

    @Test
    fun deleteCategory_logsDeleteAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val logger = FakeUserActionLogger()
            val repository = mockk<CategoryRepository>(relaxed = true)
            val workoutRepository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val categoriesFlow =
                MutableStateFlow(
                    listOf(
                        defaultCategory(id = 2L, name = "Run", sortOrder = 1),
                    ),
                )

            coEvery { categorySeeder.ensureSeeded() } returns Unit
            every { repository.observeCategories() } returns categoriesFlow

            val viewModel =
                CategoriesViewModel(
                    repository = repository,
                    workoutRepository = workoutRepository,
                    categorySeeder = categorySeeder,
                    userActionLogger = logger,
                )
            primeState(viewModel)

            viewModel.deleteCategory(categoryId = 2L)
            advanceUntilIdle()

            val action = logger.actions.last()
            assertEquals(DELETE_CATEGORY, action.actionType)
            assertEquals(2L, action.entityId)
            assertEquals(mapOf(CATEGORY_NAME to "Run"), action.metadata)
        }

    @Test
    fun restoreDefaults_logsWhenDefaultsAdded() =
        runTest(mainDispatcherRule.testDispatcher) {
            val logger = FakeUserActionLogger()
            val repository = mockk<CategoryRepository>(relaxed = true)
            val workoutRepository = mockk<WeeklyTrainingRepository>(relaxed = true)
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val categoriesFlow = MutableStateFlow(emptyList<Category>())

            coEvery { categorySeeder.ensureSeeded() } returns Unit
            coEvery { categorySeeder.restoreDefaults() } returns 2
            coEvery { categorySeeder.syncLocalizedNames(force = true) } returns Unit
            coEvery { categorySeeder.syncDefaultColors() } returns Unit
            coEvery { repository.observeCategories() } returns categoriesFlow

            val viewModel =
                CategoriesViewModel(
                    repository = repository,
                    workoutRepository = workoutRepository,
                    categorySeeder = categorySeeder,
                    userActionLogger = logger,
                )
            primeState(viewModel, expectNonEmpty = false)

            viewModel.restoreDefaultCategories()
            advanceUntilIdle()

            val action = logger.actions.last()
            assertEquals(RESTORE_DEFAULT_CATEGORIES, action.actionType)
            assertEquals(CATEGORY, action.entityType)
            assertTrue(action.metadata.isNullOrEmpty())
        }

    private fun defaultCategory(
        id: Long,
        name: String,
        sortOrder: Int,
        colorId: String = "run",
    ): Category {
        return Category(
            id = id,
            name = name,
            colorId = colorId,
            sortOrder = sortOrder,
            isHidden = false,
            isSystem = true,
        )
    }

    private class FakeUserActionLogger : UserActionLogger {
        val actions = mutableListOf<UserAction>()

        override suspend fun log(action: UserAction) {
            actions.add(action)
        }
    }

    private suspend fun primeState(
        viewModel: CategoriesViewModel,
        expectNonEmpty: Boolean = true,
    ) {
        viewModel.state.test {
            var state = awaitItem()
            if (expectNonEmpty) {
                repeat(5) {
                    if (state.categories.isNotEmpty()) return@repeat
                    state = awaitItem()
                }
            }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
