package com.rafaelfelipeac.hermes.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.features.trainingweek.data.local.WorkoutDao
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
            "hermes.db",
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideWorkoutDao(database: HermesDatabase): WorkoutDao {
        return database.workoutDao()
    }
}

private val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE workouts ADD COLUMN isRestDay INTEGER NOT NULL DEFAULT 0",
            )
        }
    }
