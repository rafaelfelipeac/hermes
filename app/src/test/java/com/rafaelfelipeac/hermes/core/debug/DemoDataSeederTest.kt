package com.rafaelfelipeac.hermes.core.debug

import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionDao
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordDao
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.RACE_EVENT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.WORKOUT
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoDataSeederTest {
    @Test
    fun clearDatabase_preservesCategoriesAndEnsuresDefaults() =
        runTest {
            val workoutDao = mockk<WorkoutDao>(relaxed = true)
            val userActionDao = mockk<UserActionDao>(relaxed = true)
            val personalRecordDao = mockk<PersonalRecordDao>(relaxed = true)
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val settingsRepository = FakeSettingsRepository()
            coEvery { categorySeeder.ensureSeeded() } returns Unit
            val seeder =
                DemoDataSeeder(
                    workoutDao = workoutDao,
                    userActionDao = userActionDao,
                    personalRecordDao = personalRecordDao,
                    stringProvider = FakeStringProvider,
                    categorySeeder = categorySeeder,
                    settingsRepository = settingsRepository,
                )

            val didClear = seeder.clearDatabase()

            assertEquals(true, didClear)
            coVerify(exactly = 1) { workoutDao.deleteAll() }
            coVerify(exactly = 1) { userActionDao.deleteAll() }
            coVerify(exactly = 1) { personalRecordDao.deleteAllEntries() }
            coVerify(exactly = 1) { personalRecordDao.deleteAllFamilies() }
            coVerify(exactly = 1) { categorySeeder.ensureSeeded() }
        }

    @Test
    fun seed_createsVariedProgressWeeks() =
        runTest {
            val workoutDao = mockk<WorkoutDao>(relaxed = true)
            val userActionDao = mockk<UserActionDao>(relaxed = true)
            val personalRecordDao = mockk<PersonalRecordDao>(relaxed = true)
            val categorySeeder = mockk<CategorySeeder>(relaxed = true)
            val settingsRepository = FakeSettingsRepository()
            val capturedWorkouts = mutableListOf<WorkoutEntity>()
            coEvery { categorySeeder.ensureSeeded() } returns Unit
            coEvery { workoutDao.insert(capture(capturedWorkouts)) } returns 1L
            coEvery { userActionDao.insert(any()) } returns 1L
            val seeder =
                DemoDataSeeder(
                    workoutDao = workoutDao,
                    userActionDao = userActionDao,
                    personalRecordDao = personalRecordDao,
                    stringProvider = FakeStringProvider,
                    categorySeeder = categorySeeder,
                    settingsRepository = settingsRepository,
                )

            val didSeed = seeder.seed()

            assertEquals(true, didSeed)
            val plannedCountsByWeek =
                capturedWorkouts
                    .groupBy { it.weekStartDate }
                    .mapValues { (_, workouts) ->
                        workouts.count { workout ->
                            workout.dayOfWeek != null &&
                                (workout.eventType == WORKOUT.name || workout.eventType == RACE_EVENT.name)
                        }
                    }

            assertTrue(plannedCountsByWeek.values.toSet().size >= 3)
            assertTrue(plannedCountsByWeek.values.maxOrNull()!! > plannedCountsByWeek.values.minOrNull()!!)
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
        override val distanceUnit: Flow<DistanceUnit> = MutableStateFlow(DistanceUnit.KILOMETERS)
        override val paceUnit: Flow<PaceUnit> = MutableStateFlow(PaceUnit.MIN_PER_KM)
        override val weightUnit: Flow<WeightUnit> = MutableStateFlow(WeightUnit.KILOGRAMS)
        override val lastBackupExportedAt: Flow<String?> = MutableStateFlow(null)
        override val lastBackupImportedAt: Flow<String?> = MutableStateFlow(null)
        override val backupFolderUri: Flow<String?> = MutableStateFlow(null)
        override val lastSeenTrophyCelebrationToken: Flow<String?> = MutableStateFlow(null)

        override fun initialThemeMode(): ThemeMode = ThemeMode.SYSTEM

        override fun initialLanguage(): AppLanguage = AppLanguage.SYSTEM

        override fun initialSlotModePolicy(): SlotModePolicy = SlotModePolicy.AUTO_WHEN_MULTIPLE

        override fun initialWeekStartDay(): WeekStartDay = WeekStartDay.MONDAY

        override fun initialDistanceUnit(): DistanceUnit = DistanceUnit.KILOMETERS

        override fun initialPaceUnit(): PaceUnit = PaceUnit.MIN_PER_KM

        override fun initialWeightUnit(): WeightUnit = WeightUnit.KILOGRAMS

        override suspend fun setThemeMode(mode: ThemeMode) = Unit

        override suspend fun setLanguage(language: AppLanguage) = Unit

        override suspend fun setSlotModePolicy(policy: SlotModePolicy) = Unit

        override suspend fun setWeekStartDay(weekStartDay: WeekStartDay) = Unit

        override suspend fun setDistanceUnit(distanceUnit: DistanceUnit) = Unit

        override suspend fun setPaceUnit(paceUnit: PaceUnit) = Unit

        override suspend fun setWeightUnit(weightUnit: WeightUnit) = Unit

        override suspend fun setLastBackupExportedAt(value: String) = Unit

        override suspend fun setLastBackupImportedAt(value: String) = Unit

        override suspend fun setBackupFolderUri(value: String?) = Unit

        override suspend fun setLastSeenTrophyCelebrationToken(value: String?) = Unit
    }
}
