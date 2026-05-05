package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType

internal fun EventType.toUserActionEntityType(): UserActionEntityType {
    return when (this) {
        EventType.WORKOUT -> UserActionEntityType.WORKOUT
        EventType.REST -> UserActionEntityType.REST
        EventType.BUSY -> UserActionEntityType.BUSY
        EventType.SICK -> UserActionEntityType.SICK
        EventType.RACE_EVENT -> UserActionEntityType.RACE_EVENT
    }
}

internal fun EventType.toCreateActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.CREATE_WORKOUT
        EventType.REST -> UserActionType.CREATE_REST_DAY
        EventType.BUSY -> UserActionType.CREATE_BUSY
        EventType.SICK -> UserActionType.CREATE_SICK
        EventType.RACE_EVENT -> UserActionType.CREATE_RACE_EVENT
    }
}

internal fun EventType.toUpdateActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.UPDATE_WORKOUT
        EventType.REST -> UserActionType.UPDATE_REST_DAY
        EventType.BUSY -> UserActionType.UPDATE_BUSY
        EventType.SICK -> UserActionType.UPDATE_SICK
        EventType.RACE_EVENT -> UserActionType.UPDATE_RACE_EVENT
    }
}

internal fun EventType.toDeleteActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.DELETE_WORKOUT
        EventType.REST -> UserActionType.DELETE_REST_DAY
        EventType.BUSY -> UserActionType.DELETE_BUSY
        EventType.SICK -> UserActionType.DELETE_SICK
        EventType.RACE_EVENT -> UserActionType.DELETE_RACE_EVENT
    }
}

internal fun EventType.toUndoDeleteActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.UNDO_DELETE_WORKOUT
        EventType.REST -> UserActionType.UNDO_DELETE_REST_DAY
        EventType.BUSY -> UserActionType.UNDO_DELETE_BUSY
        EventType.SICK -> UserActionType.UNDO_DELETE_SICK
        EventType.RACE_EVENT -> UserActionType.UNDO_DELETE_RACE_EVENT
    }
}

internal fun EventType.toReorderActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.REORDER_WORKOUT
        EventType.REST -> UserActionType.REORDER_REST
        EventType.BUSY -> UserActionType.REORDER_BUSY
        EventType.SICK -> UserActionType.REORDER_SICK
        EventType.RACE_EVENT -> UserActionType.REORDER_RACE_EVENT
    }
}

internal fun EventType.toMoveActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.MOVE_WORKOUT_BETWEEN_DAYS
        EventType.REST -> UserActionType.MOVE_REST
        EventType.BUSY -> UserActionType.MOVE_BUSY
        EventType.SICK -> UserActionType.MOVE_SICK
        EventType.RACE_EVENT -> UserActionType.MOVE_RACE_EVENT
    }
}

internal fun EventType.toUndoReorderActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.UNDO_REORDER_WORKOUT_SAME_DAY
        EventType.REST -> UserActionType.UNDO_REORDER_REST
        EventType.BUSY -> UserActionType.UNDO_REORDER_BUSY
        EventType.SICK -> UserActionType.UNDO_REORDER_SICK
        EventType.RACE_EVENT -> UserActionType.UNDO_REORDER_RACE_EVENT
    }
}

internal fun EventType.toUndoMoveActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.UNDO_MOVE_WORKOUT_BETWEEN_DAYS
        EventType.REST -> UserActionType.UNDO_MOVE_REST
        EventType.BUSY -> UserActionType.UNDO_MOVE_BUSY
        EventType.SICK -> UserActionType.UNDO_MOVE_SICK
        EventType.RACE_EVENT -> UserActionType.UNDO_MOVE_RACE_EVENT
    }
}
