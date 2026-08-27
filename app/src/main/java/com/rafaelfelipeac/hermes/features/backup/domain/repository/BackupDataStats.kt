package com.rafaelfelipeac.hermes.features.backup.domain.repository

data class BackupDataStats(
    val schemaVersion: Int,
    val challengesCount: Int,
    val challengeProgressEntriesCount: Int,
    val workoutsCount: Int,
    val categoriesCount: Int,
    val userActionsCount: Int,
)
