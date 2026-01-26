package com.rafaelfelipeac.hermes.core.di

import android.content.Context
import androidx.room.Room
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
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
            .build()
    }

    @Provides
    fun provideWorkoutDao(database: HermesDatabase): WorkoutDao {
        return database.workoutDao()
    }
}

private const val DATABASE_NAME = "hermes.db"
