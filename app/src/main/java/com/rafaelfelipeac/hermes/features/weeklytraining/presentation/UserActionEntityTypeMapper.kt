package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType

internal fun EventType.toUserActionEntityType(): UserActionEntityType {
    return when (this) {
        EventType.WORKOUT -> UserActionEntityType.WORKOUT
        EventType.REST -> UserActionEntityType.REST_DAY
        EventType.BUSY -> UserActionEntityType.BUSY
        EventType.SICK -> UserActionEntityType.SICK
    }
}
