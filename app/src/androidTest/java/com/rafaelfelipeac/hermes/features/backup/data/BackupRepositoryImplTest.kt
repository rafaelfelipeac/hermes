package com.rafaelfelipeac.hermes.features.backup.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupResult
import com.rafaelfelipeac.hermes.features.settings.data.SettingsRepositoryImpl
import com.rafaelfelipeac.hermes.features.settings.data.settingsDataStore
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupRepositoryImplTest {
    private lateinit var context: Context
    private lateinit var database: HermesDatabase
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var repository: BackupRepositoryImpl

    @Before
    fun setUp() =
        runTest {
            context = ApplicationProvider.getApplicationContext()
            context.settingsDataStore.edit { it.clear() }
            database =
                Room.inMemoryDatabaseBuilder(context, HermesDatabase::class.java)
                    .allowMainThreadQueries()
                    .build()
            settingsRepository = SettingsRepositoryImpl(context)
            repository =
                BackupRepositoryImpl(
                    database = database,
                    workoutDao = database.workoutDao(),
                    categoryDao = database.categoryDao(),
                    userActionDao = database.userActionDao(),
                    settingsRepository = settingsRepository,
                )
        }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun exportBackupJson_writesWeekStartDayInV2Settings() =
        runTest {
            settingsRepository.setWeekStartDay(WeekStartDay.WEDNESDAY)

            val exportResult = repository.exportBackupJson("1.5.0")

            assertTrue(exportResult.isSuccess)
            val decoded = BackupJsonCodec.decode(checkNotNull(exportResult.getOrNull()))
            assertTrue(decoded is com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult.Success)
            val snapshot =
                (decoded as com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult.Success).snapshot
            assertEquals(BackupJsonCodec.SCHEMA_VERSION_V2, snapshot.schemaVersion)
            assertEquals(WeekStartDay.WEDNESDAY.name, snapshot.settings?.weekStartDay)
        }

    @Test
    fun importBackupJson_v2RestoresWeekStartDay() =
        runTest {
            val raw =
                """
                {
                  "schemaVersion": 2,
                  "exportedAt": "2026-02-25T10:00:00Z",
                  "workouts": [],
                  "categories": [],
                  "userActions": [],
                  "settings": {
                    "themeMode": "SYSTEM",
                    "languageTag": "en",
                    "slotModePolicy": "AUTO_WHEN_MULTIPLE",
                    "weekStartDay": "FRIDAY"
                  }
                }
                """.trimIndent()

            val result = repository.importBackupJson(raw)

            assertTrue(result is ImportBackupResult.Success)
            assertEquals(WeekStartDay.FRIDAY, settingsRepository.weekStartDay.first())
        }

    @Test
    fun importBackupJson_v1DefaultsMissingWeekStartDayToMonday() =
        runTest {
            settingsRepository.setWeekStartDay(WeekStartDay.SATURDAY)
            val raw =
                """
                {
                  "schemaVersion": 1,
                  "exportedAt": "2026-02-25T10:00:00Z",
                  "workouts": [],
                  "categories": [],
                  "userActions": [],
                  "settings": {
                    "themeMode": "SYSTEM",
                    "languageTag": "en",
                    "slotModePolicy": "AUTO_WHEN_MULTIPLE"
                  }
                }
                """.trimIndent()

            val result = repository.importBackupJson(raw)

            assertTrue(result is ImportBackupResult.Success)
            assertEquals(WeekStartDay.MONDAY, settingsRepository.weekStartDay.first())
        }
}
