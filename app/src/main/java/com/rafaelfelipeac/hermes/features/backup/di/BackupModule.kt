package com.rafaelfelipeac.hermes.features.backup.di

import com.rafaelfelipeac.hermes.features.backup.data.BackupRepositoryImpl
import com.rafaelfelipeac.hermes.features.backup.domain.repository.BackupRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupModule {
    @Binds
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository
}
