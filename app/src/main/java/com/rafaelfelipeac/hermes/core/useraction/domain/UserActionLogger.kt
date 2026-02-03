package com.rafaelfelipeac.hermes.core.useraction.domain

import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType

interface UserActionLogger {
    suspend fun log(action: UserAction)

    suspend fun log(
        actionType: UserActionType,
        entityType: UserActionEntityType,
        entityId: Long? = null,
        metadata: Map<String, String>? = null,
        timestamp: Long = System.currentTimeMillis(),
    ) {
        log(
            UserAction(
                actionType = actionType,
                entityType = entityType,
                entityId = entityId,
                metadata = metadata,
                timestamp = timestamp,
            ),
        )
    }
}
