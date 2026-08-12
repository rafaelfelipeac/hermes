package com.rafaelfelipeac.hermes.features.personalrecords.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.HIGHER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.DISTANCE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOMETER
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class PersonalRecordsRepositoryImplTest {
    private lateinit var context: Context
    private lateinit var database: HermesDatabase
    private lateinit var repository: PersonalRecordsRepositoryImpl

    @Before
    fun setUp() =
        runTest {
            context = ApplicationProvider.getApplicationContext()
            database =
                Room.inMemoryDatabaseBuilder(context, HermesDatabase::class.java)
                    .allowMainThreadQueries()
                    .build()
            repository = PersonalRecordsRepositoryImpl(database.personalRecordDao())
        }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertFamilyAndEntries_persistsAndObserves() =
        runTest {
            val familyId = repository.insertFamily(sampleFamily())
            val firstEntryId =
                repository.insertEntry(
                    sampleEntry(familyId = familyId, id = 0, value = 5.0, recordDate = LocalDate.parse("2024-01-01")),
                )
            val secondEntryId =
                repository.insertEntry(
                    sampleEntry(familyId = familyId, id = 0, value = 6.0, recordDate = LocalDate.parse("2024-01-02")),
                )

            val families = repository.getFamilies()
            val entries = repository.getEntries()

            assertEquals(1, families.size)
            assertEquals(2, entries.size)
            assertEquals(familyId, families.first().id)
            assertEquals(secondEntryId, entries.first().id)
            assertEquals(firstEntryId, entries.last().id)
            assertEquals(familyId, entries.first().familyId)

            val observedFamilies = repository.observeFamilies().first()
            val observedEntries = repository.observeEntriesForFamily(familyId).first()

            assertEquals(1, observedFamilies.size)
            assertEquals(2, observedEntries.size)
            assertEquals(familyId, observedFamilies.first().id)
        }

    @Test
    fun deleteFamily_removesEntries() =
        runTest {
            val familyId = repository.insertFamily(sampleFamily())
            repository.insertEntry(sampleEntry(familyId = familyId, id = 0, value = 5.0, recordDate = LocalDate.parse("2024-01-01")))

            repository.deleteFamily(familyId)

            assertEquals(0, repository.getFamilies().size)
            assertEquals(0, repository.getEntries().size)
        }

    private fun sampleFamily() =
        PersonalRecordFamily(
            id = 0L,
            categoryId = null,
            title = "5K",
            metricType = DISTANCE,
            defaultUnit = KILOMETER,
            comparisonRule = HIGHER_IS_BETTER,
            manualCurrentEntryId = null,
            sortOrder = 0,
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-01-01T00:00:00Z"),
        )

    private fun sampleEntry(
        familyId: Long,
        id: Long,
        value: Double,
        recordDate: LocalDate,
    ) = PersonalRecordEntry(
        id = id,
        familyId = familyId,
        value = value,
        unit = KILOMETER,
        customUnitLabel = null,
        recordDate = recordDate,
        note = null,
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z"),
    )
}
