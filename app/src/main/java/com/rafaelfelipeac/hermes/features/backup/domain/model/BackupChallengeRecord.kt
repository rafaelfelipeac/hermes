package com.rafaelfelipeac.hermes.features.backup.domain.model

data class BackupChallengeRecord(
    val id: Long,
    val title: String,
    val description: String?,
    val targetQuantity: Long,
    val unit: String,
    val startDate: String,
    val endDate: String,
    val lifecycle: String,
    val archivedAt: String?,
    val createdAt: String,
    val updatedAt: String,
)
