package com.rafaelfelipeac.hermes.features.personalrecords.domain

import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.LOWER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.TIME
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.SECOND
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class PersonalRecordHistoryOrdererTest {
    @Test
    fun order_lowerIsBetter_prefersMostRecentRecordOnTie() {
        val older = entry(id = 1L, recordDate = LocalDate.parse("2024-01-01"))
        val newer = entry(id = 2L, recordDate = LocalDate.parse("2024-02-01"))

        val ordered = PersonalRecordHistoryOrderer.order(family(), listOf(older, newer))

        assertEquals(listOf(newer, older), ordered)
    }

    private fun family() =
        PersonalRecordFamily(
            id = FAMILY_ID,
            categoryId = null,
            title = "5K",
            metricType = TIME,
            defaultUnit = SECOND,
            comparisonRule = LOWER_IS_BETTER,
            manualCurrentEntryId = null,
            sortOrder = 0,
            createdAt = FIXED_INSTANT,
            updatedAt = FIXED_INSTANT,
        )

    private fun entry(
        id: Long,
        recordDate: LocalDate,
    ) = PersonalRecordEntry(
        id = id,
        familyId = FAMILY_ID,
        value = TIED_VALUE_SECONDS,
        unit = SECOND,
        customUnitLabel = null,
        recordDate = recordDate,
        note = null,
        createdAt = FIXED_INSTANT,
        updatedAt = FIXED_INSTANT,
    )

    private companion object {
        const val FAMILY_ID = 1L
        const val TIED_VALUE_SECONDS = 300.0
        val FIXED_INSTANT: Instant = Instant.parse("2024-01-01T00:00:00Z")
    }
}
