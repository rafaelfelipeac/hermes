package com.rafaelfelipeac.hermes.features.backup.domain.model

data class BackupPersonalRecordEntryRecord(
    val id: Long,
    val familyId: Long,
    val value: Double,
    val unit: String,
    val customUnitLabel: String?,
    val recordDate: String,
    val note: String?,
    val createdAt: String,
    val updatedAt: String,
)
