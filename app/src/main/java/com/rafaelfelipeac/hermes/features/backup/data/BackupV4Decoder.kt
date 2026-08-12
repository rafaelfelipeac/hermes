package com.rafaelfelipeac.hermes.features.backup.data

import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupCategoryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError.INVALID_FIELD_VALUE
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeError.MISSING_REQUIRED_SECTION
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult.Failure
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupDecodeResult.Success
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupPersonalRecordEntryRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupPersonalRecordFamilyRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSettingsRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupSnapshot
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupUserActionRecord
import com.rafaelfelipeac.hermes.features.backup.domain.model.BackupWorkoutRecord
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

@Suppress("ReturnCount")
internal object BackupV4Decoder {
    fun decode(root: JsonObject): BackupDecodeResult {
        val exportedAt =
            root.stringOrNull(KEY_EXPORTED_AT)
                ?: return Failure(INVALID_FIELD_VALUE)

        val workoutsJson =
            root.arrayOrNull(KEY_WORKOUTS)
                ?: return Failure(root.requiredArrayError(KEY_WORKOUTS))

        val categoriesJson =
            root.arrayOrNull(KEY_CATEGORIES)
                ?: return Failure(root.requiredArrayError(KEY_CATEGORIES))

        val personalRecordFamiliesJson =
            root.arrayOrNull(KEY_PERSONAL_RECORD_FAMILIES)
                ?: return Failure(root.requiredArrayError(KEY_PERSONAL_RECORD_FAMILIES))

        val personalRecordEntriesJson =
            root.arrayOrNull(KEY_PERSONAL_RECORD_ENTRIES)
                ?: return Failure(root.requiredArrayError(KEY_PERSONAL_RECORD_ENTRIES))

        val userActionsJson =
            root.arrayOrNull(KEY_USER_ACTIONS)
                ?: return Failure(root.requiredArrayError(KEY_USER_ACTIONS))

        val workouts =
            workoutsJson.mapOrNull(::decodeWorkout)
                ?: return Failure(INVALID_FIELD_VALUE)

        val categories =
            categoriesJson.mapOrNull(::decodeCategory)
                ?: return Failure(INVALID_FIELD_VALUE)

        val personalRecordFamilies =
            personalRecordFamiliesJson.mapOrNull(::decodePersonalRecordFamily)
                ?: return Failure(INVALID_FIELD_VALUE)

        val personalRecordEntries =
            personalRecordEntriesJson.mapOrNull(::decodePersonalRecordEntry)
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
                    schemaVersion = BackupJsonCodec.SCHEMA_VERSION_V4,
                    exportedAt = exportedAt,
                    appVersion = root.stringOrNull(KEY_APP_VERSION),
                    workouts = workouts,
                    categories = categories,
                    personalRecordFamilies = personalRecordFamilies,
                    personalRecordEntries = personalRecordEntries,
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

    private fun decodePersonalRecordFamily(element: JsonElement): BackupPersonalRecordFamilyRecord? {
        val obj = element as? JsonObject ?: return null

        return BackupPersonalRecordFamilyRecord(
            id = obj.longOrNull(KEY_ID) ?: return null,
            categoryId = obj.longOrNull(KEY_CATEGORY_ID),
            title = obj.stringOrNull(KEY_TITLE) ?: return null,
            metricType = obj.stringOrNull(KEY_METRIC_TYPE) ?: return null,
            defaultUnit = obj.stringOrNull(KEY_DEFAULT_UNIT) ?: return null,
            comparisonRule = obj.stringOrNull(KEY_COMPARISON_RULE) ?: return null,
            manualCurrentEntryId = obj.longOrNull(KEY_MANUAL_CURRENT_ENTRY_ID),
            sortOrder = obj.intOrNull(KEY_SORT_ORDER) ?: return null,
            createdAt = obj.stringOrNull(KEY_CREATED_AT) ?: return null,
            updatedAt = obj.stringOrNull(KEY_UPDATED_AT) ?: return null,
        )
    }

    private fun decodePersonalRecordEntry(element: JsonElement): BackupPersonalRecordEntryRecord? {
        val obj = element as? JsonObject ?: return null

        return BackupPersonalRecordEntryRecord(
            id = obj.longOrNull(KEY_ID) ?: return null,
            familyId = obj.longOrNull(KEY_FAMILY_ID) ?: return null,
            value = obj.doubleOrNull(KEY_VALUE) ?: return null,
            unit = obj.stringOrNull(KEY_UNIT) ?: return null,
            customUnitLabel = obj.stringOrNull(KEY_CUSTOM_UNIT_LABEL),
            recordDate = obj.stringOrNull(KEY_RECORD_DATE) ?: return null,
            note = obj.stringOrNull(KEY_NOTE),
            createdAt = obj.stringOrNull(KEY_CREATED_AT) ?: return null,
            updatedAt = obj.stringOrNull(KEY_UPDATED_AT) ?: return null,
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
        val distanceUnit = obj.stringOrNull(KEY_DISTANCE_UNIT) ?: return null
        val paceUnit = obj.stringOrNull(KEY_PACE_UNIT) ?: return null
        val weightUnit = obj.stringOrNull(KEY_WEIGHT_UNIT) ?: return null

        if (runCatching { DistanceUnit.valueOf(distanceUnit) }.isFailure) return null
        if (runCatching { PaceUnit.valueOf(paceUnit) }.isFailure) return null
        if (runCatching { WeightUnit.valueOf(weightUnit) }.isFailure) return null

        return BackupSettingsRecord(
            themeMode = obj.stringOrNull(KEY_THEME_MODE) ?: return null,
            languageTag = obj.stringOrNull(KEY_LANGUAGE_TAG) ?: return null,
            slotModePolicy = obj.stringOrNull(KEY_SLOT_MODE_POLICY) ?: return null,
            weekStartDay = obj.stringOrNull(KEY_WEEK_START_DAY) ?: return null,
            distanceUnit = distanceUnit,
            paceUnit = paceUnit,
            weightUnit = weightUnit,
        )
    }

    private fun JsonObject.stringOrNull(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull

    private fun JsonObject.intOrNull(key: String): Int? = (this[key] as? JsonPrimitive)?.intOrNull

    private fun JsonObject.longOrNull(key: String): Long? = (this[key] as? JsonPrimitive)?.longOrNull

    private fun JsonObject.booleanOrNull(key: String): Boolean? = (this[key] as? JsonPrimitive)?.booleanOrNull

    private fun JsonObject.doubleOrNull(key: String): Double? = (this[key] as? JsonPrimitive)?.contentOrNull?.toDoubleOrNull()

    private fun JsonObject.arrayOrNull(key: String): JsonArray? = this[key] as? JsonArray

    private fun JsonObject.requiredArrayError(key: String): BackupDecodeError {
        return if (containsKey(key)) {
            INVALID_FIELD_VALUE
        } else {
            MISSING_REQUIRED_SECTION
        }
    }

    private fun JsonObject.objectOrNull(key: String): JsonObject? = this[key] as? JsonObject

    private fun <T> JsonArray.mapOrNull(transform: (JsonElement) -> T?): List<T>? {
        val mapped = mutableListOf<T>()

        for (element in this) {
            val value = transform(element) ?: return null
            mapped += value
        }

        return mapped
    }
}
