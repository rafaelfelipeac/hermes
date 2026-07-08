package com.rafaelfelipeac.hermes.features.personalrecords.domain

import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.HIGHER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.LOWER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.MANUAL
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule

object PersonalRecordBestSelector {
    fun selectBest(
        family: PersonalRecordFamily,
        entries: List<PersonalRecordEntry>,
    ): PersonalRecordEntry? {
        val familyEntries = entries.filter { it.familyId == family.id }
        if (familyEntries.isEmpty()) {
            return null
        }

        return when (family.comparisonRule) {
            HIGHER_IS_BETTER ->
                familyEntries.maxWithOrNull { first, second ->
                    compareEntriesForRule(first, second, family.comparisonRule)
                }

            LOWER_IS_BETTER ->
                familyEntries.maxWithOrNull { first, second ->
                    compareEntriesForRule(first, second, family.comparisonRule)
                }

            MANUAL ->
                family.manualCurrentEntryId?.let { currentEntryId ->
                    familyEntries.firstOrNull { it.id == currentEntryId }
                } ?: familyEntries.maxWithOrNull(::compareEntriesForRecency)
        }
    }

    private fun compareEntriesForRule(
        first: PersonalRecordEntry,
        second: PersonalRecordEntry,
        comparisonRule: PersonalRecordComparisonRule,
    ): Int {
        val firstNormalized = PersonalRecordValueNormalizer.normalize(first.value, first.unit)
        val secondNormalized = PersonalRecordValueNormalizer.normalize(second.value, second.unit)
        val normalizedCompare =
            when (comparisonRule) {
                HIGHER_IS_BETTER -> firstNormalized.compareTo(secondNormalized)
                LOWER_IS_BETTER -> secondNormalized.compareTo(firstNormalized)
                MANUAL -> 0
            }
        if (normalizedCompare != 0) {
            return normalizedCompare
        }

        return compareEntriesForRecency(first, second)
    }

    private fun compareEntriesForRecency(
        first: PersonalRecordEntry,
        second: PersonalRecordEntry,
    ): Int {
        val recordDateCompare = first.recordDate.compareTo(second.recordDate)
        if (recordDateCompare != 0) {
            return recordDateCompare
        }

        val createdAtCompare = first.createdAt.compareTo(second.createdAt)
        if (createdAtCompare != 0) {
            return createdAtCompare
        }

        return first.id.compareTo(second.id)
    }
}
