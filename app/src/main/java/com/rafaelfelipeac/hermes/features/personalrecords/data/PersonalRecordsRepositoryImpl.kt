package com.rafaelfelipeac.hermes.features.personalrecords.data

import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordDao
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordEntryEntity
import com.rafaelfelipeac.hermes.features.personalrecords.data.local.PersonalRecordFamilyEntity
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import com.rafaelfelipeac.hermes.features.personalrecords.domain.repository.PersonalRecordsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonalRecordsRepositoryImpl
    @Inject
    constructor(
        private val personalRecordDao: PersonalRecordDao,
    ) : PersonalRecordsRepository {
        override fun observeFamilies(): Flow<List<PersonalRecordFamily>> {
            return personalRecordDao.observeFamilies().map { entities ->
                entities.map { it.toDomain() }
            }
        }

        override fun observeEntries(): Flow<List<PersonalRecordEntry>> {
            return personalRecordDao.observeEntries().map { entities ->
                entities.map { it.toDomain() }
            }
        }

        override fun observeEntriesForFamily(familyId: Long): Flow<List<PersonalRecordEntry>> {
            return personalRecordDao.observeEntriesForFamily(familyId).map { entities ->
                entities.map { it.toDomain() }
            }
        }

        override suspend fun getFamilies(): List<PersonalRecordFamily> {
            return personalRecordDao.getFamilies().map { it.toDomain() }
        }

        override suspend fun getEntries(): List<PersonalRecordEntry> {
            return personalRecordDao.getEntries().map { it.toDomain() }
        }

        override suspend fun getFamily(id: Long): PersonalRecordFamily? {
            return personalRecordDao.getFamily(id)?.toDomain()
        }

        override suspend fun getEntry(id: Long): PersonalRecordEntry? {
            return personalRecordDao.getEntry(id)?.toDomain()
        }

        override suspend fun insertFamily(family: PersonalRecordFamily): Long {
            return personalRecordDao.insertFamily(family.toEntity())
        }

        override suspend fun updateFamily(family: PersonalRecordFamily) {
            personalRecordDao.updateFamily(family.toEntity())
        }

        override suspend fun reassignCategory(
            categoryId: Long,
            newCategoryId: Long?,
        ) {
            personalRecordDao.reassignCategory(categoryId, newCategoryId)
        }

        override suspend fun deleteFamily(id: Long) {
            personalRecordDao.deleteFamilyAndEntries(id)
        }

        override suspend fun insertEntry(entry: PersonalRecordEntry): Long {
            return personalRecordDao.insertEntry(entry.toEntity())
        }

        override suspend fun updateEntry(entry: PersonalRecordEntry) {
            personalRecordDao.updateEntry(entry.toEntity())
        }

        override suspend fun deleteEntry(id: Long) {
            personalRecordDao.deleteEntry(id)
        }
    }

private fun PersonalRecordFamilyEntity.toDomain(): PersonalRecordFamily {
    return PersonalRecordFamily(
        id = id,
        categoryId = categoryId,
        title = title,
        metricType = metricType,
        defaultUnit = defaultUnit,
        comparisonRule = comparisonRule,
        manualCurrentEntryId = manualCurrentEntryId,
        sortOrder = sortOrder,
        createdAt = Instant.ofEpochMilli(createdAt),
        updatedAt = Instant.ofEpochMilli(updatedAt),
    )
}

private fun PersonalRecordEntryEntity.toDomain(): PersonalRecordEntry {
    return PersonalRecordEntry(
        id = id,
        familyId = familyId,
        value = value,
        unit = unit,
        customUnitLabel = customUnitLabel,
        recordDate = recordDate,
        note = note,
        createdAt = Instant.ofEpochMilli(createdAt),
        updatedAt = Instant.ofEpochMilli(updatedAt),
    )
}

private fun PersonalRecordFamily.toEntity(): PersonalRecordFamilyEntity {
    return PersonalRecordFamilyEntity(
        id = id,
        categoryId = categoryId,
        title = title,
        metricType = metricType,
        defaultUnit = defaultUnit,
        comparisonRule = comparisonRule,
        manualCurrentEntryId = manualCurrentEntryId,
        sortOrder = sortOrder,
        createdAt = createdAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli(),
    )
}

private fun PersonalRecordEntry.toEntity(): PersonalRecordEntryEntity {
    return PersonalRecordEntryEntity(
        id = id,
        familyId = familyId,
        value = value,
        unit = unit,
        customUnitLabel = customUnitLabel,
        recordDate = recordDate,
        note = note,
        createdAt = createdAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli(),
    )
}
