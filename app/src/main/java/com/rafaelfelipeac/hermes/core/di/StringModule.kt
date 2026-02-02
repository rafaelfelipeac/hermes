package com.rafaelfelipeac.hermes.core.di

import com.rafaelfelipeac.hermes.core.strings.AndroidStringProvider
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class StringModule {
    @Binds
    abstract fun bindStringProvider(impl: AndroidStringProvider): StringProvider
}
