package com.rafaelfelipeac.hermes.core.debug

import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionDao
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DemoDataSeederTest {
    @Test
    fun clearDatabase_preservesCategoriesAndEnsuresDefaults() =
        runTest {
            val workoutDao = mockk<WorkoutDao>(relaxed = true)
            val userActionDao = mockk<UserActionDao>(relaxed = true)
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val settingsRepository = FakeSettingsRepository()
            coEvery { categorySeeder.ensureSeeded() } returns Unit
            val seeder =
                DemoDataSeeder(
                    workoutDao = workoutDao,
                    userActionDao = userActionDao,
                    stringProvider = FakeStringProvider,
                    categorySeeder = categorySeeder,
                    settingsRepository = settingsRepository,
                )

            val didClear = seeder.clearDatabase()

            assertEquals(true, didClear)
            coVerify(exactly = 1) { workoutDao.deleteAll() }
            coVerify(exactly = 1) { userActionDao.deleteAll() }
            coVerify(exactly = 1) { categorySeeder.ensureSeeded() }
        }

    private object FakeStringProvider : StringProvider {
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

    private class FakeSettingsRepository : SettingsRepository {
        override val themeMode: Flow<ThemeMode> = MutableStateFlow(ThemeMode.SYSTEM)
        override val language: Flow<AppLanguage> = MutableStateFlow(AppLanguage.SYSTEM)
        override val slotModePolicy: Flow<SlotModePolicy> = MutableStateFlow(SlotModePolicy.AUTO_WHEN_MULTIPLE)
        override val weekStartDay: Flow<WeekStartDay> = MutableStateFlow(WeekStartDay.MONDAY)
        override val lastBackupExportedAt: Flow<String?> = MutableStateFlow(null)
        override val lastBackupImportedAt: Flow<String?> = MutableStateFlow(null)
        override val backupFolderUri: Flow<String?> = MutableStateFlow(null)
        override val lastSeenTrophyCelebrationToken: Flow<String?> = MutableStateFlow(null)

        override fun initialThemeMode(): ThemeMode = ThemeMode.SYSTEM

        override fun initialLanguage(): AppLanguage = AppLanguage.SYSTEM

        override fun initialSlotModePolicy(): SlotModePolicy = SlotModePolicy.AUTO_WHEN_MULTIPLE

        override fun initialWeekStartDay(): WeekStartDay = WeekStartDay.MONDAY

        override suspend fun setThemeMode(mode: ThemeMode) = Unit

        override suspend fun setLanguage(language: AppLanguage) = Unit

        override suspend fun setSlotModePolicy(policy: SlotModePolicy) = Unit

        override suspend fun setWeekStartDay(weekStartDay: WeekStartDay) = Unit

        override suspend fun setLastBackupExportedAt(value: String) = Unit

        override suspend fun setLastBackupImportedAt(value: String) = Unit

        override suspend fun setBackupFolderUri(value: String?) = Unit

        override suspend fun setLastSeenTrophyCelebrationToken(value: String?) = Unit
    }
}
