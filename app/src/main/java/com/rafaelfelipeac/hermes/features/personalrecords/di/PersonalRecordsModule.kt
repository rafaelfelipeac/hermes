package com.rafaelfelipeac.hermes.features.personalrecords.di

import com.rafaelfelipeac.hermes.features.personalrecords.data.PersonalRecordsRepositoryImpl
import com.rafaelfelipeac.hermes.features.personalrecords.domain.repository.PersonalRecordsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PersonalRecordsModule {
    @Binds
    abstract fun bindPersonalRecordsRepository(impl: PersonalRecordsRepositoryImpl): PersonalRecordsRepository
}
