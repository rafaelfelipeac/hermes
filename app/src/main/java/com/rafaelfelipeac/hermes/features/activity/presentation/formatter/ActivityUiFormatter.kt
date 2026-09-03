@file:Suppress("LongMethod", "MaxLineLength")

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
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuantity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import com.rafaelfelipeac.hermes.features.personalrecords.presentation.formatPersonalRecordValue
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.ALWAYS_SHOW
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.AUTO_WHEN_MULTIPLE
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import java.text.NumberFormat
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

private typealias PersonalRecordUnit =
    com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit

@Suppress("CyclomaticComplexMethod", "LargeClass", "TooManyFunctions")
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
                UserActionEntityType.REST,
                UserActionEntityType.REST_DAY,
                UserActionEntityType.BUSY,
                UserActionEntityType.SICK,
                UserActionEntityType.RACE_EVENT,
                ->
                    buildNonWorkoutTitle(
                        entityType = entityType,
                        actionType = actionType,
                        quotedWorkoutLabel = quotedWorkoutLabel,
                    )
                UserActionEntityType.CHALLENGE -> buildChallengeTitle(actionType, metadata)
                UserActionEntityType.CATEGORY -> buildCategoryTitle(actionType, metadata)
                UserActionEntityType.TROPHY -> buildTrophyTitle(actionType, metadata)
                UserActionEntityType.PERSONAL_RECORD ->
                    buildPersonalRecordTitle(actionType, metadata)
                else -> buildWorkoutTitle(actionType, quotedWorkoutLabel)
            }

        return title ?: buildGlobalActionTitle(actionType)
    }

    fun buildSubtitle(
        record: UserActionRecord,
        metadata: Map<String, String>,
        currentLocale: Locale,
    ): String? {
        val actionType = runCatching { UserActionType.valueOf(record.actionType) }.getOrNull()
        val weekSubtitle = buildWeekSubtitle(metadata, currentLocale)
        val actionSubtitle = buildActionSubtitle(actionType, metadata, currentLocale)
        val entityType = runCatching { UserActionEntityType.valueOf(record.entityType) }.getOrNull()
        val challengeSubtitle =
            if (entityType == UserActionEntityType.CHALLENGE) {
                buildChallengeSubtitle(actionType, metadata, currentLocale)
            } else {
                null
            }

        return combineSubtitles(
            weekSubtitle = weekSubtitle,
            actionSubtitle = challengeSubtitle ?: actionSubtitle,
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

    private fun buildGlobalActionTitle(actionType: UserActionType?): String {
        return when (actionType) {
            UserActionType.CHANGE_LANGUAGE ->
                stringProvider.get(R.string.activity_action_change_language)

            UserActionType.CHANGE_THEME ->
                stringProvider.get(R.string.activity_action_change_theme)

            UserActionType.CHANGE_SLOT_MODE ->
                stringProvider.get(R.string.activity_action_change_slot_mode)

            UserActionType.CHANGE_WEEK_START ->
                stringProvider.get(R.string.activity_action_change_week_start)

            UserActionType.CHANGE_DISTANCE_UNIT ->
                stringProvider.get(R.string.activity_action_change_distance_unit)

            UserActionType.CHANGE_PACE_UNIT ->
                stringProvider.get(R.string.activity_action_change_pace_unit)

            UserActionType.CHANGE_WEIGHT_UNIT ->
                stringProvider.get(R.string.activity_action_change_weight_unit)

            UserActionType.EXPORT_BACKUP ->
                stringProvider.get(R.string.activity_action_export_backup)

            UserActionType.IMPORT_BACKUP ->
                stringProvider.get(R.string.activity_action_import_backup)

            UserActionType.SET_BACKUP_FOLDER ->
                stringProvider.get(R.string.activity_action_set_backup_folder)

            UserActionType.CLEAR_BACKUP_FOLDER ->
                stringProvider.get(R.string.activity_action_clear_backup_folder)

            UserActionType.SEED_DEMO_DATA ->
                stringProvider.get(R.string.activity_action_seed_demo_data)

            UserActionType.OPEN_WEEK ->
                stringProvider.get(R.string.activity_action_open_week)

            UserActionType.COPY_LAST_WEEK ->
                stringProvider.get(R.string.activity_action_copy_last_week)

            UserActionType.UNDO_COPY_LAST_WEEK ->
                stringProvider.get(R.string.activity_action_undo_copy_last_week)

            UserActionType.COMPLETE_WEEK_WORKOUTS ->
                stringProvider.get(R.string.activity_action_complete_week_workouts)

            UserActionType.CREATE_PERSONAL_RECORD_FAMILY ->
                stringProvider.get(R.string.activity_action_create_personal_record_family)

            UserActionType.UPDATE_PERSONAL_RECORD_FAMILY ->
                stringProvider.get(R.string.activity_action_update_personal_record_family)

            UserActionType.DELETE_PERSONAL_RECORD_FAMILY ->
                stringProvider.get(R.string.activity_action_delete_personal_record_family)

            UserActionType.CREATE_PERSONAL_RECORD_ENTRY ->
                stringProvider.get(R.string.activity_action_create_personal_record_entry)

            UserActionType.UPDATE_PERSONAL_RECORD_ENTRY ->
                stringProvider.get(R.string.activity_action_update_personal_record_entry)

            UserActionType.DELETE_PERSONAL_RECORD_ENTRY ->
                stringProvider.get(R.string.activity_action_delete_personal_record_entry)

            UserActionType.SET_CURRENT_PERSONAL_RECORD_ENTRY ->
                stringProvider.get(R.string.activity_action_set_current_personal_record_entry)

            UserActionType.USE_PACE_CALCULATOR ->
                stringProvider.get(R.string.activity_action_use_pace_calculator)

            UserActionType.CREATE_CHALLENGE ->
                stringProvider.get(R.string.activity_action_create_challenge)

            UserActionType.UPDATE_CHALLENGE ->
                stringProvider.get(R.string.activity_action_update_challenge)

            UserActionType.ARCHIVE_CHALLENGE ->
                stringProvider.get(R.string.activity_action_archive_challenge)

            UserActionType.REACTIVATE_CHALLENGE ->
                stringProvider.get(R.string.activity_action_reactivate_challenge)

            UserActionType.DELETE_CHALLENGE ->
                stringProvider.get(R.string.activity_action_delete_challenge)

            UserActionType.CREATE_CHALLENGE_PROGRESS_ENTRY ->
                stringProvider.get(R.string.activity_action_create_challenge_progress_entry)

            UserActionType.UPDATE_CHALLENGE_PROGRESS_ENTRY ->
                stringProvider.get(R.string.activity_action_update_challenge_progress_entry)

            UserActionType.DELETE_CHALLENGE_PROGRESS_ENTRY ->
                stringProvider.get(R.string.activity_action_delete_challenge_progress_entry)

            UserActionType.RESTORE_CHALLENGE ->
                stringProvider.get(R.string.activity_action_restore_challenge)

            UserActionType.RESTORE_CHALLENGE_PROGRESS_ENTRY ->
                stringProvider.get(R.string.activity_action_restore_challenge_progress_entry)

            else -> stringProvider.get(R.string.activity_action_fallback)
        }
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
            UserActionType.CHANGE_WEEK_START -> weekStartDayLabel(raw)
            UserActionType.CHANGE_DISTANCE_UNIT -> distanceUnitLabel(raw)
            UserActionType.CHANGE_PACE_UNIT -> paceUnitLabel(raw)
            UserActionType.CHANGE_WEIGHT_UNIT -> weightUnitLabel(raw)
            else -> raw
        }
    }

    private fun slotModeLabel(raw: String): String {
        return when (raw.uppercase(Locale.ENGLISH)) {
            AUTO_WHEN_MULTIPLE.name -> stringProvider.get(R.string.settings_slot_mode_auto)
            ALWAYS_SHOW.name -> stringProvider.get(R.string.settings_slot_mode_always)
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

    private fun buildTrophyTitle(
        actionType: UserActionType?,
        metadata: Map<String, String>,
    ): String? {
        val label =
            metadata[UserActionMetadataKeys.TROPHY_NAME]
                ?.takeIf { it.isNotBlank() }
                ?: stringProvider.get(R.string.activity_value_unknown)

        return when (actionType) {
            UserActionType.SHARE_TROPHY ->
                stringProvider.get(
                    R.string.activity_action_share_trophy,
                    quoteValue(label) ?: label,
                )

            else -> null
        }
    }

    private fun buildPersonalRecordTitle(
        actionType: UserActionType?,
        metadata: Map<String, String>,
    ): String? {
        val metricLabel =
            personalRecordMetricLabel(metadata[UserActionMetadataKeys.PERSONAL_RECORD_METRIC_TYPE])
                ?: stringProvider.get(R.string.personal_records_metric_custom)
        val entryLabel =
            metadata[UserActionMetadataKeys.PERSONAL_RECORD_FAMILY_TITLE]
                ?.takeIf { it.isNotBlank() }
                ?: metricLabel

        return when (actionType) {
            UserActionType.CREATE_PERSONAL_RECORD_FAMILY ->
                stringProvider.get(R.string.activity_action_create_personal_record_family, metricLabel)

            UserActionType.UPDATE_PERSONAL_RECORD_FAMILY ->
                stringProvider.get(R.string.activity_action_update_personal_record_family, metricLabel)

            UserActionType.DELETE_PERSONAL_RECORD_FAMILY ->
                stringProvider.get(R.string.activity_action_delete_personal_record_family, metricLabel)

            UserActionType.CREATE_PERSONAL_RECORD_ENTRY ->
                stringProvider.get(R.string.activity_action_create_personal_record_entry, entryLabel)

            UserActionType.UPDATE_PERSONAL_RECORD_ENTRY ->
                stringProvider.get(R.string.activity_action_update_personal_record_entry, entryLabel)

            UserActionType.DELETE_PERSONAL_RECORD_ENTRY ->
                stringProvider.get(R.string.activity_action_delete_personal_record_entry, entryLabel)

            UserActionType.SET_CURRENT_PERSONAL_RECORD_ENTRY ->
                stringProvider.get(R.string.activity_action_set_current_personal_record_entry, entryLabel)

            else -> null
        }
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
        currentLocale: Locale,
    ): String? {
        return when (actionType) {
            UserActionType.CHANGE_LANGUAGE,
            UserActionType.CHANGE_THEME,
            UserActionType.CHANGE_SLOT_MODE,
            UserActionType.CHANGE_WEEK_START,
            UserActionType.CHANGE_DISTANCE_UNIT,
            UserActionType.CHANGE_PACE_UNIT,
            UserActionType.CHANGE_WEIGHT_UNIT,
            UserActionType.UPDATE_CATEGORY_NAME,
            -> buildValueChangeSubtitle(metadata, actionType)

            UserActionType.UPDATE_CATEGORY_VISIBILITY ->
                buildCategoryVisibilitySubtitle(metadata)

            UserActionType.CREATE_WORKOUT ->
                buildWorkoutCategorySubtitle(metadata)

            UserActionType.UPDATE_WORKOUT ->
                buildWorkoutCategoryChangeSubtitle(metadata)

            UserActionType.SHARE_TROPHY ->
                buildWorkoutCategorySubtitle(metadata)

            UserActionType.CREATE_PERSONAL_RECORD_FAMILY,
            UserActionType.UPDATE_PERSONAL_RECORD_FAMILY,
            UserActionType.DELETE_PERSONAL_RECORD_FAMILY,
            -> buildPersonalRecordFamilySubtitle(metadata)

            UserActionType.CREATE_PERSONAL_RECORD_ENTRY,
            UserActionType.UPDATE_PERSONAL_RECORD_ENTRY,
            UserActionType.DELETE_PERSONAL_RECORD_ENTRY,
            UserActionType.SET_CURRENT_PERSONAL_RECORD_ENTRY,
            -> buildPersonalRecordEntrySubtitle(actionType, metadata, currentLocale)

            UserActionType.MOVE_WORKOUT_BETWEEN_DAYS,
            UserActionType.MOVE_REST,
            UserActionType.MOVE_BUSY,
            UserActionType.MOVE_SICK,
            -> buildMoveSubtitle(metadata)
            UserActionType.REORDER_WORKOUT,
            UserActionType.REORDER_REST,
            UserActionType.REORDER_BUSY,
            UserActionType.REORDER_SICK,
            -> buildReorderSubtitle(metadata)
            UserActionType.UNDO_MOVE_WORKOUT_BETWEEN_DAYS,
            UserActionType.UNDO_MOVE_REST,
            UserActionType.UNDO_MOVE_BUSY,
            UserActionType.UNDO_MOVE_SICK,
            -> buildMoveSubtitle(metadata)
            UserActionType.UNDO_REORDER_WORKOUT_SAME_DAY,
            UserActionType.UNDO_REORDER_REST,
            UserActionType.UNDO_REORDER_BUSY,
            UserActionType.UNDO_REORDER_SICK,
            -> buildReorderSubtitle(metadata)
            UserActionType.CREATE_CHALLENGE,
            UserActionType.UPDATE_CHALLENGE,
            UserActionType.ARCHIVE_CHALLENGE,
            UserActionType.REACTIVATE_CHALLENGE,
            UserActionType.DELETE_CHALLENGE,
            UserActionType.CREATE_CHALLENGE_PROGRESS_ENTRY,
            UserActionType.UPDATE_CHALLENGE_PROGRESS_ENTRY,
            UserActionType.DELETE_CHALLENGE_PROGRESS_ENTRY,
            UserActionType.RESTORE_CHALLENGE,
            UserActionType.RESTORE_CHALLENGE_PROGRESS_ENTRY,
            -> buildChallengeSubtitle(actionType, metadata, currentLocale)
            else -> null
        }
    }

    private fun buildChallengeTitle(
        actionType: UserActionType?,
        metadata: Map<String, String>,
    ): String? {
        val label = challengeTitleLabel(metadata)
        val quotedLabel = label?.let { quoteValue(it) ?: it }

        if (actionType.isChallengeProgressAction() && metadata.isChallengeCompletionTransition()) {
            return challengeTitleOrFallback(
                quotedLabel,
                R.string.activity_action_complete_challenge_named,
                R.string.activity_action_complete_challenge,
            )
        }

        return when (actionType) {
            UserActionType.CREATE_CHALLENGE ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_create_challenge_named,
                    R.string.activity_action_create_challenge,
                )
            UserActionType.UPDATE_CHALLENGE ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_update_challenge_named,
                    R.string.activity_action_update_challenge,
                )
            UserActionType.ARCHIVE_CHALLENGE ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_archive_challenge_named,
                    R.string.activity_action_archive_challenge,
                )
            UserActionType.REACTIVATE_CHALLENGE ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_reactivate_challenge_named,
                    R.string.activity_action_reactivate_challenge,
                )
            UserActionType.DELETE_CHALLENGE ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_delete_challenge_named,
                    R.string.activity_action_delete_challenge,
                )
            UserActionType.CREATE_CHALLENGE_PROGRESS_ENTRY ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_create_challenge_progress_entry_named,
                    R.string.activity_action_create_challenge_progress_entry,
                )
            UserActionType.UPDATE_CHALLENGE_PROGRESS_ENTRY ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_update_challenge_progress_entry_named,
                    R.string.activity_action_update_challenge_progress_entry,
                )
            UserActionType.DELETE_CHALLENGE_PROGRESS_ENTRY ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_delete_challenge_progress_entry_named,
                    R.string.activity_action_delete_challenge_progress_entry,
                )
            UserActionType.RESTORE_CHALLENGE ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_restore_challenge_named,
                    R.string.activity_action_restore_challenge,
                )
            UserActionType.RESTORE_CHALLENGE_PROGRESS_ENTRY ->
                challengeTitleOrFallback(
                    quotedLabel,
                    R.string.activity_action_restore_challenge_progress_entry_named,
                    R.string.activity_action_restore_challenge_progress_entry,
                )
            else -> null
        }
    }

    private fun challengeTitleOrFallback(
        quotedLabel: String?,
        namedTitleRes: Int,
        fallbackTitleRes: Int,
    ): String {
        return if (quotedLabel == null) {
            stringProvider.get(fallbackTitleRes)
        } else {
            stringProvider.get(namedTitleRes, quotedLabel)
        }
    }

    private fun UserActionType?.isChallengeProgressAction(): Boolean {
        return this in challengeProgressActions
    }

    private fun Map<String, String>.isChallengeCompletionTransition(): Boolean {
        return this[UserActionMetadataKeys.WAS_COMPLETED] == false.toString() &&
            this[UserActionMetadataKeys.IS_COMPLETED] == true.toString()
    }

    private fun buildChallengeSubtitle(
        actionType: UserActionType?,
        metadata: Map<String, String>,
        currentLocale: Locale,
    ): String? {
        val targetQuantity =
            formatChallengeQuantity(
                metadata[UserActionMetadataKeys.CHALLENGE_TARGET_QUANTITY]
                    ?: metadata[UserActionMetadataKeys.CHALLENGE_NEW_VALUE]
                    ?: metadata[UserActionMetadataKeys.NEW_VALUE],
                currentLocale,
            )
        val targetType = challengeTargetType(metadata[UserActionMetadataKeys.CHALLENGE_TARGET_TYPE])
        val oldTargetType = challengeTargetType(metadata[UserActionMetadataKeys.OLD_TYPE])
        val newTargetType =
            challengeTargetType(metadata[UserActionMetadataKeys.NEW_TYPE])
                ?: targetType
        val oldQuantity =
            formatChallengeQuantity(
                metadata[UserActionMetadataKeys.CHALLENGE_OLD_VALUE]
                    ?: metadata[UserActionMetadataKeys.OLD_VALUE],
                currentLocale,
            )
        val startDate =
            formatChallengeDate(metadata[UserActionMetadataKeys.CHALLENGE_START_DATE], currentLocale)
        val endDate =
            formatChallengeDate(metadata[UserActionMetadataKeys.CHALLENGE_END_DATE], currentLocale)
        val progressQuantity =
            formatChallengeQuantity(metadata[UserActionMetadataKeys.CHALLENGE_PROGRESS_QUANTITY], currentLocale)
        val progressDate =
            formatChallengeDate(metadata[UserActionMetadataKeys.CHALLENGE_PROGRESS_DATE], currentLocale)
        val oldProgressQuantity =
            formatChallengeQuantity(metadata[UserActionMetadataKeys.CHALLENGE_OLD_VALUE], currentLocale)
        val newProgressQuantity =
            formatChallengeQuantity(
                metadata[UserActionMetadataKeys.CHALLENGE_NEW_VALUE]
                    ?: metadata[UserActionMetadataKeys.CHALLENGE_PROGRESS_QUANTITY],
                currentLocale,
            )
        val oldProgressDate =
            formatChallengeDate(metadata[UserActionMetadataKeys.CHALLENGE_OLD_DATE], currentLocale)
        val newProgressDate =
            formatChallengeDate(
                metadata[UserActionMetadataKeys.CHALLENGE_NEW_DATE]
                    ?: metadata[UserActionMetadataKeys.CHALLENGE_PROGRESS_DATE],
                currentLocale,
            )
        val recovered = metadata[UserActionMetadataKeys.CHALLENGE_RECOVERED]?.toBooleanStrictOrNull() == true
        val oldCategory = metadata[UserActionMetadataKeys.OLD_CATEGORY_NAME]?.takeIf { it.isNotBlank() }
        val newCategory = metadata[UserActionMetadataKeys.NEW_CATEGORY_NAME]?.takeIf { it.isNotBlank() }
        val category =
            metadata[UserActionMetadataKeys.CHALLENGE_CATEGORY_NAME]
                ?.takeIf { it.isNotBlank() }
                ?: newCategory
                ?: oldCategory

        return when (actionType) {
            UserActionType.CREATE_CHALLENGE,
            -> {
                val details =
                    buildList {
                        challengeTargetSubtitle(targetType, targetQuantity)?.let(::add)
                        challengeDateSubtitle(startDate, endDate)?.let(::add)
                        challengeCategorySubtitle(category)?.let(::add)
                    }

                details.takeIf { it.isNotEmpty() }?.joinToString(activitySubtitleSeparator())
            }
            UserActionType.UPDATE_CHALLENGE -> {
                val details =
                    buildList {
                        challengeTargetChangeSubtitle(
                            oldTargetType = oldTargetType ?: targetType,
                            oldQuantity = oldQuantity,
                            newTargetType = newTargetType,
                            newQuantity = targetQuantity,
                        )?.let(::add)
                        challengeDateSubtitle(startDate, endDate)?.let(::add)
                        if (oldCategory.isNullOrBlank() || newCategory.isNullOrBlank() || oldCategory == newCategory) {
                            challengeCategorySubtitle(category)?.let(::add)
                        } else {
                            val oldQuoted = quoteValue(oldCategory)
                            val newQuoted = quoteValue(newCategory)
                            if (oldQuoted != null && newQuoted != null) {
                                add(
                                    stringProvider.get(
                                        R.string.activity_subtitle_challenge_category_change,
                                        oldQuoted,
                                        newQuoted,
                                    ),
                                )
                            }
                        }
                    }

                details.takeIf { it.isNotEmpty() }?.joinToString(activitySubtitleSeparator())
            }
            UserActionType.ARCHIVE_CHALLENGE,
            UserActionType.REACTIVATE_CHALLENGE,
            UserActionType.DELETE_CHALLENGE,
            -> {
                val details =
                    buildList {
                        challengeTargetSubtitle(targetType, targetQuantity)?.let(::add)
                        challengeDateSubtitle(startDate, endDate)?.let(::add)
                        challengeCategorySubtitle(category)?.let(::add)
                    }

                details.takeIf { it.isNotEmpty() }?.joinToString(activitySubtitleSeparator())
            }
            UserActionType.CREATE_CHALLENGE_PROGRESS_ENTRY,
            UserActionType.UPDATE_CHALLENGE_PROGRESS_ENTRY,
            UserActionType.DELETE_CHALLENGE_PROGRESS_ENTRY,
            UserActionType.RESTORE_CHALLENGE_PROGRESS_ENTRY,
            -> {
                val details =
                    buildList {
                        challengeProgressActionSubtitle(
                            actionType = actionType,
                            values =
                                ChallengeProgressSubtitleValues(
                                    progressQuantity = progressQuantity,
                                    progressDate = progressDate,
                                    oldProgressQuantity = oldProgressQuantity,
                                    oldProgressDate = oldProgressDate,
                                    newProgressQuantity = newProgressQuantity,
                                    newProgressDate = newProgressDate,
                                ),
                        )?.let(::add)
                        challengeTargetSubtitle(targetType, targetQuantity)?.let(::add)
                        challengeCategorySubtitle(category)?.let(::add)
                        challengeDateSubtitle(startDate, endDate)?.let(::add)
                        if (recovered) {
                            add(stringProvider.get(R.string.activity_subtitle_challenge_recovered))
                        }
                    }

                details.takeIf { it.isNotEmpty() }?.joinToString(activitySubtitleSeparator())
            }
            UserActionType.RESTORE_CHALLENGE -> null
            else -> null
        }
    }

    private data class ChallengeProgressSubtitleValues(
        val progressQuantity: String?,
        val progressDate: String?,
        val oldProgressQuantity: String?,
        val oldProgressDate: String?,
        val newProgressQuantity: String?,
        val newProgressDate: String?,
    ) {
        val hasPreviousProgress: Boolean =
            !oldProgressQuantity.isNullOrBlank() ||
                !oldProgressDate.isNullOrBlank()
    }

    private fun challengeProgressActionSubtitle(
        actionType: UserActionType,
        values: ChallengeProgressSubtitleValues,
    ): String? {
        return when (actionType) {
            UserActionType.CREATE_CHALLENGE_PROGRESS_ENTRY ->
                challengeProgressQuantityDateSubtitle(
                    R.string.activity_subtitle_challenge_progress_added,
                    values.progressQuantity,
                    values.progressDate,
                )
            UserActionType.UPDATE_CHALLENGE_PROGRESS_ENTRY -> {
                if (values.hasPreviousProgress) {
                    stringProvider.get(
                        R.string.activity_subtitle_challenge_progress_updated,
                        values.oldProgressQuantity.orEmpty(),
                        values.oldProgressDate.orEmpty(),
                        values.newProgressQuantity.orEmpty(),
                        values.newProgressDate.orEmpty(),
                    )
                } else {
                    challengeProgressQuantityDateSubtitle(
                        R.string.activity_subtitle_challenge_progress_updated_current,
                        values.newProgressQuantity,
                        values.newProgressDate,
                    )
                }
            }
            UserActionType.DELETE_CHALLENGE_PROGRESS_ENTRY ->
                challengeProgressQuantityDateSubtitle(
                    R.string.activity_subtitle_challenge_progress_deleted,
                    values.progressQuantity,
                    values.progressDate,
                )
            UserActionType.RESTORE_CHALLENGE_PROGRESS_ENTRY ->
                challengeProgressQuantityDateSubtitle(
                    R.string.activity_subtitle_challenge_progress_restored,
                    values.progressQuantity,
                    values.progressDate,
                )
            else -> null
        }
    }

    private fun challengeProgressQuantityDateSubtitle(
        subtitleRes: Int,
        progressQuantity: String?,
        progressDate: String?,
    ): String? {
        if (progressQuantity.isNullOrBlank() && progressDate.isNullOrBlank()) return null
        return stringProvider.get(
            subtitleRes,
            progressQuantity.orEmpty(),
            progressDate.orEmpty(),
        )
    }

    private fun challengeTitleLabel(metadata: Map<String, String>): String? {
        return metadata[UserActionMetadataKeys.CHALLENGE_TITLE]
            ?.takeIf { it.isNotBlank() }
    }

    private fun challengeTargetType(raw: String?): ChallengeTargetType? {
        return raw
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { ChallengeTargetType.valueOf(it) }.getOrNull() }
    }

    private fun challengeTargetSubtitle(
        targetType: ChallengeTargetType?,
        quantity: String?,
    ): String? {
        if (quantity.isNullOrBlank()) return null
        return when (targetType ?: ChallengeTargetType.DAILY) {
            ChallengeTargetType.DAILY ->
                stringProvider.get(R.string.activity_subtitle_challenge_target_daily, quantity)
            ChallengeTargetType.TOTAL ->
                stringProvider.get(R.string.activity_subtitle_challenge_target_total, quantity)
        }
    }

    private fun challengeTargetChangeSubtitle(
        oldTargetType: ChallengeTargetType?,
        oldQuantity: String?,
        newTargetType: ChallengeTargetType?,
        newQuantity: String?,
    ): String? {
        val oldValue = challengeTargetValue(oldTargetType, oldQuantity)
        val newValue = challengeTargetValue(newTargetType, newQuantity)
        if (oldValue.isNullOrBlank() || newValue.isNullOrBlank() || oldValue == newValue) return null
        return stringProvider.get(
            R.string.activity_subtitle_challenge_target_change,
            oldValue,
            newValue,
        )
    }

    private fun challengeTargetValue(
        targetType: ChallengeTargetType?,
        quantity: String?,
    ): String? {
        if (quantity.isNullOrBlank()) return null
        return when (targetType ?: ChallengeTargetType.DAILY) {
            ChallengeTargetType.DAILY ->
                stringProvider.get(R.string.activity_subtitle_challenge_target_value_daily, quantity)
            ChallengeTargetType.TOTAL ->
                stringProvider.get(R.string.activity_subtitle_challenge_target_value_total, quantity)
        }
    }

    private fun challengeDateSubtitle(
        startDate: String?,
        endDate: String?,
    ): String? {
        if (startDate == null && endDate == null) return null
        return stringProvider.get(
            R.string.activity_subtitle_challenge_dates,
            startDate.orEmpty(),
            endDate.orEmpty(),
        )
    }

    private fun challengeCategorySubtitle(category: String?): String? {
        return category
            ?.let(::quoteValue)
            ?.let { quotedCategory ->
                stringProvider.get(
                    R.string.activity_subtitle_challenge_category,
                    quotedCategory,
                )
            }
    }

    private fun formatChallengeQuantity(
        raw: String?,
        currentLocale: Locale,
    ): String? {
        val scaled = raw?.toLongOrNull() ?: return null
        return ChallengeQuantity.format(scaled, currentLocale)
    }

    private fun formatChallengeDate(
        raw: String?,
        currentLocale: Locale,
    ): String? {
        if (raw.isNullOrBlank()) return null
        val pattern = stringProvider.get(R.string.activity_week_date_pattern)
        val formatter =
            DateTimeFormatterBuilder()
                .appendPattern(pattern)
                .toFormatter(currentLocale)
        return runCatching { LocalDate.parse(raw).format(formatter) }.getOrNull()
    }

    private fun shouldSplitLines(actionType: UserActionType?): Boolean {
        return actionType == UserActionType.MOVE_WORKOUT_BETWEEN_DAYS ||
            actionType == UserActionType.REORDER_WORKOUT ||
            actionType == UserActionType.UNDO_MOVE_WORKOUT_BETWEEN_DAYS ||
            actionType == UserActionType.UNDO_REORDER_WORKOUT_SAME_DAY ||
            actionType == UserActionType.MOVE_REST ||
            actionType == UserActionType.REORDER_REST ||
            actionType == UserActionType.UNDO_MOVE_REST ||
            actionType == UserActionType.UNDO_REORDER_REST ||
            actionType == UserActionType.MOVE_BUSY ||
            actionType == UserActionType.REORDER_BUSY ||
            actionType == UserActionType.UNDO_MOVE_BUSY ||
            actionType == UserActionType.UNDO_REORDER_BUSY ||
            actionType == UserActionType.MOVE_SICK ||
            actionType == UserActionType.REORDER_SICK ||
            actionType == UserActionType.UNDO_MOVE_SICK ||
            actionType == UserActionType.UNDO_REORDER_SICK ||
            actionType == UserActionType.CREATE_WORKOUT ||
            actionType == UserActionType.UPDATE_WORKOUT ||
            actionType == UserActionType.CHANGE_SLOT_MODE ||
            actionType == UserActionType.CHANGE_WEEK_START
    }

    private fun buildPersonalRecordFamilySubtitle(metadata: Map<String, String>): String? {
        val category =
            metadata[UserActionMetadataKeys.PERSONAL_RECORD_CATEGORY_NAME]
                ?.takeIf { it.isNotBlank() }
                ?: stringProvider.get(R.string.category_uncategorized)
        val metricType = personalRecordMetricLabel(metadata[UserActionMetadataKeys.PERSONAL_RECORD_METRIC_TYPE])
        val comparisonRule =
            personalRecordComparisonRuleLabel(
                metadata[UserActionMetadataKeys.PERSONAL_RECORD_COMPARISON_RULE],
            )

        if (metricType == null && comparisonRule == null) return null

        return listOfNotNull(
            quoteValue(category),
            metricType,
            comparisonRule,
        ).joinToString(activitySubtitleSeparator())
    }

    @Suppress("LongMethod")
    private fun buildPersonalRecordEntrySubtitle(
        actionType: UserActionType?,
        metadata: Map<String, String>,
        currentLocale: Locale,
    ): String? {
        val unit = metadata[UserActionMetadataKeys.PERSONAL_RECORD_UNIT]
        val newValue =
            personalRecordValueLabel(
                metadata[UserActionMetadataKeys.PERSONAL_RECORD_NEW_VALUE],
                unit,
                currentLocale,
            )
        val oldValue =
            personalRecordValueLabel(
                metadata[UserActionMetadataKeys.PERSONAL_RECORD_OLD_VALUE],
                unit,
                currentLocale,
            )
        val normalizedValue =
            metadata[UserActionMetadataKeys.PERSONAL_RECORD_NORMALIZED_VALUE]
                ?.takeIf { it.isNotBlank() }
                ?.toDoubleOrNull()
                ?.let { NumberFormat.getNumberInstance(currentLocale).format(it) }
        val recordDate =
            personalRecordDateLabel(
                metadata[UserActionMetadataKeys.PERSONAL_RECORD_RECORD_DATE],
                currentLocale,
            )

        return when (actionType) {
            UserActionType.UPDATE_PERSONAL_RECORD_ENTRY -> {
                val valueChange =
                    when {
                        !oldValue.isNullOrBlank() && !newValue.isNullOrBlank() ->
                            stringProvider.get(
                                R.string.activity_subtitle_change_value,
                                quoteValue(oldValue).orEmpty(),
                                quoteValue(newValue).orEmpty(),
                            )
                        !newValue.isNullOrBlank() && !normalizedValue.isNullOrBlank() -> newValue
                        else -> newValue ?: normalizedValue
                    }

                listOfNotNull(valueChange, recordDate)
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(activitySubtitleSeparator())
            }

            UserActionType.CREATE_PERSONAL_RECORD_ENTRY,
            UserActionType.DELETE_PERSONAL_RECORD_ENTRY,
            UserActionType.SET_CURRENT_PERSONAL_RECORD_ENTRY,
            -> {
                val valueWithUnit = newValue ?: normalizedValue

                listOfNotNull(valueWithUnit, recordDate)
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(activitySubtitleSeparator())
            }

            else -> null
        }
    }

    private fun personalRecordDateLabel(
        raw: String?,
        currentLocale: Locale,
    ): String? =
        raw
            ?.takeIf { it.isNotBlank() }
            ?.let { value ->
                runCatching {
                    LocalDate.parse(value).format(
                        DateTimeFormatterBuilder()
                            .appendPattern(stringProvider.get(R.string.activity_week_date_pattern))
                            .toFormatter(currentLocale),
                    )
                }.getOrDefault(value)
            }

    private fun personalRecordMetricLabel(raw: String?): String? {
        return when (raw?.uppercase(Locale.ENGLISH)) {
            "DISTANCE" -> stringProvider.get(R.string.personal_records_metric_distance)
            "TIME" -> stringProvider.get(R.string.personal_records_metric_time)
            "WEIGHT" -> stringProvider.get(R.string.personal_records_metric_weight)
            "POWER" -> stringProvider.get(R.string.personal_records_metric_power)
            "REPS" -> stringProvider.get(R.string.personal_records_metric_reps)
            "CUSTOM" -> stringProvider.get(R.string.personal_records_metric_custom)
            else -> raw
        }
    }

    private fun personalRecordComparisonRuleLabel(raw: String?): String? {
        return when (raw?.uppercase(Locale.ENGLISH)) {
            "HIGHER_IS_BETTER" -> stringProvider.get(R.string.personal_records_comparison_higher)
            "LOWER_IS_BETTER" -> stringProvider.get(R.string.personal_records_comparison_lower)
            "MANUAL" -> stringProvider.get(R.string.personal_records_comparison_manual)
            else -> raw
        }
    }

    private fun personalRecordValueLabel(
        raw: String?,
        unit: String?,
        currentLocale: Locale,
    ): String? {
        val value = raw?.takeIf { it.isNotBlank() } ?: return null
        val parsed = value.toDoubleOrNull()
        return when {
            parsed != null ->
                formatPersonalRecordValue(
                    value = parsed,
                    unit = personalRecordUnit(unit),
                    locale = currentLocale,
                    unitLabel = personalRecordUnitLabel(unit, parsed),
                )

            else -> value
        }
    }

    private fun personalRecordUnitLabel(
        raw: String?,
        value: Double? = null,
    ): String? {
        return when (raw?.uppercase(Locale.ENGLISH)) {
            "KILOMETER" -> stringProvider.get(R.string.settings_unit_kilometers)
            "MILE" -> stringProvider.get(R.string.settings_unit_miles)
            "METER" -> stringProvider.get(R.string.personal_records_unit_meter_symbol)
            "SECOND" -> null
            "MINUTE" -> null
            "HOUR" -> null
            "KILOGRAM" -> stringProvider.get(R.string.settings_unit_kilograms)
            "POUND" -> stringProvider.get(R.string.settings_unit_pounds)
            "WATT" -> stringProvider.get(R.string.personal_records_unit_watt_symbol)
            "REP" ->
                if (value == 1.0) {
                    stringProvider.get(R.string.personal_records_unit_rep_singular)
                } else {
                    stringProvider.get(R.string.personal_records_unit_rep_plural)
                }
            else -> null
        }
    }

    private fun personalRecordUnit(raw: String?): PersonalRecordUnit {
        return runCatching {
            PersonalRecordUnit.valueOf(
                raw?.uppercase(Locale.ENGLISH).orEmpty(),
            )
        }.getOrDefault(PersonalRecordUnit.CUSTOM)
    }

    private fun distanceUnitLabel(raw: String): String {
        return when (runCatching { DistanceUnit.valueOf(raw.uppercase(Locale.ENGLISH)) }.getOrNull()) {
            DistanceUnit.KILOMETERS -> stringProvider.get(R.string.settings_unit_kilometers)
            DistanceUnit.MILES -> stringProvider.get(R.string.settings_unit_miles)
            null -> raw
        }
    }

    private fun paceUnitLabel(raw: String): String {
        return when (runCatching { PaceUnit.valueOf(raw.uppercase(Locale.ENGLISH)) }.getOrNull()) {
            PaceUnit.MIN_PER_KM -> stringProvider.get(R.string.settings_unit_min_per_km)
            PaceUnit.MIN_PER_MI -> stringProvider.get(R.string.settings_unit_min_per_mi)
            null -> raw
        }
    }

    private fun weightUnitLabel(raw: String): String {
        return when (runCatching { WeightUnit.valueOf(raw.uppercase(Locale.ENGLISH)) }.getOrNull()) {
            WeightUnit.KILOGRAMS -> stringProvider.get(R.string.settings_unit_kilograms)
            WeightUnit.POUNDS -> stringProvider.get(R.string.settings_unit_pounds)
            null -> raw
        }
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
                ?.joinToString(activitySubtitleSeparator())
        }
    }

    private fun activitySubtitleSeparator(): String {
        return stringProvider.get(R.string.activity_subtitle_separator)
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

    private fun weekStartDayLabel(raw: String): String {
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

    @Suppress("CyclomaticComplexMethod", "ReturnCount")
    private fun buildNonWorkoutTitle(
        entityType: UserActionEntityType,
        actionType: UserActionType?,
        quotedWorkoutLabel: String,
    ): String? {
        if (actionType == null) return null

        val simpleResId =
            when (actionType) {
                UserActionType.COMPLETE_WORKOUT -> completeNonWorkoutRes(entityType)
                UserActionType.INCOMPLETE_WORKOUT -> incompleteNonWorkoutRes(entityType)
                UserActionType.COMPLETE_RACE_EVENT -> completeNonWorkoutRes(entityType)
                UserActionType.INCOMPLETE_RACE_EVENT -> incompleteNonWorkoutRes(entityType)
                UserActionType.UNDO_COMPLETE_RACE_EVENT -> R.string.activity_action_undo_complete_race_event
                UserActionType.UNDO_INCOMPLETE_RACE_EVENT -> R.string.activity_action_undo_incomplete_race_event
                UserActionType.REORDER_WORKOUT -> reorderNonWorkoutRes(entityType)
                UserActionType.MOVE_WORKOUT_BETWEEN_DAYS -> moveNonWorkoutRes(entityType)
                UserActionType.UNDO_REORDER_WORKOUT_SAME_DAY -> undoReorderNonWorkoutRes(entityType)
                UserActionType.UNDO_MOVE_WORKOUT_BETWEEN_DAYS -> undoMoveNonWorkoutRes(entityType)
                UserActionType.REORDER_REST,
                UserActionType.REORDER_BUSY,
                UserActionType.REORDER_SICK,
                -> reorderNonWorkoutRes(entityType)
                UserActionType.MOVE_REST,
                UserActionType.MOVE_BUSY,
                UserActionType.MOVE_SICK,
                -> moveNonWorkoutRes(entityType)
                UserActionType.UNDO_REORDER_REST,
                UserActionType.UNDO_REORDER_BUSY,
                UserActionType.UNDO_REORDER_SICK,
                -> undoReorderNonWorkoutRes(entityType)
                UserActionType.UNDO_MOVE_REST,
                UserActionType.UNDO_MOVE_BUSY,
                UserActionType.UNDO_MOVE_SICK,
                -> undoMoveNonWorkoutRes(entityType)
                UserActionType.REORDER_RACE_EVENT -> reorderNonWorkoutRes(entityType)
                UserActionType.MOVE_RACE_EVENT -> moveNonWorkoutRes(entityType)
                UserActionType.UNDO_REORDER_RACE_EVENT -> undoReorderNonWorkoutRes(entityType)
                UserActionType.UNDO_MOVE_RACE_EVENT -> undoMoveNonWorkoutRes(entityType)
                in nonWorkoutCreateActions -> createNonWorkoutRes(entityType)
                in nonWorkoutUpdateActions -> updateNonWorkoutRes(entityType)
                in nonWorkoutDeleteActions -> deleteNonWorkoutRes(entityType)
                in nonWorkoutUndoDeleteActions -> undoDeleteNonWorkoutRes(entityType)
                else -> null
            }

        if (simpleResId != null) return stringProvider.get(simpleResId, quotedWorkoutLabel)

        return when (actionType) {
            UserActionType.CONVERT_WORKOUT_TO_REST_DAY ->
                stringProvider.get(convertFromWorkoutRes(entityType), quotedWorkoutLabel)
            UserActionType.CONVERT_REST_DAY_TO_WORKOUT ->
                stringProvider.get(convertToWorkoutRes(entityType))
            else -> null
        }
    }

    private fun createNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST,
            UserActionEntityType.REST_DAY,
            -> R.string.activity_action_create_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_create_busy
            UserActionEntityType.SICK -> R.string.activity_action_create_sick
            UserActionEntityType.RACE_EVENT -> R.string.activity_action_create_race_event
            else -> R.string.activity_action_create_rest_day
        }
    }

    private fun updateNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST,
            UserActionEntityType.REST_DAY,
            -> R.string.activity_action_update_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_update_busy
            UserActionEntityType.SICK -> R.string.activity_action_update_sick
            UserActionEntityType.RACE_EVENT -> R.string.activity_action_update_race_event
            else -> R.string.activity_action_update_rest_day
        }
    }

    private fun deleteNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST,
            UserActionEntityType.REST_DAY,
            -> R.string.activity_action_delete_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_delete_busy
            UserActionEntityType.SICK -> R.string.activity_action_delete_sick
            UserActionEntityType.RACE_EVENT -> R.string.activity_action_delete_race_event
            else -> R.string.activity_action_delete_rest_day
        }
    }

    private fun undoDeleteNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST,
            UserActionEntityType.REST_DAY,
            -> R.string.activity_action_undo_delete_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_undo_delete_busy
            UserActionEntityType.SICK -> R.string.activity_action_undo_delete_sick
            UserActionEntityType.RACE_EVENT -> R.string.activity_action_undo_delete_race_event
            else -> R.string.activity_action_undo_delete_rest_day
        }
    }

    private fun reorderNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST,
            UserActionEntityType.REST_DAY,
            -> R.string.activity_action_reorder_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_reorder_busy
            UserActionEntityType.SICK -> R.string.activity_action_reorder_sick
            UserActionEntityType.RACE_EVENT -> R.string.activity_action_reorder_race_event
            else -> R.string.activity_action_reorder_rest_day
        }
    }

    private fun moveNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST,
            UserActionEntityType.REST_DAY,
            -> R.string.activity_action_move_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_move_busy
            UserActionEntityType.SICK -> R.string.activity_action_move_sick
            UserActionEntityType.RACE_EVENT -> R.string.activity_action_move_race_event
            else -> R.string.activity_action_move_rest_day
        }
    }

    private fun undoReorderNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST,
            UserActionEntityType.REST_DAY,
            -> R.string.activity_action_undo_reorder_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_undo_reorder_busy
            UserActionEntityType.SICK -> R.string.activity_action_undo_reorder_sick
            UserActionEntityType.RACE_EVENT -> R.string.activity_action_undo_reorder_race_event
            else -> R.string.activity_action_undo_reorder_rest_day
        }
    }

    private fun undoMoveNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST,
            UserActionEntityType.REST_DAY,
            -> R.string.activity_action_undo_move_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_undo_move_busy
            UserActionEntityType.SICK -> R.string.activity_action_undo_move_sick
            UserActionEntityType.RACE_EVENT -> R.string.activity_action_undo_move_race_event
            else -> R.string.activity_action_undo_move_rest_day
        }
    }

    private fun completeNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST,
            UserActionEntityType.REST_DAY,
            -> R.string.activity_action_complete_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_complete_busy
            UserActionEntityType.SICK -> R.string.activity_action_complete_sick
            UserActionEntityType.RACE_EVENT -> R.string.activity_action_complete_race_event
            else -> R.string.activity_action_complete_rest_day
        }
    }

    private fun incompleteNonWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST,
            UserActionEntityType.REST_DAY,
            -> R.string.activity_action_incomplete_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_incomplete_busy
            UserActionEntityType.SICK -> R.string.activity_action_incomplete_sick
            UserActionEntityType.RACE_EVENT -> R.string.activity_action_incomplete_race_event
            else -> R.string.activity_action_incomplete_rest_day
        }
    }

    private fun convertFromWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST,
            UserActionEntityType.REST_DAY,
            -> R.string.activity_action_convert_workout_to_rest_day
            UserActionEntityType.BUSY -> R.string.activity_action_convert_workout_to_busy
            UserActionEntityType.SICK -> R.string.activity_action_convert_workout_to_sick
            else -> R.string.activity_action_convert_workout_to_rest_day
        }
    }

    private fun convertToWorkoutRes(entityType: UserActionEntityType): Int {
        return when (entityType) {
            UserActionEntityType.REST,
            UserActionEntityType.REST_DAY,
            -> R.string.activity_action_convert_rest_day_to_workout
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

    private val nonWorkoutCreateActions =
        setOf(
            UserActionType.CREATE_REST_DAY,
            UserActionType.CREATE_BUSY,
            UserActionType.CREATE_SICK,
            UserActionType.CREATE_RACE_EVENT,
        )

    private val nonWorkoutUpdateActions =
        setOf(
            UserActionType.UPDATE_REST_DAY,
            UserActionType.UPDATE_BUSY,
            UserActionType.UPDATE_SICK,
            UserActionType.UPDATE_RACE_EVENT,
        )

    private val nonWorkoutDeleteActions =
        setOf(
            UserActionType.DELETE_REST_DAY,
            UserActionType.DELETE_BUSY,
            UserActionType.DELETE_SICK,
            UserActionType.DELETE_RACE_EVENT,
        )

    private val nonWorkoutUndoDeleteActions =
        setOf(
            UserActionType.UNDO_DELETE_REST_DAY,
            UserActionType.UNDO_DELETE_BUSY,
            UserActionType.UNDO_DELETE_SICK,
            UserActionType.UNDO_DELETE_RACE_EVENT,
        )

    private val challengeProgressActions =
        setOf(
            UserActionType.CREATE_CHALLENGE_PROGRESS_ENTRY,
            UserActionType.UPDATE_CHALLENGE_PROGRESS_ENTRY,
            UserActionType.RESTORE_CHALLENGE_PROGRESS_ENTRY,
        )
}
