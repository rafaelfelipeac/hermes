package com.rafaelfelipeac.hermes.features.backup.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupResult
import com.rafaelfelipeac.hermes.features.knowledgebase.data.local.KnowledgeNoteEntity
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
                    knowledgeNoteDao = database.knowledgeNoteDao(),
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
    fun exportBackupJson_writesWeekStartDayInV4SettingsAndNotes() =
        runTest {
            settingsRepository.setWeekStartDay(WeekStartDay.WEDNESDAY)
            database.knowledgeNoteDao().insert(
                KnowledgeNoteEntity(
                    kind = "IMPORTANT",
                    status = "ACTIVE",
                    title = "Pace",
                    body = "Keep effort controlled",
                    sourceWorkoutId = null,
                    sourceType = null,
                    sourceTitle = null,
                    categoryId = null,
                    triggerScope = "WORKOUT",
                    createdAt = 1L,
                    updatedAt = 2L,
                ),
            )

            val exportResult = repository.exportBackupJson(TEST_APP_VERSION)

            assertTrue(exportResult.isSuccess)
            val decoded = BackupJsonCodec.decode(checkNotNull(exportResult.getOrNull()))
            assertTrue(decoded is com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult.Success)
            val snapshot =
                (decoded as com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult.Success).snapshot
            assertEquals(BackupJsonCodec.SCHEMA_VERSION_V4, snapshot.schemaVersion)
            assertEquals(1, snapshot.notes.size)
            assertEquals("IMPORTANT", snapshot.notes.single().kind)
            assertEquals("WORKOUT", snapshot.notes.single().triggerScope)
            assertEquals(WeekStartDay.WEDNESDAY.name, snapshot.settings?.weekStartDay)
        }

    @Test
    fun importBackupJson_v4RestoresWeekStartDayAndNotes() =
        runTest {
            val raw =
                buildImportBackupJson(
                    schemaVersion = BackupJsonCodec.SCHEMA_VERSION_V4,
                    weekStartDay = WeekStartDay.FRIDAY.name,
                    workoutsJson =
                        workoutArrayJson(
                            """
                        {
                          "$KEY_ID": 100,
                          "$KEY_WEEK_START_DATE": "2026-02-23",
                          "$KEY_SORT_ORDER": 0,
                          "$KEY_EVENT_TYPE": "WORKOUT",
                          "$KEY_TYPE": "Run",
                          "$KEY_DESCRIPTION": "Easy run",
                          "$KEY_IS_COMPLETED": false
                        }
                            """.trimIndent(),
                        ),
                    notesJson = noteArrayJson(
                        """
                        {
                          "$KEY_ID": 7,
                          "$KEY_KIND": "SESSION",
                          "$KEY_STATUS": "ACTIVE",
                          "$KEY_BODY": "Hydrate before the session",
                          "$KEY_SOURCE_WORKOUT_ID": 100,
                          "$KEY_SOURCE_TYPE": "WORKOUT",
                          "$KEY_SOURCE_TITLE": "Tempo",
                          "$KEY_CREATED_AT": 10,
                          "$KEY_UPDATED_AT": 10
                        }
                        """.trimIndent(),
                    ),
                )

            val result = repository.importBackupJson(raw)

            assertTrue(result is ImportBackupResult.Success)
            assertEquals(1, (result as ImportBackupResult.Success).notesCount)
            assertEquals(WeekStartDay.FRIDAY, settingsRepository.weekStartDay.first())
            val notes = database.knowledgeNoteDao().getAll()
            assertEquals(1, notes.size)
            assertEquals("SESSION", notes.single().kind)
            assertEquals(100L, notes.single().sourceWorkoutId)
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

    @Test
    fun importBackupJson_invalidNoteReferenceFails() =
        runTest {
            val raw =
                buildImportBackupJson(
                    schemaVersion = BackupJsonCodec.SCHEMA_VERSION_V4,
                    weekStartDay = WeekStartDay.FRIDAY.name,
                    workoutsJson = "[]",
                    notesJson =
                        noteArrayJson(
                            """
                            {
                              "$KEY_ID": 7,
                              "$KEY_KIND": "SESSION",
                              "$KEY_STATUS": "ACTIVE",
                              "$KEY_BODY": "Hydrate before the session",
                              "$KEY_SOURCE_WORKOUT_ID": 100,
                              "$KEY_SOURCE_TYPE": "WORKOUT",
                              "$KEY_CREATED_AT": 10,
                              "$KEY_UPDATED_AT": 10
                            }
                            """.trimIndent(),
                        ),
                )

            val result = repository.importBackupJson(raw)

            assertTrue(result is ImportBackupResult.Failure)
            assertEquals(
                com.rafaelfelipeac.hermes.features.backup.domain.repository.ImportBackupError.INVALID_REFERENCE,
                (result as ImportBackupResult.Failure).error,
            )
        }
}

private fun buildImportBackupJson(
    schemaVersion: Int,
    weekStartDay: String?,
    workoutsJson: String = "[]",
    notesJson: String = if (schemaVersion >= BackupJsonCodec.SCHEMA_VERSION_V4) "[]" else "",
): String {
    return buildString {
        appendLine("{")
        appendLine("""  "$KEY_SCHEMA_VERSION": $schemaVersion,""")
        appendLine("""  "$KEY_EXPORTED_AT": "$EXPORTED_AT",""")
        appendLine("""  "$KEY_WORKOUTS": $workoutsJson,""")
        if (schemaVersion >= BackupJsonCodec.SCHEMA_VERSION_V4) {
            appendLine("""  "$KEY_NOTES": $notesJson,""")
        }
        appendLine("""  "$KEY_CATEGORIES": [],""")
        appendLine("""  "$KEY_USER_ACTIONS": [],""")
        appendLine("""  "$KEY_SETTINGS": {""")
        appendLine("""    "$KEY_THEME_MODE": "$THEME_MODE_SYSTEM",""")
        appendLine("""    "$KEY_LANGUAGE_TAG": "$LANGUAGE_TAG_ENGLISH",""")
        appendLine("    \"$KEY_SLOT_MODE_POLICY\": \"$SLOT_MODE_POLICY_AUTO_WHEN_MULTIPLE\",")
        if (weekStartDay != null) {
            appendLine("    \"$KEY_WEEK_START_DAY\": \"$weekStartDay\"")
        }
        appendLine("  }")
        appendLine("}")
    }
}

private fun noteArrayJson(noteJson: String): String {
    return "[\n$noteJson\n]"
}

private fun workoutArrayJson(workoutJson: String): String {
    return "[\n$workoutJson\n]"
}

private const val EXPORTED_AT = "2026-02-25T10:00:00Z"
private const val TEST_APP_VERSION = "1.5.0"
private const val THEME_MODE_SYSTEM = "SYSTEM"
private const val LANGUAGE_TAG_ENGLISH = "en"
private const val SLOT_MODE_POLICY_AUTO_WHEN_MULTIPLE = "AUTO_WHEN_MULTIPLE"
