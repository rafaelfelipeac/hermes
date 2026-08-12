package com.rafaelfelipeac.hermes.features.personalrecords.domain

import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.HIGHER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.LOWER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.MANUAL
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily

object PersonalRecordHistoryOrderer {
    fun order(
        family: PersonalRecordFamily,
        entries: List<PersonalRecordEntry>,
    ): List<PersonalRecordEntry> {
        val familyEntries = entries.filter { it.familyId == family.id }

        return when (family.comparisonRule) {
            HIGHER_IS_BETTER -> familyEntries.sortedWith(::compareForBestFirst)
            LOWER_IS_BETTER -> familyEntries.sortedWith { first, second -> compareForBestFirst(second, first) }
            MANUAL -> orderManualHistory(family, familyEntries)
        }
    }

    private fun orderManualHistory(
        family: PersonalRecordFamily,
        entries: List<PersonalRecordEntry>,
    ): List<PersonalRecordEntry> {
        val manualCurrent = family.manualCurrentEntryId?.let { currentEntryId ->
            entries.firstOrNull { it.id == currentEntryId }
        }
        val remaining = entries.filterNot { it.id == manualCurrent?.id }.sortedWith(::compareByRecencyDescending)

        return listOfNotNull(manualCurrent) + remaining
    }

    private fun compareForBestFirst(
        first: PersonalRecordEntry,
        second: PersonalRecordEntry,
    ): Int {
        val normalizedFirst = PersonalRecordValueNormalizer.normalize(first.value, first.unit)
        val normalizedSecond = PersonalRecordValueNormalizer.normalize(second.value, second.unit)
        val normalizedCompare = normalizedSecond.compareTo(normalizedFirst)

        if (normalizedCompare != 0) return normalizedCompare

        return compareByRecencyDescending(first, second)
    }

    private fun compareByRecencyDescending(
        first: PersonalRecordEntry,
        second: PersonalRecordEntry,
    ): Int {
        val dateCompare = second.recordDate.compareTo(first.recordDate)
        if (dateCompare != 0) return dateCompare

        val createdAtCompare = second.createdAt.compareTo(first.createdAt)
        if (createdAtCompare != 0) return createdAtCompare

        return second.id.compareTo(first.id)
    }
}
