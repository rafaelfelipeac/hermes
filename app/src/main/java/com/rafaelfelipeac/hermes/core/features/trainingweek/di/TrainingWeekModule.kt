package com.rafaelfelipeac.hermes.core.features.trainingweek.di

import com.rafaelfelipeac.hermes.core.features.trainingweek.data.TrainingWeekRepositoryImpl
import com.rafaelfelipeac.hermes.core.features.trainingweek.domain.repository.TrainingWeekRepository
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