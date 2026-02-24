package com.rafaelfelipeac.hermes.features.activity.presentation.formatter

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataSerializer
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import java.time.DayOfWeek
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale

@Suppress("LargeClass", "TooManyFunctions")
class ActivityUiFormatter(
    private val stringProvider: StringProvider,
) {
    fun parseMetadata(raw: String?): Map<String, String> {
        return UserActionMetadataSerializer.fromJson(raw)
    }

    fun formatTime(
        timestamp: Long,
        zoneId: ZoneId,
        locale: Locale,
    ): String {
        val pattern = stringProvider.get(R.string.activity_time_pattern)
        val formatter =
            DateTimeFormatterBuilder()
                .appendPattern(pattern)
                .toFormatter(locale)

        return Instant.ofEpochMilli(timestamp).atZone(zoneId).format(formatter)
    }

    fun buildTitle(
        record: UserActionRecord,
        metadata: Map<String, String>,
    ): String {
        val actionType = runCatching { UserActionType.valueOf(record.actionType) }.getOrNull()
        val entityType = runCatching { UserActionEntityType.valueOf(record.entityType) }.getOrNull()
        val quotedWorkoutLabel = buildQuotedWorkoutLabel(metadata)

        val title =
            when (entityType) {
                UserActionEntityType.REST_DAY,
                UserActionEntityType.BUSY,
                UserActionEntityType.SICK,
                ->
                    buildNonWorkoutTitle(
                        entityType = entityType,
                        actionType = actionType,
                        quotedWorkoutLabel = quotedWorkoutLabel,
                    )
                UserActionEntityType.CATEGORY -> buildCategoryTitle(actionType, metadata)
                else -> buildWorkoutTitle(actionType, quotedWorkoutLabel)
            }

        return title ?: when (actionType) {
            UserActionType.CHANGE_LANGUAGE ->
                stringProvider.get(R.string.activity_action_change_language)

            UserActionType.CHANGE_THEME ->
                stringProvider.get(R.string.activity_action_change_theme)

            UserActionType.CHANGE_SLOT_MODE ->
                stringProvider.get(R.string.activity_action_change_slot_mode)

            UserActionType.OPEN_WEEK ->
                stringProvider.get(R.string.activity_action_open_week)

            UserActionType.COPY_LAST_WEEK ->
                stringProvider.get(R.string.activity_action_copy_last_week)

            UserActionType.UNDO_COPY_LAST_WEEK ->
                stringProvider.get(R.string.activity_action_undo_copy_last_week)

            else -> stringProvider.get(R.string.activity_action_fallback)
        }
    }

    fun buildSubtitle(
        record: UserActionRecord,
        metadata: Map<String, String>,
        currentLocale: Locale,
    ): String? {
        val actionType = runCatching { UserActionType.valueOf(record.actionType) }.getOrNull()
        val weekSubtitle = buildWeekSubtitle(metadata, currentLocale)
        val actionSubtitle = buildActionSubtitle(actionType, metadata)

        return combineSubtitles(
            weekSubtitle = weekSubtitle,
            actionSubtitle = actionSubtitle,
            shouldSplitLines = shouldSplitLines(actionType),
        )
    }

    private fun buildValueChangeSubtitle(
        metadata: Map<String, String>,
        actionType: UserActionType,
    ): String? {
        val oldValue =
            quoteValue(
                formatChangeValue(metadata[UserActionMetadataKeys.OLD_VALUE], actionType),
            )
        val newValue =
            quoteValue(
                formatChangeValue(metadata[UserActionMetadataKeys.NEW_VALUE], actionType),
            )

        if (oldValue.isNullOrBlank() && newValue.isNullOrBlank()) return null

        return stringProvider.get(
            R.string.activity_subtitle_change_value,
            oldValue.orEmpty(),
            newValue.orEmpty(),
        )
    }

    private fun buildMoveSubtitle(metadata: Map<String, String>): String? {
        val oldDay = dayLabel(metadata[UserActionMetadataKeys.OLD_DAY_OF_WEEK])
        val newDay = dayLabel(metadata[UserActionMetadataKeys.NEW_DAY_OF_WEEK])
        val oldSlot = timeSlotLabel(metadata[UserActionMetadataKeys.OLD_TIME_SLOT])
        val newSlot = timeSlotLabel(metadata[UserActionMetadataKeys.NEW_TIME_SLOT])

        val oldLocation = quoteValue(locationLabel(oldDay, oldSlot))
        val newLocation = quoteValue(locationLabel(newDay, newSlot))

        if (oldLocation.isNullOrBlank() && newLocation.isNullOrBlank()) return null

        return stringProvider.get(
            R.string.activity_subtitle_move,
            oldLocation.orEmpty(),
            newLocation.orEmpty(),
        )
    }

    private fun buildReorderSubtitle(metadata: Map<String, String>): String? {
        val oldDay =
            quoteValue(
                dayLabel(metadata[UserActionMetadataKeys.OLD_DAY_OF_WEEK]),
            )
        val newDay =
            quoteValue(
                dayLabel(metadata[UserActionMetadataKeys.NEW_DAY_OF_WEEK]),
            )

        val hasAnyDay = !oldDay.isNullOrBlank() || !newDay.isNullOrBlank()
        val isSameDay = oldDay != null && oldDay == newDay

        return if (!hasAnyDay || isSameDay) {
            null
        } else {
            stringProvider.get(
                R.string.activity_subtitle_move,
                oldDay.orEmpty(),
                newDay.orEmpty(),
            )
        }
    }

    private fun buildWeekSubtitle(
        metadata: Map<String, String>,
        currentLocale: Locale,
    ): String? {
        val weekStart = metadata[UserActionMetadataKeys.WEEK_START_DATE] ?: return null
        val pattern = stringProvider.get(R.string.activity_week_date_pattern)
        val formatter =
            DateTimeFormatterBuilder()
                .appendPattern(pattern)
                .toFormatter(currentLocale)
        val formatted =
            runCatching { LocalDate.parse(weekStart).format(formatter) }
                .getOrDefault(weekStart)

        return stringProvider.get(R.string.activity_subtitle_week, formatted)
    }

    private fun formatChangeValue(
        raw: String?,
        actionType: UserActionType,
    ): String? {
        if (raw.isNullOrBlank()) return stringProvider.get(R.string.activity_value_unknown)

        return when (actionType) {
            UserActionType.CHANGE_LANGUAGE -> languageLabel(raw)
            UserActionType.CHANGE_THEME -> themeLabel(raw)
            UserActionType.CHANGE_SLOT_MODE -> slotModeLabel(raw)
            else -> raw
        }
    }

    private fun slotModeLabel(raw: String): String {
        return when (raw.uppercase(Locale.ENGLISH)) {
            "AUTO_WHEN_MULTIPLE" -> stringProvider.get(R.string.settings_slot_mode_auto)
            "ALWAYS_SHOW" -> stringProvider.get(R.string.settings_slot_mode_always)
            else -> raw
        }
    }

    private fun buildCategoryTitle(
        actionType: UserActionType?,
        metadata: Map<String, String>,
    ): String? {
        val label = buildCategoryLabel(metadata)

        return when (actionType) {
            UserActionType.CREATE_CATEGORY ->
                stringProvider.get(
                    R.string.activity_action_create_category,
                    quoteValue(label) ?: label,
                )

            UserActionType.UPDATE_CATEGORY_NAME ->
                stringProvider.get(R.string.activity_action_update_category_name)

            UserActionType.UPDATE_CATEGORY_COLOR ->
                stringProvider.get(
                    R.string.activity_action_update_category_color,
                    quoteValue(label) ?: label,
                )

            UserActionType.UPDATE_CATEGORY_VISIBILITY ->
                stringProvider.get(
                    R.string.activity_action_update_category_visibility,
                    quoteValue(label) ?: label,
                )

            UserActionType.REORDER_CATEGORY ->
                stringProvider.get(
                    R.string.activity_action_reorder_category,
                    quoteValue(label) ?: label,
                )

            UserActionType.DELETE_CATEGORY ->
                stringProvider.get(
                    R.string.activity_action_delete_category,
                    quoteValue(label) ?: label,
                )

            UserActionType.RESTORE_DEFAULT_CATEGORIES ->
                stringProvider.get(R.string.categories_restore_defaults)

            else -> null
        }
    }

    private fun buildCategoryLabel(metadata: Map<String, String>): String {
        return metadata[UserActionMetadataKeys.CATEGORY_NAME]
            ?: metadata[UserActionMetadataKeys.NEW_VALUE]
            ?: metadata[UserActionMetadataKeys.OLD_VALUE]
            ?: stringProvider.get(R.string.activity_category_fallback)
    }

    private fun buildCategoryVisibilitySubtitle(metadata: Map<String, String>): String? {
        val oldValue = formatVisibilityValue(metadata[UserActionMetadataKeys.OLD_VALUE])
        val newValue = formatVisibilityValue(metadata[UserActionMetadataKeys.NEW_VALUE])

        if (oldValue.isNullOrBlank() && newValue.isNullOrBlank()) return null

        return stringProvider.get(
            R.string.activity_subtitle_change_value,
            oldValue.orEmpty(),
            newValue.orEmpty(),
        )
    }

    private fun buildWorkoutCategorySubtitle(metadata: Map<String, String>): String? {
        return metadata[UserActionMetadataKeys.CATEGORY_NAME]
            ?.takeIf { it.isNotBlank() }
            ?.let(::quoteValue)
            ?.let { quotedCategory ->
                stringProvider.get(
                    R.string.activity_subtitle_workout_category,
                    quotedCategory,
                )
            }
    }

    private fun buildWorkoutCategoryChangeSubtitle(metadata: Map<String, String>): String? {
        val oldCategory = metadata[UserActionMetadataKeys.OLD_CATEGORY_NAME]?.takeIf { it.isNotBlank() }
        val newCategory = metadata[UserActionMetadataKeys.NEW_CATEGORY_NAME]?.takeIf { it.isNotBlank() }
        val fallbackCategory =
            metadata[UserActionMetadataKeys.CATEGORY_NAME]
                ?.takeIf { it.isNotBlank() }
                ?: newCategory
                ?: oldCategory

        return when {
            oldCategory.isNullOrBlank() || newCategory.isNullOrBlank() ->
                fallbackCategory
                    ?.let(::quoteValue)
                    ?.let { quotedCategory ->
                        stringProvider.get(
                            R.string.activity_subtitle_workout_category,
                            quotedCategory,
                        )
                    }
            oldCategory == newCategory -> null
            else -> {
                val oldQuoted = quoteValue(oldCategory)
                val newQuoted = quoteValue(newCategory)
                if (oldQuoted == null || newQuoted == null) {
                    null
                } else {
                    stringProvider.get(
                        R.string.activity_subtitle_workout_category_change,
                        oldQuoted,
                        newQuoted,
                    )
                }
            }
        }
    }

    private fun buildActionSubtitle(
        actionType: UserActionType?,
        metadata: Map<String, String>,
    ): String? {
        return when (actionType) {
            UserActionType.CHANGE_LANGUAGE,
            UserActionType.CHANGE_THEME,
            UserActionType.CHANGE_SLOT_MODE,
            UserActionType.UPDATE_CATEGORY_NAME,
            -> buildValueChangeSubtitle(metadata, actionType)

            UserActionType.UPDATE_CATEGORY_VISIBILITY ->
                buildCategoryVisibilitySubtitle(metadata)

            UserActionType.CREATE_WORKOUT ->
                buildWorkoutCategorySubtitle(metadata)

            UserActionType.UPDATE_WORKOUT ->
                buildWorkoutCategoryChangeSubtitle(metadata)

            UserActionType.MOVE_WORKOUT_BETWEEN_DAYS -> buildMoveSubtitle(metadata)
            UserActionType.REORDER_WORKOUT -> buildReorderSubtitle(metadata)
            UserActionType.UNDO_MOVE_WORKOUT_BETWEEN_DAYS -> buildMoveSubtitle(metadata)
            UserActionType.UNDO_REORDER_WORKOUT_SAME_DAY -> buildReorderSubtitle(metadata)
            else -> null
        }
    }

    private fun shouldSplitLines(actionType: UserActionType?): Boolean {
        return actionType == UserActionType.MOVE_WORKOUT_BETWEEN_DAYS ||
            actionType == UserActionType.REORDER_WORKOUT ||
            actionType == UserActionType.UNDO_MOVE_WORKOUT_BETWEEN_DAYS ||
            actionType == UserActionType.UNDO_REORDER_WORKOUT_SAME_DAY ||
            actionType == UserActionType.CREATE_WORKOUT ||
            actionType == UserActionType.UPDATE_WORKOUT ||
            actionType == UserActionType.CHANGE_SLOT_MODE
    }

    private fun combineSubtitles(
        weekSubtitle: String?,
        actionSubtitle: String?,
        shouldSplitLines: Boolean,
    ): String? {
        return if (weekSubtitle != null && actionSubtitle != null && shouldSplitLines) {
            "$weekSubtitle\n$actionSubtitle"
        } else {
            listOfNotNull(weekSubtitle, actionSubtitle)
                .takeIf { it.isNotEmpty() }
                ?.joinToString(stringProvider.get(R.string.activity_subtitle_separator))
        }
    }

    private fun formatVisibilityValue(raw: String?): String? {
        return when (raw) {
            UserActionMetadataValues.CATEGORY_VISIBLE ->
                stringProvider.get(R.string.activity_category_visible)
            UserActionMetadataValues.CATEGORY_HIDDEN ->
                stringProvider.get(R.string.activity_category_hidden)
            else -> raw
        }
    }

    private fun languageLabel(raw: String): String {
        if (raw.equals(AppLanguage.SYSTEM.tag, ignoreCase = true)) {
            return stringProvider.get(R.string.activity_value_system)
        }

        val language = AppLanguage.entries.firstOrNull { it.tag.equals(raw, ignoreCase = true) }

        return when (language) {
            AppLanguage.ENGLISH -> stringProvider.get(R.string.settings_language_english)
            AppLanguage.PORTUGUESE_BRAZIL ->
                stringProvider.get(R.string.settings_language_portuguese_brazil)

            AppLanguage.GERMAN -> stringProvider.get(R.string.settings_language_german)
            AppLanguage.FRENCH -> stringProvider.get(R.string.settings_language_french)
            AppLanguage.SPANISH -> stringProvider.get(R.string.settings_language_spanish)
            AppLanguage.ITALIAN -> stringProvider.get(R.string.settings_language_italian)
            AppLanguage.ARABIC -> stringProvider.get(R.string.settings_language_arabic)
            AppLanguage.HINDI -> stringProvider.get(R.string.settings_language_hindi)
            AppLanguage.JAPANESE -> stringProvider.get(R.string.settings_language_japanese)
            else -> raw
        }
    }

    private fun themeLabel(raw: String): String {
        val mode = runCatching { ThemeMode.valueOf(raw.uppercase(Locale.ENGLISH)) }.getOrNull()

        return when (mode) {
            ThemeMode.SYSTEM -> stringProvider.get(R.string.activity_value_system)
            ThemeMode.LIGHT -> stringProvider.get(R.string.settings_theme_light)
            ThemeMode.DARK -> stringProvider.get(R.string.settings_theme_dark)
            else -> raw
        }
    }

    private fun dayLabel(raw: String?): String? {
        val cleaned = raw?.takeIf { it.isNotBlank() }

        return when (cleaned) {
            null -> null
            UserActionMetadataValues.UNPLANNED ->
                stringProvider.get(R.string.activity_day_unplanned)

            else -> {
                val dayNumber = cleaned.toIntOrNull()
                if (dayNumber == null) {
                    normalizeDayToken(cleaned)
                } else {
                    dayNumberLabel(dayNumber, cleaned)
                }
            }
        }
    }

    private fun timeSlotLabel(raw: String?): String? {
        val cleaned = raw?.takeIf { it.isNotBlank() }
        val parsedSlot =
            cleaned
                ?.takeUnless { it == UserActionMetadataValues.UNPLANNED }
                ?.let { value ->
                    runCatching { TimeSlot.valueOf(value.uppercase(Locale.ENGLISH)) }.getOrNull()
                }

        return when (parsedSlot) {
            TimeSlot.MORNING -> stringProvider.get(R.string.weekly_training_slot_morning)
            TimeSlot.AFTERNOON -> stringProvider.get(R.string.weekly_training_slot_afternoon)
            TimeSlot.NIGHT -> stringProvider.get(R.string.weekly_training_slot_night)
            null -> cleaned?.takeUnless { it == UserActionMetadataValues.UNPLANNED }
        }
    }

    private fun locationLabel(
        dayLabel: String?,
        timeSlotLabel: String?,
    ): String? {
        return when {
            dayLabel.isNullOrBlank() && timeSlotLabel.isNullOrBlank() -> null
            dayLabel.isNullOrBlank() -> timeSlotLabel
            timeSlotLabel.isNullOrBlank() -> dayLabel
            else -> "$dayLabel | $timeSlotLabel"
        }
    }

    private fun normalizeDayToken(raw: String): String {
        val cleaned = raw.trim().replace(Regex("[^A-Za-z]"), EMPTY)

        if (cleaned.isBlank()) return raw

        val normalized = cleaned.uppercase(Locale.ENGLISH)
        val day =
            runCatching { DayOfWeek.valueOf(normalized) }.getOrNull()
                ?: DayOfWeek.entries.firstOrNull { dayOfWeek ->
                    dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH)
                        .equals(cleaned, ignoreCase = true) ||
                        dayOfWeek.getDisplayName(
                            java.time.format.TextStyle.FULL,
                            Locale.ENGLISH,
                        )
                            .equals(cleaned, ignoreCase = true)
                }

        return when (day) {
            MONDAY -> stringProvider.get(R.string.day_monday)
            TUESDAY -> stringProvider.get(R.string.day_tuesday)
            WEDNESDAY -> stringProvider.get(R.string.day_wednesday)
            THURSDAY -> stringProvider.get(R.string.day_thursday)
            FRIDAY -> stringProvider.get(R.string.day_friday)
            SATURDAY -> stringProvider.get(R.string.day_saturday)
            SUNDAY -> stringProvider.get(R.string.day_sunday)
            else -> raw
        }
    }

    private fun dayNumberLabel(
        dayNumber: Int,
        fallback: String,
    ): String {
        return when (dayNumber) {
            DAY_NUMBER_MONDAY -> stringProvider.get(R.string.day_monday)
            DAY_NUMBER_TUESDAY -> stringProvider.get(R.string.day_tuesday)
            DAY_NUMBER_WEDNESDAY -> stringProvider.get(R.string.day_wednesday)
            DAY_NUMBER_THURSDAY -> stringProvider.get(R.string.day_thursday)
            DAY_NUMBER_FRIDAY -> stringProvider.get(R.string.day_friday)
            DAY_NUMBER_SATURDAY -> stringProvider.get(R.string.day_saturday)
            DAY_NUMBER_SUNDAY -> stringProvider.get(R.string.day_sunday)
            else -> fallback
        }
    }

    private fun buildQuotedWorkoutLabel(metadata: Map<String, String>): String {
        val workoutName =
            metadata[UserActionMetadataKeys.NEW_TYPE]
                ?.takeIf { it.isNotBlank() }
                ?: metadata[UserActionMetadataKeys.NEW_DESCRIPTION]
                    ?.takeIf { it.isNotBlank() }
        val workoutLabel =
            workoutName
                ?: metadata[UserActionMetadataKeys.OLD_TYPE]?.takeIf { it.isNotBlank() }
                ?: metadata[UserActionMetadataKeys.OLD_DESCRIPTION]
                    ?.takeIf { it.isNotBlank() }
                ?: stringProvider.get(R.string.activity_workout_fallback)

        return stringProvider.get(R.string.activity_value_quoted, workoutLabel)
    }

    private fun quoteValue(value: String?): String? {
        if (value.isNullOrBlank()) return value
        return stringProvider.get(R.string.activity_value_quoted, value)
    }

    private fun buildNonWorkoutTitle(
        entityType: UserActionEntityType,
        actionType: UserActionType?,
        quotedWorkoutLabel: String,
    ): String? {
        val convertFromWorkoutRes = convertFromWorkoutRes(entityType)
        val convertToWorkoutRes = convertToWorkoutRes(entityType)

        return when (actionType) {
            UserActionType.COMPLETE_WORKOUT ->
                stringProvider.get(completeNonWorkoutRes(entityType))

            UserActionType.INCOMPLETE_WORKOUT ->
                stringProvider.get(incompleteNonWorkoutRes(entityType))

            UserActionType.REORDER_WORKOUT ->
                stringProvider.get(reorderNonWorkoutRes(entityType))

            UserActionType.MOVE_WORKOUT_BETWEEN_DAYS ->
                stringProvider.get(moveNonWorkoutRes(entityType))

            UserActionType.UNDO_REORDER_WORKOUT_SAME_DAY ->
                stringProvider.get(undoReorderNonWorkoutRes(entityType))

            UserActionType.UNDO_MOVE_WORKOUT_BETWEEN_DAYS ->
                stringProvider.get(undoMoveNonWorkoutRes(entityType))

            UserActionType.CREATE_REST_DAY ->
                stringProvider.get(createNonWorkoutRes(entityType))

            UserActionType.UPDATE_REST_DAY ->
                stringProvider.get(updateNonWorkoutRes(entityType))

            UserActionType.DELETE_REST_DAY ->
                stringProvider.get(deleteNonWorkoutRes(entityType))

            UserActionType.UNDO_DELETE_REST_DAY ->
                stringProvider.get(undoDeleteNonWorkoutRes(entityType))

            UserActionType.CONVERT_WORKOUT_TO_REST_DAY ->
                stringProvider.get(convertFromWorkoutRes, quotedWorkoutLabel)

            UserActionType.CONVERT_REST_DAY_TO_WORKOUT ->
                stringProvider.get(convertToWorkoutRes)

            else -> null
        }
    }

    private fun createNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST_DAY -> R.string.activity_action_create_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_create_busy
            UserActionEntityType.SICK -> R.string.activity_action_create_sick
            else -> R.string.activity_action_create_rest_day
        }
    }

    private fun updateNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST_DAY -> R.string.activity_action_update_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_update_busy
            UserActionEntityType.SICK -> R.string.activity_action_update_sick
            else -> R.string.activity_action_update_rest_day
        }
    }

    private fun deleteNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST_DAY -> R.string.activity_action_delete_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_delete_busy
            UserActionEntityType.SICK -> R.string.activity_action_delete_sick
            else -> R.string.activity_action_delete_rest_day
        }
    }

    private fun undoDeleteNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST_DAY -> R.string.activity_action_undo_delete_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_undo_delete_busy
            UserActionEntityType.SICK -> R.string.activity_action_undo_delete_sick
            else -> R.string.activity_action_undo_delete_rest_day
        }
    }

    private fun reorderNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST_DAY -> R.string.activity_action_reorder_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_reorder_busy
            UserActionEntityType.SICK -> R.string.activity_action_reorder_sick
            else -> R.string.activity_action_reorder_rest_day
        }
    }

    private fun moveNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST_DAY -> R.string.activity_action_move_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_move_busy
            UserActionEntityType.SICK -> R.string.activity_action_move_sick
            else -> R.string.activity_action_move_rest_day
        }
    }

    private fun undoReorderNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST_DAY -> R.string.activity_action_undo_reorder_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_undo_reorder_busy
            UserActionEntityType.SICK -> R.string.activity_action_undo_reorder_sick
            else -> R.string.activity_action_undo_reorder_rest_day
        }
    }

    private fun undoMoveNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST_DAY -> R.string.activity_action_undo_move_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_undo_move_busy
            UserActionEntityType.SICK -> R.string.activity_action_undo_move_sick
            else -> R.string.activity_action_undo_move_rest_day
        }
    }

    private fun completeNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST_DAY -> R.string.activity_action_complete_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_complete_busy
            UserActionEntityType.SICK -> R.string.activity_action_complete_sick
            else -> R.string.activity_action_complete_rest_day
        }
    }

    private fun incompleteNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST_DAY -> R.string.activity_action_incomplete_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_incomplete_busy
            UserActionEntityType.SICK -> R.string.activity_action_incomplete_sick
            else -> R.string.activity_action_incomplete_rest_day
        }
    }

    private fun convertFromWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST_DAY -> R.string.activity_action_convert_workout_to_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_convert_workout_to_busy
            UserActionEntityType.SICK -> R.string.activity_action_convert_workout_to_sick
            else -> R.string.activity_action_convert_workout_to_rest_day
        }
    }

    private fun convertToWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST_DAY -> R.string.activity_action_convert_rest_day_to_workout
            UserActionEntityType.BUSY -> R.string.activity_action_convert_busy_to_workout
            UserActionEntityType.SICK -> R.string.activity_action_convert_sick_to_workout
            else -> R.string.activity_action_convert_rest_day_to_workout
        }
    }

    private fun buildWorkoutTitle(
        actionType: UserActionType?,
        quotedWorkoutLabel: String,
    ): String? {
        val resId = workoutTitleResByAction[actionType] ?: return null

        return if (resId == R.string.activity_action_convert_rest_day_to_workout) {
            stringProvider.get(resId)
        } else {
            stringProvider.get(resId, quotedWorkoutLabel)
        }
    }

    private companion object {
        const val DAY_NUMBER_MONDAY = 1
        const val DAY_NUMBER_TUESDAY = 2
        const val DAY_NUMBER_WEDNESDAY = 3
        const val DAY_NUMBER_THURSDAY = 4
        const val DAY_NUMBER_FRIDAY = 5
        const val DAY_NUMBER_SATURDAY = 6
        const val DAY_NUMBER_SUNDAY = 7
    }

    private val workoutTitleResByAction =
        mapOf(
            UserActionType.CREATE_WORKOUT to R.string.activity_action_create_workout,
            UserActionType.UPDATE_WORKOUT to R.string.activity_action_update_workout,
            UserActionType.DELETE_WORKOUT to R.string.activity_action_delete_workout,
            UserActionType.UNDO_DELETE_WORKOUT to R.string.activity_action_undo_delete_workout,
            UserActionType.COMPLETE_WORKOUT to R.string.activity_action_complete_workout,
            UserActionType.INCOMPLETE_WORKOUT to R.string.activity_action_incomplete_workout,
            UserActionType.UNDO_COMPLETE_WORKOUT to R.string.activity_action_undo_complete_workout,
            UserActionType.UNDO_INCOMPLETE_WORKOUT to
                R.string.activity_action_undo_incomplete_workout,
            UserActionType.REORDER_WORKOUT to R.string.activity_action_reorder_workout,
            UserActionType.MOVE_WORKOUT_BETWEEN_DAYS to R.string.activity_action_move_workout,
            UserActionType.UNDO_REORDER_WORKOUT_SAME_DAY to
                R.string.activity_action_undo_reorder_workout,
            UserActionType.UNDO_MOVE_WORKOUT_BETWEEN_DAYS to
                R.string.activity_action_undo_move_workout,
            UserActionType.CONVERT_REST_DAY_TO_WORKOUT to
                R.string.activity_action_convert_rest_day_to_workout,
        )
}
