package com.rafaelfelipeac.hermes.features.backup.domain.model

data class BackupPersonalRecordFamilyRecord(
    val id: Long,
    val categoryId: Long?,
    val title: String,
    val metricType: String,
    val defaultUnit: String,
    val comparisonRule: String,
    val manualCurrentEntryId: Long?,
    val sortOrder: Int,
    val createdAt: String,
    val updatedAt: String,
)
