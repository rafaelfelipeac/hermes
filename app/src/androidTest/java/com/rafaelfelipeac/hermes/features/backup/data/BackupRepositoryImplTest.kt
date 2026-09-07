package com.rafaelfelipeac.hermes.features.backup.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupChallengeProgressEntryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupChallengeRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupPersonalRecordEntryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupPersonalRecordFamilyRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSettingsRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSnapshot
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupUserActionRecord
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupError
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupResult
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryEntity
import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeEntity
import com.rafaelfelipeac.hermes.features.challenges.data.local.ChallengeProgressEntryEntity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordEntryEntity
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordFamilyEntity
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit
import com.rafaelfelipeac.hermes.features.settings.data.SettingsRepositoryImpl
import com.rafaelfelipeac.hermes.features.settings.data.settingsDataStore
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
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
                    challengeDao = database.challengeDao(),
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
    fun exportBackupJson_writesPersonalRecordsAndUnitPreferencesInV5() =
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
            assertEquals(BackupJsonCodec.SCHEMA_VERSION_V6, snapshot.schemaVersion)
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

    @Test
    fun importBackupJson_rejectsChallengeProgressAggregateOverflowBeforeWriting() =
        runTest {
            val raw =
                """
                {
                  "$KEY_SCHEMA_VERSION": ${BackupJsonCodec.SCHEMA_VERSION_V6},
                  "$KEY_EXPORTED_AT": "$EXPORTED_AT",
                  "$KEY_WORKOUTS": [],
                  "$KEY_CATEGORIES": [],
                  "$KEY_USER_ACTIONS": [],
                  "$KEY_PERSONAL_RECORD_FAMILIES": [],
                  "$KEY_PERSONAL_RECORD_ENTRIES": [],
                  "$KEY_CHALLENGES": [
                    {
                      "$KEY_ID": 1,
                      "$KEY_CATEGORY_ID": null,
                      "$KEY_TITLE": "Overflow target",
                      "$KEY_DESCRIPTION": null,
                      "$KEY_TARGET_TYPE": "TOTAL",
                      "$KEY_TARGET_QUANTITY": $LONG_MAX_VALUE,
                      "$KEY_START_DATE": "2026-08-01",
                      "$KEY_END_DATE": "2026-08-02",
                      "$KEY_LIFECYCLE": "ACTIVE",
                      "$KEY_ARCHIVED_AT": null,
                      "$KEY_CREATED_AT": "2026-08-01T10:00:00Z",
                      "$KEY_UPDATED_AT": "2026-08-01T10:00:00Z"
                    }
                  ],
                  "$KEY_CHALLENGE_PROGRESS_ENTRIES": [
                    {
                      "$KEY_ID": 1,
                      "$KEY_CHALLENGE_ID": 1,
                      "$KEY_VALUE": $LONG_MAX_VALUE,
                      "$KEY_ENTRY_DATE": "2026-08-01",
                      "$KEY_OCCURRED_AT": "2026-08-01T11:00:00Z",
                      "$KEY_CREATED_AT": "2026-08-01T11:00:00Z",
                      "$KEY_UPDATED_AT": "2026-08-01T11:00:00Z"
                    },
                    {
                      "$KEY_ID": 2,
                      "$KEY_CHALLENGE_ID": 1,
                      "$KEY_VALUE": 1,
                      "$KEY_ENTRY_DATE": "2026-08-02",
                      "$KEY_OCCURRED_AT": "2026-08-02T11:00:00Z",
                      "$KEY_CREATED_AT": "2026-08-02T11:00:00Z",
                      "$KEY_UPDATED_AT": "2026-08-02T11:00:00Z"
                    }
                  ],
                  "$KEY_SETTINGS": {
                    "$KEY_THEME_MODE": "$THEME_MODE_SYSTEM",
                    "$KEY_LANGUAGE_TAG": "$LANGUAGE_TAG_ENGLISH",
                    "$KEY_SLOT_MODE_POLICY": "$SLOT_MODE_POLICY_AUTO_WHEN_MULTIPLE"
                  }
                }
                """.trimIndent()

            val result = repository.importBackupJson(raw)

            assertEquals(ImportBackupResult.Failure(ImportBackupError.INVALID_FIELD_VALUE), result)
            assertTrue(database.challengeDao().getAllChallenges().isEmpty())
            assertTrue(database.challengeDao().getAllProgressEntries().isEmpty())
        }

    @Test
    fun importBackupJson_rollsBackDatabaseChangesWhenWriteFailsMidTransaction() =
        runTest {
            seedBaselineData()
            database.openHelper.writableDatabase.execSQL(
                """
                CREATE TRIGGER backup_import_fail_on_user_actions
                AFTER INSERT ON user_actions
                BEGIN
                    SELECT RAISE(ABORT, 'backup import failure');
                END
                """.trimIndent(),
            )

            val raw =
                BackupJsonCodec.encode(
                    BackupSnapshot(
                        schemaVersion = BackupJsonCodec.SCHEMA_VERSION_V6,
                        exportedAt = EXPORTED_AT,
                        challenges =
                            listOf(
                                BackupChallengeRecord(
                                    id = 2L,
                                    categoryId = null,
                                    title = "Imported challenge",
                                    description = null,
                                    targetType = ChallengeTargetType.TOTAL.name,
                                    targetQuantity = 10L,
                                    startDate = "2026-02-01",
                                    endDate = "2026-02-28",
                                    lifecycle = ChallengeLifecycle.ACTIVE.name,
                                    archivedAt = null,
                                    createdAt = "2026-02-01T10:00:00Z",
                                    updatedAt = "2026-02-01T10:00:00Z",
                                ),
                            ),
                        challengeProgressEntries =
                            listOf(
                                BackupChallengeProgressEntryRecord(
                                    id = 2L,
                                    challengeId = 2L,
                                    quantity = 3L,
                                    entryDate = "2026-02-02",
                                    occurredAt = "2026-02-02T11:00:00Z",
                                    createdAt = "2026-02-02T11:00:00Z",
                                    updatedAt = "2026-02-02T11:00:00Z",
                                ),
                            ),
                        workouts = emptyList(),
                        categories = emptyList(),
                        personalRecordFamilies = emptyList(),
                        personalRecordEntries = emptyList(),
                        userActions =
                            listOf(
                                BackupUserActionRecord(
                                    id = 1L,
                                    actionType = "CREATE_WORKOUT",
                                    entityType = "WORKOUT",
                                    entityId = 1L,
                                    metadata = null,
                                    timestamp = 1740456000000,
                                ),
                            ),
                        settings =
                            BackupSettingsRecord(
                                themeMode = ThemeMode.SYSTEM.name,
                                languageTag = "en",
                                slotModePolicy = com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.AUTO_WHEN_MULTIPLE.name,
                                weekStartDay = WeekStartDay.FRIDAY.name,
                                distanceUnit = DistanceUnit.MILES.name,
                                paceUnit = PaceUnit.MIN_PER_MI.name,
                                weightUnit = WeightUnit.POUNDS.name,
                            ),
                    ),
                )

            val result = repository.importBackupJson(raw)

            assertEquals(ImportBackupResult.Failure(ImportBackupError.WRITE_FAILED), result)
            assertEquals(1, database.challengeDao().getAllChallenges().size)
            assertEquals(1, database.challengeDao().getAllProgressEntries().size)
            assertEquals(1, database.categoryDao().getCategories().size)
            assertEquals(1, database.workoutDao().getAll().size)
            assertEquals(1, database.personalRecordDao().getFamilies().size)
            assertEquals(1, database.personalRecordDao().getEntries().size)
        }

    @Test
    fun importBackupJson_keepsCoreDataWhenSettingsRestoreFails() =
        runTest {
            val failingSettingsRepository = FailingSettingsRepository(SettingsRepositoryImpl(context))
            val repositoryWithFailingSettings =
                BackupRepositoryImpl(
                    database = database,
                    challengeDao = database.challengeDao(),
                    workoutDao = database.workoutDao(),
                    categoryDao = database.categoryDao(),
                    userActionDao = database.userActionDao(),
                    personalRecordDao = database.personalRecordDao(),
                    settingsRepository = failingSettingsRepository,
                )

            val raw =
                BackupJsonCodec.encode(
                    BackupSnapshot(
                        schemaVersion = BackupJsonCodec.SCHEMA_VERSION_V6,
                        exportedAt = EXPORTED_AT,
                        challenges = emptyList(),
                        challengeProgressEntries = emptyList(),
                        workouts = emptyList(),
                        categories = emptyList(),
                        personalRecordFamilies =
                            listOf(
                                BackupPersonalRecordFamilyRecord(
                                    id = 1L,
                                    categoryId = null,
                                    title = "5K PR",
                                    metricType = PersonalRecordMetricType.DISTANCE.name,
                                    defaultUnit = PersonalRecordUnit.MILE.name,
                                    comparisonRule = PersonalRecordComparisonRule.LOWER_IS_BETTER.name,
                                    manualCurrentEntryId = 1L,
                                    sortOrder = 0,
                                    createdAt = "2026-02-25T10:00:00Z",
                                    updatedAt = "2026-02-25T10:00:00Z",
                                ),
                            ),
                        personalRecordEntries =
                            listOf(
                                BackupPersonalRecordEntryRecord(
                                    id = 1L,
                                    familyId = 1L,
                                    value = 20.5,
                                    unit = PersonalRecordUnit.MILE.name,
                                    customUnitLabel = null,
                                    recordDate = "2026-02-24",
                                    note = "All-out effort",
                                    createdAt = "2026-02-25T10:15:00Z",
                                    updatedAt = "2026-02-25T10:15:00Z",
                                ),
                            ),
                        userActions = emptyList(),
                        settings =
                            BackupSettingsRecord(
                                themeMode = ThemeMode.SYSTEM.name,
                                languageTag = "en",
                                slotModePolicy =
                                    com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.AUTO_WHEN_MULTIPLE.name,
                                weekStartDay = WeekStartDay.FRIDAY.name,
                                distanceUnit = DistanceUnit.MILES.name,
                                paceUnit = PaceUnit.MIN_PER_MI.name,
                                weightUnit = WeightUnit.POUNDS.name,
                            ),
                    ),
                )

            val result = repositoryWithFailingSettings.importBackupJson(raw)

            assertTrue(result is ImportBackupResult.Success)
            assertEquals(WeekStartDay.MONDAY, failingSettingsRepository.delegate.weekStartDay.first())
            assertEquals(DistanceUnit.KILOMETERS, failingSettingsRepository.delegate.distanceUnit.first())
            assertEquals(1, database.personalRecordDao().getFamilies().size)
            assertEquals(1, database.personalRecordDao().getEntries().size)
        }

    private suspend fun seedBaselineData() {
        database.challengeDao().insertChallenges(
            listOf(
                ChallengeEntity(
                    id = 1L,
                    title = "Before import",
                    description = null,
                    targetType = ChallengeTargetType.TOTAL,
                    targetQuantity = 10L,
                    categoryId = null,
                    startDate = LocalDate.parse("2026-02-01"),
                    endDate = LocalDate.parse("2026-02-28"),
                    lifecycle = ChallengeLifecycle.ACTIVE,
                    archivedAt = null,
                    createdAt = Instant.parse("2026-02-01T10:00:00Z").toEpochMilli(),
                    updatedAt = Instant.parse("2026-02-01T10:00:00Z").toEpochMilli(),
                ),
            ),
        )
        database.challengeDao().insertProgressEntries(
            listOf(
                ChallengeProgressEntryEntity(
                    id = 1L,
                    challengeId = 1L,
                    quantity = 5L,
                    entryDate = LocalDate.parse("2026-02-02"),
                    occurredAt = Instant.parse("2026-02-02T10:00:00Z").toEpochMilli(),
                    createdAt = Instant.parse("2026-02-02T10:00:00Z").toEpochMilli(),
                    updatedAt = Instant.parse("2026-02-02T10:00:00Z").toEpochMilli(),
                ),
            ),
        )
        database.categoryDao().insertAll(
            listOf(
                CategoryEntity(
                    id = 1L,
                    name = "Run",
                    colorId = "COLOR_RUN",
                    sortOrder = 0,
                    isHidden = false,
                    isSystem = true,
                ),
            ),
        )
        database.workoutDao().insertAllReplace(
            listOf(
                WorkoutEntity(
                    id = 1L,
                    weekStartDate = LocalDate.parse("2026-02-02"),
                    dayOfWeek = 1,
                    type = "Run",
                    description = "",
                    isCompleted = false,
                    isRestDay = false,
                    eventType = EventType.WORKOUT.name,
                    timeSlot = null,
                    categoryId = null,
                    sortOrder = 0,
                ),
            ),
        )
        database.personalRecordDao().insertFamily(
            PersonalRecordFamilyEntity(
                categoryId = null,
                title = "Baseline PR",
                metricType = PersonalRecordMetricType.DISTANCE,
                defaultUnit = PersonalRecordUnit.MILE,
                comparisonRule = PersonalRecordComparisonRule.LOWER_IS_BETTER,
                manualCurrentEntryId = null,
                sortOrder = 0,
                createdAt = Instant.parse("2026-02-01T10:00:00Z").toEpochMilli(),
                updatedAt = Instant.parse("2026-02-01T10:00:00Z").toEpochMilli(),
            ),
        ).let { familyId ->
            database.personalRecordDao().insertEntry(
                PersonalRecordEntryEntity(
                    familyId = familyId,
                    value = 20.5,
                    unit = PersonalRecordUnit.MILE,
                    customUnitLabel = null,
                    recordDate = LocalDate.parse("2026-02-02"),
                    note = null,
                    createdAt = Instant.parse("2026-02-02T10:00:00Z").toEpochMilli(),
                    updatedAt = Instant.parse("2026-02-02T10:00:00Z").toEpochMilli(),
                ),
            )
        }
    }

    private class FailingSettingsRepository(
        val delegate: SettingsRepository,
    ) : SettingsRepository by delegate {
        override suspend fun setThemeMode(mode: ThemeMode) {
            throw IllegalStateException("settings restore failed")
        }
    }
}

private fun buildImportBackupJson(
    schemaVersion: Int,
    weekStartDay: String?,
    distanceUnit: String?,
    paceUnit: String?,
    weightUnit: String?,
    includePersonalRecords: Boolean,
    includeUserActions: Boolean = false,
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
    val userActionsField =
        if (includeUserActions) {
            """
              ,
            "$KEY_USER_ACTIONS": [
              {
                "$KEY_ID": 1,
                "$KEY_ACTION_TYPE": "CREATE_WORKOUT",
                "$KEY_ENTITY_TYPE": "WORKOUT",
                "$KEY_ENTITY_ID": 1,
                "$KEY_METADATA": null,
                "$KEY_TIMESTAMP": 1740456000000
              }
            ]
            """.trimIndent()
        } else {
            """
            ,
                "$KEY_USER_ACTIONS": []
            """.trimIndent()
        }

    return """
        {
          "$KEY_SCHEMA_VERSION": $schemaVersion,
          "$KEY_EXPORTED_AT": "$EXPORTED_AT",
          "$KEY_WORKOUTS": [],
          "$KEY_CATEGORIES": []$userActionsField$personalRecordFamiliesField$personalRecordEntriesField,
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
private const val LONG_MAX_VALUE = Long.MAX_VALUE
