package com.rafaelfelipeac.hermes.core.useraction.domain

import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import kotlinx.coroutines.flow.Flow

interface UserActionRepository {
    fun observeActions(): Flow<List<UserActionRecord>>
}
