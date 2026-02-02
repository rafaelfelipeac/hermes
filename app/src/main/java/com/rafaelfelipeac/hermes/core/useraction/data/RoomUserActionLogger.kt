package com.rafaelfelipeac.hermes.core.useraction.data

import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionDao
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionEntity
import com.rafaelfelipeac.hermes.core.useraction.domain.UserAction
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataSerializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomUserActionLogger
    @Inject
    constructor(
        private val userActionDao: UserActionDao,
    ) : UserActionLogger {
        override suspend fun log(action: UserAction) {
            val metadata = action.metadata?.let(UserActionMetadataSerializer::toJson)
            val entity =
                UserActionEntity(
                    actionType = action.actionType.name,
                    entityType = action.entityType.name,
                    entityId = action.entityId,
                    metadata = metadata,
                    timestamp = action.timestamp,
                )

            userActionDao.insert(entity)
        }
    }
