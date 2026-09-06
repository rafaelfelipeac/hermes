package com.rafaelfelipeac.hermes.features.trophies.presentation

import app.cash.turbine.test
import com.rafaelfelipeac.hermes.core.useraction.domain.UserAction
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionRepository
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.TROPHY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.TROPHY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataSerializer
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyId
import com.rafaelfelipeac.hermes.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TrophyViewModelIntegrationTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun state_reactsToRepositoryUpdates() =
        runTest(mainDispatcherRule.testDispatcher) {
            val actions = MutableStateFlow<List<UserActionRecord>>(emptyList())
            val categories = MutableStateFlow<List<Category>>(emptyList())
            val viewModel =
                TrophyViewModel(
                    userActionRepository = FakeUserActionRepository(actions),
                    categoryRepository = FakeCategoryRepository(categories),
                    userActionLogger = RecordingUserActionLogger(),
                )

            viewModel.state.test {
                awaitItem()
                awaitItem()

                actions.value =
                    listOf(
                        weekAction(
                            id = 1L,
                            weekStartDate = LocalDate.of(2026, 4, 6),
                            timestamp = 10L,
                        ),
                    )
                advanceUntilIdle()

                val updated = awaitItem()

                assertTrue(updated.families.isNotEmpty())
                assertEquals(TrophyFamilyUi.FOLLOW_THROUGH, updated.families.first().family)
                assertEquals(
                    TrophyId.FULL_TIME,
                    updated.families.first().sections.first().trophies.first().trophyId,
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun logShareTrophy_recordsExactMetadata() =
        runTest(mainDispatcherRule.testDispatcher) {
            val logger = RecordingUserActionLogger()
            val viewModel =
                TrophyViewModel(
                    userActionRepository =
                        FakeUserActionRepository(
                            MutableStateFlow<List<UserActionRecord>>(emptyList()),
                        ),
                    categoryRepository =
                        FakeCategoryRepository(
                            MutableStateFlow<List<Category>>(emptyList()),
                        ),
                    userActionLogger = logger,
                )
            val trophy =
                TrophyCardUi(
                    stableId = "podium_place_10",
                    trophyId = TrophyId.PODIUM_PLACE,
                    family = TrophyFamilyUi.CATEGORIES,
                    sortOrder = 1,
                    badgeRank = 1,
                    categoryId = 10L,
                    categoryName = "Run",
                    currentValue = 3,
                    target = 5,
                    isUnlocked = true,
                    unlockedAt = 1_000L,
                )

            viewModel.logShareTrophy(trophy = trophy, trophyName = "Podium Place")
            advanceUntilIdle()

            assertEquals(1, logger.loggedActions.size)
            assertEquals(UserActionType.SHARE_TROPHY, logger.loggedActions.single().actionType)
            assertEquals(UserActionEntityType.TROPHY, logger.loggedActions.single().entityType)
            assertEquals(
                mapOf(
                    TROPHY_ID to TrophyId.PODIUM_PLACE.name,
                    TROPHY_NAME to "Podium Place",
                    CATEGORY_ID to "10",
                    CATEGORY_NAME to "Run",
                ),
                logger.loggedActions.single().metadata,
            )
        }

    private class FakeUserActionRepository(
        private val flow: MutableStateFlow<List<UserActionRecord>>,
    ) : UserActionRepository {
        override fun observeActions(): Flow<List<UserActionRecord>> = flow
    }

    private class FakeCategoryRepository(
        private val flow: MutableStateFlow<List<Category>>,
    ) : CategoryRepository {
        override fun observeCategories(): Flow<List<Category>> = flow

        override suspend fun getCategories(): List<Category> = flow.value

        override suspend fun getCategory(id: Long): Category? = flow.value.firstOrNull { it.id == id }

        override suspend fun getCount(): Int = flow.value.size

        override suspend fun insertCategory(category: Category): Long = error("Not needed in test")

        override suspend fun insertCategories(categories: List<Category>): List<Long> = error("Not needed in test")

        override suspend fun updateCategory(category: Category) = error("Not needed in test")

        override suspend fun updateCategoryName(
            id: Long,
            name: String,
        ) = error("Not needed in test")

        override suspend fun updateCategoryColor(
            id: Long,
            colorId: String,
        ) = error("Not needed in test")

        override suspend fun updateCategoryVisibility(
            id: Long,
            isHidden: Boolean,
        ) = error("Not needed in test")

        override suspend fun updateCategorySortOrder(
            id: Long,
            sortOrder: Int,
        ) = error("Not needed in test")

        override suspend fun deleteCategory(id: Long) = error("Not needed in test")
    }

    private class RecordingUserActionLogger : UserActionLogger {
        val loggedActions = mutableListOf<UserAction>()

        override suspend fun log(action: UserAction) {
            loggedActions += action
        }
    }

    private companion object {
        fun weekAction(
            id: Long,
            weekStartDate: LocalDate,
            timestamp: Long,
        ): UserActionRecord {
            return UserActionRecord(
                id = id,
                actionType = UserActionType.COMPLETE_WEEK_WORKOUTS.name,
                entityType = UserActionEntityType.WEEK.name,
                entityId = weekStartDate.toEpochDay(),
                metadata =
                    UserActionMetadataSerializer.toJson(
                        mapOf(WEEK_START_DATE to weekStartDate.toString()),
                    ),
                timestamp = timestamp,
            )
        }
    }
}
