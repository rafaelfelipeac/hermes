package com.rafaelfelipeac.hermes.features.backup.data

import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupCategoryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult
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
                schemaVersion = 1,
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
                    ),
            )

        val encoded = BackupJsonCodec.encode(snapshot)
        val decoded = BackupJsonCodec.decode(encoded)

        assertTrue(decoded is BackupDecodeResult.Success)
        val restored = (decoded as BackupDecodeResult.Success).snapshot
        assertEquals(snapshot.schemaVersion, restored.schemaVersion)
        assertEquals(snapshot.exportedAt, restored.exportedAt)
        assertEquals(snapshot.workouts.single().eventType, restored.workouts.single().eventType)
        assertEquals(snapshot.categories.single().name, restored.categories.single().name)
        assertEquals(snapshot.userActions.single().actionType, restored.userActions.single().actionType)
        assertEquals(snapshot.settings?.slotModePolicy, restored.settings?.slotModePolicy)
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
    fun decode_unsupportedSchema_returnsUnsupportedSchema() {
        val raw =
            """
            {
              "schemaVersion": 2,
              "exportedAt": "2026-02-25T10:00:00Z",
              "workouts": [],
              "categories": [],
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
              "schemaVersion": 1,
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
              "schemaVersion": 1,
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
}
