package com.rafaelfelipeac.hermes.features.activity.presentation.formatter

import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataSerializer
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import java.time.ZoneId
import java.util.Locale

class ActivityUiFormatter(
    private val stringProvider: StringProvider,
) {
    private val shared = ActivityUiFormatterShared(stringProvider)
    private val globalFormatter = ActivityUiFormatterGlobal(stringProvider, shared)
    private val workoutFormatter = ActivityUiFormatterWorkout(stringProvider, shared)
    private val categoryFormatter = ActivityUiFormatterCategory(stringProvider, shared)
    private val personalRecordFormatter = ActivityUiFormatterPersonalRecord(stringProvider, shared)
    private val challengeFormatter = ActivityUiFormatterChallenge(stringProvider, shared)
    private val trophyFormatter = ActivityUiFormatterTrophy(stringProvider, shared)

    fun parseMetadata(raw: String?): Map<String, String> {
        return UserActionMetadataSerializer.fromJson(raw)
    }

    fun formatTime(
        timestamp: Long,
        zoneId: ZoneId,
        locale: Locale,
    ): String {
        return shared.formatTime(timestamp, zoneId, locale)
    }

    fun buildTitle(
        record: UserActionRecord,
        metadata: Map<String, String>,
    ): String {
        val actionType = runCatching { UserActionType.valueOf(record.actionType) }.getOrNull()
        val entityType = runCatching { UserActionEntityType.valueOf(record.entityType) }.getOrNull()
        val quotedWorkoutLabel = workoutFormatter.buildQuotedWorkoutLabel(metadata)

        val title =
            when (entityType) {
                UserActionEntityType.REST,
                UserActionEntityType.REST_DAY,
                UserActionEntityType.BUSY,
                UserActionEntityType.SICK,
                UserActionEntityType.RACE_EVENT,
                ->
                    workoutFormatter.buildNonWorkoutTitle(
                        entityType = entityType,
                        actionType = actionType,
                        quotedWorkoutLabel = quotedWorkoutLabel,
                    )

                UserActionEntityType.CHALLENGE -> challengeFormatter.buildChallengeTitle(actionType, metadata)
                UserActionEntityType.CATEGORY -> categoryFormatter.buildCategoryTitle(actionType, metadata)
                UserActionEntityType.TROPHY -> trophyFormatter.buildTrophyTitle(actionType, metadata)
                UserActionEntityType.PERSONAL_RECORD ->
                    personalRecordFormatter.buildPersonalRecordTitle(actionType, metadata)

                else -> workoutFormatter.buildWorkoutTitle(actionType, quotedWorkoutLabel)
            }

        return title ?: globalFormatter.buildGlobalActionTitle(actionType)
    }

    fun buildSubtitle(
        record: UserActionRecord,
        metadata: Map<String, String>,
        currentLocale: Locale,
    ): String? {
        val actionType = runCatching { UserActionType.valueOf(record.actionType) }.getOrNull()
        val weekSubtitle = shared.buildWeekSubtitle(metadata, currentLocale)
        val entityType = runCatching { UserActionEntityType.valueOf(record.entityType) }.getOrNull()
        val actionSubtitle =
            if (entityType == UserActionEntityType.CHALLENGE) {
                challengeFormatter.buildChallengeSubtitle(actionType, metadata, currentLocale)
            } else {
                buildActionSubtitle(actionType, metadata, currentLocale)
            }

        return shared.combineSubtitles(
            weekSubtitle = weekSubtitle,
            actionSubtitle = actionSubtitle,
            shouldSplitLines = shared.shouldSplitLines(actionType),
        )
    }

    @Suppress("LongMethod")
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
            UserActionType.UPDATE_CATEGORY_VISIBILITY,
            -> globalFormatter.buildActionSubtitle(actionType, metadata)

            UserActionType.CREATE_WORKOUT ->
                categoryFormatter.buildWorkoutCategorySubtitle(metadata)

            UserActionType.UPDATE_WORKOUT ->
                categoryFormatter.buildWorkoutCategoryChangeSubtitle(metadata)

            UserActionType.SHARE_TROPHY ->
                categoryFormatter.buildWorkoutCategorySubtitle(metadata)

            UserActionType.CREATE_PERSONAL_RECORD_FAMILY,
            UserActionType.UPDATE_PERSONAL_RECORD_FAMILY,
            UserActionType.DELETE_PERSONAL_RECORD_FAMILY,
            -> personalRecordFormatter.buildPersonalRecordFamilySubtitle(metadata)

            UserActionType.CREATE_PERSONAL_RECORD_ENTRY,
            UserActionType.UPDATE_PERSONAL_RECORD_ENTRY,
            UserActionType.DELETE_PERSONAL_RECORD_ENTRY,
            UserActionType.SET_CURRENT_PERSONAL_RECORD_ENTRY,
            -> personalRecordFormatter.buildPersonalRecordEntrySubtitle(actionType, metadata, currentLocale)

            UserActionType.MOVE_WORKOUT_BETWEEN_DAYS,
            UserActionType.MOVE_REST,
            UserActionType.MOVE_BUSY,
            UserActionType.MOVE_SICK,
            -> workoutFormatter.buildMoveSubtitle(metadata)

            UserActionType.REORDER_WORKOUT,
            UserActionType.REORDER_REST,
            UserActionType.REORDER_BUSY,
            UserActionType.REORDER_SICK,
            -> workoutFormatter.buildReorderSubtitle(metadata)

            UserActionType.UNDO_MOVE_WORKOUT_BETWEEN_DAYS,
            UserActionType.UNDO_MOVE_REST,
            UserActionType.UNDO_MOVE_BUSY,
            UserActionType.UNDO_MOVE_SICK,
            -> workoutFormatter.buildMoveSubtitle(metadata)

            UserActionType.UNDO_REORDER_WORKOUT_SAME_DAY,
            UserActionType.UNDO_REORDER_REST,
            UserActionType.UNDO_REORDER_BUSY,
            UserActionType.UNDO_REORDER_SICK,
            -> workoutFormatter.buildReorderSubtitle(metadata)

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
            -> challengeFormatter.buildChallengeSubtitle(actionType, metadata, currentLocale)

            else -> null
        }
    }
}
