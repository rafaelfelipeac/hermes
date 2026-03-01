package com.rafaelfelipeac.hermes.features.backup.domain.model

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
