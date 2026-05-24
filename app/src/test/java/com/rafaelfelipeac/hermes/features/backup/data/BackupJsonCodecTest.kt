package com.rafaelfelipeac.hermes.features.backup.data

import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupCategoryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupKnowledgeNoteRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSettingsRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSnapshot
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupUserActionRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupWorkoutRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupJsonCodecTest {
    @Test
    @Suppress("LongMethod")
    fun encodeDecode_roundTrip_preservesCoreFields() {
        val snapshot =
            BackupSnapshot(
                schemaVersion = BackupJsonCodec.SCHEMA_VERSION_V4,
                exportedAt = "2026-02-25T10:00:00Z",
                appVersion = "1.3.0",
                workouts =
                    listOf(
                        BackupWorkoutRecord(
                            id = 10L,
                            weekStartDate = "2026-02-23",
                            dayOfWeek = 1,
                            timeSlot = "MORNING",
                            sortOrder = 0,
                            eventType = "WORKOUT",
                            type = "Run",
                            description = "Easy run",
                            isCompleted = false,
                            categoryId = 1L,
                        ),
                        BackupWorkoutRecord(
                            id = 11L,
                            weekStartDate = "2026-03-02",
                            dayOfWeek = 6,
                            timeSlot = null,
                            sortOrder = 0,
                            eventType = "RACE_EVENT",
                            type = "Race day",
                            description = "Half marathon",
                            isCompleted = false,
                            categoryId = 1L,
                        ),
                    ),
                notes =
                    listOf(
                        BackupKnowledgeNoteRecord(
                            id = 20L,
                            kind = "IMPORTANT",
                            status = "ACTIVE",
                            title = "Fueling",
                            body = "Take gel before the workout",
                            sourceWorkoutId = null,
                            sourceType = null,
                            sourceTitle = null,
                            categoryId = 1L,
                            triggerScope = "WORKOUT",
                            createdAt = 1_772_040_000_000L,
                            updatedAt = 1_772_040_500_000L,
                        ),
                        BackupKnowledgeNoteRecord(
                            id = 21L,
                            kind = "SESSION",
                            status = "ACTIVE",
                            title = null,
                            body = "Warm up is longer on hills",
                            sourceWorkoutId = 10L,
                            sourceType = "WORKOUT",
                            sourceTitle = "Morning Run",
                            categoryId = null,
                            triggerScope = null,
                            createdAt = 1_772_040_100_000L,
                            updatedAt = 1_772_040_100_000L,
                        ),
                    ),
                categories =
                    listOf(
                        BackupCategoryRecord(
                            id = 1L,
                            name = "Run",
                            colorId = "COLOR_RUN",
                            sortOrder = 0,
                            isHidden = false,
                            isSystem = true,
                        ),
                    ),
                userActions =
                    listOf(
                        BackupUserActionRecord(
                            id = 100L,
                            actionType = "CREATE_WORKOUT",
                            entityType = "WORKOUT",
                            entityId = 10L,
                            metadata = "{\"new_type\":\"Run\"}",
                            timestamp = 1_772_040_000_000L,
                        ),
                    ),
                settings =
                    BackupSettingsRecord(
                        themeMode = "DARK",
                        languageTag = "en",
                        slotModePolicy = "AUTO_WHEN_MULTIPLE",
                        weekStartDay = "WEDNESDAY",
                    ),
            )

        val encoded = BackupJsonCodec.encode(snapshot)
        val decoded = BackupJsonCodec.decode(encoded)

        assertTrue(decoded is BackupDecodeResult.Success)
        val restored = (decoded as BackupDecodeResult.Success).snapshot
        assertEquals(snapshot.schemaVersion, restored.schemaVersion)
        assertEquals(snapshot.exportedAt, restored.exportedAt)
        assertEquals("WORKOUT", restored.workouts.first().eventType)
        assertEquals("RACE_EVENT", restored.workouts.last().eventType)
        assertEquals(2, restored.notes.size)
        assertEquals("IMPORTANT", restored.notes.first().kind)
        assertEquals("SESSION", restored.notes.last().kind)
        assertEquals(snapshot.categories.single().name, restored.categories.single().name)
        assertEquals(snapshot.userActions.single().actionType, restored.userActions.single().actionType)
        assertEquals(snapshot.settings?.slotModePolicy, restored.settings?.slotModePolicy)
        assertEquals(snapshot.settings?.weekStartDay, restored.settings?.weekStartDay)
    }

    @Test
    fun decode_v2Backup_withRaceEventCompatibleData_stillWorks() {
        val raw =
            """
            {
              "schemaVersion": 2,
              "exportedAt": "2026-02-25T10:00:00Z",
              "workouts": [
                {
                  "id": 10,
                  "weekStartDate": "2026-02-23",
                  "dayOfWeek": 1,
                  "timeSlot": "MORNING",
                  "sortOrder": 0,
                  "eventType": "RACE_EVENT",
                  "type": "Race day",
                  "description": "Half marathon",
                  "isCompleted": false,
                  "categoryId": 1
                }
              ],
              "categories": [],
              "userActions": []
            }
            """.trimIndent()

        val result = BackupJsonCodec.decode(raw)

        assertTrue(result is BackupDecodeResult.Success)
        assertEquals("RACE_EVENT", (result as BackupDecodeResult.Success).snapshot.workouts.single().eventType)
    }

    @Test
    fun decode_missingRequiredSection_returnsMissingRequiredSection() {
        val raw =
            """
            {
              "schemaVersion": 1,
              "exportedAt": "2026-02-25T10:00:00Z",
              "categories": [],
              "userActions": []
            }
            """.trimIndent()

        val result = BackupJsonCodec.decode(raw)

        assertTrue(result is BackupDecodeResult.Failure)
        assertEquals(
            BackupDecodeError.MISSING_REQUIRED_SECTION,
            (result as BackupDecodeResult.Failure).error,
        )
    }

    @Test
    fun decode_v1Settings_defaultsWeekStartDayToMonday() {
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

        val result = BackupJsonCodec.decode(raw)

        assertTrue(result is BackupDecodeResult.Success)
        assertEquals(
            "MONDAY",
            (result as BackupDecodeResult.Success).snapshot.settings?.weekStartDay,
        )
    }

    @Test
    fun decode_unknownFutureSchema_returnsUnsupportedSchema() {
        val raw =
            """
            {
              "schemaVersion": 5,
              "exportedAt": "2026-02-25T10:00:00Z",
              "workouts": [],
              "categories": [],
              "notes": [],
              "userActions": []
            }
            """.trimIndent()

        val result = BackupJsonCodec.decode(raw)

        assertTrue(result is BackupDecodeResult.Failure)
        assertEquals(
            BackupDecodeError.UNSUPPORTED_SCHEMA_VERSION,
            (result as BackupDecodeResult.Failure).error,
        )
    }

    @Test
    fun decode_workoutsWrongType_returnsInvalidFieldValue() {
        val raw =
            """
            {
              "schemaVersion": 2,
              "exportedAt": "2026-02-25T10:00:00Z",
              "workouts": {},
              "categories": [],
              "userActions": []
            }
            """.trimIndent()

        val result = BackupJsonCodec.decode(raw)

        assertTrue(result is BackupDecodeResult.Failure)
        assertEquals(
            BackupDecodeError.INVALID_FIELD_VALUE,
            (result as BackupDecodeResult.Failure).error,
        )
    }

    @Test
    fun decode_settingsWrongType_returnsInvalidFieldValue() {
        val raw =
            """
            {
              "schemaVersion": 2,
              "exportedAt": "2026-02-25T10:00:00Z",
              "workouts": [],
              "categories": [],
              "userActions": [],
              "settings": []
            }
            """.trimIndent()

        val result = BackupJsonCodec.decode(raw)

        assertTrue(result is BackupDecodeResult.Failure)
        assertEquals(
            BackupDecodeError.INVALID_FIELD_VALUE,
            (result as BackupDecodeResult.Failure).error,
        )
    }

    @Test
    fun decode_v4MissingNotesSection_returnsMissingRequiredSection() {
        val raw =
            """
            {
              "schemaVersion": 4,
              "exportedAt": "2026-02-25T10:00:00Z",
              "workouts": [],
              "categories": [],
              "userActions": []
            }
            """.trimIndent()

        val result = BackupJsonCodec.decode(raw)

        assertTrue(result is BackupDecodeResult.Failure)
        assertEquals(
            BackupDecodeError.MISSING_REQUIRED_SECTION,
            (result as BackupDecodeResult.Failure).error,
        )
    }
}
