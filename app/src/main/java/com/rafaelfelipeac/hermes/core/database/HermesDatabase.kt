package com.rafaelfelipeac.hermes.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rafaelfelipeac.hermes.core.database.HermesDatabase.Companion.DATABASE_VERSION
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity

@Database(
    entities = [WorkoutEntity::class],
    version = DATABASE_VERSION,
)
@TypeConverters(LocalDateConverters::class)
abstract class HermesDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        private const val DATABASE_VERSION = 1
    }
}
