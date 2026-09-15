package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.time.DurationParts
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.HIGHER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.LOWER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.DISTANCE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.TIME
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.WEIGHT
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOGRAM
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOMETER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.MILE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.MINUTE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.POUND
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.SECOND
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.MILES
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit.POUNDS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class PersonalRecordEditorRulesTest {
    @Test
    fun newDistanceEntry_usesPreferredUnitAndCurrentBest() {
        val family = family(metricType = DISTANCE, defaultUnit = KILOMETER)
        val best = entry(id = 1L, value = 10.0, unit = KILOMETER)
        val latest = entry(id = 2L, value = 5.0, unit = KILOMETER, date = LocalDate.of(2024, 2, 1))

        val defaults =
            personalRecordEntryEditorDefaults(
                source(family = family, entries = listOf(best, latest)),
            )

        assertEquals(MILE, defaults.unit)
        assertEquals("6.21", defaults.valueText)
    }

    @Test
    fun editTimeEntry_normalizesPersistedMinutesAndBuildsSecondsInput() {
        val family = family(metricType = TIME, defaultUnit = SECOND, comparisonRule = LOWER_IS_BETTER)
        val entry = entry(value = 5.0, unit = MINUTE)
        val defaults =
            personalRecordEntryEditorDefaults(source(family, initialEntry = entry, isEdit = true))

        assertEquals(5, defaults.time.minutes)
        val input =
            buildPersonalRecordEntryInput(
                family = family,
                fields =
                    fields(
                        valueText = EMPTY,
                        selectedUnit = defaults.unit,
                        time = defaults.time,
                        note = " note ",
                    ),
            )

        assertEquals(300.0, input?.value ?: 0.0, 0.0)
        assertEquals(SECOND, input?.unit)
        assertEquals("note", input?.note)
        assertNull(input?.customUnitLabel)
    }

    @Test
    fun editWeightEntry_keepsStoredUnitInsteadOfPreference() {
        val family = family(metricType = WEIGHT, defaultUnit = KILOGRAM)
        val defaults =
            personalRecordEntryEditorDefaults(
                source(
                    family = family,
                    initialEntry = entry(value = 220.0, unit = POUND),
                    isEdit = true,
                ),
            )

        assertEquals(POUND, defaults.unit)
        assertEquals("220", defaults.valueText)
    }

    @Test
    fun conversionAndValidation_preserveEditorBehavior() {
        assertEquals("1.61", convertPersonalRecordEditorValue("1,609344", KILOMETER, KILOMETER))
        val family = family(metricType = DISTANCE, defaultUnit = KILOMETER)
        val today = LocalDate.of(2024, 1, 1)

        assertTrue(canSavePersonalRecordEntry(family, fields(valueText = "5,25"), today))
        assertFalse(canSavePersonalRecordEntry(family, fields(valueText = "invalid"), today))
        assertFalse(
            canSavePersonalRecordEntry(
                family,
                fields(valueText = "5", recordDate = today.plusDays(1)),
                today,
            ),
        )
    }

    @Test
    fun metricSelection_usesMetricDefaultComparisonRule() {
        assertEquals(LOWER_IS_BETTER, comparisonRuleAfterMetricSelection(TIME))
        assertEquals(HIGHER_IS_BETTER, comparisonRuleAfterMetricSelection(WEIGHT))
    }

    private fun family(
        metricType: PersonalRecordMetricType,
        defaultUnit: PersonalRecordUnit,
        comparisonRule: PersonalRecordComparisonRule = HIGHER_IS_BETTER,
    ) = PersonalRecordFamily(
        id = 10L,
        categoryId = null,
        title = "Series",
        metricType = metricType,
        defaultUnit = defaultUnit,
        comparisonRule = comparisonRule,
        manualCurrentEntryId = null,
        sortOrder = 0,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
    )

    private fun source(
        family: PersonalRecordFamily,
        entries: List<PersonalRecordEntry> = emptyList(),
        initialEntry: PersonalRecordEntry? = null,
        isEdit: Boolean = false,
    ) = PersonalRecordEntryEditorSource(
        family = family,
        entries = entries,
        initialEntry = initialEntry,
        isEdit = isEdit,
        settingsDistanceUnit = MILES,
        settingsWeightUnit = POUNDS,
    )

    private fun fields(
        valueText: String,
        selectedUnit: PersonalRecordUnit = KILOMETER,
        time: DurationParts = DurationParts(),
        recordDate: LocalDate = LocalDate.of(2024, 1, 1),
        note: String = EMPTY,
    ) = PersonalRecordEntryEditorFields(
        valueText = valueText,
        selectedUnit = selectedUnit,
        time = time,
        recordDate = recordDate,
        note = note,
        customUnitLabel = EMPTY,
    )

    private fun entry(
        id: Long = 1L,
        value: Double,
        unit: PersonalRecordUnit,
        date: LocalDate = LocalDate.of(2024, 1, 1),
    ) = PersonalRecordEntry(
        id = id,
        familyId = 10L,
        value = value,
        unit = unit,
        customUnitLabel = null,
        recordDate = date,
        note = null,
        createdAt = Instant.EPOCH.plusSeconds(id),
        updatedAt = Instant.EPOCH.plusSeconds(id),
    )
}
