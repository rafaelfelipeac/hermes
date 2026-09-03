package com.rafaelfelipeac.hermes.features.backup.domain.model

data class BackupChallengeRecord(
    val id: Long,
    val categoryId: Long?,
    val title: String,
    val description: String?,
    val targetType: String,
    val targetQuantity: Long,
    val startDate: String,
    val endDate: String,
    val lifecycle: String,
    val archivedAt: String?,
    val createdAt: String,
    val updatedAt: String,
)
