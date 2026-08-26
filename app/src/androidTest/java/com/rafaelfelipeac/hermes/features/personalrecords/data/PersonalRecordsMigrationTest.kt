package com.rafaelfelipeac.hermes.features.personalrecords.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.core.database.MIGRATION_3_4
import com.rafaelfelipeac.hermes.core.database.MIGRATION_4_5
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersonalRecordsMigrationTest {
    private val testDbName = "personal-records-migration-test"

    @get:Rule
    val helper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            HermesDatabase::class.java,
        )

    @Test
    fun migration_4_5_keepsPersonalRecordTablesAvailable() {
        helper.createDatabase(testDbName, 4).close()

        val migrated =
            helper.runMigrationsAndValidate(
                testDbName,
                5,
                true,
                MIGRATION_4_5,
            )

        assertTableExists(migrated, "personal_record_families")
        assertTableExists(migrated, "personal_record_entries")
    }

    @Test
    fun migration_3_4_createsPersonalRecordTables() {
        helper.createDatabase(testDbName, 3).close()

        val migrated =
            helper.runMigrationsAndValidate(
                testDbName,
                4,
                true,
                MIGRATION_3_4,
            )

        assertTableExists(migrated, "personal_record_families")
        assertTableExists(migrated, "personal_record_entries")
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
