package com.rafaelfelipeac.hermes.features.personalrecords.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.core.useraction.domain.UserAction
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_COMPARISON_RULE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_ENTRY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_FAMILY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_FAMILY_TITLE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_METRIC_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_NORMALIZED_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_RECORD_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_UNIT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_PERSONAL_RECORD_ENTRY
import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryEntity
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordEntryEntity
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordFamilyEntity
import com.rafaelfelipeac.hermes.features.personalrecords.domain.command.PersonalRecordCommandResult
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.MANUAL
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.DISTANCE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOMETER
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@RunWith(AndroidJUnit4::class)
class RoomPersonalRecordCommandRepositoryTest {
    private lateinit var context: Context
    private lateinit var database: HermesDatabase
    private lateinit var logger: FakeUserActionLogger
    private lateinit var repository: RoomPersonalRecordCommandRepository

    @Before
    fun setUp() =
        runTest {
            context = ApplicationProvider.getApplicationContext()
            database =
                Room.inMemoryDatabaseBuilder(context, HermesDatabase::class.java)
                    .allowMainThreadQueries()
                    .build()
            logger = FakeUserActionLogger()
            repository = RoomPersonalRecordCommandRepository(database, logger, FIXED_CLOCK)
        }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun deleteEntry_clearsManualReferenceDeletesEntryPreservesFamilyFieldsAndLogsOnce() =
        runTest {
            seedFamilyWithEntries(manualCurrentEntryId = ENTRY_ID)

            val result = repository.deleteEntry(ENTRY_ID)

            assertEquals(PersonalRecordCommandResult.Changed, result)
            assertNull(database.personalRecordDao().getEntry(ENTRY_ID))
            val family = database.personalRecordDao().getFamily(FAMILY_ID)
            assertEquals(CATEGORY_ID, family?.categoryId)
            assertEquals(FAMILY_TITLE, family?.title)
            assertEquals(DISTANCE, family?.metricType)
            assertEquals(KILOMETER, family?.defaultUnit)
            assertEquals(MANUAL, family?.comparisonRule)
            assertNull(family?.manualCurrentEntryId)
            assertEquals(0, family?.sortOrder)
            assertEquals(CREATED_AT, family?.createdAt)
            assertEquals(UPDATED_AT, family?.updatedAt)
            assertEquals(OTHER_ENTRY_ID, database.personalRecordDao().getEntries().single().id)
            logger.assertDeleteEntryLoggedOnce()
        }

    @Test
    fun deleteEntry_keepsManualReferenceWhenDeletingAnotherEntry() =
        runTest {
            seedFamilyWithEntries(manualCurrentEntryId = OTHER_ENTRY_ID)

            val result = repository.deleteEntry(ENTRY_ID)

            assertEquals(PersonalRecordCommandResult.Changed, result)
            assertNull(database.personalRecordDao().getEntry(ENTRY_ID))
            assertEquals(OTHER_ENTRY_ID, database.personalRecordDao().getFamily(FAMILY_ID)?.manualCurrentEntryId)
            logger.assertDeleteEntryLoggedOnce()
        }

    @Test
    fun deleteEntry_returnsNoChangeForMissingEntryWithoutLogging() =
        runTest {
            seedFamilyWithEntries(manualCurrentEntryId = ENTRY_ID)

            val result = repository.deleteEntry(999L)

            assertEquals(PersonalRecordCommandResult.NoChange, result)
            assertEquals(2, database.personalRecordDao().getEntries().size)
            assertEquals(ENTRY_ID, database.personalRecordDao().getFamily(FAMILY_ID)?.manualCurrentEntryId)
            assertTrue(logger.actions.isEmpty())
        }

    @Test
    fun deleteEntry_rollsBackEntryFamilyAndLogWhenLoggerFails() =
        runTest {
            seedFamilyWithEntries(manualCurrentEntryId = ENTRY_ID)
            logger.failNextLog = true

            val result = runCatching { repository.deleteEntry(ENTRY_ID) }

            assertTrue(result.isFailure)
            assertEquals(ENTRY_ID, database.personalRecordDao().getEntry(ENTRY_ID)?.id)
            assertEquals(ENTRY_ID, database.personalRecordDao().getFamily(FAMILY_ID)?.manualCurrentEntryId)
            assertEquals(UPDATED_AT, database.personalRecordDao().getFamily(FAMILY_ID)?.updatedAt)
            assertTrue(logger.actions.isEmpty())
        }

