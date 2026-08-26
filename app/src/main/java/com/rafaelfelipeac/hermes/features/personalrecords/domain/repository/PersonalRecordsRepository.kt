package com.rafaelfelipeac.hermes.features.personalrecords.domain.repository

import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import kotlinx.coroutines.flow.Flow

interface PersonalRecordsRepository {
    fun observeFamilies(): Flow<List<PersonalRecordFamily>>

    fun observeEntries(): Flow<List<PersonalRecordEntry>>

    fun observeEntriesForFamily(familyId: Long): Flow<List<PersonalRecordEntry>>

    suspend fun getFamilies(): List<PersonalRecordFamily>

    suspend fun getEntries(): List<PersonalRecordEntry>

    suspend fun getFamily(id: Long): PersonalRecordFamily?

    suspend fun getEntry(id: Long): PersonalRecordEntry?

    suspend fun insertFamily(family: PersonalRecordFamily): Long

    suspend fun updateFamily(family: PersonalRecordFamily)

    suspend fun reassignCategory(
        categoryId: Long,
        newCategoryId: Long?,
    )

    suspend fun deleteFamily(id: Long)

    suspend fun insertEntry(entry: PersonalRecordEntry): Long

    suspend fun updateEntry(entry: PersonalRecordEntry)

    suspend fun deleteEntry(id: Long)
}
