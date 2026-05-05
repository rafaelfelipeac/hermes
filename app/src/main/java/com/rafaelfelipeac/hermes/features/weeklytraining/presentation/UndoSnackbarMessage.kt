package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.BUSY
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.RACE_EVENT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.REST
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.SICK
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.WORKOUT

@Composable
fun undoSnackbarMessage(
    message: UndoMessage,
    eventType: EventType,
): String {
    return when (message) {
        UndoMessage.WeekCopied ->
            stringResource(R.string.weekly_training_week_copied)
        UndoMessage.Moved ->
            stringResource(undoMovedMessageRes(eventType))
        UndoMessage.Deleted ->
            stringResource(undoDeletedMessageRes(eventType))
        UndoMessage.Completed ->
            stringResource(completionMessageRes(eventType, true))
        UndoMessage.CompletedWeek ->
            stringResource(R.string.weekly_training_week_completed_celebration)
        UndoMessage.MarkedIncomplete ->
            stringResource(completionMessageRes(eventType, false))
    }
}

private fun undoMovedMessageRes(eventType: EventType): Int {
    return when (eventType) {
        WORKOUT -> R.string.weekly_training_workout_moved
        REST -> R.string.weekly_training_rest_day_moved
        BUSY -> R.string.weekly_training_busy_moved
        SICK -> R.string.weekly_training_sick_moved
        RACE_EVENT -> R.string.weekly_training_race_event_moved
    }
}

private fun undoDeletedMessageRes(eventType: EventType): Int {
    return when (eventType) {
        WORKOUT -> R.string.weekly_training_workout_deleted
        REST -> R.string.weekly_training_rest_day_deleted
        BUSY -> R.string.weekly_training_busy_deleted
        SICK -> R.string.weekly_training_sick_deleted
        RACE_EVENT -> R.string.weekly_training_race_event_deleted
    }
}

private fun completionMessageRes(
    eventType: EventType,
    isCompleted: Boolean,
): Int {
    return when (eventType) {
        RACE_EVENT ->
            if (isCompleted) {
                R.string.activity_action_complete_race_event
            } else {
                R.string.activity_action_incomplete_race_event
            }
        else ->
            if (isCompleted) {
                R.string.weekly_training_workout_completed
            } else {
                R.string.weekly_training_workout_marked_incomplete
            }
    }
}
