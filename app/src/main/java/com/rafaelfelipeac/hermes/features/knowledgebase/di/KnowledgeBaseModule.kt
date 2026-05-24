package com.rafaelfelipeac.hermes.features.knowledgebase.di

import com.rafaelfelipeac.hermes.features.knowledgebase.data.KnowledgeNoteRepositoryImpl
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.repository.KnowledgeNoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class KnowledgeBaseModule {
    @Binds
    abstract fun bindKnowledgeNoteRepository(impl: KnowledgeNoteRepositoryImpl): KnowledgeNoteRepository
}
