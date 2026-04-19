package com.rafaelfelipeac.hermes.features.trophies.presentation

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionRepository
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataSerializer
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COMPLETE_WEEK_WORKOUTS
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TrophyCelebrationViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun events_replaysInitUnlockToLaterCollector() =
        runTest(mainDispatcherRule.testDispatcher) {
            val weekStart = LocalDate.of(2026, 4, 6)
            val viewModel =
                TrophyCelebrationViewModel(
                    userActionRepository =
                        FakeUserActionRepository(
                            actions =
                                List(FULL_TIME_TARGET) { index ->
                                    weekAction(
                                        id = index.toLong() + 1,
                                        weekStartDate = weekStart.plusWeeks(index.toLong()),
                                        timestamp = (index + 1) * TIMESTAMP_STEP,
                                    )
                                },
                        ),
                    categoryRepository = FakeCategoryRepository(),
                    settingsRepository = FakeSettingsRepository(),
                    stringProvider = FakeStringProvider(),
                )

            advanceUntilIdle()

            val event = withTimeout(EVENT_TIMEOUT_MS) { viewModel.events.first() }

            assertTrue(event.token.endsWith(":$FULL_TIME_UNLOCKED_AT"))
            assertTrue(event.trophyStableId.isNotBlank())
            assertEquals("New trophy unlocked: Trophy.", event.message)
        }

    private class FakeUserActionRepository(
        actions: List<UserActionRecord>,
    ) : UserActionRepository {
        private val flow = MutableStateFlow(actions)

        override fun observeActions(): Flow<List<UserActionRecord>> = flow
    }

    private class FakeCategoryRepository : CategoryRepository {
        private val flow = MutableStateFlow(emptyList<Category>())

        override fun observeCategories(): Flow<List<Category>> = flow

        override suspend fun getCategories(): List<Category> = flow.value

        override suspend fun getCategory(id: Long): Category? = null

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

    private class FakeSettingsRepository : SettingsRepository {
        override val themeMode = MutableStateFlow(ThemeMode.SYSTEM)
        override val language = MutableStateFlow(AppLanguage.SYSTEM)
        override val slotModePolicy = MutableStateFlow(SlotModePolicy.AUTO_WHEN_MULTIPLE)
        override val weekStartDay = MutableStateFlow(WeekStartDay.MONDAY)
        override val lastBackupExportedAt = MutableStateFlow<String?>(null)
        override val lastBackupImportedAt = MutableStateFlow<String?>(null)
        override val backupFolderUri = MutableStateFlow<String?>(null)
        override val lastSeenTrophyCelebrationToken = MutableStateFlow<String?>(null)

        override fun initialThemeMode(): ThemeMode = ThemeMode.SYSTEM

        override fun initialLanguage(): AppLanguage = AppLanguage.SYSTEM

        override fun initialSlotModePolicy(): SlotModePolicy = SlotModePolicy.AUTO_WHEN_MULTIPLE

        override fun initialWeekStartDay(): WeekStartDay = WeekStartDay.MONDAY

        override suspend fun setThemeMode(mode: ThemeMode) = error("Not needed in test")

        override suspend fun setLanguage(language: AppLanguage) = error("Not needed in test")

        override suspend fun setSlotModePolicy(policy: SlotModePolicy) = error("Not needed in test")

        override suspend fun setWeekStartDay(weekStartDay: WeekStartDay) = error("Not needed in test")

        override suspend fun setLastBackupExportedAt(value: String) = error("Not needed in test")

        override suspend fun setLastBackupImportedAt(value: String) = error("Not needed in test")

        override suspend fun setBackupFolderUri(value: String?) = error("Not needed in test")

        override suspend fun setLastSeenTrophyCelebrationToken(value: String?) {
            lastSeenTrophyCelebrationToken.value = value
        }
    }

    private class FakeStringProvider : StringProvider {
        override fun get(
            id: Int,
            vararg args: Any,
        ): String {
            return when (id) {
                R.string.trophies_unlock_banner -> "New trophy unlocked: ${args.first()}."
                else -> "Trophy"
            }
        }

        override fun getForLanguage(
            languageTag: String?,
            id: Int,
            vararg args: Any,
        ): String = get(id, *args)
    }

    private companion object {
        const val FULL_TIME_TARGET = 4
        const val TIMESTAMP_STEP = 10L
        const val FULL_TIME_UNLOCKED_AT = FULL_TIME_TARGET * TIMESTAMP_STEP
        const val EVENT_TIMEOUT_MS = 1_000L

        fun weekAction(
            id: Long,
            weekStartDate: LocalDate,
            timestamp: Long,
        ): UserActionRecord {
            return UserActionRecord(
                id = id,
                actionType = COMPLETE_WEEK_WORKOUTS.name,
                entityType = WEEK.name,
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
