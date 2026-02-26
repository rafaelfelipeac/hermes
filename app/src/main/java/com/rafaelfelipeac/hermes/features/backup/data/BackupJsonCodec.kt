package com.rafaelfelipeac.hermes.features.backup.data

import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupCategoryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError.INVALID_FIELD_VALUE
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError.MISSING_REQUIRED_SECTION
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError.UNSUPPORTED_SCHEMA_VERSION
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult.Failure
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult.Success
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSettingsRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSnapshot
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupUserActionRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupWorkoutRecord
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
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
            root.intOrNull(KEY_SCHEMA_VERSION)
                ?: return Failure(INVALID_FIELD_VALUE)

        if (schemaVersion != SUPPORTED_SCHEMA_VERSION) {
            return Failure(UNSUPPORTED_SCHEMA_VERSION)
        }

        val exportedAt =
            root.stringOrNull(KEY_EXPORTED_AT)
                ?: return Failure(INVALID_FIELD_VALUE)

        val workoutsJson =
            root.arrayOrNull(KEY_WORKOUTS)
                ?: return Failure(MISSING_REQUIRED_SECTION)

        val categoriesJson =
            root.arrayOrNull(KEY_CATEGORIES)
                ?: return Failure(MISSING_REQUIRED_SECTION)

        val userActionsJson =
            root.arrayOrNull(KEY_USER_ACTIONS)
                ?: return Failure(MISSING_REQUIRED_SECTION)

        val workouts =
            workoutsJson.mapOrNull(::decodeWorkout)
                ?: return Failure(INVALID_FIELD_VALUE)

        val categories =
            categoriesJson.mapOrNull(::decodeCategory)
                ?: return Failure(INVALID_FIELD_VALUE)

        val userActions =
            userActionsJson.mapOrNull(::decodeUserAction)
                ?: return Failure(INVALID_FIELD_VALUE)

        val settings =
            root.objectOrNull(KEY_SETTINGS)?.let(::decodeSettings)
                ?: run {
                    if (root.containsKey(KEY_SETTINGS)) {
                        return Failure(INVALID_FIELD_VALUE)
                    }
                    null
                }

        return Success(
            snapshot =
                BackupSnapshot(
                    schemaVersion = schemaVersion,
                    exportedAt = exportedAt,
                    appVersion = root.stringOrNull(KEY_APP_VERSION),
                    workouts = workouts,
                    categories = categories,
                    userActions = userActions,
                    settings = settings,
                ),
        )
    }

    private fun decodeWorkout(element: JsonElement): BackupWorkoutRecord? {
        val obj = element as? JsonObject ?: return null

        return BackupWorkoutRecord(
            id = obj.longOrNull(KEY_ID) ?: return null,
            weekStartDate = obj.stringOrNull(KEY_WEEK_START_DATE) ?: return null,
            dayOfWeek = obj.intOrNull(KEY_DAY_OF_WEEK),
            timeSlot = obj.stringOrNull(KEY_TIME_SLOT),
            sortOrder = obj.intOrNull(KEY_SORT_ORDER) ?: return null,
            eventType = obj.stringOrNull(KEY_EVENT_TYPE) ?: return null,
            type = obj.stringOrNull(KEY_TYPE) ?: return null,
            description = obj.stringOrNull(KEY_DESCRIPTION) ?: return null,
            isCompleted = obj.booleanOrNull(KEY_IS_COMPLETED) ?: return null,
            categoryId = obj.longOrNull(KEY_CATEGORY_ID),
        )
    }

    private fun decodeCategory(element: JsonElement): BackupCategoryRecord? {
        val obj = element as? JsonObject ?: return null

        return BackupCategoryRecord(
            id = obj.longOrNull(KEY_ID) ?: return null,
            name = obj.stringOrNull(KEY_NAME) ?: return null,
            colorId = obj.stringOrNull(KEY_COLOR_ID) ?: return null,
            sortOrder = obj.intOrNull(KEY_SORT_ORDER) ?: return null,
            isHidden = obj.booleanOrNull(KEY_IS_HIDDEN) ?: return null,
            isSystem = obj.booleanOrNull(KEY_IS_SYSTEM) ?: return null,
        )
    }

    private fun decodeUserAction(element: JsonElement): BackupUserActionRecord? {
        val obj = element as? JsonObject ?: return null

        return BackupUserActionRecord(
            id = obj.longOrNull(KEY_ID) ?: return null,
            actionType = obj.stringOrNull(KEY_ACTION_TYPE) ?: return null,
            entityType = obj.stringOrNull(KEY_ENTITY_TYPE) ?: return null,
            entityId = obj.longOrNull(KEY_ENTITY_ID),
            metadata = obj.stringOrNull(KEY_METADATA),
            timestamp = obj.longOrNull(KEY_TIMESTAMP) ?: return null,
        )
    }

    private fun decodeSettings(obj: JsonObject): BackupSettingsRecord? {
        return BackupSettingsRecord(
            themeMode = obj.stringOrNull(KEY_THEME_MODE) ?: return null,
            languageTag = obj.stringOrNull(KEY_LANGUAGE_TAG) ?: return null,
            slotModePolicy = obj.stringOrNull(KEY_SLOT_MODE_POLICY) ?: return null,
        )
    }

    private fun JsonObject.stringOrNull(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull

    private fun JsonObject.intOrNull(key: String): Int? = this[key]?.jsonPrimitive?.intOrNull

    private fun JsonObject.longOrNull(key: String): Long? = this[key]?.jsonPrimitive?.longOrNull

    private fun JsonObject.booleanOrNull(key: String): Boolean? = this[key]?.jsonPrimitive?.booleanOrNull

    private fun JsonObject.arrayOrNull(key: String): JsonArray? = this[key]?.jsonArray

    private fun JsonObject.objectOrNull(key: String): JsonObject? = this[key]?.jsonObject

    private fun <T> JsonArray.mapOrNull(transform: (JsonElement) -> T?): List<T>? {
        val mapped = mutableListOf<T>()

        for (element in this) {
            val value = transform(element) ?: return null
            mapped += value
        }

        return mapped
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

private const val KEY_SCHEMA_VERSION = "schemaVersion"
private const val KEY_EXPORTED_AT = "exportedAt"
private const val KEY_APP_VERSION = "appVersion"
private const val KEY_WORKOUTS = "workouts"
private const val KEY_CATEGORIES = "categories"
private const val KEY_USER_ACTIONS = "userActions"
private const val KEY_SETTINGS = "settings"
private const val KEY_ID = "id"
private const val KEY_WEEK_START_DATE = "weekStartDate"
private const val KEY_DAY_OF_WEEK = "dayOfWeek"
private const val KEY_TIME_SLOT = "timeSlot"
private const val KEY_SORT_ORDER = "sortOrder"
private const val KEY_EVENT_TYPE = "eventType"
private const val KEY_TYPE = "type"
private const val KEY_DESCRIPTION = "description"
private const val KEY_IS_COMPLETED = "isCompleted"
private const val KEY_CATEGORY_ID = "categoryId"
private const val KEY_NAME = "name"
private const val KEY_COLOR_ID = "colorId"
private const val KEY_IS_HIDDEN = "isHidden"
private const val KEY_IS_SYSTEM = "isSystem"
private const val KEY_ACTION_TYPE = "actionType"
private const val KEY_ENTITY_TYPE = "entityType"
private const val KEY_ENTITY_ID = "entityId"
private const val KEY_METADATA = "metadata"
private const val KEY_TIMESTAMP = "timestamp"
private const val KEY_THEME_MODE = "themeMode"
private const val KEY_LANGUAGE_TAG = "languageTag"
private const val KEY_SLOT_MODE_POLICY = "slotModePolicy"
