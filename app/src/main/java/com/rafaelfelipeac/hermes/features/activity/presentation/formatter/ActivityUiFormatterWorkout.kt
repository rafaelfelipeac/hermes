package com.rafaelfelipeac.hermes.features.activity.presentation.formatter

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType

internal class ActivityUiFormatterWorkout(
    private val stringProvider: StringProvider,
    private val shared: ActivityUiFormatterShared,
) {
    fun buildQuotedWorkoutLabel(metadata: Map<String, String>): String {
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

    fun buildWorkoutTitle(
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

    @Suppress("CyclomaticComplexMethod", "ReturnCount")
    fun buildNonWorkoutTitle(
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

    fun buildMoveSubtitle(metadata: Map<String, String>): String? {
        val oldDay = shared.dayLabel(metadata[UserActionMetadataKeys.OLD_DAY_OF_WEEK])
        val newDay = shared.dayLabel(metadata[UserActionMetadataKeys.NEW_DAY_OF_WEEK])
        val oldSlot = shared.timeSlotLabel(metadata[UserActionMetadataKeys.OLD_TIME_SLOT])
        val newSlot = shared.timeSlotLabel(metadata[UserActionMetadataKeys.NEW_TIME_SLOT])
        val oldLocation = shared.quoteValue(shared.locationLabel(oldDay, oldSlot))
        val newLocation = shared.quoteValue(shared.locationLabel(newDay, newSlot))

        if (oldLocation.isNullOrBlank() && newLocation.isNullOrBlank()) return null

        return stringProvider.get(
            R.string.activity_subtitle_move,
            oldLocation.orEmpty(),
            newLocation.orEmpty(),
        )
    }

    fun buildReorderSubtitle(metadata: Map<String, String>): String? {
        val oldDay =
            shared.quoteValue(
                shared.dayLabel(metadata[UserActionMetadataKeys.OLD_DAY_OF_WEEK]),
            )
        val newDay =
            shared.quoteValue(
                shared.dayLabel(metadata[UserActionMetadataKeys.NEW_DAY_OF_WEEK]),
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
}
