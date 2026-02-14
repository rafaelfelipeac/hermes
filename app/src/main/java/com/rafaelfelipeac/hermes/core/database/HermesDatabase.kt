package com.rafaelfelipeac.hermes.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rafaelfelipeac.hermes.core.database.HermesDatabase.Companion.DATABASE_VERSION
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionDao
import com.rafaelfelipeac.hermes.core.useraction.data.local.UserActionEntity
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryDao
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryEntity
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity

@Database(
    entities = [WorkoutEntity::class, UserActionEntity::class, CategoryEntity::class],
    version = DATABASE_VERSION,
)
@TypeConverters(LocalDateConverters::class)
abstract class HermesDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    abstract fun userActionDao(): UserActionDao

    abstract fun categoryDao(): CategoryDao

    companion object {
        private const val DATABASE_VERSION = 2
    }
}
