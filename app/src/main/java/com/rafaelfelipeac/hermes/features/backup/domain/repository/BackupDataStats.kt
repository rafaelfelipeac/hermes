package com.rafaelfelipeac.hermes.features.backup.domain.repository

data class BackupDataStats(
    val schemaVersion: Int,
    val workoutsCount: Int,
    val notesCount: Int = 0,
    val categoriesCount: Int,
    val userActionsCount: Int,
)
