package com.rafaelfelipeac.hermes.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rafaelfelipeac.hermes.core.database.HermesDatabase.Companion.DATABASE_VERSION
import com.rafaelfelipeac.hermes.core.useraction.UserActionDao
import com.rafaelfelipeac.hermes.core.useraction.UserActionEntity
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity

@Database(
    entities = [WorkoutEntity::class, UserActionEntity::class],
    version = DATABASE_VERSION,
)
@TypeConverters(LocalDateConverters::class)
abstract class HermesDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    abstract fun userActionDao(): UserActionDao

    companion object {
        private const val DATABASE_VERSION = 2

        val MIGRATION_1_2: Migration =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `user_actions` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `actionType` TEXT NOT NULL,
                            `entityType` TEXT NOT NULL,
                            `entityId` INTEGER,
                            `metadata` TEXT,
                            `timestamp` INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )
                }
            }
    }
}
