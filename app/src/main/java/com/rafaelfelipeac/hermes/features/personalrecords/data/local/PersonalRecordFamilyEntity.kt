package com.rafaelfelipeac.hermes.features.personalrecords.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit

private const val PERSONAL_RECORD_FAMILY_TABLE_NAME = "personal_record_families"

@Entity(
    tableName = PERSONAL_RECORD_FAMILY_TABLE_NAME,
    indices = [Index(value = ["categoryId"])],
)
data class PersonalRecordFamilyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val categoryId: Long?,
    val title: String,
    val metricType: PersonalRecordMetricType,
    val defaultUnit: PersonalRecordUnit,
    val comparisonRule: PersonalRecordComparisonRule,
    val manualCurrentEntryId: Long?,
    val sortOrder: Int,
    val createdAt: Long,
    val updatedAt: Long,
)
