package com.rafaelfelipeac.hermes.features.weeklytraining.di

import com.rafaelfelipeac.hermes.features.weeklytraining.data.RoomWeeklyTrainingCommandRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.data.WeeklyTrainingRepositoryImpl
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WeeklyTrainingCommandRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WeeklyTrainingModule {
    @Binds
    abstract fun bindWeeklyTrainingRepository(impl: WeeklyTrainingRepositoryImpl): WeeklyTrainingRepository

    @Binds
    abstract fun bindCommandRepository(impl: RoomWeeklyTrainingCommandRepository): WeeklyTrainingCommandRepository
}
