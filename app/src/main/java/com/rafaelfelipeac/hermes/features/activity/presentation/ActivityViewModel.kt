package com.rafaelfelipeac.hermes.features.activity.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.UserActionMetadataKeys
import com.rafaelfelipeac.hermes.core.useraction.UserActionMetadataValues
import com.rafaelfelipeac.hermes.core.useraction.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.UserActionEntityType.*
import com.rafaelfelipeac.hermes.core.useraction.UserActionMetadataKeys.NEW_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.UserActionMetadataKeys.NEW_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.UserActionMetadataKeys.NEW_TYPE
import com.rafaelfelipeac.hermes.core.useraction.UserActionMetadataKeys.NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.UserActionMetadataKeys.OLD_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.UserActionMetadataKeys.OLD_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.UserActionMetadataKeys.OLD_TYPE
import com.rafaelfelipeac.hermes.core.useraction.UserActionMetadataKeys.OLD_VALUE
import com.rafaelfelipeac.hermes.core.useraction.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.UserActionRepository
import com.rafaelfelipeac.hermes.core.useraction.UserActionType
import com.rafaelfelipeac.hermes.core.useraction.UserActionType.*
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivitySectionUi
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.DayOfWeek.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel
    @Inject
    constructor(
        repository: UserActionRepository,
        private val stringProvider: StringProvider,
    ) : ViewModel() {
        private val locale = MutableStateFlow(Locale.getDefault())

        val state: StateFlow<ActivityState> =
            combine(repository.observeActions(), locale) { actions, currentLocale ->
                ActivityState(sections = buildSections(actions, currentLocale))
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MS),
                initialValue = ActivityState(),
            )

        fun updateLocale(currentLocale: Locale) {
            locale.value = currentLocale
        }

        private fun buildSections(
            actions: List<UserActionRecord>,
            currentLocale: Locale,
        ): List<ActivitySectionUi> {
            if (actions.isEmpty()) return emptyList()

            val zoneId = ZoneId.systemDefault()
            val grouped =
                actions.groupBy { action ->
                    Instant.ofEpochMilli(action.timestamp).atZone(zoneId).toLocalDate()
                }

            return grouped.entries
                .sortedByDescending { it.key }
                .map { (date, records) ->
                    ActivitySectionUi(
                        date = date,
                        items = records.map { record -> toUi(record, zoneId, currentLocale) },
                    )
                }
        }

        private fun toUi(
            record: UserActionRecord,
            zoneId: ZoneId,
            currentLocale: Locale,
        ): ActivityItemUi {
            val metadata = parseMetadata(record.metadata)
            val time =
                Instant.ofEpochMilli(record.timestamp)
                    .atZone(zoneId)
                    .format(buildTimeFormatter(currentLocale))
            val title = buildTitle(record, metadata)
            val subtitle = buildSubtitle(record, metadata, currentLocale)

            return ActivityItemUi(
                id = record.id,
                title = title,
                subtitle = subtitle,
                time = time,
            )
        }

        private fun buildTitle(
            record: UserActionRecord,
            metadata: Map<String, String>,
        ): String {
            val actionType = runCatching { UserActionType.valueOf(record.actionType) }.getOrNull()
            val entityType = runCatching { UserActionEntityType.valueOf(record.entityType) }.getOrNull()
            val isRestDay = entityType == REST_DAY
            val workoutName =
                metadata[NEW_TYPE]
                    ?.takeIf { it.isNotBlank() }
                    ?: metadata[NEW_DESCRIPTION]?.takeIf { it.isNotBlank() }
            val workoutLabel =
                workoutName
                    ?: metadata[OLD_TYPE]?.takeIf { it.isNotBlank() }
                    ?: metadata[OLD_DESCRIPTION]?.takeIf { it.isNotBlank() }
                    ?: stringProvider.get(R.string.activity_workout_fallback)
            val quotedWorkoutLabel = stringProvider.get(R.string.activity_value_quoted, workoutLabel)

            return when (actionType) {
                CREATE_WORKOUT ->
                    stringProvider.get(R.string.activity_action_create_workout, quotedWorkoutLabel)
                UPDATE_WORKOUT ->
                    stringProvider.get(R.string.activity_action_update_workout, quotedWorkoutLabel)
                DELETE_WORKOUT ->
                    stringProvider.get(R.string.activity_action_delete_workout, quotedWorkoutLabel)
                COMPLETE_WORKOUT ->
                    if (isRestDay) {
                        stringProvider.get(R.string.activity_action_complete_rest_day)
                    } else {
                        stringProvider.get(R.string.activity_action_complete_workout, quotedWorkoutLabel)
                    }
                INCOMPLETE_WORKOUT ->
                    if (isRestDay) {
                        stringProvider.get(R.string.activity_action_incomplete_rest_day)
                    } else {
                        stringProvider.get(R.string.activity_action_incomplete_workout, quotedWorkoutLabel)
                    }
                REORDER_WORKOUT ->
                    if (isRestDay) {
                        stringProvider.get(R.string.activity_action_reorder_rest_day)
                    } else {
                        stringProvider.get(R.string.activity_action_reorder_workout, quotedWorkoutLabel)
                    }
                MOVE_WORKOUT_BETWEEN_DAYS ->
                    if (isRestDay) {
                        stringProvider.get(R.string.activity_action_move_rest_day)
                    } else {
                        stringProvider.get(R.string.activity_action_move_workout, quotedWorkoutLabel)
                    }
                CREATE_REST_DAY ->
                    stringProvider.get(R.string.activity_action_create_rest_day)
                UPDATE_REST_DAY ->
                    stringProvider.get(R.string.activity_action_update_rest_day)
                DELETE_REST_DAY ->
                    stringProvider.get(R.string.activity_action_delete_rest_day)
                CONVERT_WORKOUT_TO_REST_DAY ->
                    stringProvider.get(
                        R.string.activity_action_convert_workout_to_rest_day,
                        quotedWorkoutLabel,
                    )
                CONVERT_REST_DAY_TO_WORKOUT ->
                    stringProvider.get(R.string.activity_action_convert_rest_day_to_workout)
                CHANGE_LANGUAGE ->
                    stringProvider.get(R.string.activity_action_change_language)
                CHANGE_THEME ->
                    stringProvider.get(R.string.activity_action_change_theme)
                OPEN_WEEK ->
                    stringProvider.get(R.string.activity_action_open_week)
                else -> stringProvider.get(R.string.activity_action_fallback)
            }
        }

        private fun buildSubtitle(
            record: UserActionRecord,
            metadata: Map<String, String>,
            currentLocale: Locale,
        ): String? {
            val actionType = runCatching { UserActionType.valueOf(record.actionType) }.getOrNull()
            val parts = mutableListOf<String>()
            val weekSubtitle = buildWeekSubtitle(metadata, currentLocale)

            weekSubtitle?.let(parts::add)

            when (actionType) {
                CHANGE_LANGUAGE,
                CHANGE_THEME,
                -> buildValueChangeSubtitle(metadata, actionType)?.let(parts::add)
                MOVE_WORKOUT_BETWEEN_DAYS -> {
                    val moveSubtitle = buildMoveSubtitle(metadata, currentLocale)
                    if (weekSubtitle != null && moveSubtitle != null) {
                        return "$weekSubtitle\n$moveSubtitle"
                    }
                    moveSubtitle?.let(parts::add)
                }
                REORDER_WORKOUT -> {
                    val reorderSubtitle = buildReorderSubtitle(metadata, currentLocale)
                    if (weekSubtitle != null && reorderSubtitle != null) {
                        return "$weekSubtitle\n$reorderSubtitle"
                    }
                    reorderSubtitle?.let(parts::add)
                }
                else -> Unit
            }

            return parts.takeIf { it.isNotEmpty() }
                ?.joinToString(stringProvider.get(R.string.activity_subtitle_separator))
        }

        private fun buildValueChangeSubtitle(
            metadata: Map<String, String>,
            actionType: UserActionType,
        ): String? {
            val oldValue = formatChangeValue(metadata[OLD_VALUE], actionType)
            val newValue = formatChangeValue(metadata[NEW_VALUE], actionType)

            if (oldValue.isNullOrBlank() && newValue.isNullOrBlank()) return null

            return stringProvider.get(
                R.string.activity_subtitle_change_value,
                oldValue.orEmpty(),
                newValue.orEmpty(),
            )
        }

        private fun buildMoveSubtitle(
            metadata: Map<String, String>,
            currentLocale: Locale,
        ): String? {
            val oldDay = dayLabel(metadata[OLD_DAY_OF_WEEK], currentLocale)
            val newDay = dayLabel(metadata[NEW_DAY_OF_WEEK], currentLocale)

            if (oldDay.isNullOrBlank() && newDay.isNullOrBlank()) return null

            return stringProvider.get(
                R.string.activity_subtitle_move,
                oldDay.orEmpty(),
                newDay.orEmpty(),
            )
        }

        private fun buildReorderSubtitle(
            metadata: Map<String, String>,
            currentLocale: Locale,
        ): String? {
            val oldDay = dayLabel(metadata[OLD_DAY_OF_WEEK], currentLocale)
            val newDay = dayLabel(metadata[NEW_DAY_OF_WEEK], currentLocale)

            if (oldDay.isNullOrBlank() && newDay.isNullOrBlank()) return null

            return stringProvider.get(
                R.string.activity_subtitle_move,
                oldDay.orEmpty(),
                newDay.orEmpty(),
            )
        }

        private fun buildWeekSubtitle(
            metadata: Map<String, String>,
            currentLocale: Locale,
        ): String? {
            val weekStart = metadata[WEEK_START_DATE] ?: return null
            val formatted =
                runCatching { LocalDate.parse(weekStart).format(buildWeekFormatter(currentLocale)) }
                    .getOrDefault(weekStart)

            return stringProvider.get(R.string.activity_subtitle_week, formatted)
        }

        private fun formatChangeValue(
            raw: String?,
            actionType: UserActionType,
        ): String? {
            if (raw.isNullOrBlank()) return stringProvider.get(R.string.activity_value_unknown)

            return when (actionType) {
                CHANGE_LANGUAGE -> languageLabel(raw)
                CHANGE_THEME -> themeLabel(raw)
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
                AppLanguage.PORTUGUESE_BRAZIL -> stringProvider.get(R.string.settings_language_portuguese_brazil)
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

        private fun dayLabel(
            raw: String?,
            currentLocale: Locale,
        ): String? {
            if (raw.isNullOrBlank()) return null
            if (raw == UserActionMetadataValues.UNPLANNED) {
                return stringProvider.get(R.string.activity_day_unplanned)
            }

            val dayNumber = raw.toIntOrNull() ?: return normalizeDayToken(raw, currentLocale)

            return when (dayNumber) {
                1 -> stringProvider.get(R.string.day_monday)
                2 -> stringProvider.get(R.string.day_tuesday)
                3 -> stringProvider.get(R.string.day_wednesday)
                4 -> stringProvider.get(R.string.day_thursday)
                5 -> stringProvider.get(R.string.day_friday)
                6 -> stringProvider.get(R.string.day_saturday)
                7 -> stringProvider.get(R.string.day_sunday)
                else -> raw
            }
        }

        private fun normalizeDayToken(
            raw: String,
            currentLocale: Locale,
        ): String {
            val cleaned = raw.trim().replace(Regex("[^A-Za-z]"), "")

            if (cleaned.isBlank()) return raw

            val normalized = cleaned.uppercase(Locale.ENGLISH)
            val day =
                runCatching { DayOfWeek.valueOf(normalized) }.getOrNull()
                    ?: DayOfWeek.entries.firstOrNull { dayOfWeek ->
                        dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH)
                            .equals(cleaned, ignoreCase = true) ||
                            dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
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

        private fun buildWeekFormatter(locale: Locale): DateTimeFormatter {
            val pattern = stringProvider.get(R.string.activity_week_date_pattern)

            return DateTimeFormatterBuilder()
                .appendPattern(pattern)
                .toFormatter(locale)
        }

        private fun buildTimeFormatter(locale: Locale): DateTimeFormatter {
            val pattern = stringProvider.get(R.string.activity_time_pattern)

            return DateTimeFormatterBuilder()
                .appendPattern(pattern)
                .toFormatter(locale)
        }

        private fun parseMetadata(raw: String?): Map<String, String> {
            if (raw.isNullOrBlank()) return emptyMap()

            val regex = "\"(.*?)\":\"(.*?)\"".toRegex()

            return regex.findAll(raw).associate { match ->
                val key = unescape(match.groupValues[1])
                val value = unescape(match.groupValues[2])
                key to value
            }
        }

        private fun unescape(value: String): String {
            return value
                .replace("\\\\", "\\")
                .replace("\\\"", "\"")
        }

        private companion object {
            const val STATE_SHARING_TIMEOUT_MS = 5_000L
        }
    }
