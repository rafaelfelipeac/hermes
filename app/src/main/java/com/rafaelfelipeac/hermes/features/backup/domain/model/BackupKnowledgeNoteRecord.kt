package com.rafaelfelipeac.hermes.features.backup.domain.model

data class BackupKnowledgeNoteRecord(
    val id: Long,
    val kind: String,
    val status: String,
    val title: String?,
    val body: String,
    val sourceWorkoutId: Long?,
    val sourceType: String?,
    val sourceTitle: String?,
    val categoryId: Long?,
    val triggerScope: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
