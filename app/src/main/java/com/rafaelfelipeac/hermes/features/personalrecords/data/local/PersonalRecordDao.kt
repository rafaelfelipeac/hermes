package com.rafaelfelipeac.hermes.features.personalrecords.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
@Suppress("TooManyFunctions")
interface PersonalRecordDao {
    @Query("SELECT * FROM personal_record_families ORDER BY sortOrder ASC, id ASC")
    fun observeFamilies(): Flow<List<PersonalRecordFamilyEntity>>

    @Query("SELECT * FROM personal_record_entries ORDER BY recordDate DESC, id DESC")
    fun observeEntries(): Flow<List<PersonalRecordEntryEntity>>

    @Query("SELECT * FROM personal_record_entries WHERE familyId = :familyId ORDER BY recordDate DESC, id DESC")
    fun observeEntriesForFamily(familyId: Long): Flow<List<PersonalRecordEntryEntity>>

    @Query("SELECT * FROM personal_record_families ORDER BY sortOrder ASC, id ASC")
    suspend fun getFamilies(): List<PersonalRecordFamilyEntity>

    @Query("SELECT * FROM personal_record_entries ORDER BY recordDate DESC, id DESC")
    suspend fun getEntries(): List<PersonalRecordEntryEntity>

    @Query("SELECT * FROM personal_record_families WHERE id = :id")
    suspend fun getFamily(id: Long): PersonalRecordFamilyEntity?

    @Query("SELECT * FROM personal_record_entries WHERE id = :id")
    suspend fun getEntry(id: Long): PersonalRecordEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamily(family: PersonalRecordFamilyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilies(families: List<PersonalRecordFamilyEntity>): List<Long>

    @Update
    suspend fun updateFamily(family: PersonalRecordFamilyEntity)

    @Query("UPDATE personal_record_families SET categoryId = :newCategoryId WHERE categoryId = :categoryId")
    suspend fun reassignCategory(
        categoryId: Long,
        newCategoryId: Long?,
    )

    @Query("DELETE FROM personal_record_families WHERE id = :id")
    suspend fun deleteFamily(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: PersonalRecordEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<PersonalRecordEntryEntity>): List<Long>

    @Update
    suspend fun updateEntry(entry: PersonalRecordEntryEntity)

    @Query("DELETE FROM personal_record_entries WHERE id = :id")
    suspend fun deleteEntry(id: Long)

    @Query("DELETE FROM personal_record_entries WHERE familyId = :familyId")
    suspend fun deleteEntriesForFamily(familyId: Long)

    @Transaction
    suspend fun deleteFamilyAndEntries(familyId: Long) {
        deleteEntriesForFamily(familyId)
        deleteFamily(familyId)
    }

    @Query("DELETE FROM personal_record_families")
    suspend fun deleteAllFamilies()

    @Query("DELETE FROM personal_record_entries")
    suspend fun deleteAllEntries()
}
