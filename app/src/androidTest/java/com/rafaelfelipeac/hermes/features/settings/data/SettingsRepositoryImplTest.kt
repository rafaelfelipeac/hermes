package com.rafaelfelipeac.hermes.features.settings.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rafaelfelipeac.hermes.features.backup.data.BackupSettingsDataSource
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.SettingsSnapshot
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsRepositoryImplTest {
    private lateinit var context: Context
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setUp() =
        runTest {
            context = ApplicationProvider.getApplicationContext()
            repository = SettingsRepositoryImpl(context)
            context.settingsDataStore.edit { it.clear() }
        }

    @Test
    fun themeMode_defaultsToSystem_whenPreferenceIsMissing() =
        runTest {
            assertEquals(ThemeMode.SYSTEM, repository.themeMode.first())
            assertEquals(ThemeMode.SYSTEM, repository.initialThemeMode())
        }

    @Test
    fun themeMode_defaultsToSystem_whenStoredValueIsInvalid() =
        runTest {
            context.settingsDataStore.edit { prefs ->
                prefs[THEME_MODE_KEY] = "BROKEN_VALUE"
            }

            assertEquals(ThemeMode.SYSTEM, repository.themeMode.first())
        }

    @Test
    fun themeChangesEmitAndPersistAcrossRepositoryInstances() =
        runTest {
            ThemeMode.entries.forEach { value ->
                val emission = async(start = CoroutineStart.UNDISPATCHED) { repository.themeMode.first { it == value } }
                repository.setThemeMode(value)
                assertEquals(value, emission.await())
                assertEquals(value, SettingsRepositoryImpl(context).themeMode.first())
                assertEquals(value.name, context.settingsDataStore.data.first()[THEME_MODE_KEY])
            }
        }

    @Test
    fun languageChangesEmitAndPersistTheirExistingTags() =
        runTest {
            AppLanguage.entries.forEach { value ->
                val emission = async(start = CoroutineStart.UNDISPATCHED) { repository.language.first { it == value } }
                repository.setLanguage(value)
                assertEquals(value, emission.await())
                assertEquals(value, SettingsRepositoryImpl(context).language.first())
                assertEquals(value.tag, context.settingsDataStore.data.first()[LANGUAGE_KEY])
            }
        }

    @Test
    fun unitChangesEmitAndPersistIndependently() =
        runTest {
            DistanceUnit.entries.forEach { value ->
                val emission = async(start = CoroutineStart.UNDISPATCHED) { repository.distanceUnit.first { it == value } }
                repository.setDistanceUnit(value)
                assertEquals(value, emission.await())
                assertEquals(value, SettingsRepositoryImpl(context).distanceUnit.first())
                assertEquals(value.name, context.settingsDataStore.data.first()[DISTANCE_UNIT_KEY])
                assertEquals(PaceUnit.MIN_PER_KM, repository.paceUnit.first())
                assertEquals(WeightUnit.KILOGRAMS, repository.weightUnit.first())
            }
            PaceUnit.entries.forEach { value ->
                val emission = async(start = CoroutineStart.UNDISPATCHED) { repository.paceUnit.first { it == value } }
                repository.setPaceUnit(value)
                assertEquals(value, emission.await())
                assertEquals(value, SettingsRepositoryImpl(context).paceUnit.first())
                assertEquals(value.name, context.settingsDataStore.data.first()[PACE_UNIT_KEY])
                assertEquals(DistanceUnit.entries.last(), repository.distanceUnit.first())
            }
            WeightUnit.entries.forEach { value ->
                val emission = async(start = CoroutineStart.UNDISPATCHED) { repository.weightUnit.first { it == value } }
                repository.setWeightUnit(value)
                assertEquals(value, emission.await())
                assertEquals(value, SettingsRepositoryImpl(context).weightUnit.first())
                assertEquals(value.name, context.settingsDataStore.data.first()[WEIGHT_UNIT_KEY])
                assertEquals(PaceUnit.entries.last(), repository.paceUnit.first())
                assertEquals(DistanceUnit.entries.last(), repository.distanceUnit.first())
            }
        }

    @Test
    fun replaceSettings_persistsAllBackupPreferencesTogether() =
        runTest {
            BackupSettingsDataSource(context).replace(
                SettingsSnapshot(
                    themeMode = ThemeMode.DARK,
                    language = AppLanguage.ENGLISH,
                    slotModePolicy = SlotModePolicy.ALWAYS_SHOW,
                    weekStartDay = WeekStartDay.FRIDAY,
                    distanceUnit = DistanceUnit.MILES,
                    paceUnit = PaceUnit.MIN_PER_MI,
                    weightUnit = WeightUnit.POUNDS,
                ),
            )

            val snapshot = BackupSettingsDataSource(context).snapshot()

            assertEquals(ThemeMode.DARK, snapshot.themeMode)
            assertEquals(AppLanguage.ENGLISH, snapshot.language)
            assertEquals(SlotModePolicy.ALWAYS_SHOW, snapshot.slotModePolicy)
            assertEquals(WeekStartDay.FRIDAY, snapshot.weekStartDay)
            assertEquals(DistanceUnit.MILES, snapshot.distanceUnit)
            assertEquals(PaceUnit.MIN_PER_MI, snapshot.paceUnit)
            assertEquals(WeightUnit.POUNDS, snapshot.weightUnit)
        }
}