    private suspend fun seedFamilyWithEntries(manualCurrentEntryId: Long?) {
        database.categoryDao().insert(sampleCategory())
        database.personalRecordDao().insertFamily(sampleFamily(manualCurrentEntryId))
        database.personalRecordDao().insertEntry(sampleEntry(id = ENTRY_ID, value = 42.2))
        database.personalRecordDao().insertEntry(sampleEntry(id = OTHER_ENTRY_ID, value = 43.0))
    }

    private fun sampleCategory() =
        CategoryEntity(
            id = CATEGORY_ID,
            name = CATEGORY_NAME,
            colorId = "run",
            sortOrder = 0,
            isHidden = false,
            isSystem = true,
        )

    private fun sampleFamily(manualCurrentEntryId: Long?) =
        PersonalRecordFamilyEntity(
            id = FAMILY_ID,
            categoryId = CATEGORY_ID,
            title = FAMILY_TITLE,
            metricType = DISTANCE,
            defaultUnit = KILOMETER,
            comparisonRule = MANUAL,
            manualCurrentEntryId = manualCurrentEntryId,
            sortOrder = 0,
            createdAt = CREATED_AT,
            updatedAt = UPDATED_AT,
        )

    private fun sampleEntry(
        id: Long,
        value: Double,
    ) = PersonalRecordEntryEntity(
        id = id,
        familyId = FAMILY_ID,
        value = value,
        unit = KILOMETER,
        customUnitLabel = null,
        recordDate = LocalDate.parse("2026-09-07"),
        note = "Race day",
        createdAt = CREATED_AT,
        updatedAt = UPDATED_AT,
    )

    private class FakeUserActionLogger : UserActionLogger {
        val actions = mutableListOf<UserAction>()
        var failNextLog = false

        override suspend fun log(action: UserAction) {
            if (failNextLog) {
                failNextLog = false
                error("Forced logger failure")
            }

            actions.add(action)
        }

        fun assertDeleteEntryLoggedOnce() {
            val action = actions.single()
            assertEquals(DELETE_PERSONAL_RECORD_ENTRY, action.actionType)
            assertEquals(ENTRY_ID, action.entityId)
            assertEquals(ENTRY_ID.toString(), action.metadata?.get(PERSONAL_RECORD_ENTRY_ID))
            assertEquals(FAMILY_ID.toString(), action.metadata?.get(PERSONAL_RECORD_FAMILY_ID))
            assertEquals(FAMILY_TITLE, action.metadata?.get(PERSONAL_RECORD_FAMILY_TITLE))
            assertEquals(CATEGORY_ID.toString(), action.metadata?.get(PERSONAL_RECORD_CATEGORY_ID))
            assertEquals(CATEGORY_NAME, action.metadata?.get(PERSONAL_RECORD_CATEGORY_NAME))
            assertEquals(DISTANCE.name, action.metadata?.get(PERSONAL_RECORD_METRIC_TYPE))
            assertEquals(KILOMETER.name, action.metadata?.get(PERSONAL_RECORD_UNIT))
            assertEquals(MANUAL.name, action.metadata?.get(PERSONAL_RECORD_COMPARISON_RULE))
            assertEquals("2026-09-07", action.metadata?.get(PERSONAL_RECORD_RECORD_DATE))
            assertEquals("42.2", action.metadata?.get(PERSONAL_RECORD_NEW_VALUE))
            assertEquals("42200.0", action.metadata?.get(PERSONAL_RECORD_NORMALIZED_VALUE))
        }
    }

    private companion object {
        const val CATEGORY_ID = 100L
        const val CATEGORY_NAME = "Run"
        const val FAMILY_ID = 200L
        const val FAMILY_TITLE = "Marathon"
        const val ENTRY_ID = 300L
        const val OTHER_ENTRY_ID = 301L
        const val CREATED_AT = 1_756_800_000_000L
        const val UPDATED_AT = 1_756_886_400_000L
        val FIXED_CLOCK: Clock = Clock.fixed(Instant.ofEpochMilli(UPDATED_AT), ZoneOffset.UTC)
    }
}
