package com.rafaelfelipeac.hermes.features.personalrecords.domain

import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry

object PersonalRecordLatestEntrySelector {
    fun latestForFamily(
        familyId: Long,
        entries: List<PersonalRecordEntry>,
    ): PersonalRecordEntry? {
        return entries
            .asSequence()
            .filter { it.familyId == familyId }
            .maxWithOrNull(
                compareBy(
                    PersonalRecordEntry::recordDate,
                    PersonalRecordEntry::createdAt,
                    PersonalRecordEntry::id,
                ),
            )
    }
}
