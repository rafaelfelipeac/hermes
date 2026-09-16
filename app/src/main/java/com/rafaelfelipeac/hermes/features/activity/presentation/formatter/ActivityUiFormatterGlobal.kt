package com.rafaelfelipeac.hermes.features.activity.presentation.formatter

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType

internal class ActivityUiFormatterGlobal(
    private val stringProvider: StringProvider,
    private val shared: ActivityUiFormatterShared,
) {
    @Suppress("LongMethod", "CyclomaticComplexMethod")
    fun buildGlobalActionTitle(actionType: UserActionType?): String {
        return when (actionType) {
            UserActionType.CHANGE_LANGUAGE ->
                stringProvider.get(R.string.activity_action_change_language)

            UserActionType.CHANGE_THEME ->
                stringProvider.get(R.string.activity_action_change_theme)

            UserActionType.CHANGE_DYNAMIC_COLOR ->
                stringProvider.get(R.string.activity_action_change_dynamic_color)

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

    fun buildActionSubtitle(
        actionType: UserActionType?,
        metadata: Map<String, String>,
    ): String? {
        return when (actionType) {
            UserActionType.CHANGE_LANGUAGE,
            UserActionType.CHANGE_THEME,
            UserActionType.CHANGE_DYNAMIC_COLOR,
            UserActionType.CHANGE_SLOT_MODE,
            UserActionType.CHANGE_WEEK_START,
            UserActionType.CHANGE_DISTANCE_UNIT,
            UserActionType.CHANGE_PACE_UNIT,
            UserActionType.CHANGE_WEIGHT_UNIT,
            UserActionType.UPDATE_CATEGORY_NAME,
            -> buildValueChangeSubtitle(metadata, actionType)

            UserActionType.UPDATE_CATEGORY_VISIBILITY ->
                buildCategoryVisibilitySubtitle(metadata)

            else -> null
        }
    }

    private fun buildValueChangeSubtitle(
        metadata: Map<String, String>,
        actionType: UserActionType,
    ): String? {
        val oldValue =
            shared.quoteValue(
                shared.formatChangeValue(metadata[UserActionMetadataKeys.OLD_VALUE], actionType),
            )
        val newValue =
            shared.quoteValue(
                shared.formatChangeValue(metadata[UserActionMetadataKeys.NEW_VALUE], actionType),
            )

        if (oldValue.isNullOrBlank() && newValue.isNullOrBlank()) return null

        return stringProvider.get(
            R.string.activity_subtitle_change_value,
            oldValue.orEmpty(),
            newValue.orEmpty(),
        )
    }

    private fun buildCategoryVisibilitySubtitle(metadata: Map<String, String>): String? {
        val oldValue = shared.formatVisibilityValue(metadata[UserActionMetadataKeys.OLD_VALUE])
        val newValue = shared.formatVisibilityValue(metadata[UserActionMetadataKeys.NEW_VALUE])

        if (oldValue.isNullOrBlank() && newValue.isNullOrBlank()) return null

        return stringProvider.get(
            R.string.activity_subtitle_change_value,
            oldValue.orEmpty(),
            newValue.orEmpty(),
        )
    }
}
