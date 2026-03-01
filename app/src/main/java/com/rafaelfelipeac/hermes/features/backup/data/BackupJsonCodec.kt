package com.rafaelfelipeac.hermes.features.backup.data

import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupCategoryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError.INVALID_FIELD_VALUE
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError.UNSUPPORTED_SCHEMA_VERSION
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult.Failure
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSnapshot
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupUserActionRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupWorkoutRecord
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

@Suppress("ReturnCount")
internal object BackupJsonCodec {
    internal const val SUPPORTED_SCHEMA_VERSION = 1

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

            putJsonArray(KEY_WORKOUTS) {
                snapshot.workouts.forEach { addWorkout(it) }
            }

            putJsonArray(KEY_CATEGORIES) {
                snapshot.categories.forEach { addCategory(it) }
            }

            putJsonArray(KEY_USER_ACTIONS) {
                snapshot.userActions.forEach { addUserAction(it) }
            }

            snapshot.settings?.let { settings ->
                putJsonObject(KEY_SETTINGS) {
                    put(KEY_THEME_MODE, settings.themeMode)
                    put(KEY_LANGUAGE_TAG, settings.languageTag)
                    put(KEY_SLOT_MODE_POLICY, settings.slotModePolicy)
                }
            }
        }.toString()
    }

    fun decode(raw: String): BackupDecodeResult {
        val root =
            runCatching { json.parseToJsonElement(raw).jsonObject }
                .getOrElse { return Failure(BackupDecodeError.INVALID_JSON) }

        val schemaVersion =
            root[KEY_SCHEMA_VERSION]?.jsonPrimitive?.intOrNull
                ?: return Failure(INVALID_FIELD_VALUE)

        return when (schemaVersion) {
            SUPPORTED_SCHEMA_VERSION -> BackupV1Decoder.decode(root)
            else -> Failure(UNSUPPORTED_SCHEMA_VERSION)
        }
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
