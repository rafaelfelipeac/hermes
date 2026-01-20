package com.rafaelfelipeac.hermes.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rafaelfelipeac.hermes.features.trainingweek.data.local.WorkoutDao
import com.rafaelfelipeac.hermes.features.trainingweek.data.local.WorkoutEntity

@Database(
    entities = [WorkoutEntity::class],
    version = 2,
)
@TypeConverters(LocalDateConverters::class)
abstract class HermesDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
}
