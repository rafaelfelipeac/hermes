package com.rafaelfelipeac.hermes.core.useraction

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
