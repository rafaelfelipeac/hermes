package com.rafaelfelipeac.hermes.features.challenges.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.core.database.MIGRATION_5_6
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChallengesMigrationTest {
    private val testDbName = "challenges-migration-test"

    @get:Rule
    val helper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            HermesDatabase::class.java,
        )

    @Test
    fun migration_5_6_createsChallengeTables() {
        helper.createDatabase(testDbName, 5).close()

        val migrated =
            helper.runMigrationsAndValidate(
                testDbName,
                6,
                true,
                MIGRATION_5_6,
            )

        assertTableExists(migrated, "challenges")
        assertTableExists(migrated, "challenge_progress_entries")
    }

    private fun assertTableExists(
        database: SupportSQLiteDatabase,
        tableName: String,
    ) {
        database.query("SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?", arrayOf(tableName)).use { cursor ->
            assertEquals(1, cursor.count)
        }
    }
}
