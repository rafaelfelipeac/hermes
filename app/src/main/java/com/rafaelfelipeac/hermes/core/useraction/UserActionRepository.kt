package com.rafaelfelipeac.hermes.core.useraction

import kotlinx.coroutines.flow.Flow

interface UserActionRepository {
    fun observeActions(): Flow<List<UserActionRecord>>
}
