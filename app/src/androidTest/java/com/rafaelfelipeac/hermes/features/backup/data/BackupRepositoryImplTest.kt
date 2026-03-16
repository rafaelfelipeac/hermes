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
                buildImportBackupJson(
                    schemaVersion = BackupJsonCodec.SCHEMA_VERSION_V2,
                    weekStartDay = WeekStartDay.FRIDAY.name,
                )

            val result = repository.importBackupJson(raw)

            assertTrue(result is ImportBackupResult.Success)
            assertEquals(WeekStartDay.FRIDAY, settingsRepository.weekStartDay.first())
        }

    @Test
    fun importBackupJson_v1DefaultsMissingWeekStartDayToMonday() =
        runTest {
            settingsRepository.setWeekStartDay(WeekStartDay.SATURDAY)
            val raw =
                buildImportBackupJson(
                    schemaVersion = BackupJsonCodec.SCHEMA_VERSION_V1,
                    weekStartDay = null,
                )

            val result = repository.importBackupJson(raw)

            assertTrue(result is ImportBackupResult.Success)
            assertEquals(WeekStartDay.MONDAY, settingsRepository.weekStartDay.first())
        }
}

private fun buildImportBackupJson(
    schemaVersion: Int,
    weekStartDay: String?,
): String {
    val weekStartDayField =
        weekStartDay?.let { value ->
            """
            ,
                "$KEY_WEEK_START_DAY": "$value"
            """.trimIndent()
        }.orEmpty()

    return """
        {
          "$KEY_SCHEMA_VERSION": $schemaVersion,
          "$KEY_EXPORTED_AT": "$EXPORTED_AT",
          "$KEY_WORKOUTS": [],
          "$KEY_CATEGORIES": [],
          "$KEY_USER_ACTIONS": [],
          "$KEY_SETTINGS": {
            "$KEY_THEME_MODE": "$THEME_MODE_SYSTEM",
            "$KEY_LANGUAGE_TAG": "$LANGUAGE_TAG_ENGLISH",
            "$KEY_SLOT_MODE_POLICY": "$SLOT_MODE_POLICY_AUTO_WHEN_MULTIPLE"$weekStartDayField
          }
        }
        """.trimIndent()
}

private const val EXPORTED_AT = "2026-02-25T10:00:00Z"
private const val THEME_MODE_SYSTEM = "SYSTEM"
private const val LANGUAGE_TAG_ENGLISH = "en"
private const val SLOT_MODE_POLICY_AUTO_WHEN_MULTIPLE = "AUTO_WHEN_MULTIPLE"
