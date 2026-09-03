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
                CREATE TABLE IF NOT EXISTS personal_record_families (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    categoryId INTEGER,
                    title TEXT NOT NULL,
                    metricType TEXT NOT NULL,
                    defaultUnit TEXT NOT NULL,
                    comparisonRule TEXT NOT NULL,
                    manualCurrentEntryId INTEGER,
                    sortOrder INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS personal_record_entries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    familyId INTEGER NOT NULL,
                    value REAL NOT NULL,
                    unit TEXT NOT NULL,
                    customUnitLabel TEXT,
                    recordDate TEXT NOT NULL,
                    note TEXT,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(familyId) REFERENCES personal_record_families(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_personal_record_families_categoryId " +
                    "ON personal_record_families(categoryId)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_personal_record_entries_familyId " +
                    "ON personal_record_entries(familyId)",
            )
        }
    }

val MIGRATION_4_5 =
    object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS personal_record_families (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    categoryId INTEGER,
                    title TEXT NOT NULL,
                    metricType TEXT NOT NULL,
                    defaultUnit TEXT NOT NULL,
                    comparisonRule TEXT NOT NULL,
                    manualCurrentEntryId INTEGER,
                    sortOrder INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS personal_record_entries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    familyId INTEGER NOT NULL,
                    value REAL NOT NULL,
                    unit TEXT NOT NULL,
                    customUnitLabel TEXT,
                    recordDate TEXT NOT NULL,
                    note TEXT,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(familyId) REFERENCES personal_record_families(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_personal_record_families_categoryId " +
                    "ON personal_record_families(categoryId)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_personal_record_entries_familyId " +
                    "ON personal_record_entries(familyId)",
            )
        }
    }

val MIGRATION_5_6 =
    object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS challenges (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT,
                    targetType TEXT NOT NULL,
                    targetQuantity INTEGER NOT NULL,
                    startDate TEXT NOT NULL,
                    endDate TEXT NOT NULL,
                    lifecycle TEXT NOT NULL,
                    archivedAt INTEGER,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS challenge_progress_entries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    challengeId INTEGER NOT NULL,
                    quantity INTEGER NOT NULL,
                    entryDate TEXT NOT NULL,
                    occurredAt INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(challengeId) REFERENCES challenges(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_challenges_lifecycle ON challenges(lifecycle)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_challenges_updatedAt ON challenges(updatedAt)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_challenge_progress_entries_challengeId_entryDate_occurredAt_id " +
                    "ON challenge_progress_entries(challengeId, entryDate, occurredAt, id)",
            )
        }
    }

val MIGRATION_6_7 =
    object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE challenges ADD COLUMN categoryId INTEGER")
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_challenges_categoryId ON challenges(categoryId)",
            )
        }
    }

val ALL_MIGRATIONS =
    listOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7,
    )
