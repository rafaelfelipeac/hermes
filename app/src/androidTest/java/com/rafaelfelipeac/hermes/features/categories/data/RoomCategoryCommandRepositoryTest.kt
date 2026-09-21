package com.rafaelfelipeac.hermes.features.categories.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.core.useraction.domain.UserAction
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_CATEGORY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.REORDER_CATEGORY
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryEntity
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.domain.command.CategoryCommandResult
import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeEntity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle.ACTIVE
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType.TOTAL
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordFamilyEntity
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.HIGHER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.DISTANCE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOMETER
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class RoomCategoryCommandRepositoryTest {
    private lateinit var context: Context
    private lateinit var database: HermesDatabase
    private lateinit var logger: FakeUserActionLogger
    private lateinit var repository: RoomCategoryCommandRepository

    @Before
    fun setUp() =
        runTest {
            context = ApplicationProvider.getApplicationContext()
            database =
                Room.inMemoryDatabaseBuilder(context, HermesDatabase::class.java)
                    .allowMainThreadQueries()
                    .build()
            logger = FakeUserActionLogger()
            repository = RoomCategoryCommandRepository(database, logger)
        }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun deleteCategory_reassignsRelatedDataDeletesCategoryAndLogsOnce() =
        runTest {
            seedCategoryGraph()

            val result = repository.deleteCategory(CATEGORY_ID)

            assertEquals(CategoryCommandResult.Changed, result)
            assertNull(database.categoryDao().getCategory(CATEGORY_ID))
            assertEquals(UNCATEGORIZED_ID, database.workoutDao().getAll().single().categoryId)
            assertNull(database.personalRecordDao().getFamilies().single().categoryId)
            assertNull(database.challengeDao().getAllChallenges().single().categoryId)
            logger.assertLoggedOnce(
                actionType = DELETE_CATEGORY,
                entityId = CATEGORY_ID,
                categoryName = CATEGORY_NAME_VALUE,
            )
        }

    @Test
    fun deleteCategory_rollsBackRelatedDataDeleteAndLogWhenLoggerFails() =
        runTest {
            seedCategoryGraph()
            logger.failNextLog = true

            runCatching { repository.deleteCategory(CATEGORY_ID) }

            assertEquals(CATEGORY_NAME_VALUE, database.categoryDao().getCategory(CATEGORY_ID)?.name)
            assertEquals(CATEGORY_ID, database.workoutDao().getAll().single().categoryId)
            assertEquals(CATEGORY_ID, database.personalRecordDao().getFamilies().single().categoryId)
            assertEquals(CATEGORY_ID, database.challengeDao().getAllChallenges().single().categoryId)
            assertTrue(logger.actions.isEmpty())
        }

    @Test
    fun moveCategory_usesDatabaseOrderSwapsSortOrdersAndLogsOnce() =
        runTest {
            seedCategoriesForReorder()

            val result = repository.moveCategory(categoryId = 30L, delta = -1)

            assertEquals(CategoryCommandResult.Changed, result)
            val categoriesById = database.categoryDao().getCategories().associateBy { it.id }
            assertEquals(2, categoriesById.getValue(20L).sortOrder)
            assertEquals(1, categoriesById.getValue(30L).sortOrder)
            logger.assertLoggedOnce(
                actionType = REORDER_CATEGORY,
                entityId = 30L,
                categoryName = "Cycle",
            )
        }

    @Test
    fun moveCategory_returnsNoChangeForMissingOrBoundaryCategoryWithoutLogging() =
        runTest {
            seedCategoriesForReorder()

            val missingResult = repository.moveCategory(categoryId = 999L, delta = 1)
            val boundaryResult = repository.moveCategory(categoryId = 10L, delta = -1)

            assertEquals(CategoryCommandResult.NoChange, missingResult)
            assertEquals(CategoryCommandResult.NoChange, boundaryResult)
            assertTrue(logger.actions.isEmpty())
        }

    @Test
    fun moveCategoryToPosition_shiftsRangeAndLogsOnce() =
        runTest {
            seedCategoriesForReorder()

            val result = repository.moveCategoryToPosition(categoryId = 10L, targetIndex = 2)

            assertEquals(CategoryCommandResult.Changed, result)
            val categoriesById = database.categoryDao().getCategories().associateBy { it.id }
            assertEquals(2, categoriesById.getValue(10L).sortOrder)
            assertEquals(0, categoriesById.getValue(20L).sortOrder)
            assertEquals(1, categoriesById.getValue(30L).sortOrder)
            logger.assertLoggedOnce(
                actionType = REORDER_CATEGORY,
                entityId = 10L,
                categoryName = "Run",
            )
        }

    @Test
    fun moveCategoryToPosition_returnsNoChangeForMissingSameOrInvalidTargetWithoutLogging() =
        runTest {
            seedCategoriesForReorder()

            val missingResult = repository.moveCategoryToPosition(categoryId = 999L, targetIndex = 1)
            val sameResult = repository.moveCategoryToPosition(categoryId = 20L, targetIndex = 1)
            val invalidResult = repository.moveCategoryToPosition(categoryId = 20L, targetIndex = 99)

            assertEquals(CategoryCommandResult.NoChange, missingResult)
            assertEquals(CategoryCommandResult.NoChange, sameResult)
            assertEquals(CategoryCommandResult.NoChange, invalidResult)
            assertTrue(logger.actions.isEmpty())
        }

    @Test
    fun deleteCategory_returnsNoChangeForMissingOrUncategorizedCategoryWithoutLogging() =
        runTest {
            seedCategoriesForReorder()

            val missingResult = repository.deleteCategory(categoryId = 999L)
            val uncategorizedResult = repository.deleteCategory(categoryId = UNCATEGORIZED_ID)

            assertEquals(CategoryCommandResult.NoChange, missingResult)
            assertEquals(CategoryCommandResult.NoChange, uncategorizedResult)
            assertTrue(logger.actions.isEmpty())
        }

    private suspend fun seedCategoryGraph() {
        database.categoryDao().insert(sampleCategory(id = UNCATEGORIZED_ID, name = "Uncategorized", sortOrder = 0))
        database.categoryDao().insert(sampleCategory(id = CATEGORY_ID, name = CATEGORY_NAME_VALUE, sortOrder = 1))
        database.workoutDao().insert(sampleWorkout())
        database.personalRecordDao().insertFamily(samplePersonalRecordFamily())
        database.challengeDao().insertChallenge(sampleChallenge())
    }

    private suspend fun seedCategoriesForReorder() {
        database.categoryDao().insert(sampleCategory(id = 10L, name = "Run", sortOrder = 0))
        database.categoryDao().insert(sampleCategory(id = 20L, name = "Swim", sortOrder = 1))
        database.categoryDao().insert(sampleCategory(id = 30L, name = "Cycle", sortOrder = 2))
    }

    private fun sampleCategory(
        id: Long,
        name: String,
        sortOrder: Int,
    ) = CategoryEntity(
        id = id,
        name = name,
        colorId = "run",
        sortOrder = sortOrder,
        isHidden = false,
        isSystem = false,
    )

    private fun sampleWorkout() =
        WorkoutEntity(
            weekStartDate = LocalDate.parse("2026-09-07"),
            dayOfWeek = 1,
            type = "Run",
            description = "Easy",
            isCompleted = false,
            isRestDay = false,
            categoryId = CATEGORY_ID,
            sortOrder = 0,
        )

    private fun samplePersonalRecordFamily() =
        PersonalRecordFamilyEntity(
            categoryId = CATEGORY_ID,
            title = "5K",
            metricType = DISTANCE,
            defaultUnit = KILOMETER,
            comparisonRule = HIGHER_IS_BETTER,
            manualCurrentEntryId = null,
            sortOrder = 0,
            createdAt = TIMESTAMP,
            updatedAt = TIMESTAMP,
        )

    private fun sampleChallenge() =
        ChallengeEntity(
            categoryId = CATEGORY_ID,
            title = "Distance",
            description = null,
            targetType = TOTAL,
            targetQuantity = 42L,
            startDate = LocalDate.parse("2026-09-01"),
            endDate = LocalDate.parse("2026-09-30"),
            lifecycle = ACTIVE,
            archivedAt = null,
            createdAt = TIMESTAMP,
            updatedAt = TIMESTAMP,
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
            entityId: Long,
            categoryName: String,
        ) {
            val action = actions.single()
            assertEquals(actionType, action.actionType)
            assertEquals(entityId, action.entityId)
            assertEquals(mapOf(CATEGORY_NAME to categoryName), action.metadata)
        }
    }

    private companion object {
        const val CATEGORY_ID = 100L
        const val CATEGORY_NAME_VALUE = "Run"
        const val TIMESTAMP = 1_756_800_000_000L
    }
}
