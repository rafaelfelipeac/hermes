package com.rafaelfelipeac.hermes.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HermesMigrationDataTest {
    @get:Rule
    val helper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            HermesDatabase::class.java,
        )

    @Test
    fun migration_1_7_preservesWorkoutAndActivityHistoryData() {
        val database = helper.createDatabase("hermes-migration-data-1-7", 1)
        database.execSql(
            "INSERT INTO workouts (id, weekStartDate, dayOfWeek, type, description, isCompleted, isRestDay, sort_order) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            10L,
            "2026-09-07",
            2,
            "Run",
            "Easy run",
            1,
            0,
            3,
        )
        database.execSql(
            "INSERT INTO workouts (id, weekStartDate, dayOfWeek, type, description, isCompleted, isRestDay, sort_order) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            11L,
            "2026-09-07",
            null,
            "",
            "Rest day",
            0,
            1,
            0,
        )
        database.execSql(
            "INSERT INTO user_actions (id, actionType, entityType, entityId, metadata, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
            20L,
            "CREATE_WORKOUT",
            "WORKOUT",
            10L,
            "{\"new_description\":\"Easy run\"}",
            1_756_800_000_000L,
        )
        database.close()

        val migrated =
            helper.runMigrationsAndValidate(
                "hermes-migration-data-1-7",
                7,
                true,
                *ALL_MIGRATIONS.toTypedArray(),
            )

        migrated.query("SELECT * FROM workouts WHERE id = 10").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("2026-09-07", cursor.getString("weekStartDate"))
            assertEquals(2, cursor.getInt("dayOfWeek"))
            assertEquals("Run", cursor.getString("type"))
            assertEquals("Easy run", cursor.getString("description"))
            assertEquals(1, cursor.getInt("isCompleted"))
            assertEquals(0, cursor.getInt("isRestDay"))
            assertEquals("WORKOUT", cursor.getString("eventType"))
            assertNull(cursor.getNullableString("timeSlot"))
            assertNull(cursor.getNullableLong("categoryId"))
            assertEquals(3, cursor.getInt("sort_order"))
        }
        migrated.query("SELECT * FROM workouts WHERE id = 11").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("REST", cursor.getString("eventType"))
            assertNull(cursor.getNullableString("timeSlot"))
            assertNull(cursor.getNullableLong("categoryId"))
        }
        migrated.query("SELECT * FROM user_actions WHERE id = 20").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("CREATE_WORKOUT", cursor.getString("actionType"))
            assertEquals("WORKOUT", cursor.getString("entityType"))
            assertEquals(10L, cursor.getLong("entityId"))
            assertEquals("{\"new_description\":\"Easy run\"}", cursor.getString("metadata"))
            assertEquals(1_756_800_000_000L, cursor.getLong("timestamp"))
        }
        assertTableEmpty(migrated, "categories")
        assertTableEmpty(migrated, "personal_record_families")
        assertTableEmpty(migrated, "personal_record_entries")
        assertTableEmpty(migrated, "challenges")
        assertTableEmpty(migrated, "challenge_progress_entries")
    }

    @Test
    fun migration_4_7_preservesPersonalRecordData() {
        val database = helper.createDatabase("hermes-migration-data-4-7", 4)
        insertCategory(database)
        insertPersonalRecordFamily(database, manualCurrentEntryId = null)
        insertPersonalRecordEntry(database, entryId = 401L, value = 42.2, note = "Race day")
        insertPersonalRecordEntry(database, entryId = 402L, value = 43.0, note = null)
        database.execSql(
            "UPDATE personal_record_families SET manualCurrentEntryId = ? WHERE id = ?",
            401L,
            400L,
        )
        database.close()

        val migrated =
            helper.runMigrationsAndValidate(
                "hermes-migration-data-4-7",
                7,
                true,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
            )

        assertPersonalRecordFamily(migrated)
        migrated.query("SELECT * FROM personal_record_entries ORDER BY id").use { cursor ->
            assertEquals(2, cursor.count)
            assertTrue(cursor.moveToFirst())
            assertPersonalRecordEntry(cursor = cursor, entryId = 401L, value = 42.2, note = "Race day")
            assertTrue(cursor.moveToNext())
            assertPersonalRecordEntry(cursor = cursor, entryId = 402L, value = 43.0, note = null)
            assertFalse(cursor.moveToNext())
        }
    }

    @Test
    fun migration_5_7_preservesPersonalRecordsAndCreatesChallengeTables() {
        val database = helper.createDatabase("hermes-migration-data-5-7", 5)
        insertCategory(database)
        insertPersonalRecordFamily(database, manualCurrentEntryId = 401L)
        insertPersonalRecordEntry(database, entryId = 401L, value = 42.2, note = "Race day")
        database.close()

        val migrated =
            helper.runMigrationsAndValidate(
                "hermes-migration-data-5-7",
                7,
                true,
                MIGRATION_5_6,
                MIGRATION_6_7,
            )

        assertPersonalRecordFamily(migrated)
        migrated.query("SELECT * FROM personal_record_entries WHERE id = 401").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertPersonalRecordEntry(cursor = cursor, entryId = 401L, value = 42.2, note = "Race day")
        }
        assertTableEmpty(migrated, "challenges")
        assertTableEmpty(migrated, "challenge_progress_entries")
    }

    @Test
    fun migration_6_7_preservesChallengeDataAndAddsNullCategoryId() {
        val database = helper.createDatabase("hermes-migration-data-6-7", 6)
        database.execSql(
            "INSERT INTO challenges " +
                "(id, title, description, targetType, targetQuantity, startDate, endDate, lifecycle, " +
                "archivedAt, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            600L,
            "September distance",
            "Build consistency",
            "DAILY",
            10L,
            "2026-09-01",
            "2026-09-30",
            "ACTIVE",
            null,
            1_756_800_000_000L,
            1_756_886_400_000L,
        )
        database.execSql(
            "INSERT INTO challenge_progress_entries " +
                "(id, challengeId, quantity, entryDate, occurredAt, createdAt, updatedAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
            601L,
            600L,
            12L,
            "2026-09-02",
            1_756_886_400_000L,
            1_756_886_400_000L,
            1_756_886_400_000L,
        )
        database.close()

        val migrated =
            helper.runMigrationsAndValidate(
                "hermes-migration-data-6-7",
                7,
                true,
                MIGRATION_6_7,
            )

        migrated.query("SELECT * FROM challenges WHERE id = 600").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("September distance", cursor.getString("title"))
            assertEquals("Build consistency", cursor.getString("description"))
            assertEquals("DAILY", cursor.getString("targetType"))
            assertEquals(10L, cursor.getLong("targetQuantity"))
            assertEquals("2026-09-01", cursor.getString("startDate"))
            assertEquals("2026-09-30", cursor.getString("endDate"))
            assertEquals("ACTIVE", cursor.getString("lifecycle"))
            assertNull(cursor.getNullableLong("archivedAt"))
            assertEquals(1_756_800_000_000L, cursor.getLong("createdAt"))
            assertEquals(1_756_886_400_000L, cursor.getLong("updatedAt"))
            assertNull(cursor.getNullableLong("categoryId"))
        }
        migrated.query("SELECT * FROM challenge_progress_entries WHERE id = 601").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(600L, cursor.getLong("challengeId"))
            assertEquals(12L, cursor.getLong("quantity"))
            assertEquals("2026-09-02", cursor.getString("entryDate"))
            assertEquals(1_756_886_400_000L, cursor.getLong("occurredAt"))
            assertEquals(1_756_886_400_000L, cursor.getLong("createdAt"))
            assertEquals(1_756_886_400_000L, cursor.getLong("updatedAt"))
        }
    }

    private fun insertCategory(database: SupportSQLiteDatabase) {
        database.execSql(
            "INSERT INTO categories (id, name, colorId, sortOrder, isHidden, isSystem) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
            100L,
            "Run",
            "run",
            1,
            0,
            1,
        )
    }

    private fun insertPersonalRecordFamily(
        database: SupportSQLiteDatabase,
        manualCurrentEntryId: Long?,
    ) {
        database.execSql(
            "INSERT INTO personal_record_families " +
                "(id, categoryId, title, metricType, defaultUnit, comparisonRule, manualCurrentEntryId, " +
                "sortOrder, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            400L,
            100L,
            "Marathon",
            "DISTANCE",
            "KILOMETER",
            "HIGHER_IS_BETTER",
            manualCurrentEntryId,
            2,
            1_756_800_000_000L,
            1_756_886_400_000L,
        )
    }

    private fun insertPersonalRecordEntry(
        database: SupportSQLiteDatabase,
        entryId: Long,
        value: Double,
        note: String?,
    ) {
        database.execSql(
            "INSERT INTO personal_record_entries " +
                "(id, familyId, value, unit, customUnitLabel, recordDate, note, createdAt, updatedAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            entryId,
            400L,
            value,
            "KILOMETER",
            null,
            "2026-09-07",
            note,
            1_756_800_000_000L,
            1_756_886_400_000L,
        )
    }

    private fun assertPersonalRecordFamily(database: SupportSQLiteDatabase) {
        database.query("SELECT * FROM personal_record_families WHERE id = 400").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(100L, cursor.getLong("categoryId"))
            assertEquals("Marathon", cursor.getString("title"))
            assertEquals("DISTANCE", cursor.getString("metricType"))
            assertEquals("KILOMETER", cursor.getString("defaultUnit"))
            assertEquals("HIGHER_IS_BETTER", cursor.getString("comparisonRule"))
            assertEquals(401L, cursor.getLong("manualCurrentEntryId"))
            assertEquals(2, cursor.getInt("sortOrder"))
            assertEquals(1_756_800_000_000L, cursor.getLong("createdAt"))
            assertEquals(1_756_886_400_000L, cursor.getLong("updatedAt"))
        }
    }

    private fun assertPersonalRecordEntry(
        cursor: android.database.Cursor,
        entryId: Long,
        value: Double,
        note: String?,
    ) {
        assertEquals(entryId, cursor.getLong("id"))
        assertEquals(400L, cursor.getLong("familyId"))
        assertEquals(value, cursor.getDouble("value"), 0.0)
        assertEquals("KILOMETER", cursor.getString("unit"))
        assertNull(cursor.getNullableString("customUnitLabel"))
        assertEquals("2026-09-07", cursor.getString("recordDate"))
        assertEquals(note, cursor.getNullableString("note"))
        assertEquals(1_756_800_000_000L, cursor.getLong("createdAt"))
        assertEquals(1_756_886_400_000L, cursor.getLong("updatedAt"))
    }

    private fun assertTableEmpty(
        database: SupportSQLiteDatabase,
        tableName: String,
    ) {
        database.query("SELECT COUNT(*) FROM $tableName").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
    }

    private fun SupportSQLiteDatabase.execSql(
        sql: String,
        vararg bindArgs: Any?,
    ) {
        execSQL(sql, bindArgs)
    }

    private fun android.database.Cursor.getString(columnName: String): String {
        return getString(getColumnIndexOrThrow(columnName))
    }

    private fun android.database.Cursor.getNullableString(columnName: String): String? {
        val columnIndex = getColumnIndexOrThrow(columnName)
        return if (isNull(columnIndex)) null else getString(columnIndex)
    }

    private fun android.database.Cursor.getInt(columnName: String): Int {
        return getInt(getColumnIndexOrThrow(columnName))
    }

    private fun android.database.Cursor.getLong(columnName: String): Long {
        return getLong(getColumnIndexOrThrow(columnName))
    }

    private fun android.database.Cursor.getNullableLong(columnName: String): Long? {
        val columnIndex = getColumnIndexOrThrow(columnName)
        return if (isNull(columnIndex)) null else getLong(columnIndex)
    }

    private fun android.database.Cursor.getDouble(columnName: String): Double {
        return getDouble(getColumnIndexOrThrow(columnName))
    }
}
