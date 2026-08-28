package com.rafaelfelipeac.hermes.features.backup.data

import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupCategoryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupChallengeProgressEntryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupChallengeRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError.INVALID_FIELD_VALUE
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError.UNSUPPORTED_SCHEMA_VERSION
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult.Failure
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupPersonalRecordEntryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupPersonalRecordFamilyRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSnapshot
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupUserActionRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupWorkoutRecord
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

@Suppress("ReturnCount")
internal object BackupJsonCodec {
    internal const val SCHEMA_VERSION_V1 = 1
    internal const val SCHEMA_VERSION_V2 = 2
    internal const val SCHEMA_VERSION_V3 = 3
    internal const val SCHEMA_VERSION_V4 = 4
    internal const val SCHEMA_VERSION_V5 = 5
    internal const val SUPPORTED_SCHEMA_VERSION = SCHEMA_VERSION_V5

    private val json =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }

    fun encode(snapshot: BackupSnapshot): String {
        return buildJsonObject {
            put(KEY_SCHEMA_VERSION, snapshot.schemaVersion)
            put(KEY_EXPORTED_AT, snapshot.exportedAt)

            snapshot.appVersion?.let { put(KEY_APP_VERSION, it) }

            putJsonArray(KEY_CHALLENGES) {
                snapshot.challenges.forEach { addChallenge(it) }
            }

            putJsonArray(KEY_CHALLENGE_PROGRESS_ENTRIES) {
                snapshot.challengeProgressEntries.forEach { addChallengeProgressEntry(it) }
            }

            putJsonArray(KEY_WORKOUTS) {
                snapshot.workouts.forEach { addWorkout(it) }
            }

            putJsonArray(KEY_CATEGORIES) {
                snapshot.categories.forEach { addCategory(it) }
            }

            putJsonArray(KEY_PERSONAL_RECORD_FAMILIES) {
                snapshot.personalRecordFamilies.forEach { addPersonalRecordFamily(it) }
            }

            putJsonArray(KEY_PERSONAL_RECORD_ENTRIES) {
                snapshot.personalRecordEntries.forEach { addPersonalRecordEntry(it) }
            }

            putJsonArray(KEY_USER_ACTIONS) {
                snapshot.userActions.forEach { addUserAction(it) }
            }

            snapshot.settings?.let { settings ->
                putJsonObject(KEY_SETTINGS) {
                    put(KEY_THEME_MODE, settings.themeMode)
                    put(KEY_LANGUAGE_TAG, settings.languageTag)
                    put(KEY_SLOT_MODE_POLICY, settings.slotModePolicy)
                    put(KEY_WEEK_START_DAY, settings.weekStartDay)
                    put(KEY_DISTANCE_UNIT, settings.distanceUnit)
                    put(KEY_PACE_UNIT, settings.paceUnit)
                    put(KEY_WEIGHT_UNIT, settings.weightUnit)
                }
            }
        }.toString()
    }

    fun decode(raw: String): BackupDecodeResult {
        val root =
            runCatching { json.parseToJsonElement(raw).jsonObject }
                .getOrElse { return Failure(BackupDecodeError.INVALID_JSON) }

        val schemaVersion =
            (root[KEY_SCHEMA_VERSION] as? JsonPrimitive)?.intOrNull
                ?: return Failure(INVALID_FIELD_VALUE)

        return when (schemaVersion) {
            SCHEMA_VERSION_V1 -> BackupV1Decoder.decode(root)
            SCHEMA_VERSION_V2 -> BackupV2Decoder.decode(root)
            SCHEMA_VERSION_V3 -> BackupV3Decoder.decode(root)
            SCHEMA_VERSION_V4 -> BackupV4Decoder.decode(root)
            SCHEMA_VERSION_V5 -> BackupV5Decoder.decode(root)
            else -> Failure(UNSUPPORTED_SCHEMA_VERSION)
        }
    }

    private fun JsonArrayBuilder.addChallenge(record: BackupChallengeRecord) {
        add(
            buildJsonObject {
                put(KEY_ID, record.id)
                put(KEY_TITLE, record.title)
                record.description?.let { put(KEY_DESCRIPTION, it) }
                put(KEY_TARGET_TYPE, record.targetType)
                put(KEY_TARGET_QUANTITY, record.targetQuantity)
                put(KEY_CREATED_AT, record.createdAt)
                put(KEY_UPDATED_AT, record.updatedAt)
                put(KEY_LIFECYCLE, record.lifecycle)
                put(KEY_START_DATE, record.startDate)
                put(KEY_END_DATE, record.endDate)
                record.archivedAt?.let { put(KEY_ARCHIVED_AT, it) }
            },
        )
    }

    private fun JsonArrayBuilder.addChallengeProgressEntry(record: BackupChallengeProgressEntryRecord) {
        add(
            buildJsonObject {
                put(KEY_ID, record.id)
                put(KEY_CHALLENGE_ID, record.challengeId)
                put(KEY_VALUE, record.quantity)
                put(KEY_ENTRY_DATE, record.entryDate)
                put(KEY_OCCURRED_AT, record.occurredAt)
                put(KEY_CREATED_AT, record.createdAt)
                put(KEY_UPDATED_AT, record.updatedAt)
            },
        )
    }

    private fun JsonArrayBuilder.addWorkout(record: BackupWorkoutRecord) {
        add(
            buildJsonObject {
                put(KEY_ID, record.id)
                put(KEY_WEEK_START_DATE, record.weekStartDate)
                record.dayOfWeek?.let { put(KEY_DAY_OF_WEEK, it) }
                record.timeSlot?.let { put(KEY_TIME_SLOT, it) }
                put(KEY_SORT_ORDER, record.sortOrder)
                put(KEY_EVENT_TYPE, record.eventType)
                put(KEY_TYPE, record.type)
                put(KEY_DESCRIPTION, record.description)
                put(KEY_IS_COMPLETED, record.isCompleted)
                record.categoryId?.let { put(KEY_CATEGORY_ID, it) }
            },
        )
    }

    private fun JsonArrayBuilder.addCategory(record: BackupCategoryRecord) {
        add(
            buildJsonObject {
                put(KEY_ID, record.id)
                put(KEY_NAME, record.name)
                put(KEY_COLOR_ID, record.colorId)
                put(KEY_SORT_ORDER, record.sortOrder)
                put(KEY_IS_HIDDEN, record.isHidden)
                put(KEY_IS_SYSTEM, record.isSystem)
            },
        )
    }

    private fun JsonArrayBuilder.addPersonalRecordFamily(record: BackupPersonalRecordFamilyRecord) {
        add(
            buildJsonObject {
                put(KEY_ID, record.id)
                record.categoryId?.let { put(KEY_CATEGORY_ID, it) }
                put(KEY_TITLE, record.title)
                put(KEY_METRIC_TYPE, record.metricType)
                put(KEY_DEFAULT_UNIT, record.defaultUnit)
                put(KEY_COMPARISON_RULE, record.comparisonRule)
                record.manualCurrentEntryId?.let { put(KEY_MANUAL_CURRENT_ENTRY_ID, it) }
                put(KEY_SORT_ORDER, record.sortOrder)
                put(KEY_CREATED_AT, record.createdAt)
                put(KEY_UPDATED_AT, record.updatedAt)
            },
        )
    }

    private fun JsonArrayBuilder.addPersonalRecordEntry(record: BackupPersonalRecordEntryRecord) {
        add(
            buildJsonObject {
                put(KEY_ID, record.id)
                put(KEY_FAMILY_ID, record.familyId)
                put(KEY_VALUE, record.value)
                put(KEY_UNIT, record.unit)
                record.customUnitLabel?.let { put(KEY_CUSTOM_UNIT_LABEL, it) }
                put(KEY_RECORD_DATE, record.recordDate)
                record.note?.let { put(KEY_NOTE, it) }
                put(KEY_CREATED_AT, record.createdAt)
                put(KEY_UPDATED_AT, record.updatedAt)
            },
        )
    }

    private fun JsonArrayBuilder.addUserAction(record: BackupUserActionRecord) {
        add(
            buildJsonObject {
                put(KEY_ID, record.id)
                put(KEY_ACTION_TYPE, record.actionType)
                put(KEY_ENTITY_TYPE, record.entityType)
                record.entityId?.let { put(KEY_ENTITY_ID, it) }
                record.metadata?.let { put(KEY_METADATA, it) }
                put(KEY_TIMESTAMP, record.timestamp)
            },
        )
    }
}
