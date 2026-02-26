package com.rafaelfelipeac.hermes.features.backup.domain.model

data class BackupSnapshot(
    val schemaVersion: Int,
    val exportedAt: String,
    val appVersion: String? = null,
    val workouts: List<BackupWorkoutRecord>,
    val categories: List<BackupCategoryRecord>,
    val userActions: List<BackupUserActionRecord>,
    val settings: BackupSettingsRecord? = null,
)

data class BackupWorkoutRecord(
    val id: Long,
    val weekStartDate: String,
    val dayOfWeek: Int?,
    val timeSlot: String?,
    val sortOrder: Int,
    val eventType: String,
    val type: String,
    val description: String,
    val isCompleted: Boolean,
    val categoryId: Long?,
)

data class BackupCategoryRecord(
    val id: Long,
    val name: String,
    val colorId: String,
    val sortOrder: Int,
    val isHidden: Boolean,
    val isSystem: Boolean,
)

data class BackupUserActionRecord(
    val id: Long,
    val actionType: String,
    val entityType: String,
    val entityId: Long?,
    val metadata: String?,
    val timestamp: Long,
)

data class BackupSettingsRecord(
    val themeMode: String,
    val languageTag: String,
    val slotModePolicy: String,
)
