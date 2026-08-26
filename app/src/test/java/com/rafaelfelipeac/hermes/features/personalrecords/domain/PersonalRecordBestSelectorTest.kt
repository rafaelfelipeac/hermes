package com.rafaelfelipeac.hermes.features.personalrecords.domain

import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.HIGHER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.LOWER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.MANUAL
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.DISTANCE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOMETER
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class PersonalRecordBestSelectorTest {
    @Test
    fun selectBest_returnsHighestNormalizedValue_whenHigherIsBetter() {
        val family = family(comparisonRule = HIGHER_IS_BETTER)
        val entries =
            listOf(
                entry(id = 1, familyId = family.id, value = 1.0, unit = KILOMETER, recordDate = "2024-01-01"),
                entry(id = 2, familyId = family.id, value = 2.0, unit = KILOMETER, recordDate = "2024-01-02"),
            )

        assertEquals(entries[1], PersonalRecordBestSelector.selectBest(family, entries))
    }

    @Test
    fun selectBest_returnsLowestNormalizedValue_whenLowerIsBetter() {
        val family = family(comparisonRule = LOWER_IS_BETTER)
        val entries =
            listOf(
                entry(id = 1, familyId = family.id, value = 12.0, unit = KILOMETER, recordDate = "2024-01-01"),
                entry(id = 2, familyId = family.id, value = 10.0, unit = KILOMETER, recordDate = "2024-01-02"),
            )

        assertEquals(entries[1], PersonalRecordBestSelector.selectBest(family, entries))
    }

    @Test
    fun selectBest_returnsManualCurrentEntry_whenManualSelectionExists() {
        val family = family(comparisonRule = MANUAL, manualCurrentEntryId = 2)
        val entries =
            listOf(
                entry(id = 1, familyId = family.id, value = 1.0, unit = KILOMETER, recordDate = "2024-01-01"),
                entry(id = 2, familyId = family.id, value = 2.0, unit = KILOMETER, recordDate = "2024-01-02"),
            )

        assertEquals(entries[1], PersonalRecordBestSelector.selectBest(family, entries))
    }

    @Test
    fun selectBest_fallsBackToNewestRecordDate_whenManualSelectionIsMissing() {
        val family = family(comparisonRule = MANUAL, manualCurrentEntryId = 999)
        val entries =
            listOf(
                entry(id = 1, familyId = family.id, value = 1.0, unit = KILOMETER, recordDate = "2024-01-01"),
                entry(id = 2, familyId = family.id, value = 1.0, unit = KILOMETER, recordDate = "2024-01-03"),
            )

        assertEquals(entries[1], PersonalRecordBestSelector.selectBest(family, entries))
    }

    @Test
    fun selectBest_prefersMostRecentRecordDate_onTie() {
        val family = family(comparisonRule = HIGHER_IS_BETTER)
        val entries =
            listOf(
                entry(id = 1, familyId = family.id, value = 2.0, unit = KILOMETER, recordDate = "2024-01-01"),
                entry(id = 2, familyId = family.id, value = 2.0, unit = KILOMETER, recordDate = "2024-01-03"),
            )

        assertEquals(entries[1], PersonalRecordBestSelector.selectBest(family, entries))
    }

    private fun family(
        id: Long = 1L,
        comparisonRule: PersonalRecordComparisonRule = HIGHER_IS_BETTER,
        manualCurrentEntryId: Long? = null,
    ) = PersonalRecordFamily(
        id = id,
        categoryId = null,
        title = "5K",
        metricType = DISTANCE,
        defaultUnit = KILOMETER,
        comparisonRule = comparisonRule,
        manualCurrentEntryId = manualCurrentEntryId,
        sortOrder = 0,
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z"),
    )

    private fun entry(
        id: Long,
        familyId: Long,
        value: Double,
        unit: PersonalRecordUnit,
        recordDate: String,
    ) = PersonalRecordEntry(
        id = id,
        familyId = familyId,
        value = value,
        unit = unit,
        customUnitLabel = null,
        recordDate = LocalDate.parse(recordDate),
        note = null,
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z"),
    )
}
