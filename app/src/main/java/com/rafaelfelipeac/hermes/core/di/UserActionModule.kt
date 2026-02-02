package com.rafaelfelipeac.hermes.core.di

import com.rafaelfelipeac.hermes.core.useraction.RoomUserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.RoomUserActionRepository
import com.rafaelfelipeac.hermes.core.useraction.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.UserActionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UserActionModule {
    @Binds
    abstract fun bindUserActionLogger(impl: RoomUserActionLogger): UserActionLogger

    @Binds
    abstract fun bindUserActionRepository(impl: RoomUserActionRepository): UserActionRepository
}
