package com.rafaelfelipeac.hermes.core.useraction.domain

import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType

data class UserAction(
    val actionType: UserActionType,
    val entityType: UserActionEntityType,
    val entityId: Long? = null,
    val metadata: Map<String, String>? = null,
    val timestamp: Long = System.currentTimeMillis(),
)
