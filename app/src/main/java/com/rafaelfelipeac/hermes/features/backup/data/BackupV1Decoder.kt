package com.rafaelfelipeac.hermes.features.backup.data

import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupCategoryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError.INVALID_FIELD_VALUE
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError.MISSING_REQUIRED_SECTION
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult.Failure
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult.Success
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSettingsRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSnapshot
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupUserActionRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupWorkoutRecord
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

@Suppress("ReturnCount")
internal object BackupV1Decoder {
    fun decode(root: JsonObject): BackupDecodeResult {
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
                    schemaVersion = BackupJsonCodec.SUPPORTED_SCHEMA_VERSION,
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
}
