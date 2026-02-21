package com.rafaelfelipeac.hermes.core.di

import android.content.Context
import androidx.room.Room
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.core.database.MIGRATION_1_2
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionDao
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryDao
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): HermesDatabase {
        return Room.databaseBuilder(
            context,
            HermesDatabase::class.java,
            DATABASE_NAME,
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideWorkoutDao(database: HermesDatabase): WorkoutDao {
        return database.workoutDao()
    }

    @Provides
    fun provideUserActionDao(database: HermesDatabase): UserActionDao {
        return database.userActionDao()
    }

    @Provides
    fun provideCategoryDao(database: HermesDatabase): CategoryDao {
        return database.categoryDao()
    }
}

private const val DATABASE_NAME = "hermes.db"
