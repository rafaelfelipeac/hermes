package com.rafaelfelipeac.hermes.features.backup.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupResult
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordEntryEntity
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordFamilyEntity
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit
import com.rafaelfelipeac.hermes.features.settings.data.SettingsRepositoryImpl
import com.rafaelfelipeac.hermes.features.settings.data.settingsDataStore
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate

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
                    personalRecordDao = database.personalRecordDao(),
                    settingsRepository = settingsRepository,
                )
        }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun exportBackupJson_writesPersonalRecordsAndUnitPreferencesInV4() =
        runTest {
            settingsRepository.setDistanceUnit(DistanceUnit.MILES)
            settingsRepository.setPaceUnit(PaceUnit.MIN_PER_MI)
            settingsRepository.setWeightUnit(WeightUnit.POUNDS)
            settingsRepository.setWeekStartDay(WeekStartDay.WEDNESDAY)

            val familyId =
                database.personalRecordDao().insertFamily(
                    PersonalRecordFamilyEntity(
                        categoryId = null,
                        title = "5K PR",
                        metricType = PersonalRecordMetricType.DISTANCE,
                        defaultUnit = PersonalRecordUnit.MILE,
                        comparisonRule = PersonalRecordComparisonRule.LOWER_IS_BETTER,
                        manualCurrentEntryId = null,
                        sortOrder = 0,
                        createdAt = Instant.parse("2026-02-25T10:00:00Z").toEpochMilli(),
                        updatedAt = Instant.parse("2026-02-25T10:00:00Z").toEpochMilli(),
                    ),
                )
            database.personalRecordDao().insertEntry(
                PersonalRecordEntryEntity(
                    familyId = familyId,
                    value = 20.5,
                    unit = PersonalRecordUnit.MILE,
                    customUnitLabel = null,
                    recordDate = LocalDate.parse("2026-02-24"),
                    note = "All-out effort",
                    createdAt = Instant.parse("2026-02-25T10:15:00Z").toEpochMilli(),
                    updatedAt = Instant.parse("2026-02-25T10:15:00Z").toEpochMilli(),
                ),
            )

            val exportResult = repository.exportBackupJson(TEST_APP_VERSION)

            assertTrue(exportResult.isSuccess)
            val decoded = BackupJsonCodec.decode(checkNotNull(exportResult.getOrNull()))
            assertTrue(decoded is com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult.Success)
            val snapshot =
                (decoded as com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult.Success).snapshot
            assertEquals(BackupJsonCodec.SCHEMA_VERSION_V4, snapshot.schemaVersion)
            assertEquals(1, snapshot.personalRecordFamilies.size)
            assertEquals(1, snapshot.personalRecordEntries.size)
            assertEquals(WeekStartDay.WEDNESDAY.name, snapshot.settings?.weekStartDay)
            assertEquals(DistanceUnit.MILES.name, snapshot.settings?.distanceUnit)
            assertEquals(PaceUnit.MIN_PER_MI.name, snapshot.settings?.paceUnit)
            assertEquals(WeightUnit.POUNDS.name, snapshot.settings?.weightUnit)
        }

    @Test
    fun importBackupJson_v4RestoresPersonalRecordsAndUnitPreferences() =
        runTest {
            val raw =
                buildImportBackupJson(
                    schemaVersion = BackupJsonCodec.SCHEMA_VERSION_V4,
                    weekStartDay = WeekStartDay.FRIDAY.name,
                    distanceUnit = DistanceUnit.MILES.name,
                    paceUnit = PaceUnit.MIN_PER_MI.name,
                    weightUnit = WeightUnit.POUNDS.name,
                    includePersonalRecords = true,
                )

            val result = repository.importBackupJson(raw)

            assertTrue(result is ImportBackupResult.Success)
            assertEquals(WeekStartDay.FRIDAY, settingsRepository.weekStartDay.first())
            assertEquals(DistanceUnit.MILES, settingsRepository.distanceUnit.first())
            assertEquals(PaceUnit.MIN_PER_MI, settingsRepository.paceUnit.first())
            assertEquals(WeightUnit.POUNDS, settingsRepository.weightUnit.first())
            assertEquals(1, database.personalRecordDao().getFamilies().size)
            assertEquals(1, database.personalRecordDao().getEntries().size)
        }

    @Test
    fun importBackupJson_v3DefaultsUnitPreferencesAndClearsPersonalRecords() =
        runTest {
            val familyId =
                database.personalRecordDao().insertFamily(
                    PersonalRecordFamilyEntity(
                        categoryId = null,
                        title = "Old PR",
                        metricType = PersonalRecordMetricType.WEIGHT,
                        defaultUnit = PersonalRecordUnit.KILOGRAM,
                        comparisonRule = PersonalRecordComparisonRule.HIGHER_IS_BETTER,
                        manualCurrentEntryId = null,
                        sortOrder = 0,
                        createdAt = Instant.parse("2026-02-24T10:00:00Z").toEpochMilli(),
                        updatedAt = Instant.parse("2026-02-24T10:00:00Z").toEpochMilli(),
                    ),
                )
            database.personalRecordDao().insertEntry(
                PersonalRecordEntryEntity(
                    familyId = familyId,
                    value = 100.0,
                    unit = PersonalRecordUnit.KILOGRAM,
                    customUnitLabel = null,
                    recordDate = LocalDate.parse("2026-02-23"),
                    note = null,
                    createdAt = Instant.parse("2026-02-24T10:15:00Z").toEpochMilli(),
                    updatedAt = Instant.parse("2026-02-24T10:15:00Z").toEpochMilli(),
                ),
            )
            settingsRepository.setDistanceUnit(DistanceUnit.MILES)
            settingsRepository.setPaceUnit(PaceUnit.MIN_PER_MI)
            settingsRepository.setWeightUnit(WeightUnit.POUNDS)
            settingsRepository.setWeekStartDay(WeekStartDay.SATURDAY)
            val raw =
                buildImportBackupJson(
                    schemaVersion = BackupJsonCodec.SCHEMA_VERSION_V3,
                    weekStartDay = WeekStartDay.FRIDAY.name,
                    distanceUnit = null,
                    paceUnit = null,
                    weightUnit = null,
                    includePersonalRecords = false,
                )

            val result = repository.importBackupJson(raw)

            assertTrue(result is ImportBackupResult.Success)
            assertEquals(WeekStartDay.FRIDAY, settingsRepository.weekStartDay.first())
            assertEquals(DistanceUnit.KILOMETERS, settingsRepository.distanceUnit.first())
            assertEquals(PaceUnit.MIN_PER_KM, settingsRepository.paceUnit.first())
            assertEquals(WeightUnit.KILOGRAMS, settingsRepository.weightUnit.first())
            assertTrue(database.personalRecordDao().getFamilies().isEmpty())
            assertTrue(database.personalRecordDao().getEntries().isEmpty())
        }
}

