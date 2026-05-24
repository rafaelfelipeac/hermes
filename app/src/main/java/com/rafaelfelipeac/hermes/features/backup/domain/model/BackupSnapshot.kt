package com.rafaelfelipeac.hermes.features.backup.domain.model

data class BackupSnapshot(
    val schemaVersion: Int,
    val exportedAt: String,
    val appVersion: String? = null,
    val workouts: List<BackupWorkoutRecord>,
    val notes: List<BackupKnowledgeNoteRecord> = emptyList(),
    val categories: List<BackupCategoryRecord>,
    val userActions: List<BackupUserActionRecord>,
    val settings: BackupSettingsRecord? = null,
)
