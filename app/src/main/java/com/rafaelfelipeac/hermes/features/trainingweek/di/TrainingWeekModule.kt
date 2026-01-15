package com.rafaelfelipeac.hermes.features.trainingweek.di

import com.rafaelfelipeac.hermes.features.trainingweek.data.TrainingWeekRepositoryImpl
import com.rafaelfelipeac.hermes.features.trainingweek.domain.repository.TrainingWeekRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TrainingWeekModule {

    @Binds
    abstract fun bindTrainingWeekRepository(
        impl: TrainingWeekRepositoryImpl
    ): TrainingWeekRepository
}