package com.rafaelfelipeac.hermes.features.activity.presentation.formatter

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.AppConstants.NEW_LINE
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.ALWAYS_SHOW
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.AUTO_WHEN_MULTIPLE
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
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
import java.time.format.TextStyle
import java.util.Locale

@Suppress("TooManyFunctions")
internal class ActivityUiFormatterShared(
    private val stringProvider: StringProvider,
) {
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

    fun buildWeekSubtitle(
        metadata: Map<String, String>,
        currentLocale: Locale,
    ): String? {
        val weekStart = metadata[UserActionMetadataKeys.WEEK_START_DATE] ?: return null
        val formatted = formatDate(weekStart, currentLocale) ?: weekStart

        return stringProvider.get(R.string.activity_subtitle_week, formatted)
    }

    fun quoteValue(value: String?): String? {
        if (value.isNullOrBlank()) return value
        return stringProvider.get(R.string.activity_value_quoted, value)
    }

    fun activitySubtitleSeparator(): String {
        return stringProvider.get(R.string.activity_subtitle_separator)
    }

    fun combineSubtitles(
        weekSubtitle: String?,
        actionSubtitle: String?,
        shouldSplitLines: Boolean,
    ): String? {
        return if (weekSubtitle != null && actionSubtitle != null && shouldSplitLines) {
            "$weekSubtitle$NEW_LINE$actionSubtitle"
        } else {
            listOfNotNull(weekSubtitle, actionSubtitle)
                .takeIf { it.isNotEmpty() }
                ?.joinToString(activitySubtitleSeparator())
        }
    }

    fun formatChangeValue(
        raw: String?,
        actionType: UserActionType,
    ): String? {
        if (raw.isNullOrBlank()) return stringProvider.get(R.string.activity_value_unknown)

        return when (actionType) {
            UserActionType.CHANGE_LANGUAGE -> languageLabel(raw)
            UserActionType.CHANGE_THEME -> themeLabel(raw)
            UserActionType.CHANGE_DYNAMIC_COLOR -> booleanLabel(raw)
            UserActionType.CHANGE_SLOT_MODE -> slotModeLabel(raw)
            UserActionType.CHANGE_WEEK_START -> weekStartDayLabel(raw)
            UserActionType.CHANGE_DISTANCE_UNIT -> distanceUnitLabel(raw)
            UserActionType.CHANGE_PACE_UNIT -> paceUnitLabel(raw)
            UserActionType.CHANGE_WEIGHT_UNIT -> weightUnitLabel(raw)
            else -> raw
        }
    }

    fun formatVisibilityValue(raw: String?): String? {
        return when (raw) {
            UserActionMetadataValues.CATEGORY_VISIBLE ->
                stringProvider.get(R.string.activity_category_visible)
            UserActionMetadataValues.CATEGORY_HIDDEN ->
                stringProvider.get(R.string.activity_category_hidden)
            else -> raw
        }
    }

    fun booleanLabel(raw: String): String {
        return when (raw.lowercase(Locale.ENGLISH)) {
            "true" -> stringProvider.get(R.string.activity_value_enabled)
            "false" -> stringProvider.get(R.string.activity_value_disabled)
            else -> raw
        }
    }

    fun languageLabel(raw: String): String {
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

    fun themeLabel(raw: String): String {
        val mode = runCatching { ThemeMode.valueOf(raw.uppercase(Locale.ENGLISH)) }.getOrNull()

        return when (mode) {
            ThemeMode.SYSTEM -> stringProvider.get(R.string.activity_value_system)
            ThemeMode.LIGHT -> stringProvider.get(R.string.settings_theme_light)
            ThemeMode.DARK -> stringProvider.get(R.string.settings_theme_dark)
            else -> raw
        }
    }

    fun slotModeLabel(raw: String): String {
        return when (raw.uppercase(Locale.ENGLISH)) {
            AUTO_WHEN_MULTIPLE.name -> stringProvider.get(R.string.settings_slot_mode_auto)
            ALWAYS_SHOW.name -> stringProvider.get(R.string.settings_slot_mode_always)
            else -> raw
        }
    }

    fun weekStartDayLabel(raw: String): String {
        val day = runCatching { WeekStartDay.valueOf(raw.uppercase(Locale.ENGLISH)) }.getOrNull()

        return when (day) {
            WeekStartDay.MONDAY -> stringProvider.get(R.string.day_monday)
            WeekStartDay.TUESDAY -> stringProvider.get(R.string.day_tuesday)
            WeekStartDay.WEDNESDAY -> stringProvider.get(R.string.day_wednesday)
            WeekStartDay.THURSDAY -> stringProvider.get(R.string.day_thursday)
            WeekStartDay.FRIDAY -> stringProvider.get(R.string.day_friday)
            WeekStartDay.SATURDAY -> stringProvider.get(R.string.day_saturday)
            WeekStartDay.SUNDAY -> stringProvider.get(R.string.day_sunday)
            null -> raw
        }
    }

    fun distanceUnitLabel(raw: String): String {
        return when (runCatching { DistanceUnit.valueOf(raw.uppercase(Locale.ENGLISH)) }.getOrNull()) {
            DistanceUnit.KILOMETERS -> stringProvider.get(R.string.settings_unit_kilometers)
            DistanceUnit.MILES -> stringProvider.get(R.string.settings_unit_miles)
            null -> raw
        }
    }

    fun paceUnitLabel(raw: String): String {
        return when (runCatching { PaceUnit.valueOf(raw.uppercase(Locale.ENGLISH)) }.getOrNull()) {
            PaceUnit.MIN_PER_KM -> stringProvider.get(R.string.settings_unit_min_per_km)
            PaceUnit.MIN_PER_MI -> stringProvider.get(R.string.settings_unit_min_per_mi)
            null -> raw
        }
    }

    fun weightUnitLabel(raw: String): String {
        return when (runCatching { WeightUnit.valueOf(raw.uppercase(Locale.ENGLISH)) }.getOrNull()) {
            WeightUnit.KILOGRAMS -> stringProvider.get(R.string.settings_unit_kilograms)
            WeightUnit.POUNDS -> stringProvider.get(R.string.settings_unit_pounds)
            null -> raw
        }
    }

    fun dayLabel(raw: String?): String? {
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

    fun timeSlotLabel(raw: String?): String? {
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

    fun locationLabel(
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

    fun shouldSplitLines(actionType: UserActionType?): Boolean {
        return actionType in splitLineActions
    }

    fun formatDate(
        raw: String?,
        currentLocale: Locale,
        patternResId: Int = R.string.activity_week_date_pattern,
    ): String? {
        if (raw.isNullOrBlank()) return null
        val formatter =
            DateTimeFormatterBuilder()
                .appendPattern(stringProvider.get(patternResId))
                .toFormatter(currentLocale)

        return runCatching { LocalDate.parse(raw).format(formatter) }.getOrDefault(raw)
    }

    private fun normalizeDayToken(raw: String): String {
        val cleaned = raw.trim().replace(Regex("[^A-Za-z]"), EMPTY)

        if (cleaned.isBlank()) return raw

        val normalized = cleaned.uppercase(Locale.ENGLISH)
        val day =
            runCatching { DayOfWeek.valueOf(normalized) }.getOrNull()
                ?: DayOfWeek.entries.firstOrNull { dayOfWeek ->
                    dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                        .equals(cleaned, ignoreCase = true) ||
                        dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
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

    private companion object {
        const val DAY_NUMBER_MONDAY = 1
        const val DAY_NUMBER_TUESDAY = 2
        const val DAY_NUMBER_WEDNESDAY = 3
        const val DAY_NUMBER_THURSDAY = 4
        const val DAY_NUMBER_FRIDAY = 5
        const val DAY_NUMBER_SATURDAY = 6
        const val DAY_NUMBER_SUNDAY = 7
    }

    private val splitLineActions =
        setOf(
            UserActionType.MOVE_WORKOUT_BETWEEN_DAYS,
            UserActionType.REORDER_WORKOUT,
            UserActionType.UNDO_MOVE_WORKOUT_BETWEEN_DAYS,
            UserActionType.UNDO_REORDER_WORKOUT_SAME_DAY,
            UserActionType.MOVE_REST,
            UserActionType.REORDER_REST,
            UserActionType.UNDO_MOVE_REST,
            UserActionType.UNDO_REORDER_REST,
            UserActionType.MOVE_BUSY,
            UserActionType.REORDER_BUSY,
            UserActionType.UNDO_MOVE_BUSY,
            UserActionType.UNDO_REORDER_BUSY,
            UserActionType.MOVE_SICK,
            UserActionType.REORDER_SICK,
            UserActionType.UNDO_MOVE_SICK,
            UserActionType.UNDO_REORDER_SICK,
            UserActionType.CREATE_WORKOUT,
            UserActionType.UPDATE_WORKOUT,
            UserActionType.CHANGE_SLOT_MODE,
            UserActionType.CHANGE_WEEK_START,
        )
}