private fun buildImportBackupJson(
    schemaVersion: Int,
    weekStartDay: String?,
    distanceUnit: String?,
    paceUnit: String?,
    weightUnit: String?,
    includePersonalRecords: Boolean,
): String {
    val weekStartDayField =
        weekStartDay?.let { value ->
            """
            ,
                "$KEY_WEEK_START_DAY": "$value"
            """.trimIndent()
        }.orEmpty()
    val unitFields =
        buildString {
            distanceUnit?.let { append(",\n                \"$KEY_DISTANCE_UNIT\": \"$it\"") }
            paceUnit?.let { append(",\n                \"$KEY_PACE_UNIT\": \"$it\"") }
            weightUnit?.let { append(",\n                \"$KEY_WEIGHT_UNIT\": \"$it\"") }
        }
    val personalRecordFamiliesField =
        if (includePersonalRecords) {
            """
              ,
            "$KEY_PERSONAL_RECORD_FAMILIES": [
              {
                "$KEY_ID": 1,
                "$KEY_CATEGORY_ID": null,
                "$KEY_TITLE": "5K PR",
                "$KEY_METRIC_TYPE": "DISTANCE",
                "$KEY_DEFAULT_UNIT": "MILE",
                "$KEY_COMPARISON_RULE": "LOWER_IS_BETTER",
                "$KEY_MANUAL_CURRENT_ENTRY_ID": 1,
                "$KEY_SORT_ORDER": 0,
                "$KEY_CREATED_AT": "2026-02-25T10:00:00Z",
                "$KEY_UPDATED_AT": "2026-02-25T10:00:00Z"
              }
            ]
            """.trimIndent()
        } else {
            ""
        }
    val personalRecordEntriesField =
        if (includePersonalRecords) {
            """
              ,
            "$KEY_PERSONAL_RECORD_ENTRIES": [
              {
                "$KEY_ID": 1,
                "$KEY_FAMILY_ID": 1,
                "$KEY_VALUE": 20.5,
                "$KEY_UNIT": "MILE",
                "$KEY_CUSTOM_UNIT_LABEL": null,
                "$KEY_RECORD_DATE": "2026-02-24",
                "$KEY_NOTE": "All-out effort",
                "$KEY_CREATED_AT": "2026-02-25T10:15:00Z",
                "$KEY_UPDATED_AT": "2026-02-25T10:15:00Z"
              }
            ]
            """.trimIndent()
        } else {
            ""
        }

    return """
        {
          "$KEY_SCHEMA_VERSION": $schemaVersion,
          "$KEY_EXPORTED_AT": "$EXPORTED_AT",
          "$KEY_WORKOUTS": [],
          "$KEY_CATEGORIES": [],
          "$KEY_USER_ACTIONS": []$personalRecordFamiliesField$personalRecordEntriesField,
          "$KEY_SETTINGS": {
            "$KEY_THEME_MODE": "$THEME_MODE_SYSTEM",
            "$KEY_LANGUAGE_TAG": "$LANGUAGE_TAG_ENGLISH",
            "$KEY_SLOT_MODE_POLICY": "$SLOT_MODE_POLICY_AUTO_WHEN_MULTIPLE"$weekStartDayField
            $unitFields
          }
        }
        """.trimIndent()
}

private const val EXPORTED_AT = "2026-02-25T10:00:00Z"
private const val TEST_APP_VERSION = "1.5.0"
private const val THEME_MODE_SYSTEM = "SYSTEM"
private const val LANGUAGE_TAG_ENGLISH = "en"
private const val SLOT_MODE_POLICY_AUTO_WHEN_MULTIPLE = "AUTO_WHEN_MULTIPLE"
