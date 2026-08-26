package com.rafaelfelipeac.hermes.features.backup.domain.model

data class BackupSnapshot(
    val schemaVersion: Int,
    val exportedAt: String,
    val appVersion: String? = null,
    val workouts: List<BackupWorkoutRecord>,
    val categories: List<BackupCategoryRecord>,
    val personalRecordFamilies: List<BackupPersonalRecordFamilyRecord>,
    val personalRecordEntries: List<BackupPersonalRecordEntryRecord>,
    val userActions: List<BackupUserActionRecord>,
    val settings: BackupSettingsRecord? = null,
)
