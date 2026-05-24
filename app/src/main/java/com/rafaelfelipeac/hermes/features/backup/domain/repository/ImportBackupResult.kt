package com.rafaelfelipeac.hermes.features.backup.domain.repository

sealed interface ImportBackupResult {
    data class Success(
        val schemaVersion: Int,
        val workoutsCount: Int,
        val notesCount: Int = 0,
        val categoriesCount: Int,
        val userActionsCount: Int,
    ) : ImportBackupResult

    data class Failure(
        val error: ImportBackupError,
    ) : ImportBackupResult
}
