package com.rafaelfelipeac.hermes.features.categories.di

import com.rafaelfelipeac.hermes.features.categories.data.CategoryRepositoryImpl
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CategoryModule {
    @Binds
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository
}
