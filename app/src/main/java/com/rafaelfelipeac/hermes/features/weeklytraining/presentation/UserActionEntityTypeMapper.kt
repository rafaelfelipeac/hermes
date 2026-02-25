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
    }
}

internal fun EventType.toCreateActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.CREATE_WORKOUT
        EventType.REST -> UserActionType.CREATE_REST_DAY
        EventType.BUSY -> UserActionType.CREATE_BUSY
        EventType.SICK -> UserActionType.CREATE_SICK
    }
}

internal fun EventType.toUpdateActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.UPDATE_WORKOUT
        EventType.REST -> UserActionType.UPDATE_REST_DAY
        EventType.BUSY -> UserActionType.UPDATE_BUSY
        EventType.SICK -> UserActionType.UPDATE_SICK
    }
}

internal fun EventType.toDeleteActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.DELETE_WORKOUT
        EventType.REST -> UserActionType.DELETE_REST_DAY
        EventType.BUSY -> UserActionType.DELETE_BUSY
        EventType.SICK -> UserActionType.DELETE_SICK
    }
}

internal fun EventType.toUndoDeleteActionType(): UserActionType {
    return when (this) {
        EventType.WORKOUT -> UserActionType.UNDO_DELETE_WORKOUT
        EventType.REST -> UserActionType.UNDO_DELETE_REST_DAY
        EventType.BUSY -> UserActionType.UNDO_DELETE_BUSY
        EventType.SICK -> UserActionType.UNDO_DELETE_SICK
    }
}
