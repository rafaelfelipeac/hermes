package com.rafaelfelipeac.hermes.features.personalrecords.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit
import java.time.LocalDate

private const val PERSONAL_RECORD_ENTRY_TABLE_NAME = "personal_record_entries"

@Entity(
    tableName = PERSONAL_RECORD_ENTRY_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = PersonalRecordFamilyEntity::class,
            parentColumns = ["id"],
            childColumns = ["familyId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["familyId"])],
)
data class PersonalRecordEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val familyId: Long,
    val value: Double,
    val unit: PersonalRecordUnit,
    val customUnitLabel: String?,
    val recordDate: LocalDate,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
