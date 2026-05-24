package com.rafaelfelipeac.hermes.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE workouts ADD COLUMN categoryId INTEGER")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    colorId TEXT NOT NULL,
                    sortOrder INTEGER NOT NULL,
                    isHidden INTEGER NOT NULL,
                    isSystem INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE workouts ADD COLUMN eventType TEXT NOT NULL DEFAULT 'WORKOUT'")
            db.execSQL("ALTER TABLE workouts ADD COLUMN timeSlot TEXT")
            db.execSQL(
                "UPDATE workouts SET eventType = CASE WHEN isRestDay = 1 THEN 'REST' ELSE 'WORKOUT' END",
            )
        }
    }

val MIGRATION_3_4 =
    object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS knowledge_notes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    kind TEXT NOT NULL,
                    status TEXT NOT NULL,
                    title TEXT,
                    body TEXT NOT NULL,
                    sourceWorkoutId INTEGER,
                    sourceType TEXT,
                    sourceTitle TEXT,
                    categoryId INTEGER,
                    triggerScope TEXT,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

val ALL_MIGRATIONS =
    listOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
    )
