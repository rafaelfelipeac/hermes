package com.rafaelfelipeac.hermes.features.categories.presentation

import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.useraction.domain.UserAction
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.AddWorkoutRequest
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

private const val EMPTY_STRING = ""

class CategoriesScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun restoreDefaultsDialog_showsAndConfirms() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = FakeCategoryRepository()
        val workoutRepository = FakeWeeklyTrainingRepository()
        val categorySeeder = CategorySeeder(repository, FakeStringProvider())
        val logger = FakeUserActionLogger()
        val categoriesFlow =
            MutableStateFlow(
                listOf(
                    Category(
                        id = 2L,
                        name = "Run",
                        colorId = "run",
                        sortOrder = 1,
                        isHidden = false,
                        isSystem = true,
                    ),
                ),
            )
        repository.categoriesFlow = categoriesFlow

        val viewModel =
            CategoriesViewModel(
                repository = repository,
                workoutRepository = workoutRepository,
                categorySeeder = categorySeeder,
                userActionLogger = logger,
            )
        repository.resetInsertCounters()

        composeRule.setContent {
            CategoriesScreen(onBack = {}, viewModel = viewModel)
        }

        composeRule
            .onNodeWithText(context.getString(R.string.categories_restore_defaults))
            .performClick()

        composeRule
            .onNodeWithText(context.getString(R.string.categories_restore_defaults_title))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.categories_restore_defaults_message))
            .assertIsDisplayed()

        composeRule
            .onNodeWithText(context.getString(R.string.categories_restore_defaults_confirm))
            .performClick()

        composeRule.waitForIdle()
        assertTrue(repository.restoreDefaultsCalls() > 0)
    }

    @Test
    fun restoreDefaultsDialog_dismissesOnCancel() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = FakeCategoryRepository()
        val workoutRepository = FakeWeeklyTrainingRepository()
        val categorySeeder = CategorySeeder(repository, FakeStringProvider())
        val logger = FakeUserActionLogger()
        val categoriesFlow =
            MutableStateFlow(
                listOf(
                    Category(
                        id = 2L,
                        name = "Run",
                        colorId = "run",
                        sortOrder = 1,
                        isHidden = false,
                        isSystem = true,
                    ),
                ),
            )
        repository.categoriesFlow = categoriesFlow

        val viewModel =
            CategoriesViewModel(
                repository = repository,
                workoutRepository = workoutRepository,
                categorySeeder = categorySeeder,
                userActionLogger = logger,
            )

        composeRule.setContent {
            CategoriesScreen(onBack = {}, viewModel = viewModel)
        }

        composeRule
            .onNodeWithText(context.getString(R.string.categories_restore_defaults))
            .performClick()

        composeRule
            .onNodeWithText(context.getString(R.string.add_workout_cancel))
            .performClick()

        composeRule.waitForIdle()
        composeRule
            .onAllNodesWithText(context.getString(R.string.categories_restore_defaults_title))
            .assertCountEquals(0)
    }

    @Test
    fun languageSwitch_updatesCategoryNames() {
        val repository = FakeCategoryRepository()
        val workoutRepository = FakeWeeklyTrainingRepository()
        val categorySeeder = CategorySeeder(repository, FakeStringProvider())
        val logger = FakeUserActionLogger()
        val categoriesFlow =
            MutableStateFlow(
                listOf(
                    Category(
                        id = 2L,
                        name = "Run",
                        colorId = "run",
                        sortOrder = 1,
                        isHidden = false,
                        isSystem = true,
                    ),
                ),
            )
        repository.categoriesFlow = categoriesFlow

        val viewModel =
            CategoriesViewModel(
                repository = repository,
                workoutRepository = workoutRepository,
                categorySeeder = categorySeeder,
                userActionLogger = logger,
            )

        composeRule.setContent {
            CategoriesScreen(onBack = {}, viewModel = viewModel)
        }

        composeRule.onNodeWithText("Run").assertIsDisplayed()

        categoriesFlow.value =
            listOf(
                Category(
                    id = 2L,
                    name = "Corrida",
                    colorId = "run",
                    sortOrder = 1,
                    isHidden = false,
                    isSystem = true,
                ),
            )

        composeRule.waitForIdle()

        composeRule.onNodeWithText("Corrida").assertIsDisplayed()
        composeRule.onAllNodesWithText("Run").assertCountEquals(0)
    }

    private class FakeUserActionLogger : UserActionLogger {
        override suspend fun log(action: UserAction) = Unit
    }

    private class FakeCategoryRepository : CategoryRepository {
        var categoriesFlow = MutableStateFlow(emptyList<Category>())
        private var insertCategoryCalls = 0
        private var insertCategoriesCalls = 0

        override fun observeCategories(): Flow<List<Category>> = categoriesFlow

        override suspend fun getCategories(): List<Category> = categoriesFlow.value

        override suspend fun getCategory(id: Long): Category? = categoriesFlow.value.firstOrNull { it.id == id }

        override suspend fun getCount(): Int = categoriesFlow.value.size

        override suspend fun insertCategory(category: Category): Long {
            insertCategoryCalls += 1
            return category.id
        }

        override suspend fun insertCategories(categories: List<Category>): List<Long> {
            insertCategoriesCalls += 1
            return categories.map { it.id }
        }

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun updateCategoryName(
            id: Long,
            name: String,
        ) = Unit

        override suspend fun updateCategoryColor(
            id: Long,
            colorId: String,
        ) = Unit

        override suspend fun updateCategoryVisibility(
            id: Long,
            isHidden: Boolean,
        ) = Unit

        override suspend fun updateCategorySortOrder(
            id: Long,
            sortOrder: Int,
        ) = Unit

        override suspend fun deleteCategory(id: Long) = Unit

        fun resetInsertCounters() {
            insertCategoryCalls = 0
            insertCategoriesCalls = 0
        }

        fun restoreDefaultsCalls(): Int = insertCategoryCalls + insertCategoriesCalls
    }

    private class FakeWeeklyTrainingRepository : WeeklyTrainingRepository {
        override fun observeWorkoutsForWeek(weekStartDate: LocalDate): Flow<List<Workout>> = emptyFlow()

        override suspend fun getWorkoutsForWeek(weekStartDate: LocalDate): List<Workout> = emptyList()

        override suspend fun addWorkout(request: AddWorkoutRequest): Long = 0L

        override suspend fun addEvent(
            weekStartDate: LocalDate,
            dayOfWeek: DayOfWeek?,
            eventType: EventType,
            order: Int,
        ): Long = 0L

        override suspend fun addRestDay(
            weekStartDate: LocalDate,
            dayOfWeek: DayOfWeek?,
            order: Int,
        ): Long = 0L

        override suspend fun insertWorkout(workout: Workout): Long = 0L

        override suspend fun updateWorkoutDayAndOrder(
            workoutId: Long,
            dayOfWeek: DayOfWeek?,
            timeSlot: TimeSlot?,
            order: Int,
        ) = Unit

        override suspend fun updateWorkoutCompletion(
            workoutId: Long,
            isCompleted: Boolean,
        ) = Unit

        override suspend fun updateWorkoutDetails(
            workoutId: Long,
            type: String,
            description: String,
            eventType: EventType,
            categoryId: Long?,
        ) = Unit

        override suspend fun assignNullCategoryTo(uncategorizedId: Long) = Unit

        override suspend fun reassignCategory(
            deletedCategoryId: Long,
            uncategorizedId: Long,
        ) = Unit

        override suspend fun deleteWorkout(workoutId: Long) = Unit

        override suspend fun deleteWorkoutsForWeek(weekStartDate: LocalDate) = Unit

        override suspend fun replaceWorkoutsForWeek(
            weekStartDate: LocalDate,
            sourceWorkouts: List<Workout>,
        ) = Unit
    }

    private class FakeStringProvider : com.rafaelfelipeac.hermes.core.strings.StringProvider {
        override fun get(
            id: Int,
            vararg args: Any,
        ): String = EMPTY_STRING

        override fun getForLanguage(
            languageTag: String?,
            id: Int,
            vararg args: Any,
        ): String = EMPTY_STRING
    }
}
