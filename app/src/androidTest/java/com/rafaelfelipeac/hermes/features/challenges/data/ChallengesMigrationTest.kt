package com.rafaelfelipeac.hermes.features.challenges.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.core.database.MIGRATION_5_6
import com.rafaelfelipeac.hermes.core.database.MIGRATION_6_7
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChallengesMigrationTest {
    @get:Rule
    val helper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            HermesDatabase::class.java,
        )

    @Test
    fun migration_5_6_createsChallengeTables() {
        helper.createDatabase(MIGRATION_5_6_DB_NAME, 5).close()

        val migrated =
            helper.runMigrationsAndValidate(
                MIGRATION_5_6_DB_NAME,
                6,
                true,
                MIGRATION_5_6,
            )

        assertTableExists(migrated, "challenges")
        assertTableExists(migrated, "challenge_progress_entries")
    }

    @Test
    fun migration_6_7_addsChallengeCategoryId() {
        helper.createDatabase(MIGRATION_6_7_DB_NAME, 6).close()

        val migrated =
            helper.runMigrationsAndValidate(
                MIGRATION_6_7_DB_NAME,
                7,
                true,
                MIGRATION_6_7,
            )

        assertColumnExists(migrated, tableName = "challenges", columnName = "categoryId")
    }

    @Test
    fun migration_5_7_runsCompleteChallengeMigrationChain() {
        helper.createDatabase(MIGRATION_5_7_DB_NAME, 5).close()

        val migrated =
            helper.runMigrationsAndValidate(
                MIGRATION_5_7_DB_NAME,
                7,
                true,
                MIGRATION_5_6,
                MIGRATION_6_7,
            )

        assertTableExists(migrated, "challenge_progress_entries")
        assertColumnExists(migrated, tableName = "challenges", columnName = "categoryId")
    }

    private fun assertTableExists(
        database: SupportSQLiteDatabase,
        tableName: String,
    ) {
        database.query("SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?", arrayOf(tableName)).use { cursor ->
            assertEquals(1, cursor.count)
        }
    }

    private fun assertColumnExists(
        database: SupportSQLiteDatabase,
        tableName: String,
        columnName: String,
    ) {
        database.query("PRAGMA table_info($tableName)").use { cursor ->
            val nameColumnIndex = cursor.getColumnIndexOrThrow("name")
            var found = false
            while (cursor.moveToNext()) {
                if (cursor.getString(nameColumnIndex) == columnName) {
                    found = true
                    break
                }
            }
            assertEquals(true, found)
        }
    }

    private companion object {
        const val MIGRATION_5_6_DB_NAME = "challenges-migration-5-6-test"
        const val MIGRATION_6_7_DB_NAME = "challenges-migration-6-7-test"
        const val MIGRATION_5_7_DB_NAME = "challenges-migration-5-7-test"
    }
}
