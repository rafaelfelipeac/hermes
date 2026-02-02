package com.rafaelfelipeac.hermes.core.useraction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomUserActionRepository
    @Inject
    constructor(
        private val userActionDao: UserActionDao,
    ) : UserActionRepository {
        override fun observeActions(): Flow<List<UserActionRecord>> {
            return userActionDao.observeAll().map { actions ->
                actions.map { action ->
                    UserActionRecord(
                        id = action.id,
                        actionType = action.actionType,
                        entityType = action.entityType,
                        entityId = action.entityId,
                        metadata = action.metadata,
                        timestamp = action.timestamp,
                    )
                }
            }
        }
    }
