package com.rafaelfelipeac.hermes.features.settings.di

import com.rafaelfelipeac.hermes.features.settings.data.SettingsRepositoryImpl
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused")
abstract class SettingsModule {
    @Binds
    @Suppress("unused")
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
