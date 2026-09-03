package com.rafaelfelipeac.hermes.features.challenges.di

import com.rafaelfelipeac.hermes.features.challenges.data.ChallengeRepositoryImpl
import com.rafaelfelipeac.hermes.features.challenges.domain.repository.ChallengeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ChallengesModule {
    @Binds
    abstract fun bindChallengeRepository(impl: ChallengeRepositoryImpl): ChallengeRepository
}
